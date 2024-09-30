package com.chanop.pointpoker.view.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.intent.CreateRoomIntent
import com.chanop.pointpoker.intent.HomeIntent
import com.chanop.pointpoker.intent.RoomIntent
import com.chanop.pointpoker.repository.MemberRepositoryImpl
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.view.ViewModelFactory
import com.chanop.pointpoker.view.composables.theme.Grey
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.view.composables.theme.RedDark
import com.chanop.pointpoker.view.composables.theme.White
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Create your room", style = MaterialTheme.typography.titleLarge)

                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .clickable {
                            createRoomViewModel.processIntent(CreateRoomIntent.NavigateBack)
                        },
                    contentDescription = "Close Button"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    value = roomname,
                    onValueChange = { roomname = it }
                )

                CustomPoint(modifier = Modifier.padding(16.dp), viewModel = createRoomViewModel)
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 0.dp, 24.dp, 36.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                onClick = {
                    createRoomViewModel.processIntent(
                        CreateRoomIntent.CreateHome(
                            context = context,
                            roomName = roomname,
                        )
                    )
                }) {
                Text(text = "Create")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPoint(modifier: Modifier, viewModel: CreateRoomViewModel) {
    val pointList by viewModel.pointList.collectAsState()
    val addPoint by viewModel.addPoint.collectAsState()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
        // Add horizontal spacing between items
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        // Add vertical spacing between items
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pointList) { item ->
            Box(modifier = Modifier.fillMaxSize()) {
                Row (
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.secondary,
                        text = item.toString())
                }
                Icon(
                    Icons.Default.Close,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Grey)
                        .align(Alignment.TopEnd)
                        .clickable {
                           viewModel.processIntent(CreateRoomIntent.RemovePointList(item))
                        },
                    tint = White,
                    contentDescription = "Remove Point"
                )
            }
        }
        item {
            Box(modifier = Modifier.fillMaxSize()) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Add point",
                            color = Grey,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    value = addPoint,
                    onValueChange = viewModel::onAddPointChange,
                )
                Icon(
                    Icons.Default.Add,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Grey)
                        .align(Alignment.TopEnd)
                        .clickable {
                            viewModel.processIntent(CreateRoomIntent.AddPointList(addPoint))
                        },
                    tint = White,
                    contentDescription = "Add Point"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRoomScreenPreview() {
    val navController = rememberNavController()
    val createRoomViewModel: CreateRoomViewModel = viewModel(
        factory = ViewModelFactory(
            navController,
            UserRepositoryImpl(),
            RoomRepositoryImpl(),
            MemberRepositoryImpl()
        )
    )
    PointPokerTheme {
        CreateRoomScreen(username = "test", createRoomViewModel = createRoomViewModel)
    }
}



