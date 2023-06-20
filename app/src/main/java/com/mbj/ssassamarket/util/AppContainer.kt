package com.mbj.ssassamarket.util

import com.mbj.ssassamarket.data.source.remote.ApiClient

class AppContainer {

    private var apiClient: ApiClient? = null

    fun provideApiClient(): ApiClient {
        return apiClient ?: ApiClient.create().apply {
            apiClient = this
        }
    }
}
