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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.repository.LocationService
import com.example.locationapp.ui.Pages.FriendsPage
import com.example.locationapp.ui.Pages.HomePage
import com.example.locationapp.ui.Pages.ProfilePage
import com.example.locationapp.ui.Pages.SetHomePage
import com.example.locationapp.ui.auth.AuthScreen
import com.example.locationapp.ui.theme.BackgroundColor
import com.example.locationapp.viewmodel.AuthViewModel
import com.example.locationapp.viewmodel.AuthViewModelFactory
import com.example.locationapp.viewmodel.HomeViewModel
import com.example.locationapp.viewmodel.HomeViewModelFactory
import com.example.locationapp.viewmodel.ProfileViewModel
import com.example.locationapp.viewmodel.ProfileViewModelFactory
import com.example.locationapp.viewmodel.SetHomeViewModel
import com.example.locationapp.viewmodel.SetHomeViewModelFactory

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Re-fetch data that needs location.
                homeViewModel.fetchData()
                profileViewModel.fetchData()
            } else {
                // Explain to the user that the feature is unavailable.
            }
        }

    private fun askForLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Optionally show a dialog explaining why you need the permission.
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
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(
            AuthRepository(applicationContext),
            LocationRepository()
        )
    }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AuthRepository(applicationContext),
            LocationRepository(),
            LocationService(applicationContext)
        )}
    // ViewModel for the new "Set Home" screen
    private val setHomeViewModel: SetHomeViewModel by viewModels {
        SetHomeViewModelFactory(
            AuthRepository(applicationContext),
            LocationService(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForLocationPermission()
        setContent {
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
            // This user state is the source of truth for deciding which screen to show post-login
            val user by profileViewModel.user.collectAsState()

            // This LaunchedEffect triggers when the user's auth state changes.
            // If they just logged in, we fetch their profile data.
            LaunchedEffect(isAuthenticated) {
                if (isAuthenticated) {
                    profileViewModel.fetchData()
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = BackgroundColor) {
                if (isAuthenticated) {
                    // Check if the user object has been loaded first
                    if (user != null) {
                        // Once we have the user object, check if home has been set
                        if (user!!.hasSetHome) {
                            // If home IS set, show the main app.
                            LocationAppApp(profileViewModel, homeViewModel, authViewModel)
                        } else {
                            // If home is NOT set, show the setup page.
                            SetHomePage(
                                viewModel = setHomeViewModel,
                                onHomeSet = {
                                    // This callback is crucial. When the user confirms their home,
                                    // we re-fetch the profile data. This updates the `user` state
                                    // which causes this whole block to re-evaluate, now showing the main app.
                                    profileViewModel.fetchData()
                                }
                            )
                        }
                    } else {
                        // If user is still null (i.e., being fetched), show a loading screen.
                        // This prevents a flicker where the SetHomePage might appear for a split second.
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // If not authenticated, show the login/signup screen.
                    AuthScreen(viewModel = authViewModel, onLoginSuccess = {}, onSignupSuccess = {})
                }
            }
        }
    }
}

@Composable
fun LocationAppApp(
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(destination.icon, contentDescription = destination.label)
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = {
                        currentDestination = destination
                        // When a tab is clicked, refresh its data to keep it current
                        when (destination) {
                            AppDestinations.HOME -> homeViewModel.fetchData()
                            AppDestinations.PROFILE -> profileViewModel.fetchData()
                            AppDestinations.FRIENDS -> { /* Handle Friends refresh if needed */ }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomePage(homeViewModel)
                    AppDestinations.FRIENDS -> FriendsPage()
                    AppDestinations.PROFILE -> ProfilePage(profileViewModel, authViewModel)
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
