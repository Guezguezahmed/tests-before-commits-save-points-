package tn.esprit.dam.models

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.esprit.dam.data.AuthRepository
// We need to import the DTOs from the other file
import tn.esprit.dam.models.LoginDto
import tn.esprit.dam.models.RegisterDto
import tn.esprit.dam.models.AuthResponse

// Define the state the UI will observe
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val user: AuthResponse? = null // This holds the whole response, including UserProfile
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize the repository, which requires the application context for DataStore
    private val repository = AuthRepository(application)

    // The state holder that the Composable will collect and react to
    var uiState by mutableStateOf(AuthUiState())
        private set

    // --- Core Authentication Logic (Login) ---

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, isAuthenticated = false, errorMessage = null)

        viewModelScope.launch {
            val credentials = LoginDto(email = email, password = password)
            val result = repository.login(credentials)

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== LOGIN SUCCESS ===")
                    Log.d("AuthViewModel", "Response: $response")
                    Log.d("AuthViewModel", "Token: ${response.token}, AccessToken: ${response.accessToken}")
                    Log.d("AuthViewModel", "User: ${response.user}")
                    
                    // Save remember me preference (or clear it if false)
                    if (rememberMe) {
                        repository.saveRememberMe(true)
                        Log.d("AuthViewModel", "✅ Remember me preference saved: true")
                    } else {
                        // Clear remember me if user unchecks it
                        repository.clearRememberMe()
                        Log.d("AuthViewModel", "❌ Remember me preference cleared (user unchecked)")
                    }
                    
                    // Update state with authentication success
                    // Note: Email verification check removed - if login succeeds, allow access
                    val newState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = response,
                        errorMessage = null // Clear any previous errors
                    )
                    
                    Log.d("AuthViewModel", "Setting isAuthenticated = true")
                    Log.d("AuthViewModel", "Previous isAuthenticated = ${uiState.isAuthenticated}")
                    uiState = newState
                    Log.d("AuthViewModel", "New isAuthenticated = ${uiState.isAuthenticated}")
                    
                    // Use safe call operator (?.) to access nested nullable properties
                    val user = response.user ?: response.data
                    val userEmail = user?.email ?: "N/A"
                    Log.d("AuthViewModel", "Login Success: User $userEmail authenticated")
                    Log.d("AuthViewModel", "State updated successfully, LaunchedEffect should trigger navigation to HomeScreen")
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = error.message ?: "Login failed. Please check credentials."
                    )
                    println("Login Failure: ${error.message}")
                }
            )
        }
    }

    // --- Core Authentication Logic (Registration) ---
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        birthDate: String,
        role: String,
        password: String
    ) {
        if (uiState.isLoading) return

        // 🏆 FIX: PRE-FLIGHT VALIDATION CHECK
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phoneNumber.isBlank() || birthDate.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "All fields (Name, Email, Phone, Birth Date, Password) are required.")
            return
        }

        uiState = uiState.copy(isLoading = true, isAuthenticated = false, errorMessage = null)

        viewModelScope.launch {
            // FIX: The RegisterDto's parameter names were updated (likely in another file)
            // The DTO now expects 'firstName', 'lastName', 'phoneNumber', and 'birthDate'.
            val userData = RegisterDto(
                firstName = firstName,    // Correct parameter name is now 'firstName'
                lastName = lastName,      // Correct parameter name is now 'lastName'
                email = email,
                phoneNumber = phoneNumber,// Correct parameter name is now 'phoneNumber'
                birthDate = birthDate,    // Correct parameter name is now 'birthDate'
                role = role,
                password = password
            )

            Log.d("AuthViewModel", "Calling repository.register()")
            val result = repository.register(userData)
            Log.d("AuthViewModel", "Repository call completed")

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== REGISTRATION SUCCESS IN VIEWMODEL ===")
                    Log.d("AuthViewModel", "Response: $response")
                    Log.d("AuthViewModel", "Token: ${response.token}, AccessToken: ${response.accessToken}")
                    Log.d("AuthViewModel", "User: ${response.user}")
                    Log.d("AuthViewModel", "Message: ${response.message}")
                    
                    val hasToken = !response.token.isNullOrBlank() || !response.accessToken.isNullOrBlank()
                    Log.d("AuthViewModel", "Has token: $hasToken")

                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = hasToken,
                        user = response
                    )

                    // 1. Determine the appropriate message
                    val finalMessage = if (hasToken) {
                        "Registration successful! You are now logged in."
                    } else {
                        // User is created, but verification/final token failed.
                        "Account created successfully. Please check your email for the verification link."
                    }

                    Log.d("AuthViewModel", "Final message: $finalMessage")

                    // 2. Set the message in errorMessage state for the UI (SignupScreen) to display
                    uiState = uiState.copy(errorMessage = finalMessage)
                    Log.d("AuthViewModel", "UI State updated with message")

                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "=== REGISTRATION FAILURE IN VIEWMODEL ===")
                    Log.e("AuthViewModel", "Error: ${error.message}")
                    Log.e("AuthViewModel", "Error type: ${error.javaClass.simpleName}")
                    error.printStackTrace()
                    
                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        // Show the exact error message provided by the server/repository.
                        errorMessage = error.message ?: "Registration failed due to an unknown error."
                    )
                    Log.e("AuthViewModel", "UI State updated with error message: ${uiState.errorMessage}")
                }
            )
        }
    }

    // --- New: Resend Verification Email (Restored) ---

    fun resendVerificationEmail(email: String) {
        if (uiState.isLoading) return
        Log.d("AuthViewModel", "=== RESEND VERIFICATION EMAIL START ===")
        Log.d("AuthViewModel", "Email: $email")
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.resendVerification(email)
            Log.d("AuthViewModel", "Resend repository call completed")

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== RESEND SUCCESS IN VIEWMODEL ===")
                    Log.d("AuthViewModel", "Response: $response")
                    Log.d("AuthViewModel", "Response message: ${response.message}")
                    
                    val message = response.message ?: "Verification email re-sent successfully. Check your inbox."
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                    Log.d("AuthViewModel", "Resend Success: $message")
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "=== RESEND FAILURE IN VIEWMODEL ===")
                    Log.e("AuthViewModel", "Error: ${error.message}")
                    Log.e("AuthViewModel", "Error type: ${error.javaClass.simpleName}")
                    error.printStackTrace()
                    
                    val errorMessage = error.message ?: "Failed to re-send verification email. Please contact support."
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    Log.e("AuthViewModel", "Resend Failure: $errorMessage")
                }
            )
        }
    }

    // --- Other utility functions ---

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // --- Check Authentication State on Startup ---
    fun checkAuthState() {
        viewModelScope.launch {
            // Set loading state while checking
            uiState = uiState.copy(isLoading = true)
            
            val token = repository.getToken()
            val rememberMe = repository.getRememberMe()
            
            Log.d("AuthViewModel", "=== CHECKING AUTH STATE ===")
            Log.d("AuthViewModel", "Token present: ${!token.isNullOrBlank()}")
            Log.d("AuthViewModel", "Token value: ${if (token.isNullOrBlank()) "null/empty" else "present"}")
            Log.d("AuthViewModel", "Remember me: $rememberMe")
            
            if (rememberMe && !token.isNullOrBlank()) {
                // User is remembered and has a token, restore authentication state
                Log.d("AuthViewModel", "✅ User is remembered with valid token, restoring auth state")
                uiState = uiState.copy(
                    isAuthenticated = true,
                    isLoading = false
                )
            } else {
                // Clear remember me if token is missing
                if (rememberMe && token.isNullOrBlank()) {
                    Log.d("AuthViewModel", "⚠️ Remember me is true but token is missing, clearing remember me")
                    repository.clearRememberMe()
                }
                Log.d("AuthViewModel", "❌ User not remembered or no token, setting isAuthenticated = false")
                uiState = uiState.copy(
                    isAuthenticated = false,
                    isLoading = false
                )
            }
        }
    }

    // --- Logout ---
    fun logout() {
        viewModelScope.launch {
            repository.clearToken()
            repository.clearRememberMe()
            uiState = AuthUiState()
            Log.d("AuthViewModel", "User logged out, token and remember me cleared")
        }
    }
}