package com.example.locationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationAppTheme {
                LocationAppApp()
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
                    AppDestinations.HOME -> Greeting("Home Page")
                    AppDestinations.FAVORITES -> FriendsPage()
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
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = "Hello $name!")
    }
}

/* ------------------ PROFILE PAGE ------------------ */

data class Location(
    val city: String,
    val country: String,
    val points: Int,
    val distance: Int
)

@Composable
fun ProfilePage() {
    val userData = remember {
        UserData(
            name = "Alex Thompson",
            email = "alex.thompson@email.com",
            profileImage = "https://images.unsplash.com/photo-1570170609489-43197f518df0",
            totalPoints = 8450,
            topLocations = listOf(
                Location("Tokyo", "Japan", 2150, 6800),
                Location("Sydney", "Australia", 1980, 9500),
                Location("Barcelona", "Spain", 1620, 4200)
            ),
            citiesVisited = 24,
            countriesVisited = 12
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAE4D5))
            .padding(bottom = 24.dp)

    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2))
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Avatar
                AsyncImage(
                    model = userData.profileImage,
                    contentDescription = userData.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB6B09F))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(userData.name, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Medium)

                Text(
                    userData.email,
                    color = Color(0xFFB6B09F),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Points Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${userData.totalPoints} Points",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Top Locations
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("Top 3 Locations", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            userData.topLocations.forEachIndexed { index, location ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#${index + 1}", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(location.city, color = Color.Black, fontWeight = FontWeight.Medium)
                                Text(location.country, color = Color(0xFFB6B09F), fontSize = 12.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = Color(0xFFB6B09F),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "${location.points} pts",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                "${location.distance} km",
                                color = Color(0xFFB6B09F),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Cities Visited", userData.citiesVisited.toString(), modifier = Modifier.weight(1f))
                StatCard("Countries", userData.countriesVisited.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .heightIn(min = 80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFFB6B09F),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (value.isNotBlank()) value else "—",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


data class UserData(
    val name: String,
    val email: String,
    val profileImage: String,
    val totalPoints: Int,
    val topLocations: List<Location>,
    val citiesVisited: Int,
    val countriesVisited: Int
)


data class Friend(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String,
    val points: Int,
    val citiesVisited: Int
)

@Composable
fun FriendsPage() {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("points") } // points, alphabetical, cities

    // Mock data
    val friendsData = remember {
        listOf(
            Friend("1", "Sarah Johnson", "sarah.j@email.com",
                "https://images.unsplash.com/photo-1581065178047-8ee15951ede6", 12450, 32),
            Friend("2", "Michael Chen", "m.chen@email.com",
                "https://images.unsplash.com/photo-1597202992582-9ee5c6672095", 9870, 28),
            Friend("3", "Emma Rodriguez", "emma.r@email.com",
                "https://images.unsplash.com/photo-1709287253135-865c92751871", 8920, 25),
            Friend("4", "David Kim", "david.kim@email.com",
                "https://images.unsplash.com/photo-1644966825640-bf597f873b89", 7650, 22),
            Friend("5", "Lisa Anderson", "lisa.a@email.com",
                "https://images.unsplash.com/photo-1618491609764-0dc04604a02a", 6320, 19),
            Friend("6", "James Wilson", "j.wilson@email.com",
                "https://images.unsplash.com/photo-1704054006064-2c5b922e7a1e", 5890, 18),
            Friend("7", "Anna Martinez", "anna.m@email.com",
                "https://images.unsplash.com/photo-1581065178047-8ee15951ede6", 4750, 15),
            Friend("8", "Chris Brown", "chris.b@email.com",
                "https://images.unsplash.com/photo-1597202992582-9ee5c6672095", 3980, 12),
        )
    }

    val filtered = remember(searchQuery, sortBy) {
        friendsData
            .filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                when (sortBy) {
                    "points" -> compareByDescending<Friend> { it.points }
                    "alphabetical" -> compareBy { it.name }
                    "cities" -> compareByDescending { it.citiesVisited }
                    else -> compareByDescending<Friend> { it.points }
                }
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAE4D5))
    ) {

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2))
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Friends",
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search friends...", color = Color(0xFFB6B09F)) },
                leadingIcon = {
                    //Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB6B09F))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB6B09F),
                    unfocusedBorderColor = Color(0xFFB6B09F),
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sort Dropdown
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort by:", color = Color(0xFFB6B09F), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))

                SortDropdown(sortBy = sortBy, onChange = { sortBy = it })
            }
        }

        // Friends List
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "${filtered.size} friends",
                fontSize = 14.sp,
                color = Color(0xFFB6B09F),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            filtered.forEachIndexed { index, friend ->
                FriendCard(friend, index, sortBy)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No friends found", color = Color(0xFFB6B09F))
                }
            }
        }
    }
}


@Composable
fun SortDropdown(sortBy: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                when (sortBy) {
                    "points" -> "Points (High → Low)"
                    "alphabetical" -> "Alphabetical (A–Z)"
                    "cities" -> "Cities Visited"
                    else -> "Sort"
                },
                color = Color.Black
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Points (High to Low)") },
                onClick = {
                    onChange("points")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Alphabetical (A–Z)") },
                onClick = {
                    onChange("alphabetical")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Cities Visited") },
                onClick = {
                    onChange("cities")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun FriendCard(friend: Friend, index: Int, sortBy: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Rank badge when sorting by points
            if (sortBy == "points") {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#${index + 1}", color = Color.White)
                }
            }

            // Avatar
            AsyncImage(
                model = friend.profileImage,
                contentDescription = friend.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB6B09F)),
                contentScale = ContentScale.Crop
            )

            // Main info
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, color = Color.Black, fontWeight = FontWeight.Medium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("${friend.points} pts", color = Color(0xFFB6B09F), fontSize = 13.sp)
                    Text("${friend.citiesVisited} cities", color = Color(0xFFB6B09F), fontSize = 13.sp)
                }
            }

            // Badge
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "${friend.points} pts",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

