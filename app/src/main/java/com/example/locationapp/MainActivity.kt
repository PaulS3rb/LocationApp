package com.example.locationapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.ui.Pages.FriendsPage
import com.example.locationapp.ui.Pages.HomePage
import com.example.locationapp.ui.Pages.ProfilePage
import com.example.locationapp.viewmodel.ProfileViewModel
import com.example.locationapp.viewmodel.ProfileViewModelFactory
import com.example.locationapp.ui.auth.AuthScreen
import com.example.locationapp.ui.theme.BackgroundColor
import com.example.locationapp.viewmodel.AuthViewModel
import com.example.locationapp.viewmodel.AuthViewModelFactory
import com.example.locationapp.viewmodel.HomeViewModel
import com.example.locationapp.viewmodel.HomeViewModelFactory


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now fetch data that needs location.
                // We can trigger a refresh in the viewmodel here.
                homeViewModel.fetchData() // Re-fetch data now that we have permission
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied.
            }
        }

    private fun askForLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted. You're good to go.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show a dialog explaining why you need the permission
            }
            else -> {
                // Directly ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(applicationContext))
    }
    private val profileViewModel: ProfileViewModel by viewModels{
        ProfileViewModelFactory(AuthRepository(applicationContext))
    }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AuthRepository(applicationContext),
            LocationRepository()
        )
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForLocationPermission()
        setContent {
            // This now correctly reflects the user's auth status from the ViewModel
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

            Surface(color = BackgroundColor) {
                if (isAuthenticated) {
                    LocationAppApp(profileViewModel, homeViewModel)
                } else {
                    // The callbacks are now simplified because the ViewModel handles the state change
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { /* ViewModel handles this now */ },
                        onSignupSuccess = { /* ViewModel handles this now */ }
                    )
                }
            }
        }
    }
}


@Composable
fun LocationAppApp(profileViewModel: ProfileViewModel, homeViewModel: HomeViewModel) {
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
                    AppDestinations.HOME -> HomePage(homeViewModel)
                    AppDestinations.FRIENDS -> FriendsPage()
                    AppDestinations.PROFILE -> ProfilePage(profileViewModel)
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
