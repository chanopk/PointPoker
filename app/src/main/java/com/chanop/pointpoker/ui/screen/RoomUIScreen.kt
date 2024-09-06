package com.chanop.pointpoker.ui.screen

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
import androidx.navigation.NavController
import com.chanop.pointpoker.MainViewModel
import com.chanop.pointpoker.ui.theme.PointPokerTheme
import com.google.firebase.firestore.DocumentSnapshot

@Composable
fun RoomScreen(
    navController: NavController? = null,
    viewModel: MainViewModel,
    roomId: String,
) {
    viewModel.getCurrentRoom(roomId)
    viewModel.getCurrentMember(roomId)

    RoomLayout(navController, viewModel, roomId)
}

@Composable
fun RoomLayout(
    navController: NavController? = null,
    viewModel: MainViewModel,
    roomId: String,
) {
    val context = LocalContext.current
    val currentRoom by viewModel.currentRoom.collectAsState()
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
                            navController?.popBackStack()
                            viewModel.clearRoom()
                        }
                        .padding(16.dp),
                    contentDescription = "Close Button"
                )
            }
        },
        bottomBar = {
            if (viewModel.checkUserId(context, currentRoom?.data?.get("leader") as? String)) {
                if (currentRoom?.data?.get("average_point") == null) {
                    ButtonActionRoom(text = "average_point") {
                        viewModel.calAveragePoint(roomId)
                    }
                } else {
                    ButtonActionRoom(text = "Reset") {
                        viewModel.resetAveragePoint(roomId)
                    }
                }
            }
        },
    ) { innerPadding ->
        RoomDetailScreen(navController, viewModel, Modifier.padding(innerPadding), roomId, currentRoom)
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
fun RoomDetailScreen(navController: NavController?, viewModel: MainViewModel, modifier: Modifier, roomId: String, currentRoom: DocumentSnapshot?) {

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = (currentRoom?.data?.get("name") as? String ?: ""), fontSize = 18.sp)
        }

        Text(modifier = Modifier.padding(0.dp, 16.dp), text = "Average Point: ${currentRoom?.data?.get("average_point") ?: "nothing"}")

        PointsScreen(navController, viewModel, roomId, (currentRoom?.data?.get("points") as? ArrayList<Any>) ?: arrayListOf(), currentRoom?.data?.get("average_point"))

        MembersScreen(navController, viewModel, currentRoom?.data?.get("average_point"))
    }
}

@Composable
fun PointsScreen(
    navController: NavController?,
    viewModel: MainViewModel,
    roomId: String,
    points: ArrayList<Any>,
    averagePoint: Any?
) {
    val context = LocalContext.current

    Text(text = "Points")

    LazyRow {
        items(points) {
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = {
                    viewModel.voteAtRoom(context, roomId, it.toString().toDouble())
                },
                enabled = averagePoint == null
            ) {
                Text(modifier = Modifier.padding(4.dp), text = it.toString())
            }
        }
    }
}

@Composable
fun MembersScreen(
    navController: NavController?,
    viewModel: MainViewModel,
    averagePoint: Any?
) {
    val context = LocalContext.current
    val currentMembers by viewModel.currentMembers.collectAsState()
    var username by remember { mutableStateOf(viewModel.getUserName(context)) }
    LazyColumn {
        item {
            Text(text = "Members(${currentMembers.size})")
        }
        items(currentMembers) { item ->
            val name = (item.data?.get("name") as? String) ?: ""
            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = name)

                if (averagePoint != null) {
                    Text(modifier = Modifier.padding(4.dp), text = item.data?.get("point").toString())
                } else {
                    if (name == username) {
                        Text(modifier = Modifier.padding(4.dp), text = item.data?.get("point").toString())
                    }
                    else {
                        Text(modifier = Modifier.padding(4.dp), text = "?")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    PointPokerTheme {
        RoomLayout(viewModel = MainViewModel(), roomId = "")
    }
}