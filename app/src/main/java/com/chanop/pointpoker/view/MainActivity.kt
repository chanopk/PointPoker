package com.chanop.pointpoker.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.repository.RoomRepository
import com.chanop.pointpoker.repository.RoomRepositoryImpl
import com.chanop.pointpoker.repository.UserRepository
import com.chanop.pointpoker.repository.UserRepositoryImpl
import com.chanop.pointpoker.viewmodel.MainViewModel
import com.chanop.pointpoker.view.composables.CreateRoomScreen
import com.chanop.pointpoker.view.composables.HomeScreen
import com.chanop.pointpoker.view.composables.RoomScreen
import com.chanop.pointpoker.view.composables.theme.PointPokerTheme
import com.chanop.pointpoker.viewmodel.RoomViewModel

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

class ViewModelFactory(private val navController: NavController, private val userRepository: UserRepository, private val roomRepository: RoomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            return RoomViewModel(navController, userRepository, roomRepository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


@Composable
fun NavControllerView() {
    // TODO optimize lifccycle
    val mainViewModel = MainViewModel()

    val navController = rememberNavController()
    val roomViewModel: RoomViewModel = viewModel(
        factory = ViewModelFactory(navController, UserRepositoryImpl(), RoomRepositoryImpl())
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(roomViewModel = roomViewModel)
        }
        //TODO 1 CreateRoomScreen to mvi
        composable("createroom/{username}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("username") ?: ""
            CreateRoomScreen(navController = navController, viewModel = mainViewModel, username = name)
        }
        //TODO 2  RoomScreen to mvi
        composable("room/{roomid}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomid") ?: ""
            RoomScreen(navController = navController, viewModel = mainViewModel, roomId = roomId)
        }

    }


}