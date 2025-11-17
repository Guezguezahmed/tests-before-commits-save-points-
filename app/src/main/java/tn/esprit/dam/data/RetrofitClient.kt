package tn.esprit.dam.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Log
import okio.Buffer

// This object handles the initialization and configuration of the Retrofit HTTP client.
object RetrofitClient {

    // BASE URL comes from ApiConfig so it's a single source of truth.
    private val BASE_URL: String = ApiConfig.API_BASE_URL

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
        // Inline URL/method to avoid unused-variable analyzer warnings
        val startTime = System.currentTimeMillis()

        Log.d("Retrofit", "REQUEST -> ${request.method} ${request.url}")

         // Log request body if present
         val requestBody = request.body
         if (requestBody != null) {
             try {
                 val buffer = Buffer()
                 requestBody.writeTo(buffer)
                 val requestBodyString = buffer.readUtf8()
                Log.d("Retrofit", "REQUEST BODY: $requestBodyString")
                
                // Create a new request with the buffered body since we consumed it
                val mediaType = requestBody.contentType() ?: contentType
                val newRequestBody = requestBodyString.toRequestBody(mediaType)
                val newRequest = request.newBuilder()
                    .method(request.method, newRequestBody)
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
        var lastException: IOException? = null
        val maxAttempts = 3

        for (attempt in 1..maxAttempts) {
            try {
                val response = chain.proceed(request)
                return@Interceptor response
            } catch (e: IOException) {
                lastException = e
                val errorMessage = e.message ?: "Unknown error"

                // Don't retry on DNS/connection errors or cancellation
                val isDnsError = errorMessage.contains("Unable to resolve host", ignoreCase = true) ||
                        errorMessage.contains("No address associated with hostname", ignoreCase = true) ||
                        e is java.net.UnknownHostException
                val isCanceled = errorMessage.contains("Canceled", ignoreCase = true)

                if (isDnsError) {
                    Log.e("Retrofit", "❌ DNS/Connection Error (no retry): $errorMessage")
                    throw e
                }
                if (isCanceled) {
                    Log.e("Retrofit", "❌ Request was canceled (no retry): $errorMessage")
                    throw e
                }

                Log.e("Retrofit", "Request failed (Attempt $attempt/$maxAttempts): $errorMessage")
                if (attempt < maxAttempts) {
                    val backoffTime = (1 shl attempt) * 1000L
                    Log.d("Retrofit", "Retrying in ${backoffTime}ms...")
                    try { TimeUnit.MILLISECONDS.sleep(backoffTime) } catch (_: InterruptedException) {}
                } else {
                    Log.e("Retrofit", "❌ Request failed after $maxAttempts attempts")
                    throw e
                }
            }
        }

        throw lastException ?: IOException("Request failed after $maxAttempts attempts.")
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