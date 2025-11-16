package com.example.locationapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

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
            .verticalScroll(rememberScrollState())
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