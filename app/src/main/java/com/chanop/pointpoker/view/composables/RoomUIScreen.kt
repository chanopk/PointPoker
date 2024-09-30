package com.chanop.pointpoker.view.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.intent.CreateRoomIntent
import com.chanop.pointpoker.intent.RoomIntent
import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.repository.MemberRepositoryImpl
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.view.ViewModelFactory
import com.chanop.pointpoker.view.composables.theme.Grey
import com.chanop.pointpoker.view.composables.theme.Pink40
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.view.composables.theme.RedDark
import com.chanop.pointpoker.view.composables.theme.White
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
        roomViewModel.processIntent(RoomIntent.LeaveRoom(context = context, roomID = roomID))
    }

    RoomLayout(roomViewModel = roomViewModel)
}

@Composable
fun RoomLayout(
    roomViewModel: RoomViewModel
) {
    val context = LocalContext.current
    val currentRoom by roomViewModel.currentRoom.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    currentRoom.error?.let { errorMessage ->
        LaunchedEffect(key1 = errorMessage) { // Triggered when errorMessage changes
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = (currentRoom.room?.name ?: ""),
                    style = MaterialTheme.typography.titleLarge
                )

                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .clickable {
                            roomViewModel.processIntent(
                                RoomIntent.LeaveRoom(
                                    context = context,
                                    roomID = currentRoom.room?.id ?: ""
                                )
                            )
                        },
                    contentDescription = "Close Button"
                )
            }

        },
        bottomBar = {
            if (currentRoom.room?.owner == true) {
                if (currentRoom.room?.averagePoint == null) {
                    ButtonActionRoom(text = "Average Point") {
                        roomViewModel.processIntent(RoomIntent.AveragePoint(currentRoom = currentRoom))
                    }
                } else {
                    ButtonActionRoom(text = "Reset") {
                        roomViewModel.processIntent(RoomIntent.ResetAveragePoint(currentRoom = currentRoom))
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
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Average Point: ${currentRoom.room?.averagePoint ?: "Voting..."}",
            style = MaterialTheme.typography.titleLarge
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            PointsScreen(
                modifier = Modifier.padding(16.dp),
                roomViewModel = roomViewModel,
                currentRoom = currentRoom
            )

            MembersScreen(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                roomViewModel = roomViewModel,
                currentRoom = currentRoom
            )
        }
    }
}

@Composable
fun PointsScreen(
    modifier: Modifier = Modifier,
    roomViewModel: RoomViewModel,
    currentRoom: RoomModel
) {
    val context = LocalContext.current
    val toggle = currentRoom.room?.averagePoint == null
    var selected by remember { mutableDoubleStateOf(0.0) }

    Column(modifier = modifier) {
        Text(text = "Points")

        LazyColumn {
            items(currentRoom.room?.points ?: listOf()) { point ->
                OutlinedButton(
                    modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 4.dp),
                    onClick = {
                        selected = point
                        roomViewModel.processIntent(
                            RoomIntent.Vote(
                                context = context,
                                roomID = currentRoom.room?.id ?: "",
                                point = point
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        if (selected == point) 4.dp else 1.dp,
                        if (toggle) RedDark else Grey
                    ),
                    enabled = toggle
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        color = if (toggle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        text = point.toString()
                    )
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

    LazyColumn(modifier = modifier) {
        item {
            Text(text = "Members(${currentMembers.memberList.size})")
        }
        items(currentMembers.memberList) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp, 0.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(modifier = Modifier.weight(1f), text = item.name)

                if (currentRoom.room?.averagePoint != null) {
                    Text(
                        modifier = Modifier.padding(4.dp, 0.dp, 0.dp, 0.dp),
                        text = (item.point ?: "").toString()
                    )
                } else {
                    if (item.itsMe) {
                        Text(
                            modifier = Modifier.padding(4.dp, 0.dp, 0.dp, 0.dp),
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
        factory = ViewModelFactory(
            navController,
            UserRepositoryImpl(),
            RoomRepositoryImpl(),
            MemberRepositoryImpl()
        )
    )
    PointPokerTheme {
        RoomLayout(roomViewModel)
    }
}