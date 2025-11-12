package com.example.locationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.locationapp.ui.auth.AuthScreen
import com.example.locationapp.ui.theme.BackgroundColor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(color = BackgroundColor) {
                AuthScreen(
                    onLoginSuccess = {
                        // TODO: Navigate to HomeScreen or Dashboard
                        println("Login successful!")
                    },
                    onSignupSuccess = {
                        // TODO: Navigate to Welcome or Main App screen
                        println("Signup successful!")
                    }
                )
            }
        }
    }
}

@Composable
fun LocationAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {

    }
}
