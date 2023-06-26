package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mbj.ssassamarket.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseDataSource(private val apiClient: ApiClient) : MarketNetworkDataSource {

    override suspend fun currentUserExists(): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        return users.containsKey(user?.uid)
                    }
                } else {
                    Log.d("currentUserExists Error", "${Exception(response.code().toString())}")
                }
            } catch (e: Exception) {
                Log.d("currentUserExists Error", e.toString())
            }
        }
        return false
    }

    override suspend fun addUser(nickname: String): Boolean {
        val (user, idToken) = getUserAndIdToken()
        val userItem = User(user?.uid, nickname, null)
        if (idToken != null) {
            try {
                val response = apiClient.addUser(user?.uid ?: "", userItem, idToken)
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

    override suspend fun checkDuplicateUserName(nickname: String): Boolean {
        val (user, idToken) = getUserAndIdToken()
        if (idToken != null) {
            try {
                val response = apiClient.getUser(idToken)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        return users.values.flatMap { it.values }.any { userInfo ->
                            userInfo.userName == nickname
                        }
                    }
                } else {
                    Log.e("checkDuplicateUserName Error", "${Exception(response.code().toString())}")
                }
            } catch (e: Exception) {
                Log.e("checkDuplicateUserName Error", e.toString())
            }
        }
        return false
    }

    private suspend fun getUserAndIdToken(): Pair<FirebaseUser?, String?> {
        val user = FirebaseAuth.getInstance().currentUser
        var idToken: String? = null

        try {
            idToken = user?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            Log.e("idToken Error", e.toString())
        }
        return Pair(user, idToken)
    }
}
