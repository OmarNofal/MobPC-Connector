package com.omar.pcconnector.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore


private const val FILE_NAME = "user_prefs_store1.pb"

val Context.userPreferencesDatastore: DataStore<UserPreferences> by dataStore(
    fileName = FILE_NAME,
    serializer = UserPreferencesSerializer
)

