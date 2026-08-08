package com.example.presentation.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsScreen(onBack: () -> Unit) {
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
                    text = "CONDITIONS D'UTILISATION",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("1. OBJET", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Les présentes Conditions d'Utilisation régissent l'usage de l'application mobile PumpX.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("2. FONCTIONNEMENT ET COMPUTER VISION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("PumpX analyse le flux vidéo du capteur caméra du téléphone en temps réel localement pour estimer les répétitions de pompes.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("3. CAMÉRA ET CONFIDENTIALITÉ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Aucun flux vidéo, ni photo prise par la caméra n'est transmis vers un serveur externe. L'analyse s'effectue exclusivement sur le processeur local du téléphone.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("4. LIMITES DE TEMPS D'ÉCRAN ET APIS ANDROID", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Le calcul du temps d'utilisation repose sur l'API officielle Android UsageStatsManager. La précision dépend des permissions octroyées par l'utilisateur.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("5. DÉCHARGE DE RESPONSABILITÉ SPORTIVE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("L'utilisateur s'engage à pratiquer les exercices physiques selon ses capacités physiques personnelles.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("6. CONTACT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pour toute question: gbaguidiexauce@gmail.com")
                }
            }
        }
    }
}
