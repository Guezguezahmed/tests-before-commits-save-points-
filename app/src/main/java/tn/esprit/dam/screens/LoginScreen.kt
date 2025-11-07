package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke // Required for OutlinedButton border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R
import tn.esprit.dam.ui.theme.AppTheme // IMPORTANT: Import the theme

// -----------------------------------------------------------------------------
// --- PUBLIC ENTRY POINT - Still accepts state/toggle but doesn't use them ---
// -----------------------------------------------------------------------------
@Composable
fun LoginScreen(
    navController: NavController,
    // Kept for compatibility with the NavHost signature
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    LoginScreenContent(
        navController = navController
    )
}

// -----------------------------------------------------------------------------
// --- CONTENT COMPOSABLE (Theme parameters are now removed from content) ---
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- USE STANDARD MATERIAL THEME COLORS ---
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardSurfaceColor = MaterialTheme.colorScheme.surface
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val textOnPrimary = MaterialTheme.colorScheme.onPrimary


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(cardSurfaceColor)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header + Illustration
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_illustration),
                    contentDescription = "Login Illustration",
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "sign in to access your account",
                        fontSize = 14.sp,
                        color = secondaryTextColor
                    )
                }
            }

            // Email
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your email", color = secondaryTextColor.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    cursorColor = primaryColor,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon",
                        tint = secondaryTextColor
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    cursorColor = primaryColor,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = secondaryTextColor
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember me + Forgot password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            uncheckedColor = secondaryTextColor
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                TextButton(onClick = { navController.navigate("forgot_password") }) {
                    Text(
                        text = "Forgot password?",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- 1. Next button (Primary action) ---
            Button(
                onClick = { navController.navigate("HomeScreen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Next",
                    color = textOnPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = textOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Social Login Buttons (Side-by-Side Row) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Button
                OutlinedButton(
                    onClick = { /* Handle Google Login */ },
                    modifier = Modifier
                        .weight(1f) // Takes up half the space
                        .height(36.dp)
                        .padding(end = 6.dp), // Spacing between buttons
                    shape = RoundedCornerShape(50.dp), // 20.dp border radius
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryTextColor
                    ),
                    border = BorderStroke( // Fixed compilation error by using BorderStroke
                        width = 1.dp,
                        color = Color(0xFF4285F4) // Google Blue
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Login with Google",
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
                    onClick = { /* Handle Facebook Login */ },
                    modifier = Modifier
                        .weight(1f) // Takes up half the space
                        .height(36.dp)
                        .padding(start = 6.dp), // Spacing between buttons
                    shape = RoundedCornerShape(50.dp), // 20.dp border radius
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryTextColor
                    ),
                    border = BorderStroke( // Fixed compilation error by using BorderStroke
                        width = 1.dp,
                        color = Color(0xFF1877F2) // Facebook Blue
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Login with Facebook",
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

            // Register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New member ? ", color = primaryTextColor, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate("SignupScreen") }) {
                    Text(
                        text = "Register now",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LoginScreenPreview() {
    // Assuming 'tn.esprit.dam.ui.theme.DAMTheme' is your actual theme function.
    tn.esprit.dam.ui.theme.DAMTheme(darkTheme = false) {
        LoginScreen(
            navController = rememberNavController()
        )
    }
}