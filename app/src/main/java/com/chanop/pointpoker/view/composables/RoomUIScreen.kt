package com.chanop.pointpoker.view.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.intent.RoomIntent
import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.repository.MemberRepositoryImpl
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.view.ViewModelFactory
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.viewmodel.HomeViewModel
import com.chanop.pointpoker.viewmodel.RoomViewModel
import com.google.firebase.firestore.DocumentSnapshot

@Composable
fun RoomScreen(
    roomViewModel: RoomViewModel,
    roomID: String,
) {
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(true) }
    LaunchedEffect(isLoading.value) {
        if (isLoading.value) {
            isLoading.value = false
            roomViewModel.processIntent(RoomIntent.LoadRoom(context = context, roomID = roomID))
            roomViewModel.processIntent(RoomIntent.LoadMembers(context = context, roomID = roomID))
        }
    }

    BackHandler(enabled = true) {
        // TODO
//        viewModel.leaveRoom(context, roomId)
//        navController?.popBackStack()
    }

    RoomLayout(roomViewModel = roomViewModel)
}

@Composable
fun RoomLayout(
    roomViewModel: RoomViewModel
) {
    val context = LocalContext.current
    val currentRoom by roomViewModel.currentRoom.collectAsState()
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 16.dp, 0.dp, 0.dp), horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .clickable {
//                            TODO
//                            viewModel.leaveRoom(context, roomId)
//                            navController?.popBackStack()
                        }
                        .padding(16.dp),
                    contentDescription = "Close Button"
                )
            }
        },
        bottomBar = {
            if (currentRoom.room?.owner == true) {
                if (currentRoom.room?.averagePoint == null) {
                    ButtonActionRoom(text = "Average Point") {
//                        TODO
//                        viewModel.calAveragePoint(roomId)
                    }
                } else {
                    ButtonActionRoom(text = "Reset") {
//                        TODO
//                        viewModel.resetAveragePoint(roomId)
                    }
                }
            }
        },
    ) { innerPadding ->
        RoomDetailScreen(
            modifier = Modifier.padding(innerPadding),
            roomViewModel = roomViewModel,
            currentRoom
        )
    }
}

@Composable
fun ButtonActionRoom(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 0.dp, 24.dp, 36.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            onClick.invoke()
        }) {
        Text(text = text)
    }
}

@Composable
fun RoomDetailScreen(
    modifier: Modifier,
    roomViewModel: RoomViewModel,
    currentRoom: RoomModel
) {

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = (currentRoom.room?.name ?: ""), fontSize = 18.sp)
        }

        Text(
            modifier = Modifier.padding(16.dp),
            text = "Average Point: ${currentRoom.room?.averagePoint ?: "nothing"}"
        )

        PointsScreen(
            modifier = Modifier.padding(16.dp),
            roomViewModel = roomViewModel,
            currentRoom = currentRoom
        )

        MembersScreen(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp), roomViewModel = roomViewModel, currentRoom = currentRoom)
    }
}

@Composable
fun PointsScreen(
    modifier: Modifier = Modifier,
    roomViewModel: RoomViewModel,
    currentRoom: RoomModel
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(text = "Points")

        LazyRow {
            items(currentRoom.room?.points ?: listOf()) {
                Button(
                    modifier = modifier.padding(4.dp),
                    onClick = {
//                        TODO
//                        viewModel.voteAtRoom(context, roomId, it.toString().toDouble())
                    },
                    enabled = currentRoom.room?.averagePoint == null
                ) {
                    Text(modifier = Modifier.padding(4.dp), text = it.toString())
                }
            }
        }
    }
}

@Composable
fun MembersScreen(
    modifier: Modifier = Modifier,
    roomViewModel: RoomViewModel,
    currentRoom: RoomModel
) {
    val currentMembers by roomViewModel.currentMembers.collectAsState()

    LazyColumn (modifier = modifier) {
        item {
            Text(text = "Members(${currentMembers.memberList.size})")
        }
        items(currentMembers.memberList) { item ->
            val name = item.name
            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = name)

                if (currentRoom.room?.averagePoint != null) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = (item.point ?: "").toString()
                    )
                } else {
                    if (item.itsMe) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = (item.point ?: "").toString()
                        )
                    } else {
                        if (item.point != null) {
                            Text(modifier = Modifier.padding(4.dp), text = "?")
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    val navController = rememberNavController()
    val roomViewModel: RoomViewModel = viewModel(
        factory = ViewModelFactory(navController, UserRepositoryImpl(), RoomRepositoryImpl(), MemberRepositoryImpl())
    )
    PointPokerTheme {
        RoomLayout(roomViewModel)
    }
}