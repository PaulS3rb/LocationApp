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
import androidx.compose.material.icons.filled.Map
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.repository.LocationService
import com.example.locationapp.ui.Pages.FriendsPage
import com.example.locationapp.ui.Pages.MapPage
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
import com.example.locationapp.viewmodel.FriendsViewModel
import com.example.locationapp.viewmodel.FriendsViewModelFactory
import com.example.locationapp.repository.FriendRepository


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                homeViewModel.fetchData()
                profileViewModel.fetchData()
            } else {
                // Permission is denied.
            }
        }

    private fun askForLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            }
            else -> {
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

    private val friendsViewModel: FriendsViewModel by viewModels {
        FriendsViewModelFactory(FriendRepository())
    }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AuthRepository(applicationContext),
            LocationRepository(),
            LocationService(applicationContext),
            FriendRepository()
        )}
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
            val user by profileViewModel.user.collectAsState()

            LaunchedEffect(isAuthenticated) {
                if (isAuthenticated) {
                    profileViewModel.fetchData()
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = BackgroundColor) {
                if (isAuthenticated) {
                    if (user != null) {
                        if (user!!.hasSetHome) {
                            LocationAppApp(profileViewModel, homeViewModel, authViewModel, friendsViewModel)
                        } else {
                            SetHomePage(
                                viewModel = setHomeViewModel,
                                onHomeSet = {
                                    profileViewModel.fetchData()
                                }
                            )
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
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
    authViewModel: AuthViewModel,
    friendsViewModel: FriendsViewModel
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
                        when (destination) {
                            AppDestinations.HOME -> homeViewModel.fetchData()
                            AppDestinations.PROFILE -> profileViewModel.fetchData()
                            AppDestinations.FRIENDS -> { friendsViewModel.fetchFriends()}
                            AppDestinations.MAP -> { /* Handle Map refresh if needed */ }
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
                    AppDestinations.FRIENDS -> FriendsPage(friendsViewModel)
                    AppDestinations.MAP -> MapPage()
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
    MAP("Map", Icons.Default.Map)


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
