package com.example.locationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.locationapp.database.AppDatabase
import com.example.locationapp.ui.auth.AuthScreen
import com.example.locationapp.ui.theme.BackgroundColor
import com.example.locationapp.viewmodel.AuthViewModel
import com.example.locationapp.viewmodel.AuthViewModelFactory


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    // Use the factory to provide the UserDao to the AuthViewModel
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(db.userDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "location-app-db"
        ).build()

        enableEdgeToEdge()
        setContent {
            Surface(color = BackgroundColor) {
                AuthScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // TODO: Navigate to HomeScreen or Dashboard
                        println("Login successful! Navigating...")
                    },
                    onSignupSuccess = {
                        // TODO: Navigate to Welcome or Main App screen
                        println("Signup successful! Navigating...")
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
        // You can define your navigation graph here later
    }
}
