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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun HomeScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = MainViewModel()) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(viewModel.getUserName(context)) }
    viewModel.getRooms()

    Box(modifier = modifier.fillMaxSize(),
        ){
        Column {

            Text(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                text = "Point Poker")

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                TextField(modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = {username = it })

            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(modifier = Modifier.align(Alignment.CenterStart),
                    text = "Room")

                Row(modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        viewModel.createRoom(context, "TestRoom1", username)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Room")
                    Text(text = "Create rooms")
                }
            }

            AllRoomView(viewModel = viewModel)
        }
    }
}

@Composable
fun AllRoomView(viewModel: MainViewModel) {
    val allRoom by viewModel.allRoom.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(allRoom) {
            val roomName = (it.data?.get("name") as? String) ?: ""
            RoomView(roomName)
        }
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PointPokerTheme {
        HomeScreen()
    }
}