package tn.esprit.dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.* // Import remember and mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.screens.EventsScreen
import tn.esprit.dam.screens.FriendsScreen
import tn.esprit.dam.screens.HomeScreen
import tn.esprit.dam.screens.LeaderboardScreen
import tn.esprit.dam.screens.LoginScreen
import tn.esprit.dam.screens.PlacmentScreen
import tn.esprit.dam.screens.ProfileScreen
import tn.esprit.dam.screens.ProfileScreenSettings
import tn.esprit.dam.screens.SignupScreen
import tn.esprit.dam.screens.SocialScreen
import tn.esprit.dam.screens.SplashScreen
import tn.esprit.dam.screens.TeamsScreen
import tn.esprit.dam.screens.VerificationScreen
import tn.esprit.dam.screens.WelcomeScreen1
import tn.esprit.dam.screens.WelcomeScreen2
import tn.esprit.dam.screens.WelcomeScreen3
import tn.esprit.dam.ui.theme.DAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // FIX 1: Define the dark theme state
            var darkTheme by remember { mutableStateOf(true) }

            // FIX 2: Create the toggle function to change the state
            val onThemeToggle: () -> Unit = {
                darkTheme = !darkTheme
            }

            // FIX 3: Pass the current theme state to your DAMTheme
            DAMTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                // Define the NavHost for navigation
                NavHost(
                    navController = navController,
                    startDestination = "splash" // Start with splash screen
                ) {
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }
                    composable("welcome_screen_1") {
                        WelcomeScreen1(navController = navController)
                    }
                    composable("welcome_screen_2") {
                        WelcomeScreen2(navController = navController)
                    }
                    composable("welcome_screen_3") {
                        WelcomeScreen3(navController = navController)
                    }
                    composable("LoginScreen") {
                        LoginScreen(navController = navController)
                    }
                    composable("SignUpScreen") {
                        SignupScreen(navController = navController)
                    }
                    composable("VerificationScreen") {
                        VerificationScreen(navController = navController)
                    }

                    // Bottom navigation screens
                    composable("HomeScreen") {
                        HomeScreen(navController = navController)
                    }
                    composable("leaderboardScreen") {
                        LeaderboardScreen(navController = navController)
                    }

                    // Add placeholders for other bottom navigation items
                    composable("EventsScreen") {
                        EventsScreen(navController = navController)
                    }
                    composable("SocialScreen") {
                        SocialScreen(navController = navController)
                    }
                    composable("ProfileScreenSettings") {
                        // FIX: Pass 'darkTheme' to ProfileScreenSettings (Resolves line 93 error)
                        ProfileScreenSettings(
                            navController = navController,
                            darkTheme = darkTheme,
                            onThemeToggle = onThemeToggle )
                    }
                    composable("FriendsScreen") {
                        FriendsScreen(navController = navController)
                    }
                    composable("TeamsScreen") {
                        TeamsScreen(navController = navController)
                    }
                    composable("PlacmentsScreen") {
                        PlacmentScreen(navController = navController)
                    }
                    composable("ProfileScreen") {
                        // FIX: The parameters are now correctly defined in the ProfileScreen Composable
                        ProfileScreen(
                            navController = navController,
                            darkTheme = darkTheme // <-- New required parameter
                        )
                    }
                }
            }
        }
    }
}