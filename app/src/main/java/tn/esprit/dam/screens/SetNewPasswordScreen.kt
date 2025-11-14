package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.R // Ensure you have access to your resources
import tn.esprit.dam.ui.theme.DAMTheme // Import your theme

/**
 * Main Composable for the Set New Password Screen.
 * Uses MaterialTheme.colorScheme for dynamic light/dark theme support.
 */
@Composable
fun SetNewPasswordScreen(
    navController: NavController,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    SetNewPasswordContent(
        navController = navController,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordContent(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // 1. Dynamic Colors using MaterialTheme.colorScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val hintColor = secondaryTextColor

    // 2. State Management
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val buttonEnabled = newPassword.isNotEmpty() && newPassword == confirmPassword

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Top Left)
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryTextColor
                    )
                }

                // Theme Toggle Button (Top Right)
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryTextColor
                    )
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3. Header Text
            Text(
                text = "Set New Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            Text(
                text = "Create an unique password.",
                fontSize = 16.sp,
                color = secondaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // 4. Illustration (Placeholder)
            // Using a placeholder for the illustration image (R.drawable.ic_launcher_foreground)
            // Replace with your actual illustration asset if available.
            val illustration: Painter = painterResource(id = R.drawable.ic_launcher_foreground) // Placeholder

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Make it slightly smaller than the full width
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkTheme) Color(0xFF324A38) else Color(0xFFEFF8F1), // Dk_Tertiary / Lt_Surface for background
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = illustration,
                        contentDescription = "Set New Password Illustration",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. New Password Input Field
            Text(
                text = "New Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = primaryTextColor,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text("Create new password", color = hintColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = hintColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Confirm Password Input Field
            Text(
                text = "Confirm Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = primaryTextColor,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Re-enter password", color = hintColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = hintColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 7. Reset Password Button
            Button(
                onClick = {
                    // Navigate logic after successful password reset
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Password reset successfully!",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                        delay(600)
                        navController.navigate("PasswordChangedScreen") {
                            // Clear back stack to prevent going back to password reset flow
                            popUpTo("PasswordChangedScreen") { inclusive = true }
                        }
                    }
                },
                enabled = buttonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text(
                    text = "Reset Password",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 8. Reset password later? Link
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.navigate("LoginScreen") }
            ) {
                Text(
                    text = "Reset password later?",
                    color = primaryColor, // Using primary color for the text link
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SetNewPasswordScreenLightPreview() {
    DAMTheme(darkTheme = false) {
        SetNewPasswordScreen(navController = rememberNavController(), isDarkTheme = false)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SetNewPasswordScreenDarkPreview() {
    DAMTheme(darkTheme = true) {
        SetNewPasswordScreen(navController = rememberNavController(), isDarkTheme = true)
    }
}