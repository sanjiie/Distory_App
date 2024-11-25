package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.remote.Result
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            try {
                repository.saveSession(user)
            } catch (e: Exception) {
                // Error Logging
            }
        }
    }

    fun login(email: String, password: String): LiveData<Result<UserModel>> = liveData {
        repository.login(email, password).collect { result ->
            emit(result)
        }
    }
}
