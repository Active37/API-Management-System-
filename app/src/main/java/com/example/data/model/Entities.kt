package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val permissions: String // Comma-separated list of scopes, eg. "read:metrics,write:records"
)

@Entity(tableName = "api_keys")
data class ApiKey(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val keyString: String,       // e.g. "sk_dev_abcdef12345"
    val environment: String,     // "Development", "Staging", "Production"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val roleId: Int              // Points to a Role.id
)
