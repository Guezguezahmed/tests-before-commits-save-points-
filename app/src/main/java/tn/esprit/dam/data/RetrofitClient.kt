package tn.esprit.dam.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Log
import okio.Buffer

// This object handles the initialization and configuration of the Retrofit HTTP client.
object RetrofitClient {

    // BASE URL: Includes the required 'https://' scheme and the trailing slash '/'.
    private const val BASE_URL = "https://dam-backend-g2p9.onrender.com/api/v1/"

    // Configure JSON parsing - Crucially setting 'ignoreUnknownKeys = true'
    private val json = Json {
        ignoreUnknownKeys = true // 🏆 KEY FIX: Ignores fields present in the JSON but not in the DTO
        prettyPrint = true
        isLenient = true         // 🏆 KEY FIX: Allows non-standard JSON, like numbers as strings
    }
    private val contentType = "application/json".toMediaType()

    // --- 1. Logging Interceptor ---
    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestUrl = request.url.toString()
        val requestMethod = request.method
        val startTime = System.currentTimeMillis()

        // Log request body if present
        val requestBody = request.body
        if (requestBody != null) {
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()
                Log.d("Retrofit", "REQUEST BODY: $requestBodyString")
                
                // Create a new request with the buffered body since we consumed it
                val newRequest = request.newBuilder()
                    .method(request.method, okhttp3.RequestBody.create(requestBody.contentType(), requestBodyString))
                    .build()
                
                try {
                    Log.d("Retrofit", "Calling chain.proceed()...")
                    val response = chain.proceed(newRequest)
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val responseBody = response.peekBody(Long.MAX_VALUE).string()
                    
                    Log.d("Retrofit", "═══════════════════════════════════════════════════")
                    Log.d("Retrofit", "✅ RESPONSE RECEIVED after ${elapsedTime}ms")
                    Log.d("Retrofit", "RESPONSE CODE: ${response.code}")
                    Log.d("Retrofit", "RESPONSE IS SUCCESSFUL: ${response.isSuccessful}")
                    Log.d("Retrofit", "RESPONSE HEADERS: ${response.headers}")
                    Log.d("Retrofit", "RESPONSE BODY LENGTH: ${responseBody.length}")
                    Log.d("Retrofit", "RESPONSE BODY: $responseBody")
                    Log.d("Retrofit", "═══════════════════════════════════════════════════")
                    
                    if (responseBody.isEmpty()) {
                        Log.w("Retrofit", "⚠️ WARNING: Response body is empty!")
                    }

                    response
                } catch (e: Exception) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.e("Retrofit", "═══════════════════════════════════════════════════")
                    Log.e("Retrofit", "❌ REQUEST FAILED after ${elapsedTime}ms")
                    Log.e("Retrofit", "ERROR TYPE: ${e.javaClass.simpleName}")
                    Log.e("Retrofit", "ERROR MESSAGE: ${e.message}")
                    Log.e("Retrofit", "═══════════════════════════════════════════════════")
                    e.printStackTrace()
                    throw e
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "ERROR reading request body: ${e.message}")
                // If we can't read the body, proceed with original request
                try {
                    val response = chain.proceed(request)
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val responseBody = response.peekBody(Long.MAX_VALUE).string()
                    Log.d("Retrofit", "✅ RESPONSE RECEIVED after ${elapsedTime}ms")
                    Log.d("Retrofit", "RESPONSE CODE: ${response.code}")
                    Log.d("Retrofit", "RESPONSE BODY: $responseBody")
                    response
                } catch (e2: Exception) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.e("Retrofit", "❌ REQUEST FAILED after ${elapsedTime}ms: ${e2.message}")
                    throw e2
                }
            }
        } else {
            Log.d("Retrofit", "REQUEST HAS NO BODY")
            try {
                Log.d("Retrofit", "Calling chain.proceed()...")
                val response = chain.proceed(request)
                val elapsedTime = System.currentTimeMillis() - startTime
                val responseBody = response.peekBody(Long.MAX_VALUE).string()
                
                Log.d("Retrofit", "═══════════════════════════════════════════════════")
                Log.d("Retrofit", "✅ RESPONSE RECEIVED after ${elapsedTime}ms")
                Log.d("Retrofit", "RESPONSE CODE: ${response.code}")
                Log.d("Retrofit", "RESPONSE IS SUCCESSFUL: ${response.isSuccessful}")
                Log.d("Retrofit", "RESPONSE HEADERS: ${response.headers}")
                Log.d("Retrofit", "RESPONSE BODY LENGTH: ${responseBody.length}")
                Log.d("Retrofit", "RESPONSE BODY: $responseBody")
                Log.d("Retrofit", "═══════════════════════════════════════════════════")
                
                if (responseBody.isEmpty()) {
                    Log.w("Retrofit", "⚠️ WARNING: Response body is empty!")
                }

                response
            } catch (e: Exception) {
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.e("Retrofit", "═══════════════════════════════════════════════════")
                Log.e("Retrofit", "❌ REQUEST FAILED after ${elapsedTime}ms")
                Log.e("Retrofit", "ERROR TYPE: ${e.javaClass.simpleName}")
                Log.e("Retrofit", "ERROR MESSAGE: ${e.message}")
                Log.e("Retrofit", "═══════════════════════════════════════════════════")
                e.printStackTrace()
                throw e
            }
        }
    }

    // --- 2. Retry Interceptor Implementation ---
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        var responseHandled = false
        var attempts = 0
        val maxAttempts = 3

        while (attempts < maxAttempts && !responseHandled) {
            try {
                response = chain.proceed(request)
                responseHandled = true
            } catch (e: IOException) {
                attempts++
                val errorMessage = e.message ?: "Unknown error"
                
                // Don't retry on DNS/connection errors - they won't be fixed by retrying
                val isDnsError = errorMessage.contains("Unable to resolve host", ignoreCase = true) ||
                                 errorMessage.contains("No address associated with hostname", ignoreCase = true) ||
                                 e is java.net.UnknownHostException
                
                // Don't retry on "Canceled" errors - these happen when coroutine is cancelled
                val isCanceled = errorMessage.contains("Canceled", ignoreCase = true)
                
                if (isDnsError) {
                    Log.e("Retrofit", "❌ DNS/Connection Error (no retry): $errorMessage")
                    Log.e("Retrofit", "This usually means:")
                    Log.e("Retrofit", "  1. No internet connection")
                    Log.e("Retrofit", "  2. Server is down or sleeping (Render free tier)")
                    Log.e("Retrofit", "  3. DNS resolution problem")
                    throw e // Don't retry, throw immediately
                }
                
                if (isCanceled) {
                    Log.e("Retrofit", "❌ Request was canceled (no retry): $errorMessage")
                    Log.e("Retrofit", "This usually means the coroutine timeout was reached")
                    throw e // Don't retry, throw immediately
                }
                
                Log.e("Retrofit", "Request failed (Attempt $attempts/$maxAttempts): $errorMessage")
                if (attempts < maxAttempts) {
                    val backoffTime = (1 shl attempts) * 1000L
                    Log.d("Retrofit", "Retrying in ${backoffTime}ms...")
                    TimeUnit.MILLISECONDS.sleep(backoffTime)
                } else {
                    Log.e("Retrofit", "❌ Request failed after $maxAttempts attempts")
                    throw e
                }
            }
        }
        return@Interceptor response ?: throw IOException("Request failed after $maxAttempts attempts.")
    }

    // --- 3. OkHttpClient Configuration ---
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Add logging first
            .addInterceptor(retryInterceptor)
            // Set reasonable timeouts (30 seconds for connect, 90 seconds for read/write)
            // Render free tier servers can take 30-60s to wake up, so we need longer timeouts
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    // --- 4. Retrofit Instance ---
    private val retrofit: Retrofit by lazy {
		Log.d("Retrofit", "Initializing Retrofit with BASE_URL = $BASE_URL")
        if (!BASE_URL.startsWith("http")) {
            throw IllegalStateException("BASE_URL must start with 'http://' or 'https://'. Current value: $BASE_URL")
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // Publicly exposed service interfaces
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}