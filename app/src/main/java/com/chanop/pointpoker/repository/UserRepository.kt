package com.chanop.pointpoker.repository

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.chanop.pointpoker.SharedPreferencesUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun createUser(context: Context, name: String): Flow<Result<Unit>>
    suspend fun changeName(context: Context, userID: String, name: String): Flow<Result<Unit>>
}

class UserRepositoryImpl : UserRepository {
    override suspend fun createUser(context: Context, name: String): Flow<Result<Unit>> = callbackFlow {
        val db = Firebase.firestore
        val refCollection = db.collection("Users")

        val user = hashMapOf(
            "name" to name,
        )

        try {
            val documentReference = refCollection.add(user).await()

            SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userID, documentReference.id)
            SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userName, name)

            trySend(Result.success(Unit)).isSuccess // Signal success
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess // Signal failure with the exception
        } finally {
            close() // Close the flow
        }
    }

    override suspend fun changeName(
        context: Context,
        userID: String,
        name: String
    ): Flow<Result<Unit>> = callbackFlow {
        val user = hashMapOf(
            "name" to name,
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Users")

        try {
            refCollection.document(userID).set(user).await()

            SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userName, name)
            trySend(Result.success(Unit)).isSuccess // Signal success
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess // Signal failure with the exception
        } finally {
            close() // Close the flow
        }
    }
}