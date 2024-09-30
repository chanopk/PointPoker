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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRoomViewModel(
    private val navController: NavController,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _pointList = MutableStateFlow<ArrayList<Double>>(arrayListOf(0.5,1.0,1.5,2.0,3.0,5.0))
    val pointList: StateFlow<ArrayList<Double>> = _pointList

    private val _addPoint = MutableStateFlow<String>("")
    val addPoint = _addPoint.asStateFlow()

    fun onAddPointChange(text: String) {
        _addPoint.value = text
    }

    private val _createRoomModel = MutableStateFlow<CreateRoomModel>(CreateRoomModel())
    val createRoomModel: StateFlow<CreateRoomModel> = _createRoomModel


    fun processIntent(intent: CreateRoomIntent) {
        when (intent) {
            is CreateRoomIntent.AddPointList -> addPointList(intent)
            is CreateRoomIntent.RemovePointList -> removePointList(intent)
            is CreateRoomIntent.CreateHome -> createRoom(intent)
            is CreateRoomIntent.NavigateTo -> navController.navigate(intent.path)
            CreateRoomIntent.NavigateBack -> navController.popBackStack()
        }
    }

    private fun addPointList(intent: CreateRoomIntent.AddPointList) {
        if (intent.point.isNotEmpty() && intent.point.toDoubleOrNull() != null) {
            val newList = arrayListOf<Double>()
            newList.addAll(_pointList.value)
            newList.add(intent.point.toDouble())
            newList.sort()

            _pointList.value = newList
            _addPoint.value = ""
        } else {
            _createRoomModel.value = _createRoomModel.value.copy(error = "Invalid point")
        }
    }

    private fun removePointList(intent: CreateRoomIntent.RemovePointList) {
        val newList = arrayListOf<Double>()
        newList.addAll(_pointList.value)
        newList.remove(intent.point)

        _pointList.value = newList
    }

    private fun createRoom(intent: CreateRoomIntent.CreateHome) {
        viewModelScope.launch {
            val userID = SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)

            roomRepository.createRoom(userID = userID , roomName = intent.roomName, points = _pointList.value).collect { result ->
                if (result.isSuccess) {
                    navController.popBackStack()
                } else {
                    _createRoomModel.value = _createRoomModel.value.copy(error = "Failed to create room")
                }
            }
        }
    }
}