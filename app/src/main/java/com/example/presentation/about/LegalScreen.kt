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
fun LegalScreen(onBack: () -> Unit) {
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
                    text = "MENTIONS LÉGALES",
                    fontSize = 20.sp,
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
                    Text("1. ÉDITEUR DU SERVICE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nom de la société / Éditeur: [À RENSEIGNER PAR L'ÉDITEUR]")
                    Text("Forme juridique: [À RENSEIGNER PAR L'ÉDITEUR]")
                    Text("Siège social: [À RENSEIGNER PAR L'ÉDITEUR]")
                    Text("Numéro SIREN / Immatriculation: [À RENSEIGNER PAR L'ÉDITEUR]")
                    Text("Email de contact: [À RENSEIGNER PAR L'ÉDITEUR]")

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("2. HÉBERGEMENT ET INFRASTRUCTURE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("L'application PumpX fonctionne principalement en traitement local sur le terminal Android de l'utilisateur.")
                    Text("Hébergeur des services en ligne: [À RENSEIGNER PAR L'ÉDITEUR]")

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("3. PROPRIÉTÉ INTELLECTUELLE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("L'ensemble des marques, logos, graphismes et algorithmes de l'application PumpX sont protégés par le droit d'auteur.")
                }
            }
        }
    }
}
