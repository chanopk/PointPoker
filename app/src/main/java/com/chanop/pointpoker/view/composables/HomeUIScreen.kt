package com.chanop.pointpoker.view.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.SharedPreferencesUtils
import com.chanop.pointpoker.intent.HomeIntent
import com.chanop.pointpoker.model.Room
import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.model.RoomsModel
import com.chanop.pointpoker.repository.MemberRepositoryImpl
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.view.ViewModelFactory
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.viewmodel.HomeViewModel
import com.chanop.pointpoker.viewmodel.RoomViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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


    var username by remember { mutableStateOf(SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)) }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {

                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Point Poker",
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "call me ..."
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .background(Color.Transparent),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text("Enter your name", color = MaterialTheme.colorScheme.secondary) },
                        value = username,
                        onValueChange = { username = it }
                    )

                }

                Divider(
                    color = MaterialTheme.colorScheme.primary, // หรือสีอื่นๆ ตามต้องการ
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRoomView(
    homeViewModel: HomeViewModel,
    username: String,
    snackbarHostState: SnackbarHostState
) {
    val roomModel by homeViewModel.roomsModel.collectAsState()
    var searchState by remember { mutableStateOf(false) }

    val searchText by homeViewModel.searchText.collectAsState()
    val searchRoom by homeViewModel.searchRoom.collectAsState()
    val isSearching by homeViewModel.isSearching.collectAsState()

    roomModel.error?.let { errorMessage ->
        LaunchedEffect(key1 = errorMessage) { // Triggered when errorMessage changes
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            roomModel.roomList.find { it.recent }?.let {
                Text(modifier = Modifier.padding(4.dp), text = "Recent Room")
                RoomView(
                    homeViewModel = homeViewModel,
                    room = it,
                    username = username,
                    recentView = true,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (searchState) {
                    Column(
                        modifier = Modifier
                            .weight(9f)
                            .padding(4.dp, 0.dp)
                    ) {
                        Text(
                            modifier = Modifier, text = "Search Room"
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth()
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .background(Color.Transparent),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            value = searchText,
                            onValueChange = homeViewModel::onSearchTextChange,
                        )
                    }
                    Icon(
                        Icons.Default.Close,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                searchState = !searchState
                            },
                        contentDescription = "Search Room"
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .weight(9f)
                            .padding(4.dp, 0.dp), text = "All Room"
                    )
                    Icon(
                        Icons.Default.Search,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                searchState = !searchState
                            },
                        contentDescription = "Search Room"
                    )
                }
            }
        }
        if (!isSearching) {
            items(
                if (searchState) {
                    searchRoom.roomList
                } else {
                    roomModel.roomList
                }
            ) { room ->
                RoomView(
                    homeViewModel = homeViewModel,
                    room = room,
                    username = username,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }

//    LazyVerticalGrid(
//        columns = GridCells.Adaptive(minSize = 144.dp),
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        items(roomModel.roomList) { room ->
//            RoomView(
//                homeViewModel = homeViewModel,
//                room = room,
//                username = username,
//                snackbarHostState = snackbarHostState
//            )
//        }
//    }
}

@Composable
fun RoomView(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    room: Room,
    username: String,
    recentView: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
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
            .padding(4.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, Color.Gray)
                .padding(16.dp, 16.dp)
        ) {
            Text(
                modifier = Modifier.weight(9f),
                text = room.name
            )
            if (room.owner && !recentView) {
                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            homeViewModel.processIntent(HomeIntent.RemoveHome(roomID = room.id))
                        },
                    contentDescription = "Remove Button"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HoneScreenPreview() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(
            navController,
            UserRepositoryImpl(),
            RoomRepositoryImpl(),
            MemberRepositoryImpl()
        )
    )
    PointPokerTheme {
        HomeScreen(modifier = Modifier, homeViewModel)
    }
}