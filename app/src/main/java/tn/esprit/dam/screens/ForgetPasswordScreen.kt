package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R
import androidx.compose.material3.OutlinedTextFieldDefaults
import tn.esprit.dam.ui.theme.DAMTheme // Import your theme

// NOTE: I've added a drawable placeholder for the illustration,
// since I don't have access to your actual resource file (R.drawable.forgot_password_illustration).
// You must replace R.drawable.ic_launcher_foreground with your actual illustration drawable.
// For demonstration, I will use a simple placeholder image resource.

/**
 * Main Composable for the Forget Password Screen.
 * Uses MaterialTheme.colorScheme for dynamic light/dark theme support.
 */
@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    // Theme state management is often handled outside, but we include it for the toggle functionality
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    ForgetPasswordContent(
        navController = navController,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordContent(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // 1. Dynamic Colors using MaterialTheme.colorScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant // Used for TextField background
    val hintColor = secondaryTextColor // Using outline color for subtle text/hints

    // 2. State Management
    var emailOrMobile by remember { mutableStateOf("") }
    var isEmailSelected by remember { mutableStateOf(true) } // State for toggling input type

    Scaffold(
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
        containerColor = backgroundColor // Set the background color here
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
                text = "Forgot Password?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            Text(
                text = "No worries, We got you.",
                fontSize = 16.sp,
                color = secondaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // 4. Illustration (Placeholder)
            // NOTE: You need to replace the placeholder with your actual illustration.
            // Assuming your illustration drawable resource is named 'forgot_password_illustration'
            val illustration: Painter = painterResource(id = R.drawable.forgotpasswordillus) // Placeholder

            // To mimic the illustration's container in the image, we use a Surface/Card.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkTheme) Color(0xFF324A38) else Color(0xFFEFF8F1), // Using Dk_Tertiary / Lt_Surface for illustration background
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = illustration,
                        contentDescription = "Forgot Password Illustration",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "We'll send you code to reset it.",
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. Input Toggle (Email/Mobile Number)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                InputToggleText(
                    text = "Email Address",
                    isSelected = isEmailSelected,
                    onClick = { isEmailSelected = true; emailOrMobile = "" },
                    primaryColor = primaryColor,
                    secondaryTextColor = secondaryTextColor
                )
                Spacer(modifier = Modifier.width(24.dp))
                InputToggleText(
                    text = "Mobile Number?",
                    isSelected = !isEmailSelected,
                    onClick = { isEmailSelected = false; emailOrMobile = "" },
                    primaryColor = primaryColor,
                    secondaryTextColor = secondaryTextColor
                )
            }

            // 6. Text Field Input
            OutlinedTextField(
                value = emailOrMobile,
                onValueChange = { emailOrMobile = it },
                label = null, // Label is handled by the toggle row above
                placeholder = {
                    Text(
                        text = if (isEmailSelected) "Enter email address" else "Enter mobile number",
                        color = hintColor
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isEmailSelected) KeyboardType.Email else KeyboardType.Phone
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 7. Send Code Button
            Button(
                onClick = {
                    // TODO: Implement logic to send code and navigate
                    navController.navigate("VerificationScreen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                enabled = emailOrMobile.isNotBlank() // Enable only if field is not empty
            ) {
                Text(
                    text = "Send Code",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 8. Back to Log In link (Bottom)
            Spacer(modifier = Modifier.weight(1f)) // Pushes content to the top
            TextButton(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "← Back to log in?",
                    color = primaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Helper Composable for the Email/Mobile Number toggle text.
 */
@Composable
fun InputToggleText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) primaryColor else secondaryTextColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(if (isSelected) 100.dp else 0.dp) // Only show underline if selected
                .height(2.dp)
                .background(primaryColor)
        )
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun ForgetPasswordScreenLightPreview() {
    DAMTheme(darkTheme = false) {
        ForgetPasswordScreen(navController = rememberNavController(), isDarkTheme = false)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun ForgetPasswordScreenDarkPreview() {
    DAMTheme(darkTheme = true) {
        ForgetPasswordScreen(navController = rememberNavController(), isDarkTheme = true)
    }
}