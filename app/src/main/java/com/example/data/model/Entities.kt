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

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,       // "KEY_CREATED", "KEY_DELETED", "KEY_ROTATED", "KEY_STATUS_CHANGED", "ROLE_CREATED", "ROLE_UPDATED", "ROLE_DELETED"
    val affectedName: String,    // E.g., Name of the key or role affected
    val details: String,         // Description of what changed
    val timestamp: Long = System.currentTimeMillis()
)
