package com.example.wearzone.data.local.datasource


interface IPayMockLocalDataSource {
    suspend fun saveApiKey(key: String)
    suspend fun getApiKey(): String?
    suspend fun clearApiKey()
}
