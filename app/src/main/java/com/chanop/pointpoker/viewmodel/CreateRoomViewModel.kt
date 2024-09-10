package com.chanop.pointpoker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.chanop.pointpoker.SharedPreferencesUtils
import com.chanop.pointpoker.intent.CreateRoomIntent
import com.chanop.pointpoker.model.CreateRoomModel
import com.chanop.pointpoker.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateRoomViewModel(
    private val navController: NavController,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _createRoomModel = MutableStateFlow<CreateRoomModel>(CreateRoomModel())
    val createRoomModel: StateFlow<CreateRoomModel> = _createRoomModel


    fun processIntent(intent: CreateRoomIntent) {
        when (intent) {
            is CreateRoomIntent.CreateHome -> createRoom(intent)
            is CreateRoomIntent.NavigateTo -> navController.navigate(intent.path)
            CreateRoomIntent.NavigateBack -> navController.popBackStack()
        }
    }

    private fun createRoom(intent: CreateRoomIntent.CreateHome) {
        viewModelScope.launch {
            val userID = SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)

            roomRepository.createRoom(userID = userID , roomName = intent.roomName).collect { result ->
                if (result.isSuccess) {
                    navController.popBackStack()
                } else {
                    _createRoomModel.value = _createRoomModel.value.copy(error = "Failed to create room")
                }
            }
        }
    }
}