package com.silemore.sileme.util

import android.content.Context
import com.silemore.sileme.network.ApiClient
import com.silemore.sileme.repository.AppRepository
import com.silemore.sileme.storage.TokenStore

class AppContainer(context: Context) {
    val tokenStore = TokenStore(context)
    private val apiClient = ApiClient(tokenStore)
    val repository = AppRepository.create(apiClient, tokenStore)
}
