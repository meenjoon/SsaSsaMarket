package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mbj.ssassamarket.SsaSsaMarketApplication
import com.mbj.ssassamarket.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseDataSource() : MarketNetworkDataSource {

    private val apiClient = SsaSsaMarketApplication.appContainer.provideApiClient()

    override suspend fun currentUserExists(): Boolean {
        val uId = FirebaseAuth.getInstance().currentUser?.uid

        try {
            val response = apiClient.getUser()
            if (response.isSuccessful) {
                val users = response.body()
                if (users != null) {
                    return users.containsKey(uId)
                }
            } else {
                Log.d("currentUserExists Error", "${Exception(response.code().toString())}")
            }
        } catch (e: Exception) {
            Log.d("currentUserExists Error", e.toString())
        }

        return false
    }

    override suspend fun addUser(nickname: String): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        val idToken = user?.getIdToken(true)?.await()?.token
        val userItem = User(user?.uid, nickname, null)

        if (idToken != null) {
            try {
                val response = apiClient.addUser(user.uid, userItem, idToken)
                if (response.isSuccessful) {
                    Log.d("postUser Success", "${response.body()}")
                    return true
                } else {
                    Log.e("FirebaseDataSource", "postUser Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseDataSource", "postUser Error: $e")
            }
        }

        return false
    }
}
