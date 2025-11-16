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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

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
            .verticalScroll(rememberScrollState())

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
                color = Color(0xFF6B6658),  // DARKER! Now visible
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
