package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.api.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val message = repository.register(name, email, password).message
                onSuccess(message ?: "Registrasi berhasil")
            } catch (e: HttpException) {
                val errorMessage = try {
                    val jsonInString = e.response()?.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    errorBody.message
                } catch (jsonException: Exception) {
                    "Registrasi gagal"
                }
                onError(errorMessage)
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan pada registrasi")
            }
        }
    }
}
