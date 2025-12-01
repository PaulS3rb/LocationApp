package com.example.locationapp.repository

import com.example.locationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun signup(userName: String, email: String, password: String): Result<User> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val uid = auth.currentUser!!.uid

            val user = User(userName = userName, email = email, points = 0f)
            users.document(uid).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            val uid = auth.currentUser!!.uid

            val snapshot = users.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)!!

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
