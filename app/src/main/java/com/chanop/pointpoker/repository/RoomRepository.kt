package com.chanop.pointpoker.repository

import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.model.RoomsModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface RoomRepository {
    suspend fun getRoomsSnapshotFlow(): Flow<QuerySnapshot>

    suspend fun getRoomSnapshotFlow(roomID: String): Flow<DocumentSnapshot>

    suspend fun createRoom(userID: String, roomName: String, points: List<Double>): Flow<Result<Unit>>

    suspend fun joinRoom(roomID: String, name: String, userID: String): Flow<Result<Unit>>

    suspend fun removeRooms(roomID: String): Flow<Result<Unit>>

    suspend fun averagePoint(roomModel: RoomModel, averagePoint: Double?): Flow<Result<Unit>>
}

class RoomRepositoryImpl : RoomRepository {

    companion object {
//        TODO Repository to instance
    }

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

    override suspend fun getRoomSnapshotFlow(roomID: String): Flow<DocumentSnapshot> = callbackFlow {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")
        val documentRef = collectionRef.document(roomID)

        val listenerRegistration = documentRef.addSnapshotListener { snapshot, error ->
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

    override suspend fun createRoom(userID: String, roomName: String, points: List<Double>): Flow<Result<Unit>> = callbackFlow {
        val room = hashMapOf(
            "name" to roomName,
            "leader" to userID,
            "points" to points
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


    override suspend fun averagePoint(roomModel: RoomModel, averagePoint: Double?): Flow<Result<Unit>> = callbackFlow {

        val roomData = hashMapOf(
            "leader" to roomModel.room?.leader,
            "name" to roomModel.room?.name,
            "points" to roomModel.room?.points,
            "owner" to roomModel.room?.owner
        )

        if (averagePoint != null) {
            roomData.put("average_point", averagePoint)
        }

        val db = Firebase.firestore
        val refCollection = db.collection("Rooms")
        val refDocument = refCollection.document(roomModel.room?.id ?: "")

        try {
            refDocument
                .set(roomData)
                .await()

            trySend(Result.success(Unit)).isSuccess // Signal success
        } catch (e: Exception) {
            trySend(Result.failure(e)).isSuccess // Signal failure with the exception
        } finally {
            close() // Close the flow
        }
    }
}