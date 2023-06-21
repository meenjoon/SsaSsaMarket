package com.mbj.ssassamarket.data.source.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mbj.ssassamarket.SsaSsaMarketApplication

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
}
