package com.chanop.pointpoker.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface RoomRepository {
    suspend fun getRoomsSnapshotFlow(): Flow<QuerySnapshot>

    suspend fun createRoom(userID: String, roomName: String): Flow<Result<Unit>>

    suspend fun joinRoom(roomID: String, name: String, userID: String): Flow<Result<Unit>>

    suspend fun removeRooms(roomID: String): Flow<Result<Unit>>
}

class RoomRepositoryImpl : RoomRepository {

    override suspend fun getRoomsSnapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow with the error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                trySend(snapshot).isSuccess // Send the snapshot to the flow
            }
        }

        awaitClose {
            listenerRegistration.remove() // Clean up the listener when the flow is closed
        }
    }

    override suspend fun createRoom(userID: String, roomName: String): Flow<Result<Unit>> = callbackFlow {
        val room = hashMapOf(
            "name" to roomName,
            "leader" to userID,
            "points" to listOf(0.5,1.0,1.5,2.0,3.0,5.0,8.0)
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Rooms")

        try {
            refCollection
                .add(room)
                .await()

            trySend(Result.success(Unit)).isSuccess
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess
        } finally {
            close()
        }
    }

    override suspend fun joinRoom(roomID: String, name: String, userID: String): Flow<Result<Unit>> = callbackFlow {
        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        val user = hashMapOf(
            "name" to name
        )

        try {
            refMembers.document(userID)
                .set(user)
                .await()

            trySend(Result.success(Unit)).isSuccess
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess
        } finally {
            close()
        }
    }

    override suspend fun removeRooms(roomID: String): Flow<Result<Unit>> = callbackFlow {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")
        val documentRef = collectionRef.document(roomID)

        try {
            documentRef.delete().await() // Use await() to make it suspendable
            trySend(Result.success(Unit)).isSuccess // Signal success
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess // Signal failure with the exception
        } finally {
            close() // Close the flow
        }
    }
}