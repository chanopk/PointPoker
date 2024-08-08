package com.chanop.pointpoker.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chanop.pointpoker.MainViewModel
import com.google.firebase.firestore.DocumentSnapshot

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(viewModel.getUserName(context)) }
    viewModel.getRooms()

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {

            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                text = "Point Poker"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = { username = it })

            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterStart),
                    text = "Room"
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            navController.navigate("createroom/${username}")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Room")
                    Text(text = "Create rooms")
                }
            }

            AllRoomView(viewModel = viewModel, username = username)
        }
    }
}

@Composable
fun AllRoomView(viewModel: MainViewModel, username: String) {
    val allRoom by viewModel.allRoom.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(allRoom) {
            RoomView(viewModel = viewModel, documentSnapshot = it, username = username)
        }
    }
}

@Composable
fun RoomView(modifier: Modifier = Modifier, viewModel: MainViewModel, documentSnapshot: DocumentSnapshot, username: String) {
    val context = LocalContext.current
    val roomName = (documentSnapshot.data?.get("name") as? String) ?: ""

    Card(
        modifier
            .width(128.dp)
            .height(200.dp)
            .clickable {
                // TODO create ui
                viewModel.joinRoom(context, documentSnapshot.id, username)
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = roomName
            )
        }
    }
}