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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun HomePage() {

    val currentCity = remember {
        object {
            val name = "Cluj-Napoca"
            val country = "Romania"
            val points = 0
            val friendsVisited = 18
            val distanceFromHome = 0
            val image =
                "https://images.unsplash.com/photo-1738686373369-d83d5eba53b6?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80"
        }
    }

    val userStats = remember {
        object {
            val totalPoints = 850
            val citiesVisited = 12
            val rank = 3
            val totalFriends = 24
        }
    }

    val recentVisits = remember {
        listOf(
            Triple("Bucharest", 250, "2 days ago"),
            Triple("Brașov", 120, "1 week ago"),
            Triple("Sibiu", 100, "2 weeks ago")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAE4D5))
    ) {

        /* ---------------- HEADER ---------------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAE4D5))
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text("Welcome back,", color = Color(0xFFB6B09F), fontSize = 14.sp)
                    Text("Traveler", color = Color.Black, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${userStats.totalPoints} pts", color = Color.White)
                }
            }
        }

        /* ---------------- MAIN CONTENT ---------------- */
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            /* -------- CURRENT CITY CARD -------- */
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {

                    // IMAGE AREA
                    Box(modifier = Modifier.height(180.dp)) {
                        AsyncImage(
                            model = currentCity.image,
                            contentDescription = currentCity.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Current Location",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                currentCity.name,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                currentCity.country,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // CARD CONTENT
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            // Points earned
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Points Earned", color = Color(0xFFB6B09F), fontSize = 13.sp)
                                    Text("${currentCity.points}", color = Color.Black, fontSize = 22.sp)
                                }
                            }

                            // Friends visited
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Friends Visited", color = Color(0xFFB6B09F), fontSize = 13.sp)
                                    Text("${currentCity.friendsVisited}", color = Color.Black, fontSize = 22.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // City Info box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text("City Info", color = Color(0xFFB6B09F), fontSize = 12.sp)
                            Text(
                                "You're in your home city! Home cities don't earn points, but ${currentCity.friendsVisited} of your friends have been here. Travel to other cities to earn more points!",
                                color = Color.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 6.dp)
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
                QuickStatCard("Cities", userStats.citiesVisited.toString())
                QuickStatCard("Rank", "#${userStats.rank}")
                QuickStatCard("Friends", userStats.totalFriends.toString())
            }

            /* -------- RECENT VISITS -------- */
            Card(
                elevation = CardDefaults.cardElevation(5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent Visits", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    recentVisits.forEach { visit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFB6B09F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(visit.first, color = Color.Black)
                                    Text(visit.third, color = Color(0xFFB6B09F), fontSize = 12.sp)
                                }
                            }

                            Text("+${visit.second} pts", color = Color.Black, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        //modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(label, color = Color(0xFFB6B09F), fontSize = 12.sp)
        }
    }
}