package com.example.locationapp.repository

import com.example.locationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.locationapp.model.Friend
import com.example.locationapp.model.FriendRequest
import com.google.firebase.firestore.FieldValue

class FriendRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    suspend fun searchUser(query: String): Result<List<User>> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            // Use a trick for partial matching: query >= query AND query < query + \uf8ff
            val snapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("userName", query)
                .whereLessThanOrEqualTo("userName", query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            // Filter out yourself
            Result.success(users.filter { it.userId != myUid })
        } catch (e: Exception) { Result.failure(e) }
    }

    // 2. Updated Send Request with Double-Write
    suspend fun sendFriendRequest(targetUser: User): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val myUserSnapshot = db.collection("users").document(myUid).get().await()
        val myUser = myUserSnapshot.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))

        return try {
            db.runTransaction { transaction ->
                // Path A: THEIR incoming requests
                val incomingRef = db.collection("users").document(targetUser.userId)
                    .collection("friendRequests").document(myUid)

                // Path B: MY outgoing (sent) requests
                val sentRef = db.collection("users").document(myUid)
                    .collection("sentRequests").document(targetUser.userId)

                val requestDataForThem = mapOf(
                    "fromId" to myUid,
                    "fromName" to myUser.userName,
                    "fromImage" to myUser.profileImage,
                    "fromPoints" to myUser.points,
                    "status" to "pending"
                )

                val requestDataForMe = mapOf(
                    "fromId" to targetUser.userId,
                    "fromName" to targetUser.userName,
                    "fromImage" to targetUser.profileImage,
                    "fromPoints" to targetUser.points,
                    "status" to "pending"
                )

                transaction.set(incomingRef, requestDataForThem)
                transaction.set(sentRef, requestDataForMe)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addFriend(friend: User): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        // We use the friend.userId which is now populated by @DocumentId
        if (friend.userId.isBlank()) return Result.failure(Exception("Invalid Friend ID"))

        return try {
            // 1. Add to MY friends list
            db.collection("users").document(myUid)
                .collection("friends").document(friend.userId)
                .set(mapOf(
                    "userName" to friend.userName,
                    "profileImage" to friend.profileImage,
                    "friendId" to friend.userId,
                    "points" to friend.points,
                    "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )).await()

            // 2. OPTIONAL: Add MYSELF to THEIR friends list (Mutual friendship)
            // If you want it to be mutual, you'd repeat the logic vice-versa here.

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(): Result<List<Friend>> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = db.collection("users").document(myUid)
                .collection("friends")
                .get()
                .await()
            Result.success(snapshot.toObjects(Friend::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Accept Request (Creates Mutual Friendship)
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val myUserSnapshot = db.collection("users").document(myUid).get().await()
        val myUser = myUserSnapshot.toObject(User::class.java)!!

        return try {
            db.runTransaction { transaction ->
                // Add them to MY friends
                val myFriendRef = db.collection("users").document(myUid).collection("friends").document(request.fromId)
                transaction.set(myFriendRef, mapOf(
                    "friendId" to request.fromId,
                    "userName" to request.fromName,
                    "profileImage" to request.fromImage,
                    "points" to request.fromPoints
                ))

                // Add ME to THEIR friends
                val theirFriendRef = db.collection("users").document(request.fromId).collection("friends").document(myUid)
                transaction.set(theirFriendRef, mapOf(
                    "friendId" to myUid,
                    "userName" to myUser.userName,
                    "profileImage" to myUser.profileImage,
                    "points" to myUser.points
                ))

                // Delete the request
                val requestRef = db.collection("users").document(myUid).collection("friendRequests").document(request.fromId)
                transaction.delete(requestRef)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // 3. Fetch Requests
    suspend fun getIncomingRequests(): Result<List<FriendRequest>> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = db.collection("users").document(myUid).collection("friendRequests").get().await()
            Result.success(snapshot.toObjects(FriendRequest::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getOutgoingRequests(): Result<List<FriendRequest>> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            // This requires a slightly different query. We need to find requests WHERE fromId == myUid
            // However, with our current structure, it's easier to store a copy of sent requests
            // OR query across all users (which is slow).
            // BETTER: Create a 'sentRequests' sub-collection for the user.
            val snapshot = db.collection("users").document(myUid)
                .collection("sentRequests").get().await()
            Result.success(snapshot.toObjects(FriendRequest::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Cancel a request I sent
    suspend fun cancelRequest(targetUserId: String): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.runTransaction { transaction ->
                // Remove from my 'sentRequests'
                transaction.delete(db.collection("users").document(myUid).collection("sentRequests").document(targetUserId))
                // Remove from their 'friendRequests'
                transaction.delete(db.collection("users").document(targetUserId).collection("friendRequests").document(myUid))
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Remove a friend (Mutual)
    suspend fun removeFriend(friendId: String): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.runTransaction { transaction ->
                // Delete from my list
                transaction.delete(db.collection("users").document(myUid).collection("friends").document(friendId))
                // Delete from their list
                transaction.delete(db.collection("users").document(friendId).collection("friends").document(myUid))
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getFriendsCount(): Result<Int> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = db.collection("users")
                .document(myUid)
                .collection("friends")
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}



// Simple helper model for the sub-collection

