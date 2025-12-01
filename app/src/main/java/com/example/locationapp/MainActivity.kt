package com.example.locationapp

import HomePage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.locationapp.database.AppDatabase
import com.example.locationapp.ui.Pages.FriendsPage
import com.example.locationapp.ui.Pages.ProfilePage
import com.example.locationapp.ui.auth.AuthScreen
import com.example.locationapp.ui.theme.BackgroundColor
import com.example.locationapp.ui.theme.LocationAppTheme
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
            var isAuthenticated by rememberSaveable { mutableStateOf(false) }

            Surface(color = BackgroundColor) {
                if (isAuthenticated) {
                    LocationAppApp()
                } else {
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            isAuthenticated = true
                        },
                        onSignupSuccess = {
                            isAuthenticated = true
                        }
                    )
                }
            }
        }
    }
}


@PreviewScreenSizes
@Composable
fun LocationAppApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(it.icon, contentDescription = it.label)
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomePage()
                    AppDestinations.FRIENDS -> FriendsPage()
                    AppDestinations.PROFILE -> ProfilePage()
                }
            }
        }
    }
}


enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),

    FRIENDS("Friends", Icons.Default.Person),

    PROFILE("Profile", Icons.Default.AccountBox),


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
