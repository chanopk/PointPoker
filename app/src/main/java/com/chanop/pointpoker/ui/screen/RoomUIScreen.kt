package com.chanop.pointpoker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chanop.pointpoker.MainViewModel
import com.chanop.pointpoker.screen.RoomView
import com.chanop.pointpoker.ui.theme.PointPokerTheme

// TODO
@Composable
fun RoomScreen(navController: NavController? = null,
               modifier: Modifier = Modifier,
               viewModel: MainViewModel,
               roomId: String,) {
    val context = LocalContext.current

    // TODO rooms get data
    val roomData = viewModel.getRoom(roomId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp, 24.dp, 0.dp, 0.dp)
    ) {

        Icon(
            Icons.Default.Close,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    navController?.popBackStack()
                }
                .padding(16.dp),
            contentDescription = "Close Button"
        )

        membersScreen(navController, viewModel)

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 0.dp, 24.dp, 36.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                // TODO vote
            }) {
            Text(text = "Average Point")
        }
    }
}

@Composable
fun membersScreen(navController: NavController?, viewModel: MainViewModel) {
    val currentRoom by viewModel.currentRoom.collectAsState()


    // TODO display
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(currentRoom) {
                Text(text = (it.data?.get("name") as? String) ?: "")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    PointPokerTheme {
        RoomScreen(roomId = "", viewModel = MainViewModel())
    }
}