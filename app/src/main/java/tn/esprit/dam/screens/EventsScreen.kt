package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R // Make sure this R is correctly pointing to your resources

@Composable
fun EventsScreen(navController: NavController) {
    // A Box is the best choice for layering and specific alignment of elements
    Box(
        modifier = Modifier
            .fillMaxSize(), // Make sure the Box takes up the full screen
        contentAlignment = Alignment.Center
    ) {
        // Full-screen Image
        Image(
            painter = painterResource(id = R.drawable.cardtest), // Use your image resource
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(), // This ensures the image fills the entire screen
            contentScale = ContentScale.Crop // This ensures the image covers the screen without distortion
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventsScreenPreview() {
    EventsScreen(navController = rememberNavController())
}
