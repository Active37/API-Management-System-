package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.RetrofitGeminiClient
import com.example.data.model.ApiKey
import com.example.data.model.Role
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.SecurityViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    SecurityApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class ConsoleTab(val title: String, val icon: ImageVector) {
    KEYS("Vault", Icons.Default.Lock),
    ROLES("RBAC Roles", Icons.Default.Settings),
    SANDBOX("Auth Sandbox", Icons.Default.PlayArrow),
    SNIPPETS("SDK Snippets", Icons.Default.Build),
    AUDIT("AI Audit", Icons.Default.Warning)
}

@Composable
fun SecurityApp(
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val context = LocalContext.current
    val apiKeys by viewModel.apiKeys.collectAsStateWithLifecycle()
    val roles by viewModel.roles.collectAsStateWithLifecycle()
    val terminalOutput by viewModel.terminalOutput.collectAsStateWithLifecycle()
    val isAuditing by viewModel.isAuditing.collectAsStateWithLifecycle()
    val auditReport by viewModel.auditReport.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ConsoleTab.KEYS) }

    Column(
        modifier = modifier
            .background(ObsidianBg)
            .fillMaxSize()
    ) {
        // 1. Dashboard Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .border(width = 1.dp, color = BorderMuted, shape = RoundedCornerShape(0.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "KEYSAFE GATEWAY",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "RBAC Security Console v1.0",
                        color = NeonEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Connection Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalBlack)
                    .border(1.dp, BorderMuted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VAULT SECURED",
                    fontSize = 10.sp,
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Visual Graphic Header (Hero Banner)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Matrix Security Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay gradient for legibility and aesthetic blending
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ObsidianBg.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Tech stats overlaid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatChip(label = "ACTIVE KEYS", value = apiKeys.size.toString(), color = NeonCyan)
                StatChip(label = "ROLE PROFILES", value = roles.size.toString(), color = WarningOrange)
                StatChip(
                    label = "AI AUDIT STATUS",
                    value = if (RetrofitGeminiClient.isApiKeyAvailable()) "ONLINE" else "SANDBOX",
                    color = if (RetrofitGeminiClient.isApiKeyAvailable()) NeonEmerald else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Tab Workspace Navigation Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateSurface)
                .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConsoleTab.values().forEach { tab ->
                val active = currentTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { currentTab = tab }
                        .padding(vertical = 10.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (active) NeonCyan else TextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        color = if (active) TextWhite else TextMuted,
                        fontSize = 9.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Tiny glowing neon indicator dot
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(16.dp)
                            .clip(CircleShape)
                            .background(if (active) NeonCyan else Color.Transparent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Tab Workspace Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            when (currentTab) {
                ConsoleTab.KEYS -> KeysTabScreen(apiKeys = apiKeys, roles = roles, viewModel = viewModel, context = context)
                ConsoleTab.ROLES -> RolesTabScreen(roles = roles, viewModel = viewModel)
                ConsoleTab.SANDBOX -> SandboxTabScreen(apiKeys = apiKeys, roles = roles, terminalOutput = terminalOutput, viewModel = viewModel, context = context)
                ConsoleTab.SNIPPETS -> SnippetsTabScreen(apiKeys = apiKeys, roles = roles, context = context)
                ConsoleTab.AUDIT -> AuditTabScreen(auditReport = auditReport, isAuditing = isAuditing, viewModel = viewModel)
            }
        }
    }
}

// ==================== SUB-VIEWS & TAB SCREENS ====================

@Composable
fun StatChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TerminalBlack.copy(alpha = 0.7f))
            .border(1.dp, BorderMuted, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(text = value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun KeysTabScreen(
    apiKeys: List<ApiKey>,
    roles: List<Role>,
    viewModel: SecurityViewModel,
    context: Context
) {
    var showForm by remember { mutableStateOf(false) }
    var keyName by remember { mutableStateOf("") }
    var selectedEnv by remember { mutableStateOf("Development") }
    var selectedRoleId by remember { mutableStateOf<Int?>(null) }
    var expandedEnvDropdown by remember { mutableStateOf(false) }
    var expandedRoleDropdown by remember { mutableStateOf(false) }

    // Workbench Generator States
    var keyFormat by remember { mutableStateOf("base58") }
    var keyLength by remember { mutableStateOf(32) }
    var triggerRegenCounter by remember { mutableStateOf(0) }
    var generatedKey by remember { mutableStateOf("") }
    var generatorCopied by remember { mutableStateOf(false) }

    // Smooth regenerate preview key whenever formats or constraints update
    LaunchedEffect(selectedEnv, keyFormat, keyLength, triggerRegenCounter) {
        val prefix = when(selectedEnv.lowercase()) {
            "production" -> "sk_prod"
            "staging" -> "sk_staging"
            else -> "sk_dev"
        }
        generatedKey = com.example.data.util.KeyGeneratorUtils.assembleKey(prefix, keyLength, keyFormat)
    }

    LaunchedEffect(generatorCopied) {
        if (generatorCopied) {
            kotlinx.coroutines.delay(1800)
            generatorCopied = false
        }
    }

    // Bootstrap initial selected role
    LaunchedEffect(roles) {
        if (selectedRoleId == null && roles.isNotEmpty()) {
            selectedRoleId = roles.first().id
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Secure API Keys Vault",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) DangerCrimson else AccentBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .testTag("toggle_create_key_button")
                    .heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle key creation form"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showForm) "Cancel" else "Create Key", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Create API Key Form Drawer
        AnimatedVisibility(visible = showForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "GENERATE SYSTEM KEY",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Key Name
                    OutlinedTextField(
                        value = keyName,
                        onValueChange = { keyName = it },
                        label = { Text("App Integration Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("key_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Environment Trigger Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedEnv,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Environment", color = TextMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expandedEnvDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand env options",
                                    tint = NeonCyan,
                                    modifier = Modifier.clickable { expandedEnvDropdown = !expandedEnvDropdown }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("env_selector")
                                .clickable { expandedEnvDropdown = !expandedEnvDropdown }
                        )
                        DropdownMenu(
                            expanded = expandedEnvDropdown,
                            onDismissRequest = { expandedEnvDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(SlateSurface)
                                .border(1.dp, BorderMuted, RoundedCornerShape(4.dp))
                        ) {
                            listOf("Development", "Staging", "Production").forEach { env ->
                                DropdownMenuItem(
                                    text = { Text(text = env, color = TextWhite) },
                                    onClick = {
                                        selectedEnv = env
                                        expandedEnvDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bind Role Assignment Dropdown
                    if (roles.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val activeRoleName = roles.find { it.id == selectedRoleId }?.name ?: "Select role..."
                            OutlinedTextField(
                                value = activeRoleName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Associated RBAC Role", color = TextMuted) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (expandedRoleDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand role options",
                                        tint = NeonCyan,
                                        modifier = Modifier.clickable { expandedRoleDropdown = !expandedRoleDropdown }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = BorderMuted,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("role_association_selector")
                                    .clickable { expandedRoleDropdown = !expandedRoleDropdown }
                            )
                            DropdownMenu(
                                expanded = expandedRoleDropdown,
                                onDismissRequest = { expandedRoleDropdown = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(SlateSurface)
                                    .border(1.dp, BorderMuted, RoundedCornerShape(4.dp))
                            ) {
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(text = "${role.name} (${role.permissions})", color = TextWhite) },
                                        onClick = {
                                            selectedRoleId = role.id
                                            expandedRoleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "⚠️ No Roles configured. Go to the 'RBAC Roles' tab first to create a secure permission group before issuing keys.",
                            color = WarningOrange,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- VISUAL COPIABLE SECURE CRYPTO KEY WORKBENCH ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = TerminalBlack)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Generator Icon",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE SECURE GENERATOR",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }

                                // Interactive refresh trigger
                                Text(
                                    text = "REGEN ⟳",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clickable { triggerRegenCounter++ }
                                        .testTag("regenerate_key_button")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Config Row 1: Entropy Character Format
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FORMAT PROFILE:",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("base58", "hex", "base64", "alphanumeric").forEach { fmt ->
                                        val isSelected = keyFormat == fmt
                                        val label = when(fmt) {
                                            "base58" -> "B58"
                                            "hex" -> "HEX"
                                            "base64" -> "B64"
                                            else -> "ALPHA"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) NeonCyan else BorderMuted,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable { keyFormat = fmt }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) NeonCyan else TextMuted,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Config Row 2: Length Config
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "KEY STRENGTH (LENGTH):",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(16, 24, 32, 48, 64).forEach { len ->
                                        val isSelected = keyLength == len
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) NeonCyan else BorderMuted,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable { keyLength = len }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = len.toString(),
                                                color = if (isSelected) NeonCyan else TextMuted,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Terminal style preview with visual clip copy status
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SlateSurface)
                                    .border(1.dp, BorderMuted, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = generatedKey,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Dynamic Visual Copy Button with state feedback
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Secure API Key", generatedKey)
                                        clipboard.setPrimaryClip(clip)
                                        generatorCopied = true
                                        Toast.makeText(context, "Secure API Key copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (generatorCopied) NeonEmerald else BorderMuted
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("copy_generated_key_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (generatorCopied) Icons.Default.Check else Icons.Default.Share,
                                            contentDescription = "Copy Status Icon",
                                            tint = if (generatorCopied) TerminalBlack else NeonCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (generatorCopied) "COPIED ✓" else "COPY",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (generatorCopied) TerminalBlack else TextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Submit Generation Action Trigger
                    Button(
                        onClick = {
                            if (keyName.isBlank()) {
                                Toast.makeText(context, "Please enter an integration name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val roleIdToAssign = selectedRoleId
                            if (roleIdToAssign == null) {
                                Toast.makeText(context, "An active role assignment is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.addApiKey(keyName.trim(), selectedEnv, roleIdToAssign, generatedKey)
                            keyName = ""
                            showForm = false
                            Toast.makeText(context, "API Key successfully generated and encrypted!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_generate_key_button")
                            .heightIn(min = 48.dp)
                    ) {
                        Text(text = "GENERATE CRYPTOGRAPHIC KEY", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Keys List
        if (apiKeys.isEmpty()) {
            EmptyListPlaceholder(
                icon = Icons.Default.Lock,
                title = "Crypto Vault Empty",
                description = "No API keys created in the system database. Tap 'Create Key' to issue your first authenticated token."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(apiKeys) { apiKey ->
                    val boundRole = roles.find { it.id == apiKey.roleId }
                    ApiKeyCard(
                        apiKey = apiKey,
                        role = boundRole,
                        onToggle = { viewModel.toggleKeyStatus(apiKey) },
                        onDelete = { viewModel.deleteApiKey(apiKey) },
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun ApiKeyCard(
    apiKey: ApiKey,
    role: Role?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    context: Context
) {
    var revealed by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(1500)
            isCopied = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderMuted, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name, Environment Stamp, Active Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = apiKey.name,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val envColor = when (apiKey.environment.lowercase()) {
                            "production" -> DangerCrimson
                            "staging" -> WarningOrange
                            else -> NeonCyan
                        }
                        // Env Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(envColor.copy(alpha = 0.15f))
                                .border(1.dp, envColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = apiKey.environment.uppercase(),
                                color = envColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Role binder tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderMuted)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = role?.name ?: "ORPHANED ROLE",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Active toggle switch
                Switch(
                    checked = apiKey.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonEmerald,
                        checkedTrackColor = NeonEmerald.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderMuted
                    ),
                    modifier = Modifier.testTag("toggle_key_${apiKey.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Display / Clipboard copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalBlack)
                    .border(1.dp, BorderMuted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val keyDisplay = if (revealed) apiKey.keyString else {
                    val prefix = apiKey.keyString.take(apiKey.keyString.indexOf("_") + 6)
                    "$prefix••••••••••••••••"
                }

                Text(
                    text = keyDisplay,
                    color = if (apiKey.isActive) NeonCyan else TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.0f)
                )

                Row {
                    // Vision eye toggle
                    IconButton(
                        onClick = { revealed = !revealed },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Default.Lock else Icons.Default.Lock, // Lock serves as blind/reveal alternative in standard core icons
                            contentDescription = "Toggle blind reveal",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("API Key Vault", apiKey.keyString)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(context, "API Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("copy_key_button_${apiKey.id}")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.Share,
                            contentDescription = "Copy key to clipboard status",
                            tint = if (isCopied) NeonEmerald else NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Permissions scope list associated
            if (role != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Permissions: ", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    role.permissions.split(",").forEach { scope ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BorderMuted.copy(alpha = 0.5f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = scope, fontSize = 8.sp, color = NeonEmerald, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Delete key item action
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_key_button_${apiKey.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete key credentials",
                        tint = DangerCrimson.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RolesTabScreen(
    roles: List<Role>,
    viewModel: SecurityViewModel
) {
    val context = LocalContext.current
    var showForm by remember { mutableStateOf(false) }
    var roleName by remember { mutableStateOf("") }
    var roleDesc by remember { mutableStateOf("") }
    var roleScopes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RBAC Custom Role Profiles",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) DangerCrimson else AccentBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .testTag("toggle_create_role_button")
                    .heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle role creation form"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showForm) "Cancel" else "Add Role", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Create Role Form Card
        AnimatedVisibility(visible = showForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "NEW RBAC PROFILE",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Name
                    OutlinedTextField(
                        value = roleName,
                        onValueChange = { roleName = it },
                        label = { Text("Role Label (e.g. Invoicing Agent)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("role_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Description
                    OutlinedTextField(
                        value = roleDesc,
                        onValueChange = { roleDesc = it },
                        label = { Text("Profile Description / Purpose", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("role_desc_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role scopes
                    OutlinedTextField(
                        value = roleScopes,
                        onValueChange = { roleScopes = it },
                        label = { Text("Authorization Scopes (comma separated)", color = TextMuted) },
                        placeholder = { Text("e.g., api:read, metrics:view, billing:manage", color = BorderMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("role_scopes_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pro-tip: Use wildcard 'sys:all' to grant absolute workspace credential access.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Submit Role Generation Action
                    Button(
                        onClick = {
                            if (roleName.isBlank()) {
                                Toast.makeText(context, "Role label is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (roleScopes.isBlank()) {
                                Toast.makeText(context, "At least one authorization scope is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.addRole(roleName.trim(), roleDesc.trim(), roleScopes.trim())
                            roleName = ""
                            roleDesc = ""
                            roleScopes = ""
                            showForm = false
                            Toast.makeText(context, "RBAC Role registered successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_generate_role_button")
                            .heightIn(min = 48.dp)
                    ) {
                        Text(text = "REGISTER INTEGRATION ROLE", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Roles List
        if (roles.isEmpty()) {
            EmptyListPlaceholder(
                icon = Icons.Default.Settings,
                title = "No Roles Configured",
                description = "RBAC database mappings are empty. Please register custom role parameters above."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(roles) { role ->
                    RoleCard(role = role, onDelete = { viewModel.deleteRole(role) })
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    role: Role,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderMuted, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = role.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Delete button (Standard roles cannot be deleted or let's allow deleting everything for absolute freedom)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_role_button_${role.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Role Profile",
                        tint = DangerCrimson.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (role.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = role.description,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges for permissions
            Text(
                text = "ALLOWED SECURITY SCOPES:",
                fontSize = 9.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Multi-scoped wrap layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                role.permissions.split(",").forEach { scope ->
                    val color = if (scope == "sys:all" || scope == "*") DangerCrimson else NeonCyan
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.15f))
                            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = scope,
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SandboxTabScreen(
    apiKeys: List<ApiKey>,
    roles: List<Role>,
    terminalOutput: String?,
    viewModel: SecurityViewModel,
    context: Context
) {
    var keyInput by remember { mutableStateOf("") }
    var scopeInput by remember { mutableStateOf("api:read") }
    var expandedKeyDropdown by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Smooth scroll down console as outputs compile
    LaunchedEffect(terminalOutput) {
        if (!terminalOutput.isNullOrEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Cryptographic Auth Sandbox",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Input a key and query a permission scope to simulate server-side authorization checks.",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selection dashboard controllers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                
                // Header tool tips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEST CONTROLLER LOGS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Helper shortcut to auto-load an active key
                    if (apiKeys.isNotEmpty()) {
                        Box {
                            Text(
                                text = "Load Key ▾",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { expandedKeyDropdown = !expandedKeyDropdown }
                                    .testTag("shortcut_load_key"),
                                fontFamily = FontFamily.Monospace
                            )
                            DropdownMenu(
                                expanded = expandedKeyDropdown,
                                onDismissRequest = { expandedKeyDropdown = false },
                                modifier = Modifier
                                    .background(SlateSurface)
                                    .border(1.dp, BorderMuted, RoundedCornerShape(4.dp))
                            ) {
                                apiKeys.forEach { key ->
                                    DropdownMenuItem(
                                        text = { Text("${key.name} (${key.environment})", color = TextWhite) },
                                        onClick = {
                                            keyInput = key.keyString
                                            expandedKeyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Credentials Input text field
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Authentication Key (sk_xxx_...)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sandbox_key_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target permission scope to check
                OutlinedTextField(
                    value = scopeInput,
                    onValueChange = { scopeInput = it },
                    label = { Text("Target Verification Scope (e.g. api:write)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sandbox_scope_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Fire Trigger Auth Check
                Button(
                    onClick = {
                        viewModel.verifyKeyScope(keyInput, scopeInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_auth_check_button")
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Execute Auth Check")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "EXECUTE AUTHORIZATION TRACE", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Monospace terminal emulation screen
        Text(
            text = "DEB_TRACE TERMINAL OUT",
            fontSize = 11.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
                .border(2.dp, BorderMuted, RoundedCornerShape(6.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalBlack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (terminalOutput.isNullOrEmpty()) {
                    // Inactive idle display
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Console waiting indicator",
                            tint = BorderMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "terminal idle. await security validation signal...",
                            color = BorderMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    // Terminal code lines formatted
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUTH_CONSOLE_LOGS Active Session",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Share, // Copy surrogate
                                contentDescription = "Copy terminal outputs",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Authorization Trace", terminalOutput)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Authorization log copied!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                        Divider(color = BorderMuted, modifier = Modifier.padding(vertical = 8.dp))

                        // Render outputs custom parsing syntax color
                        terminalOutput.split("\n").forEach { line ->
                            val color = when {
                                line.contains("[AUTHORIZATION GRANTED]") || line.contains("200 OK") || line.contains("ACCESS_ALLOWED") -> NeonEmerald
                                line.contains("[SECURITY EXCEPTION]") || line.contains("401 Unauthorized") || line.contains("403 Forbidden") || line.contains("ACCESS_DENIED") || line.contains("[RBAC ACCESS DENIED]") -> DangerCrimson
                                line.contains("[ERROR]") || line.contains("[SERVER ERROR]") -> WarningOrange
                                else -> TextWhite
                            }
                            Text(
                                text = line,
                                color = color,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SnippetsTabScreen(
    apiKeys: List<ApiKey>,
    roles: List<Role>,
    context: Context
) {
    var selectedKeyId by remember { mutableStateOf<Int?>(null) }
    var selectedLanguage by remember { mutableStateOf("cURL") }
    var reqScopeInput by remember { mutableStateOf("api:read") }

    // Init keys selector
    LaunchedEffect(apiKeys) {
        if (selectedKeyId == null && apiKeys.isNotEmpty()) {
            selectedKeyId = apiKeys.first().id
        }
    }

    val activeKey = apiKeys.find { it.id == selectedKeyId }
    val boundRole = activeKey?.constrainRole(roles)

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Multi-Language Integration SDK",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Generate production-grade key-checking gateway middleware snippets crafted for your specific credential.",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target credentials selection card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {

                // Selection trigger dropdown for target key
                if (apiKeys.isNotEmpty()) {
                    var expandedDropdown by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Target API Credentials:", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Box {
                            Text(
                                text = "${activeKey?.name ?: "Select key..."} ▾",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { expandedDropdown = !expandedDropdown }
                                    .testTag("snippet_key_selector_trigger"),
                                fontFamily = FontFamily.Monospace
                            )
                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false },
                                modifier = Modifier
                                    .background(SlateSurface)
                                    .border(1.dp, BorderMuted, RoundedCornerShape(4.dp))
                            ) {
                                apiKeys.forEach { key ->
                                    DropdownMenuItem(
                                        text = { Text("${key.name} (${key.environment})", color = TextWhite) },
                                        onClick = {
                                            selectedKeyId = key.id
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(text = "⚠️ Please generate an API Key first under the Vault tab.", color = WarningOrange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target checks scope input
                OutlinedTextField(
                    value = reqScopeInput,
                    onValueChange = { reqScopeInput = it },
                    label = { Text("Required Permission Rule check", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snippet_scope_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Lanugage selection chips
                Text(text = "Select Server Framework:", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("cURL", "Node.js", "Python", "Kotlin").forEach { lang ->
                        val selected = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) AccentBlue else TerminalBlack)
                                .border(1.dp, if (selected) NeonCyan else BorderMuted, RoundedCornerShape(6.dp))
                                .clickable { selectedLanguage = lang }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("snippet_lang_${lang.lowercase()}")
                        ) {
                            Text(
                                text = lang,
                                color = if (selected) TextWhite else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Render code snippet card
        val selectedKeyString = activeKey?.keyString ?: "sk_prod_xxxxxxxxxxxxxx"
        val activeEnv = activeKey?.environment ?: "Production"
        val verifiedScope = reqScopeInput.ifBlank { "api:read" }

        val snippet = when (selectedLanguage) {
            "Node.js" -> """
                // --- Node.js Express Gateway Auth Middleware ---
                const express = require('express');
                const { SecurityGateway } = require('@keysafe/node-sdk');
                const app = express();

                // Initialize Gateway credentials
                const client = SecurityGateway.connect({
                  apiKey: "$selectedKeyString", 
                  environment: "$activeEnv"
                });

                // Secure route utilizing role scope check
                app.get('/v1/data', client.requirePermission("$verifiedScope"), (req, res) => {
                  res.json({
                    status: "authorized",
                    clientRoleId: req.securityContext.roleId,
                    trace: "SHA256_VERIFIED_OK"
                  });
                });
            """.trimIndent()

            "Python" -> """
                # --- FastAPI RBAC Token Validator ---
                from fastapi import FastAPI, Depends, HTTPException
                from keysafe_auth import GatewayValidator, require_scopes

                app = FastAPI()

                # Wire database credentials
                gateway = GatewayValidator(
                    api_key="$selectedKeyString",
                    environment="$activeEnv"
                )

                @app.get("/v1/data")
                @require_scopes(["$verifiedScope"])
                def get_secured_payload():
                    return {
                        "status": "success",
                        "auth_source": "keysafe_encryption_valve",
                        "scope_verified": "$verifiedScope"
                    }
            """.trimIndent()

            "Kotlin" -> """
                // --- Kotlin Ktor Server Gateway Plugin ---
                import io.ktor.server.application.*
                import io.ktor.server.response.*
                import io.ktor.server.routing.*
                import keysafe.gateway.client.CredentialClient
                import keysafe.gateway.compose.ValidateScope

                val security = CredentialClient {
                    apiKey = "$selectedKeyString"
                    environment = "$activeEnv"
                }

                fun Application.configureRouting() {
                    routing {
                        route("/v1/data") {
                            install(ValidateScope) {
                                requiredPermission = "$verifiedScope"
                            }
                            get {
                                call.respond(mapOf("status" to "authenticated"))
                            }
                        }
                    }
                }
            """.trimIndent()

            else -> """
                # --- Direct REST API Request Audit cURL ---
                curl -X GET "https://api.keysafe.gateway/v1/authorize" \
                  -H "Authorization: Bearer $selectedKeyString" \
                  -H "X-Required-Scope: $verifiedScope" \
                  -H "Content-Type: application/json"
            """.trimIndent()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMPILED SDK TEMPLATE",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 2.dp)
            )

            Text(
                text = "Copy Snippet",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SDK Integration", snippet)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SDK Snippet copied!", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("toggle_snippet_copy_button"),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 20.dp)
                .border(1.dp, BorderMuted, RoundedCornerShape(6.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalBlack)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                item {
                    Text(
                        text = snippet,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AuditTabScreen(
    auditReport: String?,
    isAuditing: Boolean,
    viewModel: SecurityViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Compliance Auditor",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Trigger audit
            Button(
                onClick = { viewModel.runAudit() },
                enabled = !isAuditing,
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .testTag("run_security_audit_button")
                    .heightIn(min = 48.dp)
            ) {
                if (isAuditing) {
                    CircularProgressIndicator(color = TerminalBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Scanning...", color = TerminalBlack)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Run secure scans", tint = TerminalBlack)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Run Audit", color = TerminalBlack, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (auditReport.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateSurface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Shield Guard awaiting scans",
                        tint = WarningOrange,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vulnerability Assessment Sandbox",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Trigger a security scan to evaluate credentials strength, look for structural environment leaks, wildcard abuses, or unassigned roles validation.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Render audit findings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AI SECURITY POSTURE REPORT",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Markdown style text parsing
                    auditReport.split("\n").forEach { line ->
                        val trimmed = line.trim()
                        val textStyle = when {
                            trimmed.startsWith("###") -> MaterialTheme.typography.titleMedium.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                            trimmed.startsWith("####") -> MaterialTheme.typography.titleSmall.copy(color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            trimmed.startsWith("**") || trimmed.contains("**") -> MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.Bold)
                            else -> MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        }

                        // Colors highlight alerts
                        val blockColor = when {
                            trimmed.contains("🚨") || trimmed.contains("VULNERBILITY") -> DangerCrimson
                            trimmed.contains("⚠️") || trimmed.contains("Warning") -> WarningOrange
                            trimmed.contains("✅") || trimmed.contains("Admirable") -> NeonEmerald
                            else -> Color.Unspecified
                        }

                        val finalStyle = if (blockColor != Color.Unspecified) textStyle.copy(color = blockColor) else textStyle

                        Text(
                            text = line,
                            modifier = Modifier.padding(vertical = 3.dp),
                            style = finalStyle
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = BorderMuted)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive check lists they can interact with
                    Text(text = "INTERACTIVE HARDENING CHECKLIST:", fontSize = 10.sp, color = TextWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    HardeningCheckItem(text = "Set strict environment key prefixes ('sk_prod_')")
                    HardeningCheckItem(text = "Eliminate unassigned keys and dangling role ID links")
                    HardeningCheckItem(text = "Replace general wildcards ('sys:all') with granular endpoints")
                    HardeningCheckItem(text = "Configure regular compliance rotation alerts")
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun HardeningCheckItem(text: String) {
    var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { checked = !checked }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.Check, // Fallbacks in core
            contentDescription = "Check state",
            tint = if (checked) NeonEmerald else BorderMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = if (checked) TextMuted else TextWhite,
            fontSize = 12.sp,
            textDecoration = if (checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun EmptyListPlaceholder(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = BorderMuted,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// --- HELPER EXTENSIONS FOR BUSINESS ENTITY MAPPINGS ---
private fun ApiKey.constrainRole(roles: List<Role>): Role? {
    return roles.find { it.id == this.roleId }
}
