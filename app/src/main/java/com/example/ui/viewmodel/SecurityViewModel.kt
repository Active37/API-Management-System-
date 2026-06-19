package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitGeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.ApiKey
import com.example.data.model.Role
import com.example.data.model.SecurityEvent
import com.example.data.repository.SecurityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

import com.example.data.util.KeyGeneratorUtils

class SecurityViewModel(
    application: Application,
    private val repository: SecurityRepository
) : AndroidViewModel(application) {

    val roles: StateFlow<List<Role>> = repository.allRolesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val apiKeys: StateFlow<List<ApiKey>> = repository.allApiKeysFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val securityEvents: StateFlow<List<SecurityEvent>> = repository.allSecurityEventsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    private val _auditReport = MutableStateFlow<String?>(null)
    val auditReport: StateFlow<String?> = _auditReport.asStateFlow()

    private val _terminalOutput = MutableStateFlow<String?>(null)
    val terminalOutput: StateFlow<String?> = _terminalOutput.asStateFlow()

    init {
        // Pre-propulate some simulation data if empty so the developer has immediate access
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Role Management Operations ---
    fun addRole(name: String, description: String, permissions: String) {
        viewModelScope.launch {
            val cleanedPermissions = permissions.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(",")
            repository.insertRole(
                Role(name = name, description = description, permissions = cleanedPermissions)
            )
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "PERMISSION_CHANGED",
                    affectedName = name,
                    details = "Created Role '$name' with permissions: '$cleanedPermissions'"
                )
            )
        }
    }

    fun updateRole(role: Role) {
        viewModelScope.launch {
            repository.updateRole(role)
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "PERMISSION_CHANGED",
                    affectedName = role.name,
                    details = "Updated Role '${role.name}' permissions: '${role.permissions}'"
                )
            )
        }
    }

    fun deleteRole(role: Role) {
        viewModelScope.launch {
            repository.deleteRole(role)
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "PERMISSION_CHANGED",
                    affectedName = role.name,
                    details = "Deleted Role '${role.name}' with permissions: '${role.permissions}'"
                )
            )
        }
    }

    // --- Key Management Operations ---
    fun addApiKey(name: String, environment: String, roleId: Int, customKey: String? = null) {
        viewModelScope.launch {
            val keyString = if (!customKey.isNullOrBlank()) {
                customKey.trim()
            } else {
                val prefix = when(environment.lowercase()) {
                    "production" -> "sk_prod"
                    "staging" -> "sk_staging"
                    else -> "sk_dev"
                }
                // Default secure generator producing a strong 32-char Base58 alphanumeric token
                KeyGeneratorUtils.assembleKey(prefix = prefix, length = 32, format = "base58")
            }
            
            repository.insertApiKey(
                ApiKey(
                    name = name,
                    keyString = keyString,
                    environment = environment,
                    roleId = roleId,
                    isActive = true
                )
            )

            val boundRole = repository.getRoleById(roleId)
            val roleName = boundRole?.name ?: "Unknown Role"
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "KEY_CREATED",
                    affectedName = name,
                    details = "Created credential ($environment) bound to role '$roleName'"
                )
            )
        }
    }

    fun toggleKeyStatus(apiKey: ApiKey) {
        viewModelScope.launch {
            val nextStatus = !apiKey.isActive
            repository.updateApiKey(apiKey.copy(isActive = nextStatus))
            val statusString = if (nextStatus) "Activated" else "Deactivated"
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "KEY_STATUS_CHANGED",
                    affectedName = apiKey.name,
                    details = "$statusString credential with env '${apiKey.environment}'"
                )
            )
        }
    }

    fun deleteApiKey(apiKey: ApiKey) {
        viewModelScope.launch {
            repository.deleteApiKey(apiKey)
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "KEY_DELETED",
                    affectedName = apiKey.name,
                    details = "Deleted credential with env '${apiKey.environment}'"
                )
            )
        }
    }

    fun rotateApiKey(apiKey: ApiKey) {
        viewModelScope.launch {
            val prefix = when(apiKey.environment.lowercase()) {
                "production" -> "sk_prod"
                "staging" -> "sk_staging"
                else -> "sk_dev"
            }
            // Generate a fresh key matching current key's strength/length
            val newKey = KeyGeneratorUtils.assembleKey(
                prefix = prefix,
                length = apiKey.keyString.length.coerceAtLeast(32),
                format = if (apiKey.keyString.length >= 40) "base64" else "base58"
            )
            repository.updateApiKey(
                apiKey.copy(
                    keyString = newKey,
                    createdAt = System.currentTimeMillis()
                )
            )
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "KEY_ROTATED",
                    affectedName = apiKey.name,
                    details = "Rotated credential credentials with environment '${apiKey.environment}'"
                )
            )
        }
    }

    fun simulateAging(apiKey: ApiKey) {
        viewModelScope.launch {
            // Set age to 95 days ago (8,208,000,000 milliseconds)
            val ninetyFiveDaysMs = 95L * 24 * 60 * 60 * 1000
            repository.updateApiKey(
                apiKey.copy(
                    createdAt = System.currentTimeMillis() - ninetyFiveDaysMs
                )
            )
            repository.insertSecurityEvent(
                SecurityEvent(
                    eventType = "KEY_STATUS_CHANGED",
                    affectedName = apiKey.name,
                    details = "Simulated age over 90 days for API key: ${apiKey.name}"
                )
            )
        }
    }

    fun clearAllSecurityEvents() {
        viewModelScope.launch {
            repository.clearAllSecurityEvents()
        }
    }

    // --- Simulated Cryptographic Verification Terminal ---
    fun verifyKeyScope(keyString: String, requiredScope: String) {
        viewModelScope.launch {
            if (keyString.isBlank()) {
                _terminalOutput.value = """
                    $[ERROR] 400 Bad Request
                    Reason: API key string parameter cannot be blank.
                    Please type/copy a generated API key into the console input.
                """.trimIndent()
                return@launch
            }

            if (requiredScope.isBlank()) {
                _terminalOutput.value = """
                    $[ERROR] 400 Bad Request
                    Reason: Target Permission scope parameter cannot be blank.
                    (e.g., 'api:read' or 'sys:all')
                """.trimIndent()
                return@launch
            }

            val activeKeyString = keyString.trim()
            val apiKey = repository.findActiveKeyByString(activeKeyString)

            if (apiKey == null) {
                // Let's check if the key exists at all but is simply inactive
                val allKeys = repository.getAllApiKeys()
                val inactiveKey = allKeys.find { it.keyString == activeKeyString }
                if (inactiveKey != null) {
                    _terminalOutput.value = """
                        $[SECURITY EXCEPTION] 401 Unauthorized
                        Key Name: "${inactiveKey.name}" [${inactiveKey.environment}]
                        Key ID: ${inactiveKey.id}
                        Reason: KEY_STATUS_SUSPENDED. The key exists but is currently deactivated.
                        Enable the toggle in your active keys registry to authorize requests.
                    """.trimIndent()
                } else {
                    _terminalOutput.value = """
                        $[SECURITY ACCESS REJECTED] 401 Unauthorized
                        Key Supplied: ${activeKeyString.take(12)}...
                        Reason: CREDENTIALS_NOT_FOUND. Key verification failed.
                        Invalid signature or key does not exist in the secure SQLite database vault.
                    """.trimIndent()
                }
                return@launch
            }

            val role = repository.getRoleById(apiKey.roleId)
            if (role == null) {
                _terminalOutput.value = """
                    $[SERVER ERROR] 500 Internal Server Integrity Conflict
                    Key Name: "${apiKey.name}"
                    Environment: ${apiKey.environment}
                    Reason: ORPHAN_CREDENTIAL. Key is currently bound to role ID [${apiKey.roleId}], which does not exist.
                    Assign this key to a valid active role in the database console.
                """.trimIndent()
                return@launch
            }

            val permissionsList = role.permissions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val hasPermission = permissionsList.contains("sys:all") || 
                                permissionsList.contains(requiredScope.trim())

            val formattedPermissions = permissionsList.joinToString(", ") { "'$it'" }

            if (hasPermission) {
                _terminalOutput.value = """
                    $[AUTHORIZATION GRANTED] 200 OK
                    Key Authenticated: "${apiKey.name}" (${apiKey.environment})
                    Assigned Role: "${role.name}"
                    Permissions Map: [ $formattedPermissions ]
                    Requested Scope: '$requiredScope'
                    
                    >> Verification Status: ACCESS_ALLOWED
                    >> Session Trace: SHA256_VERIFIED_SIGNATURE_OK
                    Authentication headers processed successfully in 12ms.
                """.trimIndent()
            } else {
                _terminalOutput.value = """
                    $[RBAC ACCESS DENIED] 403 Forbidden
                    Key Authenticated: "${apiKey.name}" (${apiKey.environment})
                    Assigned Role: "${role.name}"
                    Permissions Map: [ $formattedPermissions ]
                    Requested Scope: '$requiredScope'
                    
                    >> Verification Status: UNAUTHORIZED_SCOPE_VIOLATION
                    >> Required Permission: '$requiredScope' lacking in bindings.
                    Reason: Insufficient scopes. Action rejected by client gateway security controllers.
                """.trimIndent()
            }
        }
    }

    // --- Developer Audit System (Real Gemini or Interactive Simulated Rule Audit) ---
    fun runAudit() {
        viewModelScope.launch {
            _isAuditing.value = true
            _auditReport.value = null

            val currentKeys = repository.getAllApiKeys()
            val currentRoles = repository.getAllRoles()

            if (currentKeys.isEmpty() && currentRoles.isEmpty()) {
                _auditReport.value = """
                    ### Security Hub: Vault Audit Completed
                    No registry data found. Please add Roles and API Keys to perform security analyses.
                """.trimIndent()
                _isAuditing.value = false
                return@launch
            }

            val isGeminiAvailable = RetrofitGeminiClient.isApiKeyAvailable()

            if (isGeminiAvailable) {
                try {
                    val prompt = buildAnalysisPrompt(currentKeys, currentRoles)
                    val result = withContext(Dispatchers.IO) {
                        RetrofitGeminiClient.auditSecurity(prompt)
                    }
                    _auditReport.value = result
                } catch (e: Exception) {
                    Log.e("SecurityViewModel", "Gemini audit failed, falling back", e)
                    _auditReport.value = buildLocalFallbackAudit(currentKeys, currentRoles, "Gemini service encountered an error (${e.localizedMessage}). Utilizing secure internal rule auditing. ")
                }
            } else {
                // Wait briefly for natural UX feeling (200ms)
                withContext(Dispatchers.Default) {
                    kotlinx.coroutines.delay(800)
                }
                _auditReport.value = buildLocalFallbackAudit(currentKeys, currentRoles, "Secured offline-mode audit compiled successfully. ")
            }
            _isAuditing.value = false
        }
    }

    fun clearAuditReport() {
        _auditReport.value = null
    }

    private fun buildAnalysisPrompt(keys: List<ApiKey>, roles: List<Role>): String {
        val keysStr = keys.joinToString("\n") { 
            "- Name: ${it.name}, Env: ${it.environment}, Active: ${it.isActive}, Key: ${it.keyString.take(15)}..., Bound Role ID: ${it.roleId}"
        }
        val rolesStr = roles.joinToString("\n") { 
            "- ID: ${it.id}, Name: ${it.name}, Scopes: ${it.permissions}, Description: ${it.description}"
        }
        return """
            You are a senior Application Security Architect and Authorization auditor.
            Assess the following API Key list and Role-Based Access Control (RBAC) bindings for vulnerabilities, configuration flaws, or bad practices.
            
            ### REGISTERED ROLES:
            $rolesStr
            
            ### REGISTERED API KEYS:
            $keysStr
            
            Provide a detailed security audit report compiled for a developer. Format the report cleanly using Markdown.
            Include the following exact sections with detailed, actionable content:
            1. **Overall Security Rating** (Determine a grade from A+ down to F, listing a short rationale).
            2. **Vulnerability Alerts** (Explicit check for checks like: Production keys mapping to administrator/wide scope rules, duplicate key prefixes, environment naming violations, orphan keys containing unassigned roles, inactive configuration warnings, or wildcard scope '*' abuse).
            3. **Aesthetic Policy Strengths** (Identify good structure: proper environment prefixes like "sk_prod_", restrictive specific scopes on viewer keys).
            4. **Actionable Mitigation Checklist** (A list of 3-4 bullet-point actions the developer should execute to optimize their posture).
            
            Ensure the response is structured professionally, concise, developer-centric, and polite. Avoid fluff.
        """.trimIndent()
    }

    private fun buildLocalFallbackAudit(keys: List<ApiKey>, roles: List<Role>, messageHeader: String): String {
        val alerts = mutableListOf<String>()
        val strengths = mutableListOf<String>()
        var score = 100

        // Rule 1: Wildcard Scope abuse Check
        val hasWildcardRole = roles.any { it.permissions.split(",").any { p -> p == "sys:all" || p == "*" } }
        if (hasWildcardRole) {
            alerts.add("⚠️ **Wildcard Scopes (`sys:all` or `*`) active**: One or more roles is granted super-admin credentials. In production environments, prefer granular narrow scopes (least-privilege model) rather than global system authority.")
            score -= 15
        } else {
            strengths.add("✅ **Granular Least Privilege**: Admirable! No wildcard root scopes detected; all role profiles are restricted to specific endpoint namespaces.")
        }

        // Rule 2: Orphaned key bindings check
        val roleIds = roles.map { it.id }.toSet()
        val orphanedKeys = keys.filter { it.roleId !in roleIds }
        if (orphanedKeys.isNotEmpty()) {
            alerts.add("🚨 **Orphaned API Credentials**: ${orphanedKeys.size} active keys are bound to non-existent Role IDs. Requests signed by these keys will prompt server crashes or authorization check rejects.")
            score -= 20
        }

        // Rule 3: Production/Development isolation breach
        val prodKeys = keys.filter { it.environment == "Production" }
        val devKeys = keys.filter { it.environment == "Development" }

        val prodAdminScopeLeak = prodKeys.any { k ->
            val r = roles.find { it.id == k.roleId }
            r?.permissions?.contains("keys:delete") == true || r?.permissions?.contains("sys:all") == true
        }

        if (prodAdminScopeLeak) {
            alerts.add("🚨 **Overprivileged PRODUCTION Keys**: Production keys are bound directly to root administration/deletion roles. A key leak on the client-side could expose entire databases.")
            score -= 25
        } else if (prodKeys.isNotEmpty()) {
            strengths.add("✅ **Production Guarding**: Active production integrations are appropriately bound to narrow non-destructive system scopes.")
        }

        // Rule 4: Key Formatting prefixes
        val illFormedKeys = keys.filter { key ->
            val isMatch = when(key.environment) {
                "Production" -> key.keyString.startsWith("sk_prod_")
                "Staging" -> key.keyString.startsWith("sk_staging_")
                else -> key.keyString.startsWith("sk_dev_")
            }
            !isMatch
        }
        if (illFormedKeys.isNotEmpty()) {
            alerts.add("⚠️ **Inconsistent Credential Formatting**: ${illFormedKeys.size} API Keys are formatted with incorrect environment prefixes. Standard security practices suggest strict visual namespaces (e.g. `sk_prod_...`) to easily run automatic scanners.")
            score -= 10
        } else {
            strengths.add("✅ **Standardized Naming Schema**: API key prefixes conform perfectly to industry conventions (`sk_prod_`, etc.), preventing developer cross-calling confusion.")
        }

        // Rule 5: Non-active system check
        val deactivatedKeys = keys.filter { !it.isActive }
        if (deactivatedKeys.isNotEmpty()) {
            strengths.add("✅ **Deactivation Capability**: ${deactivatedKeys.size} keys are deactivated, proving active security posture management is working.")
        }

        val rating = when {
            score >= 90 -> "A (Excellent Operational Posture)"
            score >= 80 -> "B (Secure with minor warnings)"
            score >= 70 -> "C (Action recommended: environment boundaries weak)"
            score >= 50 -> "D (Weak: severe credentials risk)"
            else -> "F (Critical Vulnerability Vector)"
        }

        val checklist = mutableListOf<String>()
        if (orphanedKeys.isNotEmpty()) {
            checklist.add("Associate the orphaned keys (${orphanedKeys.map { "'${it.name}'" }.joinToString()}) to an active role portfolio.")
        }
        if (prodAdminScopeLeak) {
            checklist.add("Demote Production administrative keys to narrow operational permissions.")
        }
        if (hasWildcardRole) {
            checklist.add("Refactor general wildcard roles to target granular scopes (e.g. `users:read`, `stripe:write`).")
        }
        if (checklist.isEmpty()) {
            checklist.add("Continue checking key creation logs weekly.")
            checklist.add("Implement rotating scopes for staging tokens.")
        }

        return """
            ### Security Hub: Vault Audit Completed
            *$messageHeader*
            
            #### 1. OVERALL SECURITY RATING: **$rating** (Score: $score/100)
            
            ---
            
            #### 2. VULNERABILITY ALERT VECTOR
            ${if (alerts.isEmpty()) "*No immediate vulnerabilities detected.*" else alerts.joinToString("\n\n")}
            
            ---
            
            #### 3. POLICY STRENGTH FACTORS
            ${if (strengths.isEmpty()) "*No measurable guardrails found. Please refine keys configurations.*" else strengths.joinToString("\n\n")}
            
            ---
            
            #### 4. ACTIONABLE MITIGATION CHECKLIST
            ${checklist.mapIndexed { idx, item -> "${idx + 1}. [ ] **Action**: $item" }.joinToString("\n")}
        """.trimIndent()
    }
}

class SecurityViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val repository = SecurityRepository(db.roleDao(), db.apiKeyDao(), db.securityEventDao())
            return SecurityViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
