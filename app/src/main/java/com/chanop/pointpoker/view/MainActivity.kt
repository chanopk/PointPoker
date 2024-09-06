package com.chanop.pointpoker.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.viewmodel.MainViewModel
import com.chanop.pointpoker.view.composables.HomeScreen
import com.chanop.pointpoker.view.composables.CreateRoomScreen
import com.chanop.pointpoker.view.composables.RoomScreen
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointPokerTheme {
                NavControllerView()
            }
        }
    }
}


@Composable
fun NavControllerView() {
    // TODO optimize lifccycle , viewModel
    val mainViewModel = MainViewModel()
    mainViewModel.getRooms()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = mainViewModel)
        }
        composable("createroom/{username}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("username") ?: ""
            CreateRoomScreen(navController = navController, viewModel = mainViewModel, username = name)
        }
        composable("room/{roomid}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomid") ?: ""
            RoomScreen(navController = navController, viewModel = mainViewModel, roomId = roomId)
        }

    }


}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PointPokerTheme {
        NavControllerView()
    }
}