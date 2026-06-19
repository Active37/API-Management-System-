package com.example.data.db

import androidx.room.*
import com.example.data.model.ApiKey
import com.example.data.model.Role
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Query("SELECT * FROM roles ORDER BY name ASC")
    fun getAllRolesFlow(): Flow<List<Role>>

    @Query("SELECT * FROM roles")
    suspend fun getAllRoles(): List<Role>

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getRoleById(id: Int): Role?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: Role): Long

    @Delete
    suspend fun deleteRole(role: Role)

    @Update
    suspend fun updateRole(role: Role)
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY createdAt DESC")
    fun getAllApiKeysFlow(): Flow<List<ApiKey>>

    @Query("SELECT * FROM api_keys")
    suspend fun getAllApiKeys(): List<ApiKey>

    @Query("SELECT * FROM api_keys WHERE id = :id")
    suspend fun getApiKeyById(id: Int): ApiKey?

    @Query("SELECT * FROM api_keys WHERE keyString = :keyString AND isActive = 1 LIMIT 1")
    suspend fun findActiveKeyByString(keyString: String): ApiKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(key: ApiKey): Long

    @Delete
    suspend fun deleteApiKey(key: ApiKey)

    @Update
    suspend fun updateApiKey(key: ApiKey)
}
