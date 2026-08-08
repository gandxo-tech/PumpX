package com.example.presentation.share

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.stats.StatsViewModel
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ShareFormat {
    STORY_9_16, POST_4_5, SQUARE_1_1
}

enum class ShareStyle {
    MINIMAL, PERFORMANCE, FUN
}

@Composable
fun ShareStatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var format by remember { mutableStateOf(ShareFormat.STORY_9_16) }
    var style by remember { mutableStateOf(ShareStyle.PERFORMANCE) }

    var showPushups by remember { mutableStateOf(true) }
    var showScreenTime by remember { mutableStateOf(true) }
    var showBonusTime by remember { mutableStateOf(true) }
    var showApps by remember { mutableStateOf(false) }

    val aspectRatio = when (format) {
        ShareFormat.STORY_9_16 -> 9f / 16f
        ShareFormat.POST_4_5 -> 4f / 5f
        ShareFormat.SQUARE_1_1 -> 1f
    }
    
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    fun triggerSharesheet() {
        coroutineScope.launch {
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            val uri = withContext(Dispatchers.IO) {
                val imagePath = File(context.cacheDir, "images")
                imagePath.mkdirs()
                val file = File(imagePath, "shared_image.png")
                val stream = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }

            val shareText = buildString {
                append("🔥 J'ai repoussé mes limites avec PumpX cette semaine ! Voici mes résultats :\n\n")
                if (showPushups) append("💪 ${uiState.totalPushups} pompes effectuées pour mériter mon temps d'écran.\n")
                if (showBonusTime) append("⚡ +${uiState.totalBonusMinutes} minutes de divertissement débloquées à la sueur de mon front.\n")
                if (showApps) append("📱 ${uiState.activeDaysCount} jours consécutifs d'entraînement, la discipline paie !\n")
                append("\nRejoins l'aventure et transforme tes limites de temps d'écran en force physique avec PumpX ! 🚀")
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Partager mes statistiques PumpX")
            context.startActivity(shareIntent)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PARTAGER MES STATS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Card Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                            .background(
                                when (style) {
                                    ShareStyle.MINIMAL -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
                                    ShareStyle.PERFORMANCE -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81)))
                                    ShareStyle.FUN -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                                }
                            )
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                                .padding(32.dp)
                        ) {
                            Text(
                                text = "PUMPX",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SCREEN TIME × MOVEMENT",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            
                            Spacer(modifier = Modifier.height(36.dp))

                            if (showPushups) {
                                Text(
                                    text = "${uiState.totalPushups}",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    lineHeight = 56.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "POMPES CETTE SEMAINE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF3B82F6),
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (showBonusTime) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "+${uiState.totalBonusMinutes} MIN DÉBLOQUÉES",
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF10B981),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (showApps) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${uiState.activeDaysCount} JOURS ACTIFS",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            if (style == ShareStyle.FUN) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = "« ${uiState.dynamicHook} »",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }

            // Formats Selector
            item {
                Text("Format", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = format == ShareFormat.STORY_9_16,
                        onClick = { format = ShareFormat.STORY_9_16 },
                        label = { Text("9:16 Story") }
                    )
                    FilterChip(
                        selected = format == ShareFormat.POST_4_5,
                        onClick = { format = ShareFormat.POST_4_5 },
                        label = { Text("4:5 Post") }
                    )
                    FilterChip(
                        selected = format == ShareFormat.SQUARE_1_1,
                        onClick = { format = ShareFormat.SQUARE_1_1 },
                        label = { Text("1:1 Carré") }
                    )
                }
            }

            // Styles Selector
            item {
                Text("Style", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = style == ShareStyle.MINIMAL,
                        onClick = { style = ShareStyle.MINIMAL },
                        label = { Text("MINIMAL") }
                    )
                    FilterChip(
                        selected = style == ShareStyle.PERFORMANCE,
                        onClick = { style = ShareStyle.PERFORMANCE },
                        label = { Text("PERFORMANCE") }
                    )
                    FilterChip(
                        selected = style == ShareStyle.FUN,
                        onClick = { style = ShareStyle.FUN },
                        label = { Text("FUN") }
                    )
                }
            }

            // Checkboxes Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Contrôle du contenu", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showPushups, onCheckedChange = { showPushups = it })
                            Text("Pompes")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showBonusTime, onCheckedChange = { showBonusTime = it })
                            Text("Temps débloqué")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showApps, onCheckedChange = { showApps = it })
                            Text("Jours actifs & statistiques")
                        }
                    }
                }
            }

            // Share Action
            item {
                Button(
                    onClick = { triggerSharesheet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("trigger_sharesheet_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partager mes stats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
