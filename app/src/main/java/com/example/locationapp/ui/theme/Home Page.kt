//package com.example.locationapp.ui.pages
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//
//@Composable
//fun HomePage(
//    currentPage: String,
//    onNavigate: (String) -> Unit
//) {
//    val currentCity = CurrentCity(
//        name = "Cluj-Napoca",
//        country = "Romania",
//        points = 0,
//        friendsVisited = 18,
//        image = "https://images.unsplash.com/photo-1738686373369-d83d5eba53b6"
//    )
//
//    val userStats = UserStats(
//        totalPoints = 850,
//        citiesVisited = 12,
//        rank = 3,
//        totalFriends = 24
//    )
//
//    val recentVisits = listOf(
//        Visit("Bucharest", 250, "2 days ago"),
//        Visit("Brașov", 120, "1 week ago"),
//        Visit("Sibiu", 100, "2 weeks ago")
//    )
//
//    Scaffold(
//        bottomBar = {
//            Nav(
//                currentPage = currentPage,
//                onNavigate = onNavigate
//            )
//        }
//    ) { padding ->
//
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFFF7F6F0))
//                .padding(padding)
//        ) {
//
//            // Header
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color(0xFFEAE4D5))
//                        .padding(20.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column {
//                            Text("Welcome back,", color = Color(0xFFB6B09F))
//                            Text(
//                                "Traveler",
//                                fontSize = 26.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//
//                        Row(
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(50))
//                                .background(Color.Black)
//                                .padding(horizontal = 16.dp, vertical = 10.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                "${userStats.totalPoints} pts",
//                                color = Color(0xFFF2F2F2),
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Current City Card
//            item {
//                Card(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .fillMaxWidth(),
//                    shape = RoundedCornerShape(16.dp),
//                    elevation = CardDefaults.cardElevation(6.dp)
//                ) {
//                    Box(modifier = Modifier.height(200.dp)) {
//                        AsyncImage(
//                            model = currentCity.image,
//                            contentDescription = currentCity.name,
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(
//                                    Brush.verticalGradient(
//                                        listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
//                                    )
//                                )
//                        )
//
//                        Column(
//                            modifier = Modifier
//                                .align(Alignment.BottomStart)
//                                .padding(16.dp)
//                        ) {
//                            Text(
//                                "Current Location",
//                                color = Color.White.copy(alpha = 0.8f),
//                                fontSize = 12.sp
//                            )
//                            Text(
//                                currentCity.name,
//                                color = Color.White,
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                currentCity.country,
//                                color = Color.White.copy(alpha = 0.8f),
//                                fontSize = 14.sp
//                            )
//                        }
//                    }
//
//                    Column(modifier = Modifier.padding(16.dp)) {
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            StatRow("Points Earned", currentCity.points)
//                            StatRow("Friends Visited", currentCity.friendsVisited)
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 16.dp)
//                                .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
//                                .padding(12.dp)
//                        ) {
//                            Text(
//                                "You're in your home city! Home cities don't earn points, " +
//                                        "but ${currentCity.friendsVisited} of your friends have been here.",
//                                color = Color(0xFF3C3A36),
//                                fontSize = 14.sp
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Quick Stats
//            item {
//                Row(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    QuickStatCard("Cities", userStats.citiesVisited)
//                    QuickStatCard("Rank", "#${userStats.rank}")
//                    QuickStatCard("Friends", userStats.totalFriends)
//                }
//            }
//
//            // Recent Visits
//            item {
//                Card(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .fillMaxWidth(),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(Modifier.padding(16.dp)) {
//                        Text("Recent Visits", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        recentVisits.forEach { visit ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 8.dp)
//                                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
//                                    .padding(12.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Column {
//                                    Text(visit.city, fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                                    Text(
//                                        visit.date,
//                                        color = Color(0xFFB6B09F),
//                                        fontSize = 13.sp
//                                    )
//                                }
//
//                                Text("+${visit.points} pts", fontWeight = FontWeight.Bold)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// --- Small helper composables ---
//
//@Composable
//fun StatRow(label: String, value: Int) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Column {
//            Text(label, color = Color(0xFFB6B09F), fontSize = 14.sp)
//            Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
//        }
//    }
//}
//
//@Composable
//fun QuickStatCard(label: String, value: Any) {
//    Card(
//        modifier = Modifier.weight(1f),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
//            Text(label, color = Color(0xFFB6B09F), fontSize = 12.sp)
//        }
//    }
//}
//
//// --- Data models ---
//data class CurrentCity(
//    val name: String,
//    val country: String,
//    val points: Int,
//    val friendsVisited: Int,
//    val image: String
//)
//
//data class UserStats(
//    val totalPoints: Int,
//    val citiesVisited: Int,
//    val rank: Int,
//    val totalFriends: Int
//)
//
//data class Visit(
//    val city: String,
//    val points: Int,
//    val date: String
//)
