package com.omar.pcconnector.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class DeviceEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    val name: String,

    val token: String,

    val os: String,

    val autoConnect: Boolean
)