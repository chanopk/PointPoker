package com.chanop.pointpoker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.chanop.pointpoker.SharedPreferencesUtils
import com.chanop.pointpoker.intent.HomeIntent
import com.chanop.pointpoker.model.Room
import com.chanop.pointpoker.model.RoomsModel
import com.chanop.pointpoker.repository.RoomRepository
import com.chanop.pointpoker.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class HomeViewModel(
    private val navController: NavController,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _roomsModel = MutableStateFlow<RoomsModel>(RoomsModel())
    val roomsModel: StateFlow<RoomsModel> = _roomsModel

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    val searchRoom = searchText
        .debounce(300L)
        .onEach { _isSearching.update { true } }
        .combine(_roomsModel) { text, room ->
            if(text.isBlank()) {
                room
            } else {
                delay(500L)

                RoomsModel(roomList = room.roomList.filter {
                    it.name.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))
                })
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _roomsModel.value
        )

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadHome -> loadAllRoom(intent)
            is HomeIntent.JoinHome -> joinRoom(intent = intent)
            is HomeIntent.NavigateTo -> navController.navigate(intent.path)
            HomeIntent.NavigateBack -> navController.popBackStack()
            is HomeIntent.RemoveHome -> removeRoom(intent)
        }
    }

    private fun loadAllRoom(intent: HomeIntent.LoadHome) {
        viewModelScope.launch {
            _roomsModel.value = _roomsModel.value.copy(isLoading = true)
            try {
                roomRepository.getRoomsSnapshotFlow().collect { snapshot ->
                    val roomList = snapshot.map { document ->
                        Room(
                            id = document.id,
                            name = document.data["name"] as String,
                            leader = document.data["leader"] as String,
//                            averagePoint = document.data["average_point"] as Double?,
                            owner = (document.data["leader"] as String) == SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID),
                            recent = document.id == SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.recentRoomID)
                        )
                    }
                    _roomsModel.value = RoomsModel(roomList = roomList)
                }
            } catch (e: Exception) {
                _roomsModel.value = RoomsModel(isLoading = false, error = "Failed to load room")
            }
        }
    }

    private fun joinRoom(intent: HomeIntent.JoinHome) {
        viewModelScope.launch {
            isUserReady(intent.context, intent.name).collect { isReady ->
                if (isReady) {
                    val userID =
                        SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)

                    roomRepository.joinRoom(intent.roomID, intent.name, userID).collect { result ->
                        if (result.isSuccess) {
                            SharedPreferencesUtils.putString(intent.context, SharedPreferencesUtils.recentRoomID, intent.roomID)
                            navController.navigate("room/${intent.roomID}")
                        } else {
                            _roomsModel.value = _roomsModel.value.copy(error = "Failed to join room : firebase error")
                        }
                    }

                } else {
                    _roomsModel.value = _roomsModel.value.copy(error = "Failed to join room : account error")
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

    private fun removeRoom(intent: HomeIntent.RemoveHome) {
        viewModelScope.launch {
            roomRepository.removeRooms(intent.roomID).collect { result ->
                result.onSuccess {
                    // Room removed successfully
                }.onFailure { exception ->
                    _roomsModel.value = _roomsModel.value.copy(isLoading = false, error = "Failed to remove room")
                }
            }
        }
    }

}