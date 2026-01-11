package com.example.locationapp.ui.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.example.locationapp.R
import com.example.locationapp.model.Friend
import com.example.locationapp.model.User
import com.example.locationapp.viewmodel.FriendsViewModel
import com.example.locationapp.model.FriendRequest

@Composable
fun FriendsPage(viewModel: FriendsViewModel) {
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val outgoingRequests by viewModel.outgoingRequests.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sortByPoints by remember { mutableStateOf(true) }
    var friendToRemove by remember { mutableStateOf<Friend?>(null) }

    val sortedFriends by remember(friends, sortByPoints) {
        derivedStateOf {
            if (sortByPoints) friends.sortedByDescending { it.points }
            else friends.sortedBy { it.userName.lowercase() }
        }
    }

    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = { Text("Remove Friend") },
            text = { Text("Are you sure you want to remove ${friendToRemove?.userName}? This will remove the friendship for both of you.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeFriend(friendToRemove!!.friendId)
                    friendToRemove = null
                }) { Text("Remove", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { friendToRemove = null }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEAE4D5)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Friends", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { sortByPoints = !sortByPoints }) {
                Icon(Icons.Default.Sort, null, Modifier.size(18.dp), tint = Color.Black)
                Spacer(Modifier.width(4.dp))
                Text(if (sortByPoints) "Points" else "A-Z", color = Color.Black)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.search(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by username...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = ""; viewModel.search("") }) { Icon(Icons.Default.Close, null) }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {

            if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                item { Text("Search Results", fontWeight = FontWeight.Bold, color = Color(0xFF6B6658)) }
                items(searchResults) { user ->
                    SearchResultItem(user) {
                        viewModel.sendRequest(user)
                        searchQuery = ""
                    }
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            }

            if (incomingRequests.isNotEmpty()) {
                item { Text("Incoming Requests", fontWeight = FontWeight.Bold, color = Color(0xFF6B6658)) }
                items(incomingRequests) { req ->
                    RequestItem(req, onAccept = { viewModel.acceptRequest(req) })
                }
            }

            if (outgoingRequests.isNotEmpty()) {
                item { Text("Sent Requests", fontWeight = FontWeight.Bold, color = Color(0xFF6B6658)) }
                items(outgoingRequests) { req ->
                    SentRequestItem(req, onCancel = { viewModel.cancelSentRequest(req.fromId) })
                }
            }

            item { Text("Your Friends", fontWeight = FontWeight.Bold, color = Color(0xFF6B6658)) }
            if (sortedFriends.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(top = 40.dp), Alignment.Center) { Text("No friends added yet.", color = Color(0xFFB6B09F)) } }
            } else {
                items(sortedFriends) { friend ->
                    FriendItem(friend, onRemove = { friendToRemove = friend })
                }
            }
        }
    }
}

@Composable
fun SentRequestItem(req: FriendRequest, onCancel: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = req.fromImage.ifBlank { R.drawable.profile_fallback },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(req.fromName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Pending invitation", fontSize = 11.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = onCancel) { Icon(Icons.Default.Cancel, "Cancel", tint = Color.LightGray) }
        }
    }
}

@Composable
fun RequestItem(req: FriendRequest, onAccept: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = req.fromImage.ifBlank { R.drawable.profile_fallback },
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFB6B09F)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(req.fromName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${req.fromPoints.toInt()} pts", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Accept", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = friend.profileImage.ifBlank { R.drawable.profile_fallback },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFB6B09F)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(friend.userName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Surface(color = Color.Black, shape = RoundedCornerShape(50)) {
                        Text(
                            "${friend.points.toInt()} pts",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.PersonRemove, "Remove Friend", tint = Color(0xFFB6B09F))
            }
        }
    }
}

@Composable
fun SearchResultItem(user: User, onSendRequest: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.profileImage.ifBlank { R.drawable.profile_fallback },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Text(user.userName, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onSendRequest) { Icon(Icons.Default.PersonAdd, "Send Request") }
        }
    }
}
