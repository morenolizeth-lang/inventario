package com.example.inventariolt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.screen.inventario.AgregarProductoScreen
import com.example.inventariolt.screen.inventario.CrearVarianteScreen
import com.example.inventariolt.screen.inventario.DetalleProductoScreen
import com.example.inventariolt.screen.inventario.InventarioHomeScreen
import com.example.inventariolt.screen.login.LoginScreen
import com.example.inventariolt.screen.login.RegistroScreen
import com.example.inventariolt.screen.perfil.ActualizarPerfilScreen
import com.example.inventariolt.screen.perfil.PerfilScreen
import com.example.inventariolt.screen.inventario.EstadisticasScreen
import com.example.inventariolt.screen.inventario.GenerarVentaScreen
import com.example.inventariolt.ui.theme.InventarioTheme
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventarioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Pantalla de Login
        composable("login") {
            LoginScreen(navController = navController)
        }

        // Pantalla de Registro
        composable("registro") {
            RegistroScreen(navController = navController)
        }

        // Pantalla de Inventario (Home)
        composable(
            route = "inventario_home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            InventarioHomeScreen(navController = navController, userId = userId)
        }

        // Pantalla de Agregar Producto
        composable(
            route = "agregar_producto/{tiendaId}/{userId}",
            arguments = listOf(
                navArgument("tiendaId") { type = NavType.LongType },
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tiendaId = backStackEntry.arguments?.getLong("tiendaId") ?: 0L
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            AgregarProductoScreen(
                navController = navController,
                tiendaId = tiendaId,
                userId = userId
            )
        }

        // Pantalla de Perfil
        composable(
            route = "perfil/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            PerfilScreen(navController = navController, userId = userId)
        }

        // Pantalla de Actualizar Perfil
        composable(
            route = "actualizar_perfil/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            ActualizarPerfilScreen(navController = navController, userId = userId)
        }

        // Pantalla de Crear Variante
        composable("crear_variante") {
            CrearVarianteScreen(navController = navController)
        }

        // Pantalla de Lista de Variantes
        composable("lista_variantes") {
            com.example.inventariolt.screen.inventario.ListaVariantesScreen(navController = navController)
        }

        // Pantalla de Detalle de Variante
        composable(
            route = "detalle_variante/{varianteJson}",
            arguments = listOf(navArgument("varianteJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val varianteJson = backStackEntry.arguments?.getString("varianteJson") ?: ""
            val variante = Gson().fromJson(varianteJson, com.example.inventariolt.model.inventario.VarianteVisualResponseDTO::class.java)
            com.example.inventariolt.screen.inventario.DetalleVarianteScreen(
                navController = navController,
                variante = variante
            )
        }

        // Pantalla de Generar Venta
        composable(
            route = "generar_venta/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            GenerarVentaScreen(navController = navController, userId = userId)
        }

        // Pantalla de Estadísticas
        composable(
            route = "estadisticas/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            EstadisticasScreen(navController = navController, userId = userId)
        }

        composable(
            route = "detalle_producto/{userId}/{productoJson}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("productoJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val productoJson = backStackEntry.arguments?.getString("productoJson") ?: ""
            val producto = Gson().fromJson(productoJson, ProductoResponseDTO::class.java)
            DetalleProductoScreen(
                navController = navController,
                producto = producto,
                userId = userId
            )
        }
    }
}