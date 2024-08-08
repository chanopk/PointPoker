package com.chanop.pointpoker

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
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
                val previousName = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
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
                Log.w(ContentValues.TAG, "Error adding User", e)
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

    fun createRoom(context: Context, roomName: String, userName: String) {
        isUserReady(context, userName) { isReady ->
            if (isReady) {
                val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)
                val room = hashMapOf(
                    "name" to roomName,
                    "leader" to userID,
                    "points" to listOf(0.5,1,1.5,2,3,5,8)
                )

                val db = Firebase.firestore
                val refCollection = db.collection("Rooms")

                refCollection
                    .add(room)
                    .addOnSuccessListener {
                        it
                    }
                    .addOnFailureListener { e ->
                        e
                    }
            } else {
                // TODO alert some user error
                // TODO room empty
            }
        }
    }

    fun joinRoom(context: Context, roomID: String, name: String) {
        isUserReady(context, name) { isReady ->
            if (isReady) {
                val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)
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
                        it
                    }
                    .addOnFailureListener { e ->
                        e
                    }
            } else {
                // TODO alert some user error
                // TODO room empty
            }
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
}