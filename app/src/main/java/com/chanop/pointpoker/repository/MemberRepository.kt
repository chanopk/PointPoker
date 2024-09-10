package com.chanop.pointpoker.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface MemberRepository {
    suspend fun getMembersSnapshotFlow(roomID: String): Flow<QuerySnapshot>
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
}