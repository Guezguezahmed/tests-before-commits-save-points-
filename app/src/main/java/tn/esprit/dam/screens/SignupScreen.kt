package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke // ADDED: Required for OutlinedButton border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import java.util.regex.Pattern

// ----------------------------------------------------------------------------------
// --- PUBLIC ENTRY POINT ---
// ----------------------------------------------------------------------------------
@Composable
fun SignupScreen(
    navController: NavController,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    SignupScreenContent(navController)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreenContent(
    navController: NavController
) {
    // ACCESS COLORS VIA MATERIALTHEME
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardSurfaceColor = MaterialTheme.colorScheme.surface
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    // State for input fields (rest of the screen logic)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    // State for validation errors
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    // Snackbar Host State for displaying messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Validation functions remain the same
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$")

    fun validateInputs(): Boolean {
        var isValid = true

        // Reset all errors first
        fullNameError = null; emailError = null; phoneError = null; passwordError = null; termsError = null
        // Checks
        if (fullName.trim().length < 6) { fullNameError = "Full name must be at least 6 characters."; isValid = false }
        if (!emailPattern.matcher(email).matches()) { emailError = "Invalid email format."; isValid = false }
        if (!phoneNumber.matches(Regex("^\\d{8}$"))) { phoneError = "Phone number must be exactly 8 digits."; isValid = false }
        if (!passwordPattern.matcher(password).matches()) { passwordError = "Password must contain 6+ chars, uppercase, lowercase, and a number."; isValid = false }
        if (!agreedToTerms) { termsError = "Unchecked"; isValid = false }

        return isValid
    }

    // ------------------------------------

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor) // Dynamic background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(cardSurfaceColor) // Dynamic surface/card background
                        .padding(horizontal = 24.dp)
                        .padding(paddingValues)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- 1. Illustration and Header ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.login_illustration),
                            contentDescription = "Registration Illustration",
                            modifier = Modifier.size(150.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get Started",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor // Dynamic primary text color
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor) // Dynamic primary color
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "by creating a free account.",
                                fontSize = 14.sp,
                                color = secondaryTextColor // Dynamic secondary color
                            )
                        }
                    }

                    // --- 2. Input Fields (Dynamic Colors) ---

                    // Full Name Input
                    TextField(
                        value = fullName, onValueChange = { fullName = it; fullNameError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Full name", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true, isError = fullNameError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Person Icon", tint = if (fullNameError != null) errorColor else secondaryTextColor) }
                    )
                    if (fullNameError != null) { Text(fullNameError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Valid Email Input
                    TextField(
                        value = email, onValueChange = { email = it; emailError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Valid email", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, isError = emailError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon", tint = if (emailError != null) errorColor else secondaryTextColor) }
                    )
                    if (emailError != null) { Text(emailError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Input
                    TextField(
                        value = phoneNumber, onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) { phoneNumber = it; phoneError = null } },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Phone number", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, isError = phoneError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone Icon", tint = if (phoneError != null) errorColor else secondaryTextColor) }
                    )
                    if (phoneError != null) { Text(phoneError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Strong Password Input
                    TextField(
                        value = password, onValueChange = { password = it; passwordError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Strong password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, isError = passwordError != null,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = if (passwordError != null) errorColor else secondaryTextColor)
                            }
                        }
                    )
                    if (passwordError != null) { Text(passwordError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(10.dp))

                    // --- 3. Terms and Conditions Checkbox (Dynamic Colors) ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreedToTerms,
                                onCheckedChange = { agreedToTerms = it; termsError = null },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryColor,
                                    uncheckedColor = if (termsError != null) errorColor else secondaryTextColor
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            val termsText = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 12.sp)) { append("By checking the box you agree to our ") }
                                withStyle(style = SpanStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append("Terms") }
                                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 12.sp)) { append(" and ") }
                                withStyle(style = SpanStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append("Conditions") }
                            }
                            Text(text = termsText, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- 4. Next Button (Dynamic Colors) ---
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                scope.launch {
                                    delay(50)
                                    navController.navigate("VerificationScreen") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(text = "Next", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 5. Social Login Buttons (Side-by-Side Row) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Button
                        OutlinedButton(
                            onClick = { /* Handle Google Signup */ },
                            modifier = Modifier
                                .weight(1f) // Takes up half the space
                                .height(36.dp)
                                .padding(end = 6.dp), // Spacing between buttons
                            shape = RoundedCornerShape(50.dp), // 20.dp border radius
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = primaryTextColor
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF4285F4) // Google Blue
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Signup with Google",
                                modifier = Modifier.size(24.dp)
                                    .padding(end = 8.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Facebook Button
                        OutlinedButton(
                            onClick = { /* Handle Facebook Signup */ },
                            modifier = Modifier
                                .weight(1f) // Takes up half the space
                                .height(36.dp)
                                .padding(start = 6.dp), // Spacing between buttons
                            shape = RoundedCornerShape(50.dp), // 20.dp border radius
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = primaryTextColor
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF1877F2) // Facebook Blue
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.facebook),
                                contentDescription = "Signup with Facebook",
                                modifier = Modifier.size(24.dp)
                                    .padding(end = 8.dp),
                                tint = Color(0xFF1877F2)
                            )
                            Text(
                                text = "Facebook",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 6. Already a member? Login in (Dynamic Colors) ---
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Already a member? ", color = primaryTextColor, fontSize = 14.sp)
                        TextButton(onClick = { navController.navigate("LoginScreen") }) {
                            Text(text = "Login in", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SignupScreenPreview() {
    // Assuming you have access to DAMTheme and can pass the darkTheme flag for previewing
    // tn.esprit.dam.ui.theme.DAMTheme(darkTheme = false) {
    SignupScreen(navController = rememberNavController())
    // }
}