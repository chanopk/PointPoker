package com.chanop.pointpoker.view.composables

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chanop.pointpoker.viewmodel.MainViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // example
//    val isLoading = remember { mutableStateOf( true ) }
//    LaunchedEffect(isLoading.value) {
//        if (isLoading.value) {
//            isLoading.value = false
//        }
//    }

    var username by remember { mutableStateOf(viewModel.getUserName(context)) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                                if (username.isEmpty()) {
                                    scope.launch() {
                                        snackbarHostState.showSnackbar(
                                            message = "name is Empty",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    navController.navigate("createroom/${username}")
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Room")
                        Text(text = "Create rooms")
                    }
                }

                AllRoomView(
                    navController = navController,
                    viewModel = viewModel,
                    username = username,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun AllRoomView(
    navController: NavController,
    viewModel: MainViewModel,
    username: String,
    snackbarHostState: SnackbarHostState
) {
    val allRoom by viewModel.allRoom.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(allRoom) {
            RoomView(
                navController = navController,
                viewModel = viewModel,
                documentSnapshot = it,
                username = username,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
fun RoomView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    documentSnapshot: DocumentSnapshot,
    username: String,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val roomName = (documentSnapshot.data?.get("name") as? String) ?: ""

    Card(
        modifier
            .width(128.dp)
            .height(200.dp)
            .clickable {
                if (username.isEmpty()) {
                    scope.launch() {
                        snackbarHostState.showSnackbar(
                            message = "name is Empty",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    viewModel.joinRoom(context, documentSnapshot.id, username) { status, message ->
                        if (status) {
                            navController.navigate("room/${documentSnapshot.id}")
                        } else {
                            scope.launch() {
                                snackbarHostState.showSnackbar(
                                    message = "Join Room Error",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (viewModel.checkUserId(context, documentSnapshot.data?.get("leader") as? String)) {
                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable {
                            viewModel.removeRooms(documentSnapshot.id)
                        },
                    contentDescription = "Remove Button"
                )
            }
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = roomName
            )
        }
    }
}