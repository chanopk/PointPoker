package com.chanop.pointpoker.view.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.intent.CreateRoomIntent
import com.chanop.pointpoker.intent.HomeIntent
import com.chanop.pointpoker.repository.MemberRepositoryImpl
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.view.ViewModelFactory
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.view.composables.theme.RedDark
import com.chanop.pointpoker.viewmodel.CreateRoomViewModel
import com.chanop.pointpoker.viewmodel.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    modifier: Modifier = Modifier,
    createRoomViewModel: CreateRoomViewModel,
    username: String
) {
    val context = LocalContext.current
    var roomname by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val createRoomModel by createRoomViewModel.createRoomModel.collectAsState()

    createRoomModel.error?.let { errorMessage ->
        LaunchedEffect(key1 = errorMessage) { // Triggered when errorMessage changes
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Icon(
                Icons.Default.Close,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable {
                        createRoomViewModel.processIntent(CreateRoomIntent.NavigateBack)
                    }
                    .padding(16.dp),
                contentDescription = "Close Button"
            )

            TextField(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .background(Color.Transparent)
                    .align(Alignment.Center),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                value = roomname,
                onValueChange = { roomname = it }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 0.dp, 24.dp, 36.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedDark),
                onClick = {
                    createRoomViewModel.processIntent(CreateRoomIntent.CreateHome(context = context, roomName = roomname))
                }) {
                Text(text = "Create")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRoomScreenPreview() {
    val navController = rememberNavController()
    val createRoomViewModel: CreateRoomViewModel = viewModel(
        factory = ViewModelFactory(navController, UserRepositoryImpl(), RoomRepositoryImpl(), MemberRepositoryImpl())
    )
    PointPokerTheme {
        CreateRoomScreen(username = "test", createRoomViewModel = createRoomViewModel)
    }
}



