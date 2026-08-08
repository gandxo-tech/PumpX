package com.example.presentation.blocker

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PumpXApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.material3.MaterialTheme

@Composable
fun LimitReachedScreen(
    targetPackage: String,
    targetPushups: Int,
    bonusMinutes: Int,
    onStartPushups: () -> Unit
) {
    val context = LocalContext.current
    var appName by remember { mutableStateOf(targetPackage) }

    LaunchedEffect(targetPackage) {
        if (targetPackage.isBlank() || targetPackage == "general") {
            appName = "vos applications"
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val appInfo = pm.getApplicationInfo(targetPackage, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    if (label.isNotBlank()) {
                        appName = label
                    } else {
                        val app = context.applicationContext as PumpXApplication
                        val monitored = app.monitoredAppRepository.getApp(targetPackage)
                        if (monitored != null) {
                            appName = monitored.appName
                        }
                    }
                } catch (e: Exception) {
                    val app = context.applicationContext as PumpXApplication
                    val monitored = app.monitoredAppRepository.getApp(targetPackage)
                    appName = monitored?.appName ?: targetPackage
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Limite Atteinte",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tu as atteint ta limite quotidienne sur $appName.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pour obtenir $bonusMinutes minutes supplémentaires, réalise $targetPushups pompes !",
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(44.dp))

            Button(
                onClick = onStartPushups,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .testTag("do_pushups_button")
            ) {
                Text(
                    text = "Faire mes pompes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
