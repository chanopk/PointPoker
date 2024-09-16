package com.chanop.pointpoker.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface MemberRepository {
    suspend fun getMembersSnapshotFlow(roomID: String): Flow<QuerySnapshot>
    suspend fun leaveRoom(userID: String, roomID: String): Flow<Result<Unit>>
    suspend fun voteAtRoom(roomID: String, userID: String, username: String, point: Double): Flow<Result<Unit>>
    suspend fun resetPoint(roomID: String, userID: String, username: String): Flow<Result<Unit>>
}

class MemberRepositoryImpl : MemberRepository {
    override suspend fun getMembersSnapshotFlow(roomID: String): Flow<QuerySnapshot> = callbackFlow {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")
        val documentRef = collectionRef.document(roomID)
        val membersCollectionRef = documentRef.collection("Members")

        val listenerRegistration = membersCollectionRef.addSnapshotListener { snapshot, error ->
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

    override suspend fun leaveRoom(userID: String, roomID: String): Flow<Result<Unit>> = callbackFlow {
        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        try {
            refMembers.document(userID)
                .delete()
                .await()

            trySend(Result.success(Unit)).isSuccess
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess
        } finally {
            close()
        }
    }

    override suspend fun voteAtRoom(
        roomID: String,
        userID: String,
        username: String,
        point: Double
    ): Flow<Result<Unit>> = callbackFlow {
        val vote = hashMapOf(
            "name" to username,
            "point" to point
        )

        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        try {
            refMembers.document(userID)
                .set(vote)
                .await()


            trySend(Result.success(Unit)).isSuccess
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess
        } finally {
            close()
        }
    }

    override suspend fun resetPoint(
        roomID: String,
        userID: String,
        username: String
    ): Flow<Result<Unit>> = callbackFlow {
        val vote = hashMapOf(
            "name" to username,
        )

        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        try {
            refMembers.document(userID)
                .set(vote)
                .await()


            trySend(Result.success(Unit)).isSuccess
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess
        } finally {
            close()
        }
    }
}