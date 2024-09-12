package com.chanop.pointpoker.viewmodel

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.chanop.pointpoker.SharedPreferencesUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {

    private val _allRoom = MutableStateFlow<List<DocumentSnapshot>>(emptyList())
    val allRoom: StateFlow<List<DocumentSnapshot>> = _allRoom.asStateFlow()

    private val _currentRoom = MutableStateFlow<DocumentSnapshot?>(null)
    val currentRoom: StateFlow<DocumentSnapshot?> = _currentRoom.asStateFlow()

    private val _currentMembers = MutableStateFlow<List<DocumentSnapshot>>(emptyList())
    val currentMembers: StateFlow<List<DocumentSnapshot>> = _currentMembers.asStateFlow()

    fun isUserReady(context: Context, name: String, callback: (Boolean) -> Unit) {
        if (name.isEmpty()) {
            callback.invoke(false)
        } else {
            val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)

            if (userID.isEmpty()) {
                createUser(context, name) { status ->
                    if (status) {
                        callback.invoke(true)
                    } else {
                        callback.invoke(false)
                    }
                }
            } else {
                val previousName =
                    SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
                if (name != previousName) {
                    changeName(context, userID, name) { status ->
                        if (status) {
                            callback.invoke(true)
                        } else {
                            callback.invoke(false)
                        }
                    }
                } else {
                    callback.invoke(true)
                }
            }
        }
    }

    fun checkUserId(context: Context, userID: String?): Boolean {
        return SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID) == userID
    }

    fun createUser(context: Context, name: String, success: (Boolean) -> Unit) {
        val user = hashMapOf(
            "name" to name,
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Users")

        refCollection
            .add(user)
            .addOnSuccessListener {
                SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userID, it.id)
                SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userName, name)
                success.invoke(true)
            }
            .addOnFailureListener { e ->
                success.invoke(false)
            }
    }

    fun changeName(context: Context, userID: String, name: String, success: (Boolean) -> Unit) {
        val user = hashMapOf(
            "name" to name,
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Users")
        refCollection.document(userID)
            .set(user)
            .addOnSuccessListener {
                SharedPreferencesUtils.putString(context, SharedPreferencesUtils.userName, name)
                success.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding User", e)
                success.invoke(false)
            }
    }

    fun createRoom(context: Context, roomName: String, userName: String, status: (Boolean, String) -> Unit) {
        if (roomName.isEmpty()) {
            status.invoke(false, "Error: Room name is empty")
        } else {
            isUserReady(context, userName) { isReady ->
                if (isReady) {
                    val userID =
                        SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)
                    val room = hashMapOf(
                        "name" to roomName,
                        "leader" to userID,
                        "points" to listOf(0.5,1.0,1.5,2.0,3.0,5.0,8.0)
                    )

                    val db = Firebase.firestore
                    val refCollection = db.collection("Rooms")

                    refCollection
                        .add(room)
                        .addOnSuccessListener {
                            status.invoke(true, "Success: $roomName is ready")
                        }
                        .addOnFailureListener { e ->
                            status.invoke(false, "Error: Firebase add room Failure")
                        }
                } else {
                    status.invoke(false, "Error: User not ready")
                }
            }
        }
    }

    fun joinRoom(context: Context, roomID: String, name: String, status: (Boolean, String) -> Unit) {
        isUserReady(context, name) { isReady ->
            if (isReady) {
                val userID =
                    SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)
                val user = hashMapOf(
                    "name" to name,
                )

                val db = Firebase.firestore
                val refRooms = db.collection("Rooms")
                val refRoom = refRooms.document(roomID)
                val refMembers = refRoom.collection("Members")

                refMembers.document(userID)
                    .set(user)
                    .addOnSuccessListener {
                        status.invoke(true, "Success: join room")
                    }
                    .addOnFailureListener { e ->
                        status.invoke(false, "Error: Firebase join room Failure")
                    }
            } else {
                status.invoke(false, "Error: User not ready")
            }
        }
    }

    fun leaveRoom(context: Context, roomID: String) {
        val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)

        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        refMembers.document(userID)
            .delete()
            .addOnSuccessListener {
                clearRoom()
            }
            .addOnFailureListener { e ->
                clearRoom()
            }
    }

    fun removeRooms(roomID: String) {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms") // Reference to the collection
        val documentRef = collectionRef.document(roomID)
        documentRef.delete()
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }
    }

    fun getUserName(context: Context): String {
        return SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
    }

    fun getRooms() {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms") // Reference to the collection

        collectionRef.addSnapshotListener{ snapshot, error ->
            val tmpAllRoom = arrayListOf<DocumentSnapshot>()
            tmpAllRoom.addAll(_allRoom.value)
            snapshot?.documentChanges?.forEach { documentChange ->
                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        if (tmpAllRoom.find { it.id == documentChange.document.id } == null) {
                            tmpAllRoom.add(documentChange.document)
                        }
                    }
                    DocumentChange.Type.MODIFIED -> {
                        val index = tmpAllRoom.indexOfFirst { it.id == documentChange.document.id }
                        tmpAllRoom.removeAt(index)
                        tmpAllRoom.add(index, documentChange.document)
                    }
                    DocumentChange.Type.REMOVED -> {
                        val index = tmpAllRoom.indexOfFirst { it.id == documentChange.document.id }
                        tmpAllRoom.removeAt(index)
                    }
                    else -> {}
                }
            }

            _allRoom.value = tmpAllRoom
        }
    }

    fun getCurrentRoom(roomId: String) {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")
        val documentRef = collectionRef.document(roomId)
        documentRef.addSnapshotListener{ snapshot, error ->
            _currentRoom.value = snapshot
        }
    }
    fun getCurrentMember(roomId: String) {
        val db = Firebase.firestore
        val collectionRef = db.collection("Rooms")
        val documentRef = collectionRef.document(roomId)
        val membersCollectionRef = documentRef.collection("Members")
        _currentMembers.value = arrayListOf<DocumentSnapshot>()

        membersCollectionRef.addSnapshotListener{ snapshot, error ->
            val tmpAllCurrentMembers = arrayListOf<DocumentSnapshot>()
            tmpAllCurrentMembers.addAll(_currentMembers.value)
            snapshot?.documentChanges?.forEach { documentChange ->
                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        if (tmpAllCurrentMembers.find { it.id == documentChange.document.id } == null) {
                            tmpAllCurrentMembers.add(documentChange.document)
                        }
                    }
                    DocumentChange.Type.MODIFIED -> {
                        if (tmpAllCurrentMembers.isNotEmpty()) {
                            val index = tmpAllCurrentMembers.indexOfFirst { it.id == documentChange.document.id }
                            tmpAllCurrentMembers[index] = documentChange.document
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        if (tmpAllCurrentMembers.isNotEmpty()) {
                            val index = tmpAllCurrentMembers.indexOfFirst { it.id == documentChange.document.id }
                            tmpAllCurrentMembers.removeAt(index)
                        }
                    }
                    else -> {}
                }
            }

            _currentMembers.value = tmpAllCurrentMembers
        }
    }

    fun voteAtRoom(context: Context, roomID: String, point: Double) {
        val username = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
        val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)
        val vote = hashMapOf(
            "name" to username,
            "point" to point
        )

        val db = Firebase.firestore
        val refRooms = db.collection("Rooms")
        val refRoom = refRooms.document(roomID)
        val refMembers = refRoom.collection("Members")

        refMembers.document(userID)
            .set(vote)
            .addOnSuccessListener {
                it
            }
            .addOnFailureListener { e ->
                e
            }
    }

    fun clearRoom() {
        _currentRoom.value = null
        _currentMembers.value = arrayListOf<DocumentSnapshot>()
    }

    fun calAveragePoint(roomId: String) {
        val sumPoint = _currentMembers.value.sumOf { it.data?.get("point") as? Double ?: 0.0 }
        val averagePoint = sumPoint / _currentMembers.value.size

        val room = hashMapOf(
            "average_point" to averagePoint,
            "leader" to (_currentRoom.value?.data?.get("leader") ?: ""),
            "name" to (_currentRoom.value?.data?.get("name") ?: ""),
            "points" to (_currentRoom.value?.data?.get("points") ?: listOf<Double>()),
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Rooms")
        val refDocument = refCollection.document(roomId)

        refDocument
            .set(room)
            .addOnSuccessListener {
                it
            }
            .addOnFailureListener { e ->
                e
            }
    }

    fun resetAveragePoint(roomId: String) {
        val room = hashMapOf(
            "leader" to (_currentRoom.value?.data?.get("leader") ?: ""),
            "name" to (_currentRoom.value?.data?.get("name") ?: ""),
            "points" to (_currentRoom.value?.data?.get("points") ?: listOf<Double>()),
        )

        val db = Firebase.firestore
        val refCollection = db.collection("Rooms")
        val refDocument = refCollection.document(roomId)

        refDocument
            .set(room)
            .addOnSuccessListener {
                it
            }
            .addOnFailureListener { e ->
                e
            }
    }
}