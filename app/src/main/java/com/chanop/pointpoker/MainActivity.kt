package com.chanop.pointpoker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chanop.pointpoker.screen.HomeScreen
import com.chanop.pointpoker.ui.screen.CreateRoomScreen
import com.chanop.pointpoker.ui.theme.PointPokerTheme

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
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = mainViewModel)
        }
        composable("createroom/{username}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("username") ?: ""
            CreateRoomScreen(navController = navController, viewModel = mainViewModel, username = name)
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