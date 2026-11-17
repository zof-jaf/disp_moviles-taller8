package com.example.taller_8.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taller_8.data.model.Personaje
import com.example.taller_8.ui.theme.Taller_8Theme
import com.example.taller_8.ui.viewmodel.PersonajeDetalleUiState
import com.example.taller_8.ui.viewmodel.PersonajeDetalleViewModel
import com.example.taller_8.ui.viewmodel.PersonajesUiState
import com.example.taller_8.ui.viewmodel.PersonajesViewModel

@Composable
fun PersonajesScreen(
    viewModel: PersonajesViewModel,
    onPersonajeClick: (Personaje) -> Unit,
    onRetry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Taller_8Theme {
        Scaffold(
            topBar = {
                Text(
                    text = "Rick & Morty",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        ) { innerPadding ->
            val content = Modifier.padding(innerPadding)

            when (val state = uiState) {
                is PersonajesUiState.Loading -> {
                    CenteredLoader(modifier = content.fillMaxSize())
                }
                is PersonajesUiState.Success -> {
                    PersonajesList(
                        personajes = state.items,
                        onPersonajeClick = onPersonajeClick,
                        modifier = content
                    )
                }
                is PersonajesUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = onRetry,
                        modifier = content.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PersonajesList(
    personajes: List<Personaje>,
    onPersonajeClick: (Personaje) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(personajes, key = { it.id }) { personaje ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable(onClick = { onPersonajeClick(personaje) })
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = personaje.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Estado: ${personaje.status}"
                        )
                    }
                    Text(
                        text = "${personaje.species} • id ${personaje.id}"
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Reintentar")
        }
    }
}

@Composable
fun CenteredLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(Modifier.size(40.dp))
    }
}

@Composable
fun PersonajeDetalleScreen(
    viewModel: PersonajeDetalleViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Taller_8Theme {
        Scaffold(
            topBar = {
                Text(
                    text = "Detalle",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        ) { innerPadding ->
            val content = Modifier.padding(innerPadding)

            when (val state = uiState) {
                is PersonajeDetalleUiState.Loading -> {
                    CenteredLoader(modifier = content.fillMaxSize())
                }
                is PersonajeDetalleUiState.Success -> {
                    PersonajeDetalleContent(
                        personaje = state.personaje,
                        modifier = content,
                        onNavigateBack = onNavigateBack
                    )
                }
                is PersonajeDetalleUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = viewModel::cargar,
                        modifier = content.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PersonajeDetalleContent(
    personaje: Personaje,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        IconButton(onClick = onNavigateBack) {
            Text(text = "Atrás")
        }
        // Avatar de marcador de posición (sin dependencias de carga de imágenes).
        // Se usa el id para elegir un color de fondo estable y se muestra la inicial del nombre.
        val initial = personaje.name.take(1).ifEmpty { "?" }.uppercase()
        val rgb = (0xFFFFFFL and (personaje.id.toLong() * 0x9E3779B9L)).toInt()
        val avatarColor = Color((0xFF shl 24) or rgb)
        Box(
            modifier = Modifier.size(120.dp).background(avatarColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = personaje.name,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Estado: ${personaje.status}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Especie: ${personaje.species}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ID: ${personaje.id}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
