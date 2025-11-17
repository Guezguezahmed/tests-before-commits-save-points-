package tn.esprit.dam.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- DTO for Authentication Responses ---
@Serializable
data class AuthResponse(
    // The server might return the token as 'token'
    val token: String? = null,
    // The server might return the token as 'access_token' (map snake_case)
    @SerialName("access_token")
    val accessToken: String? = null,
    val user: User? = null,
    val message: String? = null,
    // Additional fields that might be present
    val data: User? = null, // Some APIs wrap user in 'data'
    val success: Boolean? = null, // Some APIs return success flag
    val status: String? = null, // Some APIs return status

    // Direct user fields from registration response (used if the user object isn't nested)
    @SerialName("prenom")
    val prenom: String? = null,
    @SerialName("nom")
    val nom: String? = null,
    val email: String? = null,

    // FIX: Changed to String? because the server returns phone numbers as a String,
    // which caused the 'String' vs 'Long' type mismatch error during compilation/parsing.
    val tel: String? = null,

    // Represents a date string (e.g., "2000-01-01T00:00:00.000Z")
    val age: String? = null,
    val role: String? = null,
    @SerialName("emailVerified")
    val emailVerified: Boolean? = null,
    @SerialName("isVerified")
    val isVerified: Boolean? = null,
    @SerialName("_id")
    val id: String? = null
) {
    // Helper function to get email from any field (renamed to avoid conflict with the email property getter)
    fun extractEmail(): String? {
        return email ?: user?.email ?: data?.email
    }
}

// --- DTO for User Login Request ---
@Serializable
data class LoginDto(
    val email: String,
    val password: String
)

// --- DTO for User Registration Request ---
@Serializable
data class RegisterDto(
    // UI: firstName -> API: prenom
    @SerialName("prenom")
    val firstName: String,

    // UI: lastName -> API: nom
    @SerialName("nom")
    val lastName: String,

    // API: email
    val email: String,

    // FIX: Changed to String. Phone numbers should almost always be Strings.
    @SerialName("tel")
    val phoneNumber: String,

    // UI: birthDate -> API: age (This must contain the date in "YYYY-MM-DD" format as a String)
    @SerialName("age")
    val birthDate: String,

    // API: role
    val role: String,

    // API: password
    val password: String
)

// --- User Model (Removed: use User from DataModels.kt) ---
//@Serializable
//data class User(
//    @SerialName("_id")
//    val id: String? = null,
//    val prenom: String? = null,
//    val nom: String? = null,
//    val email: String? = null,
//
//    // FIX: Changed to String? to match the server's JSON output.
//    val tel: String? = null,
//
//    val role: String? = null,
//    val age: String? = null, // Store as String (YYYY-MM-DD or ISO format from DB)
//    @SerialName("emailVerified")
//    val emailVerified: Boolean? = null,
//    @SerialName("isVerified")
//    val isVerified: Boolean? = null,
//    // verificationCode and codeExpiresAt may be present before verification; make them nullable
//    val verificationCode: String? = null,
//    val codeExpiresAt: String? = null
//)

// --- Resend Verification Email Request DTO (Also used for Forgot Password step 1) ---
@Serializable
data class ResendVerificationDto(
    val email: String
)

// DTO for verify email (OTP code) (Also used for Forgot Password step 2)
@Serializable
data class VerifyEmailDto(
    val code: String,      // The OTP code
    val email: String      // The email to identify which user to verify
)

// --- Error Response Model ---
@Serializable
data class ErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)

// DTO for resetting password after forgot-password verification (Forgot Password step 3)
@Serializable
data class ResetPasswordDto(
    val email: String,
    val code: String,
    val newPassword: String,
    val confirmPassword: String
)