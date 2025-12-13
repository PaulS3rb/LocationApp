package com.example.locationapp.ui.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.locationapp.viewmodel.HomeViewModel

@Composable
fun HomePage(viewModel: HomeViewModel) {
    val user by viewModel.user.collectAsState()
    val potentialPoints by viewModel.potentialPoints.collectAsState()
    val isClaimable by viewModel.isClaimable.collectAsState()
    val isClaiming by viewModel.isClaiming.collectAsState()
    val claimResult by viewModel.claimResult.collectAsState() // Collect the new state

    // --- ADD SNACKBAR HOST STATE ---
    val snackbarHostState = remember { SnackbarHostState() }

    // --- SHOW SNACKBAR WHEN CLAIM RESULT CHANGES ---
    LaunchedEffect(claimResult) {
        claimResult?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Return a loading indicator or an empty screen until the user data is ready
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // --- WRAP IN SCAFFOLD TO HOST THE SNACKBAR ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFEAE4D5) // Match your background color
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Use padding from scaffold
                .verticalScroll(rememberScrollState())
        ) {
            // ... (The rest of your Column content remains exactly the same)

            /* ---------------- HEADER ---------------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Welcome back,", color = Color(0xFFB6B09F), fontSize = 14.sp)
                        Text(user!!.userName, color = Color.Black, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Points", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${user!!.points.toInt()} pts", color = Color.White)
                    }
                }
            }

            /* ---------------- MAIN CONTENT ---------------- */
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                /* -------- ACTION CARD: CURRENT LOCATION -------- */
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(modifier = Modifier.height(180.dp)) {
                            AsyncImage(
                                model = user!!.currentLocationImage,
                                contentDescription = user!!.currentLocation,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Current Location", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                                }
                                Text(
                                    if (user!!.currentLocation.isNotBlank()) user!!.currentLocation else "No City Detected",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isClaimable) {
                                Text("New City Detected!", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Claim your points for traveling to ${user!!.currentLocation}.", color = Color(0xFF6B6658), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.claimPoints() },
                                    enabled = !isClaiming,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    if (isClaiming) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("CLAIM $potentialPoints POINTS", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (user!!.currentLocation.isNotBlank()) {
                                Text("Welcome back to ${user!!.currentLocation}", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("You have already claimed points for this city.", color = Color(0xFF6B6658), fontSize = 14.sp)
                            } else {
                                Text("You are at your Home Base", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Travel to a new city to earn points! Your next adventure awaits.",
                                    color = Color(0xFF6B6658),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                /* -------- QUICK STATS -------- */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickStatCard("Cities", user!!.citiesVisited.toString(), modifier = Modifier.weight(1f))
                    QuickStatCard("Rank", "#-", modifier = Modifier.weight(1f)) // Placeholder
                    QuickStatCard("Friends", "0", modifier = Modifier.weight(1f)) // Placeholder
                }
            }
        }
    }
}


@Composable
fun QuickStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color(0xFFB6B09F), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
