package com.chanop.pointpoker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.chanop.pointpoker.SharedPreferencesUtils
import com.chanop.pointpoker.intent.RoomIntent
import com.chanop.pointpoker.model.Room
import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.repository.RoomRepository
import com.chanop.pointpoker.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val navController: NavController,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _roomModel = MutableStateFlow<RoomModel>(RoomModel())
    val roomModel: StateFlow<RoomModel> = _roomModel

    // TODO optimize
    fun getUserName(context: Context): String {
        return SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
    }

    fun processIntent(intent: RoomIntent) {
        when (intent) {
            is RoomIntent.LoadRoom -> loadAllRoom(intent)
            is RoomIntent.JoinRoom -> joinRoom(intent = intent)
            is RoomIntent.NavigateTo -> navController.navigate(intent.path)
            is RoomIntent.RemoveRoom -> removeRoom(intent)
        }
    }

    private fun loadAllRoom(intent: RoomIntent.LoadRoom) {
        viewModelScope.launch {
            _roomModel.value = _roomModel.value.copy(isLoading = true)
            try {
                roomRepository.getRoomsSnapshotFlow().collect { snapshot ->
                    val roomList = snapshot.map { document ->
                        Room(
                            id = document.id,
                            name = document.data["name"] as String,
                            leader = document.data["leader"] as String,
                            averagePoint = document.data["average_point"] as Double?,
//                            points = document.data["points"] as List<Double>,
                            owner = (document.data["leader"] as String) == SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)
                        )
                    }
                    _roomModel.value = RoomModel(roomList = roomList)
                }
            } catch (e: Exception) {
                _roomModel.value = RoomModel(isLoading = false, error = "Failed to load room")
            }
        }
    }

    private fun joinRoom(intent: RoomIntent.JoinRoom) {
        viewModelScope.launch {
            isUserReady(intent.context, intent.name).collect { isReady ->
                if (isReady) {
                    val userID =
                        SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)

                    roomRepository.joinRoom(intent.roomID, intent.name, userID).collect { result ->
                        if (result.isSuccess) {
                            navController.navigate("room/${intent.roomID}")
                        } else {
                            _roomModel.value = _roomModel.value.copy(error = "Failed to join room : firebase error")
                        }
                    }

                } else {
                    _roomModel.value = _roomModel.value.copy(error = "Failed to join room : account error")
                }
            }
        }
    }

    private suspend fun isUserReady(context: Context, name: String): Flow<Boolean> = flow {
        if (name.isEmpty()) {
            emit(false)
        } else {
            val userID = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userID)

            if (userID.isEmpty()) {
                    userRepository.createUser(context, name).collect { result ->
                        emit(result.isSuccess)
                    }
            } else {
                val previousName =
                    SharedPreferencesUtils.getString(context, SharedPreferencesUtils.userName)
                if (name != previousName) {
                    userRepository.changeName(context, userID, name).collect { result ->
                        emit(result.isSuccess)
                    }
                } else {
                    emit(true)
                }
            }
        }
    }

    private fun removeRoom(intent: RoomIntent.RemoveRoom) {
        viewModelScope.launch {
            roomRepository.removeRooms(intent.roomID).collect { result ->
                result.onSuccess {
                    // Room removed successfully
                }.onFailure { exception ->
                    _roomModel.value = _roomModel.value.copy(isLoading = false, error = "Failed to remove room")
                }
            }
        }
    }

}