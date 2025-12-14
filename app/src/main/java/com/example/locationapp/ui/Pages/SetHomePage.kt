package com.example.locationapp.ui.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationapp.viewmodel.CitySearchResult
import com.example.locationapp.viewmodel.SetHomeViewModel

@Composable
fun SetHomePage(
    viewModel: SetHomeViewModel,
    onHomeSet: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val detectedLocation = user?.currentLocation?.takeIf { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAE4D5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home Icon",
            modifier = Modifier.size(48.dp),
            tint = Color.Black
        )
        Spacer(Modifier.height(16.dp))
        Text("Set Your Home Base", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            "Confirm your auto-detected city or search for it manually.",
            textAlign = TextAlign.Center,
            color = Color(0xFF6B6658)
        )
        Spacer(Modifier.height(24.dp))

        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.onSearchTextChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search for your home city") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(Modifier.height(16.dp))

        if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show detected location as the first, special option if available and no search is active
                if (detectedLocation != null && user != null && searchText.isEmpty()) {
                    item {
                        Text("Detected Location:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        CityRow(
                            // The detected location might not be fully formatted, so we create a simple CitySearchResult for it
                            city = CitySearchResult(detectedLocation, user!!.currentLatitude, user!!.currentLongitude),
                            onClick = { city -> viewModel.setHomeLocation(city, onHomeSet) }
                        )
                    }
                }

                // Show search results
                if (searchResults.isNotEmpty()) {
                    item {
                        Text("Search Results:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(searchResults) { city ->
                        CityRow(
                            city = city,
                            onClick = { selectedCity -> viewModel.setHomeLocation(selectedCity, onHomeSet) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityRow(city: CitySearchResult, onClick: (CitySearchResult) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(city) },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the formatted address from the CitySearchResult
            Text(city.formattedAddress, fontSize = 16.sp)
        }
    }
}
