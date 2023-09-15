package com.applligent.formdatarequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.applligent.formdatarequest.model.ApiResponse
import com.applligent.formdatarequest.network.Repository
import com.applligent.koindi.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val repository: Repository) : ViewModel() {
   /* private val _users = MutableLiveData<Resource<List<ApiResponse>>>()
    val users: LiveData<Resource<List<ApiResponse>>>
        get() = _users*/

    private val _users = MutableLiveData<Resource<ApiResponse>>()
    val users: LiveData<Resource<ApiResponse>>
        get() = _users


    fun addProduct(map: RequestBody, token:String){
        val response = repository.addProduct(map,token)
        response.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                _users.postValue(Resource.loading(null))
                if (response.isSuccessful){
                    _users.postValue(Resource.success(response.body()))
                }else{
                    _users.postValue(Resource.error(response.errorBody().toString(), null))
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                _users.postValue(Resource.loading(null))
                _users.postValue(Resource.error(t.message.toString(), null))
            }
        })
    }
}

class MainViewModelFactory constructor(private val repository: Repository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}