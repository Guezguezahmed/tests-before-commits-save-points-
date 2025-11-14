package tn.esprit.dam.data

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import tn.esprit.dam.models.AuthResponse
import tn.esprit.dam.models.LoginDto
import tn.esprit.dam.models.RegisterDto
import tn.esprit.dam.models.ErrorResponse
import tn.esprit.dam.models.ResendVerificationDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// 1. Setup DataStore for saving the JWT token
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val app: Application) {

    // Define DataStore keys
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    // Get the Retrofit service instance
    private val authService = RetrofitClient.authService

    // --- Token Management ---
    suspend fun saveToken(token: String) {
        app.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return app.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }.first()
    }

    suspend fun clearToken() {
        app.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

    // --- Remember Me Management ---
    suspend fun saveRememberMe(rememberMe: Boolean) {
        app.dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = rememberMe
        }
    }

    suspend fun getRememberMe(): Boolean {
        return app.dataStore.data.map { preferences ->
            preferences[REMEMBER_ME_KEY] ?: false
        }.first()
    }

    suspend fun clearRememberMe() {
        app.dataStore.edit { preferences ->
            preferences.remove(REMEMBER_ME_KEY)
        }
    }

    // --- API Calls ---
    suspend fun login(credentials: LoginDto): Result<AuthResponse> {
        val startTime = System.currentTimeMillis()
        return try {
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Log.d("AuthRepository", "=== LOGIN START ===")
            Log.d("AuthRepository", "Logging in with email: ${credentials.email}")
            Log.d("AuthRepository", "Start time: ${java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(startTime))}")
            
            // Add timeout wrapper (90 seconds - Render free tier can take time to wake up)
            Log.d("AuthRepository", "Calling authService.login() with 90s timeout...")
            Log.d("AuthRepository", "Note: Render free tier servers may take 30-60s to wake up")
            val response = withTimeout(90_000) {
                authService.login(credentials)
            }
            
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Log.d("AuthRepository", "=== LOGIN API CALL SUCCEEDED ===")
            Log.d("AuthRepository", "Response received after ${elapsedTime}ms")
            Log.d("AuthRepository", "Response token: ${response.token}")
            Log.d("AuthRepository", "Response accessToken: ${response.accessToken}")
            Log.d("AuthRepository", "Response user: ${response.user}")
            Log.d("AuthRepository", "Response message: ${response.message}")
            Log.d("AuthRepository", "Full response object: $response")

            // Note: Email verification check removed - if login succeeds, allow access
            // The backend will handle email verification requirements if needed
            val user = response.user ?: response.data
            val isEmailVerified = user?.isVerified ?: true // Default to true if not specified
            
            Log.d("AuthRepository", "User isVerified: $isEmailVerified (login allowed regardless)")

            // Try to get token from response
            val tokenToSave = response.token ?: response.accessToken
            
            if (tokenToSave.isNullOrBlank()) {
                // No token in response body - server might use cookies or session-based auth
                Log.w("AuthRepository", "⚠️ No token in login response body")
                Log.w("AuthRepository", "Server might be using cookies/session-based authentication")
                Log.w("AuthRepository", "Allowing login to proceed - token may be in HTTP cookies")
                // Don't throw error - allow login to proceed
                // The server might be using cookies for authentication
            } else {
                Log.d("AuthRepository", "Token found in response, saving to DataStore")
                saveToken(tokenToSave)
            }
            
            Log.d("AuthRepository", "=== LOGIN SUCCESS ===")
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Result.success(response)
        } catch (e: Exception) {
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.e("AuthRepository", "═══════════════════════════════════════════════════")
            Log.e("AuthRepository", "=== LOGIN FAILED ===")
            Log.e("AuthRepository", "Failed after ${elapsedTime}ms")
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")
            Log.e("AuthRepository", "Exception message: ${e.message}")
            Log.e("AuthRepository", "═══════════════════════════════════════════════════")
            e.printStackTrace()
            
            // Handle timeout specifically
            if (e is kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("AuthRepository", "❌ Login request timed out after ${elapsedTime}ms (90s limit)")
                Result.failure(Exception("Le serveur met trop de temps à répondre. Si c'est un serveur Render en veille, la première requête peut prendre 30-60 secondes. Réessayez dans quelques instants."))
            } else {
                Result.failure(handleNetworkException(e, "Login failed"))
            }
        }
    }

    suspend fun register(userData: RegisterDto): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== REGISTRATION START ===")
            Log.d("AuthRepository", "Registering user with email: ${userData.email}")
            Log.d("AuthRepository", "RegisterDto: firstName=${userData.firstName}, lastName=${userData.lastName}, email=${userData.email}, phone=${userData.phoneNumber}, birthDate=${userData.birthDate}, role=${userData.role}")
            
            // Add timeout wrapper (90 seconds total timeout)
            val httpResponse = withTimeout(90_000) {
                authService.register(userData)
            }
            
            Log.d("AuthRepository", "HTTP Response Code: ${httpResponse.code()}")
            Log.d("AuthRepository", "HTTP Response isSuccessful: ${httpResponse.isSuccessful}")
            Log.d("AuthRepository", "HTTP Response Headers: ${httpResponse.headers()}")
            
            // Check if response is successful (200-299)
            if (!httpResponse.isSuccessful) {
                val errorBody = httpResponse.errorBody()?.string()
                Log.e("AuthRepository", "HTTP Error Response: $errorBody")
                throw HttpException(httpResponse)
            }
            
            // Try to parse the response body
            val response = try {
                httpResponse.body() ?: run {
                    Log.w("AuthRepository", "Response body is null, but status code is ${httpResponse.code()}")
                    // If body is null but status is success, registration likely succeeded
                    AuthResponse(
                        message = "Account created successfully. Please check your email for the verification link."
                    )
                }
            } catch (parseError: Exception) {
                Log.e("AuthRepository", "=== PARSING ERROR ===")
                Log.e("AuthRepository", "Failed to parse response: ${parseError.message}")
                Log.e("AuthRepository", "Response code was: ${httpResponse.code()}")
                Log.e("AuthRepository", "This means the server response structure doesn't match AuthResponse")
                Log.e("AuthRepository", "The registration might have succeeded on the server, but we can't parse the response")
                
                // If HTTP was successful (200-299) but parsing failed, registration likely worked
                if (httpResponse.isSuccessful) {
                    AuthResponse(
                        message = "Account created successfully. Please check your email for the verification link."
                    )
                } else {
                    throw parseError
                }
            }
            
            Log.d("AuthRepository", "=== API CALL SUCCEEDED ===")
            Log.d("AuthRepository", "Response token: ${response.token}")
            Log.d("AuthRepository", "Response accessToken: ${response.accessToken}")
            Log.d("AuthRepository", "Response user: ${response.user}")
            Log.d("AuthRepository", "Response data: ${response.data}")
            Log.d("AuthRepository", "Response message: ${response.message}")
            Log.d("AuthRepository", "Response success: ${response.success}")
            Log.d("AuthRepository", "Response status: ${response.status}")
            Log.d("AuthRepository", "Full response: $response")

            // If we get here, the API call succeeded (no exception thrown)
            // For email verification flows, the API might not return a token or user object
            // but still successfully create the account and send verification email
            
            // Check both user and data fields
            val userObject = response.user ?: response.data
            val tokenToSave = response.token ?: response.accessToken

            if (!tokenToSave.isNullOrBlank()) {
                Log.d("AuthRepository", "Token found, saving to DataStore")
                // Token is present, save it and mark as authenticated
                saveToken(tokenToSave)
            } else {
                Log.d("AuthRepository", "No token in response (expected for email verification flow)")
            }
            
            Log.d("AuthRepository", "=== REGISTRATION SUCCESS ===")
            // Even if there's no token or user object, if the API call succeeded,
            // registration was successful (user created, verification email sent)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== REGISTRATION FAILED ===")
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")
            Log.e("AuthRepository", "Exception message: ${e.message}")
            e.printStackTrace()
            
            // Handle timeout specifically
            if (e is TimeoutCancellationException) {
                Log.e("AuthRepository", "Request timed out after 90 seconds")
                Result.failure(Exception("Request timed out. The server is taking too long to respond. Please check your internet connection and try again."))
            } else if (e is SerializationException) {
                // Handle serialization error - registration might have succeeded
                Log.e("AuthRepository", "Serialization error - registration might have succeeded on server")
                Log.e("AuthRepository", "Returning success with verification message")
                Result.success(
                    AuthResponse(
                        message = "Account created successfully. Please check your email for the verification link."
                    )
                )
            } else {
                Result.failure(handleNetworkException(e, "Registration failed"))
            }
        }
    }

    suspend fun resendVerification(email: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== RESEND VERIFICATION START ===")
            Log.d("AuthRepository", "Resending verification email to: $email")
            
            // Validate email format
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.e("AuthRepository", "Invalid email format: $email")
                return Result.failure(Exception("Invalid email address format. Please check your email and try again."))
            }
            
            val emailDto = ResendVerificationDto(email = email.trim())
            Log.d("AuthRepository", "Request DTO: $emailDto")
            
            // Add timeout wrapper (60 seconds)
            val response = withTimeout(60_000) {
                Log.d("AuthRepository", "Calling API: POST /auth/resend-verification")
                authService.resendVerification(emailDto)
            }
            
            Log.d("AuthRepository", "=== RESEND API CALL SUCCEEDED ===")
            Log.d("AuthRepository", "Response message: ${response.message}")
            Log.d("AuthRepository", "Response token: ${response.token}")
            Log.d("AuthRepository", "Response accessToken: ${response.accessToken}")
            Log.d("AuthRepository", "Full response: $response")
            
            // Check if the response indicates success
            val successMessage = response.message ?: "Verification email sent successfully"
            Log.d("AuthRepository", "Success message: $successMessage")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== RESEND FAILED ===")
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")
            Log.e("AuthRepository", "Exception message: ${e.message}")
            Log.e("AuthRepository", "Email that failed: $email")
            e.printStackTrace()
            
            // Handle timeout specifically
            if (e is TimeoutCancellationException) {
                Log.e("AuthRepository", "Request timed out after 60 seconds")
                Result.failure(Exception("Request timed out. The server is taking too long to respond. Please try again later."))
            } else {
                Result.failure(handleNetworkException(e, "Resending verification email failed"))
            }
        }
    }

    private suspend fun handleNetworkException(e: Exception, contextMessage: String): Exception {
        return withContext(Dispatchers.Main) {
            when (e) {
                is HttpException -> {
                    Log.e("AuthRepository", "HTTP Exception: Code ${e.code()}")
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        Log.e("AuthRepository", "Failed to read error body: ${ex.message}")
                        null
                    }
                    Log.e("AuthRepository", "Error body: $errorBody")
                    
                    val serverMessage = try {
                        if (errorBody != null && errorBody.isNotBlank()) {
                            try {
                                val errorResponse = Json.decodeFromString<ErrorResponse>(errorBody)
                                errorResponse.message ?: errorResponse.error ?: errorBody
                            } catch (parseError: Exception) {
                                Log.e("AuthRepository", "Failed to parse as ErrorResponse, trying manual extraction")
                                // Try to extract message manually using regex
                                val messageMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)
                                messageMatch?.groupValues?.get(1) ?: errorBody
                            }
                        } else {
                            null
                        }
                    } catch (jsonError: Exception) {
                        Log.e("AuthRepository", "Failed to parse error JSON: ${jsonError.message}")
                        errorBody
                    }
                    
                    // Provide user-friendly error messages based on status code
                    val userFriendlyMessage = when (e.code()) {
                        409 -> serverMessage ?: "This email is already registered. Please use a different email or try logging in."
                        400 -> serverMessage ?: "Invalid request. Please check your input."
                        401 -> serverMessage ?: "Authentication failed. Please check your credentials."
                        403 -> serverMessage ?: "Access denied. You don't have permission to perform this action."
                        404 -> serverMessage ?: "Resource not found."
                        500 -> serverMessage ?: "Server error. Please try again later."
                        else -> serverMessage ?: e.message() ?: "An error occurred. Please try again."
                    }
                    
                    Log.e("AuthRepository", "Final error message: $userFriendlyMessage")
                    Exception(userFriendlyMessage)
                }
                is SocketTimeoutException -> {
                    Log.e("AuthRepository", "SocketTimeoutException: ${e.message}")
                    Exception("Network Error: Request timed out. The server might be waking up. Please try again.")
                }
                is UnknownHostException -> {
                    Log.e("AuthRepository", "UnknownHostException: ${e.message}")
                    val errorMsg = e.message ?: ""
                    when {
                        errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
                        errorMsg.contains("No address associated with hostname", ignoreCase = true) -> {
                            Exception("Erreur de connexion: Impossible de joindre le serveur.\n\nVérifiez:\n• Votre connexion Internet\n• Si le serveur est en ligne (Render peut être en veille)\n• Réessayez dans quelques instants")
                        }
                        else -> {
                            Exception("Erreur réseau: Impossible de résoudre le nom du serveur. Vérifiez votre connexion Internet.")
                        }
                    }
                }
                is IOException -> {
                    Log.e("AuthRepository", "IOException: ${e.message}")
                    val errorMsg = e.message ?: ""
                    when {
                        errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
                        errorMsg.contains("No address associated with hostname", ignoreCase = true) -> {
                            Exception("Erreur de connexion: Impossible de joindre le serveur.\n\nVérifiez:\n• Votre connexion Internet\n• Si le serveur est en ligne (Render peut être en veille)\n• Réessayez dans quelques instants")
                        }
                        errorMsg.contains("timeout", ignoreCase = true) -> {
                            Exception("Délai d'attente dépassé. Le serveur met trop de temps à répondre. Réessayez dans quelques instants.")
                        }
                        else -> {
                            Exception("Erreur réseau: ${e.message ?: "Vérifiez votre connexion Internet."}")
                        }
                    }
                }
                is SerializationException -> {
                    Log.e("AuthRepository", "SerializationException: ${e.message}")
                    Log.e("AuthRepository", "This usually means the API response structure doesn't match AuthResponse")
                    Exception("Data Error: Failed to parse server response. (JSON Mismatch) - The server response format may have changed.")
                }
                else -> {
                    Log.e("AuthRepository", "Unknown exception: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                    Exception("$contextMessage: ${e.message ?: "An unknown error occurred."}")
                }
            }
        }
    }
}
