package com.chanop.pointpoker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.chanop.pointpoker.SharedPreferencesUtils
import com.chanop.pointpoker.intent.RoomIntent
import com.chanop.pointpoker.model.Member
import com.chanop.pointpoker.model.MembersModel
import com.chanop.pointpoker.model.Room
import com.chanop.pointpoker.model.RoomModel
import com.chanop.pointpoker.model.RoomsModel
import com.chanop.pointpoker.repository.MemberRepository
import com.chanop.pointpoker.repository.RoomRepository
import com.chanop.pointpoker.repository.UserRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val navController: NavController,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _currentRoom = MutableStateFlow<RoomModel>(RoomModel())
    val currentRoom: StateFlow<RoomModel> = _currentRoom.asStateFlow()

    private val _currentMembers = MutableStateFlow<MembersModel>(MembersModel())
    val currentMembers: StateFlow<MembersModel> = _currentMembers.asStateFlow()


    fun processIntent(intent: RoomIntent) {
        when (intent) {
            is RoomIntent.LoadRoom -> getCurrentRoom(intent)
            is RoomIntent.LoadMembers -> getCurrentMembers(intent)
            is RoomIntent.NavigateTo -> navController.navigate(intent.path)
            RoomIntent.NavigateBack -> navController.popBackStack()
        }
    }

    private fun getCurrentRoom(intent: RoomIntent.LoadRoom) {
        viewModelScope.launch {
            roomRepository.getRoomSnapshotFlow(intent.roomID).collect { snapshot ->

                _currentRoom.value = RoomModel( room = Room(
                        id = snapshot.id,
                        name = snapshot.data?.get("name") as String,
                        leader = snapshot.data?.get("leader") as String,
                        averagePoint = snapshot.data?.get("average_point") as Double?,
                        points = snapshot.data?.get("points") as List<Double>,
                        owner = (snapshot.data?.get("leader") as String) == SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userID)
                    )
                )
            }
        }
    }

    private fun getCurrentMembers(intent: RoomIntent.LoadMembers) {
        viewModelScope.launch {
            memberRepository.getMembersSnapshotFlow(intent.roomID).collect { snapshot ->
                val memberList = snapshot.map { document ->
                    Member(
                        id = document.id,
                        name = document.data["name"] as String,
                        point = document.data["point"] as Double?,
                        itsMe = (document.data["name"] as String) == SharedPreferencesUtils.getString(intent.context, SharedPreferencesUtils.userName)
                    )
                }
                _currentMembers.value = MembersModel(memberList = memberList)
            }
        }
    }
}