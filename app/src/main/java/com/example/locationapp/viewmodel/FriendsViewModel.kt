package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.User
import com.example.locationapp.model.Friend
import com.example.locationapp.model.FriendRequest
import com.example.locationapp.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class FriendsViewModel(private val repository: FriendRepository) : ViewModel() {
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    init { fetchFriends() }

    fun fetchFriends() {
        viewModelScope.launch {
            repository.getFriends().onSuccess { _friends.value = it }
            repository.getIncomingRequests().onSuccess { _incomingRequests.value = it }
            repository.getOutgoingRequests().onSuccess { _outgoingRequests.value = it }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            repository.searchUser(query).onSuccess { _searchResults.value = it }
        }
    }

    fun addFriend(user: User) {
        viewModelScope.launch {
            repository.addFriend(user).onSuccess {
                fetchFriends()
                _searchResults.value = emptyList()
            }
        }
    }



    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests = _incomingRequests.asStateFlow()

    private val _outgoingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())

    val outgoingRequests = _outgoingRequests.asStateFlow()

    fun fetchFriendsAndRequests() {
        viewModelScope.launch {
            repository.getFriends().onSuccess { _friends.value = it }
            repository.getIncomingRequests().onSuccess { _incomingRequests.value = it }
            repository.getOutgoingRequests().onSuccess { _outgoingRequests.value = it }
        }
    }

    fun removeFriend(friendId: String) = viewModelScope.launch {
        repository.removeFriend(friendId).onSuccess { fetchFriendsAndRequests() }
    }

    fun cancelSentRequest(targetId: String) = viewModelScope.launch {
        repository.cancelRequest(targetId).onSuccess { fetchFriendsAndRequests() }
    }

    fun sendRequest(user: User) = viewModelScope.launch {
        repository.sendFriendRequest(user).onSuccess { fetchFriendsAndRequests() }
    }
    fun acceptRequest(req: FriendRequest) = viewModelScope.launch {
        repository.acceptFriendRequest(req).onSuccess { fetchFriendsAndRequests() }
    }
}

class FriendsViewModelFactory(private val repository: FriendRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendsViewModel(repository) as T
    }
}
