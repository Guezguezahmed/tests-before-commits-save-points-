package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

// Roles for the dropdown
private val roles = listOf("JOUEUR", "OWNER", "ARBITRE")
private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

    // State for new and existing input fields
    var name by remember { mutableStateOf("") } // FullName split into Name and Surname
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") } // Displayed birth date
    var selectedRole by remember { mutableStateOf<String?>(null) } // Role
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // New field
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) } // New field
    var agreedToTerms by remember { mutableStateOf(false) }

    // State for new and existing validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var surnameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) } // New error state
    var termsError by remember { mutableStateOf<String?>(null) }

    // State for Role Dropdown
    var expanded by remember { mutableStateOf(false) }

    // Snackbar Host State for displaying messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Date Picker Dialog State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    // Validation patterns
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$")

    // Date Picker Logic
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDateText = DATE_FORMAT.format(Date(millis))
                            birthDateError = null // Clear error on successful selection
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun validateInputs(): Boolean {
        var isValid = true

        // Reset all errors first
        nameError = null; surnameError = null; emailError = null; phoneError = null; birthDateError = null; roleError = null
        passwordError = null; confirmPasswordError = null; termsError = null

        // Name and Surname Checks
        if (name.trim().length < 3) { nameError = "Name must be at least 3 characters."; isValid = false }
        if (surname.trim().length < 3) { surnameError = "Surname must be at least 3 characters."; isValid = false }

        // Email Check
        if (!emailPattern.matcher(email).matches()) { emailError = "Invalid email format."; isValid = false }

        // Phone Check
        if (!phoneNumber.matches(Regex("^\\d{8}$"))) { phoneError = "Phone number must be exactly 8 digits."; isValid = false }

        // Birth Date Check (Must be 18 years or older)
        if (birthDateText.isEmpty()) {
            birthDateError = "Birth date is required."; isValid = false
        } else {
            try {
                val birthDate = DATE_FORMAT.parse(birthDateText)
                val eighteenYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }.time
                if (birthDate == null || birthDate.after(eighteenYearsAgo)) {
                    birthDateError = "You must be 18 or older to register."; isValid = false
                }
            } catch (e: Exception) {
                birthDateError = "Invalid date format. Use DD/MM/YYYY."; isValid = false
            }
        }

        // Role Check
        if (selectedRole == null) { roleError = "Please select a role."; isValid = false }

        // Password Checks
        if (!passwordPattern.matcher(password).matches()) { passwordError = "Password must contain 6+ chars, uppercase, lowercase, and a number."; isValid = false }
        if (confirmPassword.isEmpty()) { confirmPasswordError = "Please confirm your password."; isValid = false }
        if (password != confirmPassword) { confirmPasswordError = "Passwords do not match."; isValid = false }

        // Terms Check
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

                    // Name Input
                    TextField(
                        value = name, onValueChange = { name = it; nameError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Name", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true, isError = nameError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Person Icon", tint = if (nameError != null) errorColor else secondaryTextColor) }
                    )
                    if (nameError != null) { Text(nameError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Surname Input
                    TextField(
                        value = surname, onValueChange = { surname = it; surnameError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Surname", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true, isError = surnameError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Person Icon", tint = if (surnameError != null) errorColor else secondaryTextColor) }
                    )
                    if (surnameError != null) { Text(surnameError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Input
                    TextField(
                        value = phoneNumber, onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) { phoneNumber = it; phoneError = null } },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Phone number (8 digits)", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, isError = phoneError != null,
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone Icon", tint = if (phoneError != null) errorColor else secondaryTextColor) }
                    )
                    if (phoneError != null) { Text(phoneError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
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

                    // Birth Date Input (Clickable for Date Picker)
                    TextField(
                        value = birthDateText, onValueChange = { /* Prevent manual entry */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }, // Opens DatePicker
                        placeholder = { Text("Birth date (DD/MM/YYYY)", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        enabled = false, // Disable manual text input
                        isError = birthDateError != null,
                        colors = TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Transparent, // Ensure indicators are hidden
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = inputBackgroundColor,
                            unfocusedContainerColor = inputBackgroundColor,
                            disabledContainerColor = inputBackgroundColor,
                            disabledTextColor = primaryTextColor // Text color when disabled
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar Icon", tint = if (birthDateError != null) errorColor else secondaryTextColor) }
                    )
                    if (birthDateError != null) { Text(birthDateError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))


                    // Role Dropdown Input
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = selectedRole ?: "Select Role",
                            onValueChange = { /* Read-only */ },
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            placeholder = { Text("Select Role", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            singleLine = true,
                            isError = roleError != null,
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        roleError = null
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    if (roleError != null) { Text(roleError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Strong Password Input
                    TextField(
                        value = password, onValueChange = { password = it; passwordError = null; if (confirmPassword.isNotEmpty()) confirmPasswordError = null },
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Input (New Field)
                    TextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; confirmPasswordError = null },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Confirm password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, isError = confirmPasswordError != null,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor, disabledContainerColor = inputBackgroundColor, cursorColor = primaryColor, focusedTextColor = primaryTextColor, unfocusedTextColor = primaryTextColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password", tint = if (confirmPasswordError != null) errorColor else secondaryTextColor)
                            }
                        }
                    )
                    if (confirmPasswordError != null) { Text(confirmPasswordError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.fillMaxWidth()) }
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
                                    // You can use a more explicit route name if you have one
                                    navController.navigate("VerificationScreen") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            } else {
                                // Scroll to the top to show the first error easily
                                scope.launch {
                                    delay(50) // Small delay to allow layout to update errors
                                    scrollState.animateScrollTo(0)
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
                                .weight(1f)
                                .height(36.dp)
                                .padding(end = 6.dp),
                            shape = RoundedCornerShape(50.dp),
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
                                .weight(1f)
                                .height(36.dp)
                                .padding(start = 6.dp),
                            shape = RoundedCornerShape(50.dp),
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
// ----------------------------------------------------------------------------------
// --- PREVIEW ---
// ----------------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SignupScreenPreview() {
    SignupScreen(navController = rememberNavController())
}