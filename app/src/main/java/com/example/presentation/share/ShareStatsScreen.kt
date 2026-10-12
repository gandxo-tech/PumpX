package com.example.presentation.share

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.stats.StatsViewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ShareFormat(val label: String, val ratio: Float) {
    STORY_9_16("Story 9:16", 9f / 16f),
    POST_4_5("Post 4:5", 4f / 5f),
    SQUARE_1_1("Carré 1:1", 1f),
    LANDSCAPE_16_9("Paysage 16:9", 16f / 9f)
}

enum class CardTheme(val label: String) {
    NEON_DARK("PumpX Neon"),
    FIRE_RED("Feu & Passion"),
    CYBER_BLUE("Cyber Bleu"),
    EMERALD_BEAST("Mode Bête"),
    SUNSET_GLOW("Crépuscule"),
    TITANIUM_LIGHT("Titane Épuré")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareStatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Interactive State Variables (Editable!)
    var username by remember { mutableStateOf("@pumpx_warrior") }
    var targetAppName by remember { mutableStateOf("TikTok") }
    var pushupCount by remember { mutableStateOf("50") }
    var bonusTimeText by remember { mutableStateOf("+30 MIN") }
    var streakDaysText by remember { mutableStateOf("14 JOURS") }
    var punchlineText by remember { mutableStateOf("« Pas de scroll sans sueur ! »") }

    var format by remember { mutableStateOf(ShareFormat.STORY_9_16) }
    var cardTheme by remember { mutableStateOf(CardTheme.NEON_DARK) }

    var showLogo by remember { mutableStateOf(true) }
    var showBadge by remember { mutableStateOf(true) }
    var showStreak by remember { mutableStateOf(true) }
    var showPunchline by remember { mutableStateOf(true) }

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Presets, 1: Textes, 2: Chiffres, 3: Style

    // Auto-fill real data when loaded
    LaunchedEffect(uiState) {
        if (uiState.totalPushups > 0) {
            pushupCount = uiState.totalPushups.toString()
            bonusTimeText = "+${uiState.totalBonusMinutes} MIN"
            streakDaysText = "${uiState.activeDaysCount} JOURS"
            if (uiState.dynamicHook.isNotBlank()) {
                punchlineText = "« ${uiState.dynamicHook} »"
            }
        }
    }

    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    val generatedCaption = buildString {
        append("🔥 Challenge PumpX réussi ! $pushupCount pompes effectuées pour débloquer $bonusTimeText sur $targetAppName !\n\n")
        if (showStreak) append("⚡ Série actuelle : $streakDaysText d'affilée.\n")
        append("💪 $punchlineText\n\n")
        append("Rejoins l'aventure et mérite ton temps d'écran ! 🚀\n#PumpX #FitnessChallenge #Discipline #PumpXApp")
    }

    fun triggerSharesheet() {
        coroutineScope.launch {
            try {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                val uri = withContext(Dispatchers.IO) {
                    val imagePath = File(context.cacheDir, "images")
                    imagePath.mkdirs()
                    val file = File(imagePath, "pumpx_post_card.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, generatedCaption)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val shareIntent = Intent.createChooser(sendIntent, "Partager mon post PumpX")
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erreur lors de la création du visuel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 12.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "STUDIO POSTS RÉSEAUX",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ajuste et personnalise ton visuel à 100%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: LIVE CARD PREVIEW (GRAPHICS LAYER)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(format.ratio)
                            .background(
                                when (cardTheme) {
                                    CardTheme.NEON_DARK -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)))
                                    CardTheme.FIRE_RED -> Brush.verticalGradient(listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF18181B)))
                                    CardTheme.CYBER_BLUE -> Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0F172A), Color(0xFF0369A1)))
                                    CardTheme.EMERALD_BEAST -> Brush.verticalGradient(listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF09090B)))
                                    CardTheme.SUNSET_GLOW -> Brush.verticalGradient(listOf(Color(0xFF581C87), Color(0xFF831843), Color(0xFF18181B)))
                                    CardTheme.TITANIUM_LIGHT -> Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                                }
                            )
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val textColor = if (cardTheme == CardTheme.TITANIUM_LIGHT) Color(0xFF0F172A) else Color.White
                        val subTextColor = if (cardTheme == CardTheme.TITANIUM_LIGHT) Color(0xFF475569) else Color.LightGray
                        val accentColor = when (cardTheme) {
                            CardTheme.NEON_DARK -> Color(0xFFFF5722)
                            CardTheme.FIRE_RED -> Color(0xFFEF4444)
                            CardTheme.CYBER_BLUE -> Color(0xFF38BDF8)
                            CardTheme.EMERALD_BEAST -> Color(0xFF10B981)
                            CardTheme.SUNSET_GLOW -> Color(0xFFEC4899)
                            CardTheme.TITANIUM_LIGHT -> Color(0xFFFF5722)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (cardTheme == CardTheme.TITANIUM_LIGHT) Color.White.copy(alpha = 0.85f)
                                    else Color.Black.copy(alpha = 0.45f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (cardTheme == CardTheme.TITANIUM_LIGHT) Color.Black.copy(alpha = 0.1f)
                                    else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            // Header Username + Verified Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = username.ifBlank { "@pumpx_user" },
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (showBadge) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Brand Title
                            if (showLogo) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PUMPX",
                                        color = textColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 3.sp
                                    )
                                }
                                Text(
                                    text = "DISCIPLINE × $targetAppName",
                                    color = subTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Huge Pushup Count
                            Text(
                                text = pushupCount.ifBlank { "0" },
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                lineHeight = 48.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "POMPES EFFECTUÉES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bonus Time Badge
                            Box(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$bonusTimeText DÉBLOQUÉS",
                                        fontWeight = FontWeight.Black,
                                        color = accentColor,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showStreak) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SÉRIE : $streakDaysText",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (showPunchline && punchlineText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = punchlineText,
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: EDITING TABS & PANELS
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Modèles", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Textes", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Chiffres", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        text = { Text("Style", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // TAB 0: PRESETS (QUICK MODEL SELECTOR)
            if (selectedTabIndex == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Sélectionne un modèle prêt à l'emploi :", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            // Preset 1: Données Réelles
                            OutlinedButton(
                                onClick = {
                                    pushupCount = uiState.totalPushups.takeIf { it > 0 }?.toString() ?: "30"
                                    bonusTimeText = "+${uiState.totalBonusMinutes.takeIf { it > 0 } ?: 20} MIN"
                                    streakDaysText = "${uiState.activeDaysCount.takeIf { it > 0 } ?: 7} JOURS"
                                    targetAppName = "PumpX"
                                    punchlineText = "« Mon bilan de la semaine ! »"
                                    cardTheme = CardTheme.NEON_DARK
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("📊 Utiliser mes vraies données")
                                }
                            }

                            // Preset 2: TikTok Challenge
                            OutlinedButton(
                                onClick = {
                                    username = "@pumpx_warrior"
                                    targetAppName = "TikTok"
                                    pushupCount = "50"
                                    bonusTimeText = "+30 MIN"
                                    streakDaysText = "10 JOURS"
                                    punchlineText = "« 10 pompes = 15 min de scroll ! »"
                                    cardTheme = CardTheme.CYBER_BLUE
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🎵 TikTok Challenge (50 pompes = +30 min)")
                            }

                            // Preset 3: Mode Guerrier
                            OutlinedButton(
                                onClick = {
                                    username = "@bête_de_routine"
                                    targetAppName = "Instagram"
                                    pushupCount = "100"
                                    bonusTimeText = "+60 MIN"
                                    streakDaysText = "21 JOURS"
                                    punchlineText = "« Je ne scroll plus gratuitement. »"
                                    cardTheme = CardTheme.FIRE_RED
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🔥 Mode Guerrier (100 pompes)")
                            }

                            // Preset 4: Mode Bête Émeraude
                            OutlinedButton(
                                onClick = {
                                    username = "@discipline_100"
                                    targetAppName = "Réseaux"
                                    pushupCount = "200"
                                    bonusTimeText = "+2 HEURES"
                                    streakDaysText = "30 JOURS"
                                    punchlineText = "« Transforme ton temps d'écran en muscles ! »"
                                    cardTheme = CardTheme.EMERALD_BEAST
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🌿 Challenge 30 Jours (200 pompes)")
                            }
                        }
                    }
                }
            }

            // TAB 1: TEXTS & PSEUDO
            if (selectedTabIndex == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Personnalise les textes :", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Pseudo / Handle (@)") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = targetAppName,
                                onValueChange = { targetAppName = it },
                                label = { Text("Application Ciblée") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = punchlineText,
                                onValueChange = { punchlineText = it },
                                label = { Text("Citation / Accroche") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // TAB 2: NUMBERS & STATS
            if (selectedTabIndex == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Ajuste les chiffres :", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            OutlinedTextField(
                                value = pushupCount,
                                onValueChange = { pushupCount = it },
                                label = { Text("Nombre de pompes") },
                                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Quick pushup adjusters
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = false, onClick = { pushupCount = "20" }, label = { Text("20") })
                                FilterChip(selected = false, onClick = { pushupCount = "50" }, label = { Text("50") })
                                FilterChip(selected = false, onClick = { pushupCount = "100" }, label = { Text("100") })
                                FilterChip(selected = false, onClick = { pushupCount = "250" }, label = { Text("250") })
                            }

                            OutlinedTextField(
                                value = bonusTimeText,
                                onValueChange = { bonusTimeText = it },
                                label = { Text("Temps débloqué (ex: +30 MIN)") },
                                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = streakDaysText,
                                onValueChange = { streakDaysText = it },
                                label = { Text("Série / Streak (ex: 14 JOURS)") },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // TAB 3: STYLE, FORMATS & THEMES
            if (selectedTabIndex == 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Format du Visuel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShareFormat.values().forEach { fmt ->
                                    FilterChip(
                                        selected = format == fmt,
                                        onClick = { format = fmt },
                                        label = { Text(fmt.label) }
                                    )
                                }
                            }

                            Divider()

                            Text("Thème & Couleurs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CardTheme.values().forEach { theme ->
                                    FilterChip(
                                        selected = cardTheme == theme,
                                        onClick = { cardTheme = theme },
                                        label = { Text(theme.label) }
                                    )
                                }
                            }

                            Divider()

                            Text("Éléments affichés", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Afficher le logo PumpX")
                                Switch(checked = showLogo, onCheckedChange = { showLogo = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Afficher le badge certifié")
                                Switch(checked = showBadge, onCheckedChange = { showBadge = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Afficher la série (Streak)")
                                Switch(checked = showStreak, onCheckedChange = { showStreak = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Afficher la citation")
                                Switch(checked = showPunchline, onCheckedChange = { showPunchline = it })
                            }
                        }
                    }
                }
            }

            // SECTION 3: READY-TO-COPY CAPTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Légende de post (TikTok / Insta)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(generatedCaption))
                                Toast.makeText(context, "Légende copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copier la légende")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generatedCaption,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }

            // SECTION 4: MAIN ACTION BUTTONS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { triggerSharesheet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("trigger_sharesheet_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORTER & PARTAGER MON POST", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedCaption))
                            Toast.makeText(context, "Légende copiée !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copier uniquement la légende texte", fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
