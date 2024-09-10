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
import androidx.compose.runtime.LaunchedEffect
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
import com.chanop.pointpoker.intent.HomeIntent
import com.chanop.pointpoker.model.Room
import com.chanop.pointpoker.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(true) }
    LaunchedEffect(isLoading.value) {
        if (isLoading.value) {
            homeViewModel.processIntent(HomeIntent.LoadHome(context = context))
        }
    }

    var username by remember { mutableStateOf(homeViewModel.getUserName(context)) }

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
                                    homeViewModel.processIntent(HomeIntent.NavigateTo("createroom/${username}"))
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Room")
                        Text(text = "Create rooms")
                    }
                }

                AllRoomView(
                    homeViewModel = homeViewModel,
                    username = username,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun AllRoomView(
    homeViewModel: HomeViewModel,
    username: String,
    snackbarHostState: SnackbarHostState
) {
    val roomModel by homeViewModel.roomsModel.collectAsState()

    roomModel.error?.let { errorMessage ->
        LaunchedEffect(key1 = errorMessage) { // Triggered when errorMessage changes
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(roomModel.roomList) { room ->
            RoomView(
                homeViewModel = homeViewModel,
                room = room,
                username = username,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
fun RoomView(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    room: Room,
    username: String,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                    homeViewModel.processIntent(HomeIntent.JoinHome(context, room.id, username))
                }
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (room.owner) {
                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable {
                            homeViewModel.processIntent(HomeIntent.RemoveHome(roomID = room.id))
                        },
                    contentDescription = "Remove Button"
                )
            }
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = room.name
            )
        }
    }
}