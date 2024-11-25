package com.dicoding.picodiploma.loginwithanimation.view.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.story.StoryResponse
import retrofit2.HttpException
import java.io.IOException

class MapsViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStoriesWithLocation(token: String): LiveData<StoryResponse?> = liveData {
        try {
            val response = repository.getStoriesWithLocation(token)
            emit(response)
        } catch (e: HttpException) {
            Log.e("MapsViewModel", "HTTP Error: ${e.message}")
            emit(null)
        } catch (e: IOException) {
            Log.e("MapsViewModel", "Network Error: ${e.message}")
            emit(null)
        }
    }
}
