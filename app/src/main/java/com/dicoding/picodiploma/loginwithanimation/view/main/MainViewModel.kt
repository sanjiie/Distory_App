package com.dicoding.picodiploma.loginwithanimation.view.main

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.story.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.story.StoryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    fun getStoriesPaging(token: String): Flow<PagingData<ListStoryItem>> {
        return repository.getStoriesPaging(token).cachedIn(viewModelScope)
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getStories(token: String): LiveData<StoryResponse?> = liveData {
        try {
            val stories = repository.getStories(token)
            Log.d("MainViewModel", "Stories fetched: ${stories.listStory?.size ?: 0}")
            emit(stories)
        } catch (e: HttpException) {
            val errorResponse = e.response()?.errorBody()?.string() ?: "Unknown error"
            Log.e(
                "MainViewModel", "HTTP Error: ${e.code()} - ${e.message()}\n" +
                        "Body Response: $errorResponse"
            )
            emit(null)
        } catch (e: IOException) {
            Log.e("MainViewModel", "Network Error: ${e.message}")
            emit(null)
        }
    }
}
