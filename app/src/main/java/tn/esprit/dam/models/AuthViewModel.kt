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

    // Holds the email address pending verification (set after registration) - make it observable
    var pendingVerificationEmail by mutableStateOf<String?>(null)
        private set

    // Indicates whether the current pending verification is for the forgot-password reset flow
    var pendingIsForPasswordReset by mutableStateOf(false)
        private set

    // When forgot-password code is verified, store the email and code so SetNewPasswordScreen can use them
    var forgotVerifiedEmail by mutableStateOf<String?>(null)
        private set
    var forgotVerifiedCode by mutableStateOf<String?>(null)
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

                    // Fetch latest user data by ID after login
                    val userId = response.user?.id ?: response.data?.id
                    if (!userId.isNullOrBlank()) {
                        val user = repository.fetchUserById(userId)
                        if (user != null) {
                            val updatedResponse = response.copy(user = user, data = user)
                            uiState = uiState.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                user = updatedResponse,
                                errorMessage = null
                            )
                            return@fold
                        }
                    }

                    // Fallback: Update state with authentication success
                    val newState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = response,
                        errorMessage = null
                    )
                    uiState = newState
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
        phoneNumber: String, // Still String input from UI
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

        // 💡 FIX: Convert phone number from String (UI input) to Long (Number DTO requirement)
        val telNumber: Long = phoneNumber.toLongOrNull() ?: run {
            uiState = uiState.copy(errorMessage = "Phone number must be a valid number.")
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
                phoneNumber = phoneNumber,  // <-- Passed as Long now
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
                    Log.d("AuthViewModel", "Email from response: ${response.email}")

                    val hasToken = !response.token.isNullOrBlank() || !response.accessToken.isNullOrBlank()
                    Log.d("AuthViewModel", "Has token: $hasToken")

                    // If registration succeeded but no token (verification flow), keep the email for verification
                    if (!hasToken) {
                        // Extract email from response - it could be in response.email (direct) or response.user.email or response.data?.email
                        val emailToStore = response.email ?: response.user?.email ?: response.data?.email ?: email
                        Log.d("AuthViewModel", "Setting pendingVerificationEmail to: $emailToStore")
                        pendingVerificationEmail = emailToStore
                        // Persist it via repository to be available for new ViewModel instances
                        try {
                            repository.savePendingVerificationEmail(emailToStore)
                            Log.d("AuthViewModel", "Persisted pendingVerificationEmail via repository: $emailToStore")
                        } catch (ex: Exception) {
                            Log.e("AuthViewModel", "Failed to persist pendingVerificationEmail: ${ex.message}")
                        }
                    }

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
                    Log.d("AuthViewModel", "Resend Failure: $errorMessage")
                }
            )
        }
    }

    // --- Verify Email ---
    fun verifyEmail(token: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            // Debug logging: dump candidate sources for email
            Log.d("AuthViewModel", "[VERIFY] pendingVerificationEmail(memory) = ${pendingVerificationEmail}")
            Log.d("AuthViewModel", "[VERIFY] uiState.user (full) = ${uiState.user}")
            Log.d("AuthViewModel", "[VERIFY] uiState.user?.user = ${uiState.user?.user}")
            Log.d("AuthViewModel", "[VERIFY] uiState.user?.data = ${uiState.user?.data}")
            Log.d("AuthViewModel", "[VERIFY] uiState.user?.email = ${uiState.user?.email}")

            // Get the email - check all possible locations (pending, nested user/data, or direct top-level fields)
            var emailToVerify = pendingVerificationEmail
                ?: uiState.user?.user?.email
                ?: uiState.user?.data?.email
                ?: uiState.user?.email
                ?: uiState.user?.let { it.extractEmail() }

            // If still null, try reading the persisted pending email from DataStore
            if (emailToVerify.isNullOrBlank()) {
                try {
                    val persisted = repository.getPendingVerificationEmail()
                    Log.d("AuthViewModel", "[VERIFY] persisted pendingVerificationEmail(DataStore) = $persisted")
                    if (!persisted.isNullOrBlank()) emailToVerify = persisted
                } catch (ex: Exception) {
                    Log.e("AuthViewModel", "[VERIFY] Failed to read persisted pending email: ${ex.message}")
                }
            }

            Log.d("AuthViewModel", "[VERIFY] chosen emailToVerify = $emailToVerify")

            if (emailToVerify.isNullOrBlank()) {
                // No email available - this shouldn't happen but handle it gracefully
                Log.e("AuthViewModel", "[VERIFY] No email candidate found. Aborting verification.")
                uiState = uiState.copy(isLoading = false, errorMessage = "Email address not found. Please register again.")
                onComplete(false, "Email address not found")
                return@launch
            }

            val result = repository.verifyEmail(token, emailToVerify)
            result.fold(
                onSuccess = { response ->
                    // If response contains user or data, update uiState.user accordingly
                    val newUser = response.user ?: response.data

                    // If we already have a stored AuthResponse in uiState, merge and ensure isVerified = true
                    val currentAuth = uiState.user

                    if (currentAuth != null) {
                        // Determine the existing user object (could be in user or data)
                        val existingUser = currentAuth.user ?: currentAuth.data

                        // Prefer the server-returned user if present, otherwise use the existing one
                        val baseUser = newUser ?: existingUser

                        // If we have a user to update, set isVerified = true and clear verification fields
                        val verifiedUser = baseUser?.copy(
                            isVerified = true,
                            emailVerified = true,
                            verificationCode = null,
                            codeExpiresAt = null
                        )

                        // Merge into an updated AuthResponse while preserving other fields
                        val merged = currentAuth.copy(
                            user = verifiedUser ?: currentAuth.user,
                            data = verifiedUser ?: currentAuth.data,
                            // keep message/token if provided by verification response
                            message = response.message ?: currentAuth.message,
                            token = response.token ?: currentAuth.token,
                            accessToken = response.accessToken ?: currentAuth.accessToken
                        )

                        uiState = uiState.copy(isLoading = false, user = merged, errorMessage = null)
                    } else {
                        // No existing AuthResponse in state - construct one from the verification response
                        val verifiedUser = newUser?.copy(
                            isVerified = true,
                            emailVerified = true,
                            verificationCode = null,
                            codeExpiresAt = null
                        )
                        val newAuthResp = response.copy(user = verifiedUser, data = verifiedUser)
                        uiState = uiState.copy(isLoading = false, user = newAuthResp, errorMessage = null)
                    }

                    // Clear pending email as it's now verified both in memory and DataStore
                    pendingVerificationEmail = null
                    try { repository.clearPendingVerificationEmail() } catch (ex: Exception) { Log.e("AuthViewModel","[VERIFY] Failed clearing persisted pending email: ${ex.message}") }

                    onComplete(true, response.message ?: "Verification successful.")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Verification failed.")
                    onComplete(false, error.message ?: "Verification failed.")
                }
            )
        }
    }

    // --- Additional helper to extract email from AuthResponse ---
    private fun AuthResponse.extractEmail(): String? {
        return this.email ?: this.user?.email ?: this.data?.email
    }

    // --- Load persisted pending verification email from DataStore into memory ---
    fun loadPendingVerificationEmail() {
        viewModelScope.launch {
            try {
                val persisted = repository.getPendingVerificationEmail()
                Log.d("AuthViewModel", "[LOAD] persisted pendingVerificationEmail on startup = $persisted")
                if (!persisted.isNullOrBlank()) {
                    pendingVerificationEmail = persisted
                }
            } catch (ex: Exception) {
                Log.e("AuthViewModel", "[LOAD] Failed to load persisted pending email: ${ex.message}")
            }
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

            // Load persisted pending verification email as part of startup checks
            try {
                val persisted = repository.getPendingVerificationEmail()
                Log.d("AuthViewModel", "[CHECK] persisted pendingVerificationEmail = $persisted")
                if (!persisted.isNullOrBlank()) pendingVerificationEmail = persisted
            } catch (ex: Exception) {
                Log.e("AuthViewModel", "[CHECK] Failed to read persisted pending email: ${ex.message}")
            }

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

    // --- Forgot-password flow helpers ---
    fun forgotPassword(email: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.forgotPassword(email)

            result.fold(
                onSuccess = { response ->
                    // Set the email that is pending verification for the reset flow
                    pendingVerificationEmail = email
                    pendingIsForPasswordReset = true
                    try { repository.savePendingVerificationEmail(email) } catch (_: Exception) { }

                    uiState = uiState.copy(isLoading = false, errorMessage = response.message ?: "Code sent to email")
                    onComplete(true, response.message ?: "Code sent to email")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Failed to send code")
                    onComplete(false, error.message ?: "Failed to send code")
                }
            )
        }
    }

    fun verifyForgotPasswordCode(code: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        // Determine email to verify (the one set in forgotPassword)
        viewModelScope.launch {
            var emailToVerify = pendingVerificationEmail
            if (emailToVerify.isNullOrBlank()) {
                try {
                    val persisted = repository.getPendingVerificationEmail()
                    if (!persisted.isNullOrBlank()) emailToVerify = persisted
                } catch (ex: Exception) { }
            }
            if (emailToVerify.isNullOrBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Email address not found. Please restart the forgot password flow.")
                onComplete(false, "Email address not found. Please restart the forgot password flow.")
                return@launch
            }
            val result = repository.verifyForgotPasswordCode(code, emailToVerify)
            result.fold(
                onSuccess = { response ->
                    forgotVerifiedEmail = emailToVerify
                    forgotVerifiedCode = code
                    // Persist these for reset step
                    try {
                        repository.saveForgotPasswordContext(emailToVerify, code)
                    } catch (_: Exception) { }
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onComplete(true, response.message ?: "Code verified")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Verification failed")
                    onComplete(false, error.message ?: "Verification failed")
                }
            )
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            var email = forgotVerifiedEmail
            var code = forgotVerifiedCode
            // Try to reload from persistence if missing
            if (email.isNullOrBlank() || code.isNullOrBlank()) {
                try {
                    val (persistedEmail, persistedCode) = repository.getForgotPasswordContext()
                    if (!persistedEmail.isNullOrBlank() && !persistedCode.isNullOrBlank()) {
                        email = persistedEmail
                        code = persistedCode
                        forgotVerifiedEmail = email
                        forgotVerifiedCode = code
                    }
                } catch (_: Exception) { }
            }
            android.util.Log.d("AuthViewModel", "resetPassword: email=$email, code=$code, newPassword=$newPassword, confirmPassword=$confirmPassword")
            if (email.isNullOrBlank() || code.isNullOrBlank()) {
                android.util.Log.e("AuthViewModel", "Reset context missing. Email or code is blank.")
                uiState = uiState.copy(isLoading = false, errorMessage = "Reset context missing. Please request a new code.")
                onComplete(false, "Reset context missing. Please request a new code.")
                return@launch
            }

            val result = repository.resetForgotPassword(email, code, newPassword, confirmPassword)
            result.fold(
                onSuccess = { response ->
                    android.util.Log.d("AuthViewModel", "resetPassword success: ${'$'}{response.message}")
                    pendingVerificationEmail = null
                    pendingIsForPasswordReset = false
                    forgotVerifiedEmail = null
                    forgotVerifiedCode = null
                    try {
                        repository.clearPendingVerificationEmail()
                        repository.clearForgotPasswordContext()
                    } catch (_: Exception) { }
                    uiState = uiState.copy(isLoading = false, errorMessage = response.message ?: "Password changed successfully")
                    onComplete(true, response.message ?: "Password changed successfully")
                },
                onFailure = { error ->
                    android.util.Log.e("AuthViewModel", "resetPassword failed: ${'$'}{error.message}")
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Reset failed")
                    onComplete(false, error.message ?: "Reset failed")
                }
            )
        }
    }

    // --- New: Fetch and Update User by ID ---
    fun fetchAndSetUserById(id: String) {
        viewModelScope.launch {
            val user = repository.fetchUserById(id)
            if (user != null) {
                uiState = uiState.copy(user = uiState.user?.copy(user = user, data = user))
            }
        }
    }
}