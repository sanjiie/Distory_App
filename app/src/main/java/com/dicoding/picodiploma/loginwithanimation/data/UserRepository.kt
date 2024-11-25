package com.dicoding.picodiploma.loginwithanimation.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.remote.Result
import com.dicoding.picodiploma.loginwithanimation.story.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.story.StoryPagingSource
import com.dicoding.picodiploma.loginwithanimation.story.StoryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    suspend fun register(name: String, email: String, password: String) =
        apiService.register(name, email, password)

    fun login(email: String, password: String): Flow<Result<UserModel>> = flow {
        emit(Result.Loading)
        val response = apiService.login(email, password)
        if (response.error == false) {
            val user = UserModel(
                email = response.loginResult?.name.orEmpty(),
                token = response.loginResult?.token.orEmpty(),
                isLogin = true
            )
            saveSession(user)
            emit(Result.Success(user))
        } else {
            emit(Result.Error(response.message ?: "Login failed"))
        }
    }.catch { e -> emit(Result.Error(e.message ?: "Login error")) }


    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() = userPreference.logout()

    suspend fun getStories(token: String) = apiService.getStories("Bearer $token")

    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        return apiService.getStoriesWithLocation(token, location = 1)
    }


    fun getStoriesPaging(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { StoryPagingSource(apiService, "Bearer $token") }
        ).flow
    }

    companion object {
        fun getInstance(apiService: ApiService, userPreference: UserPreference) =
            UserRepository(userPreference, apiService)
    }
}
