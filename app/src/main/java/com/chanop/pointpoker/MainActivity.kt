package com.chanop.pointpoker

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chanop.pointpoker.ui.theme.PointPokerTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointPokerTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    // TODO get from firebase
    var roomName = listOf("Room1", "Room2", "Room3", "Room4",
        "Room1", "Room2", "Room3", "Room4",
        "Room1", "Room2", "Room3", "Room4",
        "Room1", "Room2", "Room3", "Room4")

    Box(modifier = modifier.fillMaxSize(),
        ){
        Column {

            Text(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                text = "Point Poker")

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(modifier = Modifier.align(Alignment.CenterStart)
                    .clickable {
                        getData()
                    },
                    text = "Room")

                Row(modifier = Modifier.align(Alignment.CenterEnd)
                    .clickable {
                        testFirebase()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Room")
                    Text(text = "Create rooms")
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 144.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(roomName) {
                    RoomView(it)
                }
            }
        }

//        LazyRow(
//            contentPadding = PaddingValues(8.dp)
//        ) {
//            items(roomName) {
//                RoomView(it)
//            }
//        }
    }
}

@Composable
fun RoomView(roomName: String, modifier: Modifier = Modifier) {
    Card(
        modifier
            .width(128.dp)
            .height(200.dp)
            .padding(8.dp)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
        ) {
            Text(modifier = Modifier.align(Alignment.BottomCenter),
                text = roomName)
        }
    }
}

fun testFirebase() {
    val db = Firebase.firestore

    val chanop = hashMapOf(
        "username" to "chanop",
        "role" to "leader",
        "voted" to "",
    )

    val room = hashMapOf(
        "average point" to 0,
        "user" to listOf(chanop),
    )

    db.collection("room name")
//        .document("Test")
        .add(room)
        .addOnSuccessListener {
            Log.d(TAG, "Success")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding data", e)
        }
}

fun testAddDoc() {
    val db = Firebase.firestore
    val user = hashMapOf(
        "first" to "Ada3",
        "last" to "Lovelace3",
        "born" to 18152,
    )

    // Add a new document with a generated ID
    db.collection("users")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
}

fun getData() {
    val db = Firebase.firestore
    val collectionRef = db.collection("Rooms") // Reference to the collection

    collectionRef.addSnapshotListener{ snapshot, error ->
        Log.i("test", snapshot.toString())
        Log.i("test", error.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PointPokerTheme {
        HomeScreen()
    }
}