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
            val snapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("userName", query)
                .whereLessThanOrEqualTo("userName", query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            Result.success(users.filter { it.userId != myUid })
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendFriendRequest(targetUser: User): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val myUserSnapshot = db.collection("users").document(myUid).get().await()
        val myUser = myUserSnapshot.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))

        return try {
            db.runTransaction { transaction ->
                val incomingRef = db.collection("users").document(targetUser.userId)
                    .collection("friendRequests").document(myUid)

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

        if (friend.userId.isBlank()) return Result.failure(Exception("Invalid Friend ID"))

        return try {
            db.collection("users").document(myUid)
                .collection("friends").document(friend.userId)
                .set(mapOf(
                    "userName" to friend.userName,
                    "profileImage" to friend.profileImage,
                    "friendId" to friend.userId,
                    "points" to friend.points,
                    "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )).await()

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

    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val myUserSnapshot = db.collection("users").document(myUid).get().await()
        val myUser = myUserSnapshot.toObject(User::class.java)!!

        return try {
            db.runTransaction { transaction ->
                val myFriendRef = db.collection("users").document(myUid).collection("friends").document(request.fromId)
                transaction.set(myFriendRef, mapOf(
                    "friendId" to request.fromId,
                    "userName" to request.fromName,
                    "profileImage" to request.fromImage,
                    "points" to request.fromPoints
                ))

                val theirFriendRef = db.collection("users").document(request.fromId).collection("friends").document(myUid)
                transaction.set(theirFriendRef, mapOf(
                    "friendId" to myUid,
                    "userName" to myUser.userName,
                    "profileImage" to myUser.profileImage,
                    "points" to myUser.points
                ))

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
            val snapshot = db.collection("users").document(myUid)
                .collection("sentRequests").get().await()
            Result.success(snapshot.toObjects(FriendRequest::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun cancelRequest(targetUserId: String): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.runTransaction { transaction ->
                transaction.delete(db.collection("users").document(myUid).collection("sentRequests").document(targetUserId))
                transaction.delete(db.collection("users").document(targetUserId).collection("friendRequests").document(myUid))
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun removeFriend(friendId: String): Result<Unit> {
        val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.runTransaction { transaction ->
                transaction.delete(db.collection("users").document(myUid).collection("friends").document(friendId))
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

