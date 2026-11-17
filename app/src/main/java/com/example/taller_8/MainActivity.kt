package com.example.taller_8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller_8.ui.screen.PersonajeDetalleScreen
import com.example.taller_8.ui.screen.PersonajesScreen
import com.example.taller_8.ui.theme.Taller_8Theme
import com.example.taller_8.ui.viewmodel.PersonajeDetalleViewModel
import com.example.taller_8.ui.viewmodel.PersonajesViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taller_8Theme {
                RickMortyNavHost()
            }
        }
    }
}

/**
 * Navegación con Navigation Compose (Clase 6).
 *
 * Se envía únicamente el id del personaje a la ruta de detalle (nunca el objeto
 * completo). El ViewModel de detalle (Opción A) recupera el personaje mediante
 * character/{id}, por lo que abrir la ruta directamente funciona sin lista previa.
 */
@Composable
fun RickMortyNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "personajes") {
        composable("personajes") {
            val viewModel: PersonajesViewModel = viewModel()
            PersonajesScreen(
                viewModel = viewModel,
                onPersonajeClick = { personaje ->
                    navController.navigate("detalle/${personaje.id}")
                },
                onRetry = viewModel::cargar
            )
        }

        composable("detalle/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            // Se crea el ViewModel de detalle recordando el id, así al navegar entre
            // personajes distintos se crea una instancia que recupera ese id.
            val detalleViewModel: PersonajeDetalleViewModel = remember(id) {
                PersonajeDetalleViewModel(id)
            }
            PersonajeDetalleScreen(
                viewModel = detalleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
