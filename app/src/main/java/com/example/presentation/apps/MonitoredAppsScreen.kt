package com.example.presentation.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.permissions.PermissionUtils
import com.example.data.repository.InstalledAppInfo

@Composable
fun MonitoredAppsScreen(
    onStartPushupForApp: (String, Int, Int) -> Unit,
    viewModel: MonitoredAppsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedAppForEdit by remember { mutableStateOf<MonitoredAppWithUsage?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_monitored_app_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "MES APPLICATIONS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Définis des limites quotidiennes de temps d'écran pour tes applications.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!uiState.hasUsagePermission) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Permission Temps d'Écran requise",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Appuie pour ouvrir les réglages Android",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                TextButton(onClick = { PermissionUtils.openUsageStatsSettings(context) }) {
                                    Text("Activer")
                                }
                            }
                        }
                    }
                }

                if (uiState.monitoredApps.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucune application surveillée",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ajoute une première application installée sur ton téléphone pour contrôler son temps d'utilisation.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.openAddDialog() },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("add_app_screen_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ajouter une application")
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.monitoredApps) { appWithUsage ->
                        val entity = appWithUsage.entity
                        val used = appWithUsage.usageMinutesToday
                        val limit = entity.dailyLimitMinutes + entity.bonusMinutesUnlockedToday
                        val isLimitReached = used >= limit && limit > 0
                        val remaining = maxOf(0, limit - used)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLimitReached) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = appWithUsage.iconDrawable,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = entity.appName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = entity.packageName,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { selectedAppForEdit = appWithUsage }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer")
                                        }
                                        IconButton(onClick = { viewModel.removeApp(entity.packageName) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Usage Progress
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$used / $limit min",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isLimitReached) "LIMITE ATTEINTE" else "$remaining min restantes",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 1f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                if (isLimitReached) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "« Le bouton “encore 5 minutes” a été convoqué. 😂 »",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onStartPushupForApp(entity.packageName, 10, 15) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("10 POMPES → +15 MIN")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add App Dialog
    if (uiState.isAddDialogOpen) {
        AddAppModalDialog(
            installedApps = uiState.installedApps,
            searchQuery = uiState.searchQuery,
            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onDismiss = { viewModel.closeAddDialog() },
            onConfirmAdd = { appInfo, limitMinutes ->
                viewModel.addAppToMonitor(appInfo.packageName, appInfo.appName, limitMinutes)
            }
        )
    }

    // Edit App Dialog
    selectedAppForEdit?.let { appWithUsage ->
        EditAppLimitDialog(
            appName = appWithUsage.entity.appName,
            currentLimit = appWithUsage.entity.dailyLimitMinutes,
            onDismiss = { selectedAppForEdit = null },
            onConfirm = { newLimit ->
                viewModel.updateAppLimit(
                    appWithUsage.entity.packageName,
                    appWithUsage.entity.appName,
                    newLimit
                )
                selectedAppForEdit = null
            }
        )
    }
}

@Composable
private fun AddAppModalDialog(
    installedApps: List<InstalledAppInfo>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmAdd: (InstalledAppInfo, Int) -> Unit
) {
    var selectedApp by remember { mutableStateOf<InstalledAppInfo?>(null) }
    var limitMinutes by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajouter une application", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedApp == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Rechercher une application...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(installedApps) { app ->
                            Card(
                                onClick = { selectedApp = app },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    coil.compose.AsyncImage(
                                        model = app.iconDrawable,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(app.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Application: ${selectedApp?.appName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Limite quotidienne: $limitMinutes minutes")
                    Slider(
                        value = limitMinutes.toFloat(),
                        onValueChange = { limitMinutes = it.toInt() },
                        valueRange = 5f..180f,
                        steps = 34
                    )
                    TextButton(onClick = { selectedApp = null }) {
                        Text("← Choisir une autre application")
                    }
                }
            }
        },
        confirmButton = {
            if (selectedApp != null) {
                Button(onClick = {
                    selectedApp?.let { onConfirmAdd(it, limitMinutes) }
                }) {
                    Text("Ajouter la limite")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun EditAppLimitDialog(
    appName: String,
    currentLimit: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var limitMinutes by remember { mutableIntStateOf(currentLimit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Éditer la limite — $appName", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Nouvelle limite quotidienne: $limitMinutes min")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = limitMinutes.toFloat(),
                    onValueChange = { limitMinutes = it.toInt() },
                    valueRange = 5f..180f,
                    steps = 34
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(limitMinutes) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
