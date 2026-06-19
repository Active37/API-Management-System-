package com.example.data.repository

import com.example.data.db.ApiKeyDao
import com.example.data.db.RoleDao
import com.example.data.db.SecurityEventDao
import com.example.data.model.ApiKey
import com.example.data.model.Role
import com.example.data.model.SecurityEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class SecurityRepository(
    private val roleDao: RoleDao,
    private val apiKeyDao: ApiKeyDao,
    private val securityEventDao: SecurityEventDao
) {
    val allRolesFlow: Flow<List<Role>> = roleDao.getAllRolesFlow()
    val allApiKeysFlow: Flow<List<ApiKey>> = apiKeyDao.getAllApiKeysFlow()
    val allSecurityEventsFlow: Flow<List<SecurityEvent>> = securityEventDao.getAllSecurityEventsFlow()

    suspend fun insertSecurityEvent(event: SecurityEvent) = securityEventDao.insertSecurityEvent(event)
    suspend fun clearAllSecurityEvents() = securityEventDao.clearAllSecurityEvents()

    suspend fun getAllRoles(): List<Role> = roleDao.getAllRoles()
    suspend fun getRoleById(id: Int): Role? = roleDao.getRoleById(id)
    suspend fun insertRole(role: Role): Long = roleDao.insertRole(role)
    suspend fun updateRole(role: Role) = roleDao.updateRole(role)
    suspend fun deleteRole(role: Role) = roleDao.deleteRole(role)

    suspend fun getAllApiKeys(): List<ApiKey> = apiKeyDao.getAllApiKeys()
    suspend fun getApiKeyById(id: Int): ApiKey? = apiKeyDao.getApiKeyById(id)
    suspend fun findActiveKeyByString(keyString: String): ApiKey? = apiKeyDao.findActiveKeyByString(keyString)
    suspend fun insertApiKey(key: ApiKey): Long = apiKeyDao.insertApiKey(key)
    suspend fun updateApiKey(key: ApiKey) = apiKeyDao.updateApiKey(key)
    suspend fun deleteApiKey(key: ApiKey) = apiKeyDao.deleteApiKey(key)

    /**
     * Seeds the local SQLite database with realistic default data if no roles exist yet.
     */
    suspend fun seedDatabaseIfEmpty() {
        val existingRoles = roleDao.getAllRoles()
        if (existingRoles.isEmpty()) {
            // 1. Seed Roles
            val adminRole = Role(
                name = "Administrator",
                description = "Full control over all platform services, billing, and credential managers.",
                permissions = "sys:all,keys:write,keys:read,keys:delete,billing:manage"
            )
            val devRole = Role(
                name = "Lead Developer",
                description = "Engineering privileges to execute, deploy, and inspect logs.",
                permissions = "api:read,api:write,metrics:view"
            )
            val viewerRole = Role(
                name = "Support / Viewer",
                description = "Read-Only operational viewer roles with zero mutating permissions.",
                permissions = "api:read,metrics:view"
            )
            val billingRole = Role(
                name = "Billing Agent",
                description = "Account access restricted strictly to payment policies and metrics logs.",
                permissions = "billing:manage,api:read"
            )

            val adminId = roleDao.insertRole(adminRole).toInt()
            val devId = roleDao.insertRole(devRole).toInt()
            val viewerId = roleDao.insertRole(viewerRole).toInt()
            val billingId = roleDao.insertRole(billingRole).toInt()

            // 2. Seed realistic API keys linked to these roles
            val prodKey = ApiKey(
                name = "Stripe Gateway Integration",
                keyString = "sk_prod_7a2b918d0c3e4f50",
                environment = "Production",
                roleId = adminId,
                isActive = true,
                createdAt = System.currentTimeMillis() - (105L * 24 * 60 * 60 * 1000) // 105 days old
            )
            val stagingKey = ApiKey(
                name = "GitHub Webhook Event-Bus",
                keyString = "sk_staging_cf19e2a863740db2",
                environment = "Staging",
                roleId = devId,
                isActive = true
            )
            val devKey = ApiKey(
                name = "Local Mock-Analytics Core",
                keyString = "sk_dev_59012a938cdc45eb",
                environment = "Development",
                roleId = viewerId,
                isActive = true
            )

            apiKeyDao.insertApiKey(prodKey)
            apiKeyDao.insertApiKey(stagingKey)
            apiKeyDao.insertApiKey(devKey)
        }
    }
}
