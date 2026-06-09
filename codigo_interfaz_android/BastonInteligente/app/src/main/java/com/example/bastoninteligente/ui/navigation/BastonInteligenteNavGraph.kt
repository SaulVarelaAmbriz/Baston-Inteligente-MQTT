package com.example.bastoninteligente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bastoninteligente.ui.home.InicioSesionDestination
import com.example.bastoninteligente.ui.home.InicioSesionScreen
import com.example.bastoninteligente.ui.home.MenuDestination
import com.example.bastoninteligente.ui.home.MenuScreen
import com.example.bastoninteligente.ui.item.ClimaDestination
import com.example.bastoninteligente.ui.item.ClimaScreen
import com.example.bastoninteligente.ui.item.GpsCamaraDestination
import com.example.bastoninteligente.ui.item.GpsCamaraScreen
import com.example.bastoninteligente.ui.item.HistorialAlertasDestination
import com.example.bastoninteligente.ui.item.HistorialAlertasScreen
import com.example.bastoninteligente.ui.item.TableroInteractivoDestination
import com.example.bastoninteligente.ui.item.TableroInteractivoScreen
import com.example.bastoninteligente.ui.item.TableroLecturaDestination
import com.example.bastoninteligente.ui.item.TableroLecturaScreen

@Composable
fun BastonInteligenteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = InicioSesionDestination.ruta,
        modifier = modifier
    ) {
        composable(route = InicioSesionDestination.ruta) {
            InicioSesionScreen(
                navegarA_Menu = { navController.navigate(MenuDestination.ruta) }
            )
        }

        composable(route = MenuDestination.ruta) {
            MenuScreen(
                navegarArriba = { navController.navigateUp() },
                navegarA_Clima = { navController.navigate(ClimaDestination.ruta) },
                navegarA_TableroLectura= { navController.navigate(TableroLecturaDestination.ruta) },
                navegarA_TableroInteractivo= { navController.navigate(TableroInteractivoDestination.ruta) },
                navegarA_GpsCamara= { navController.navigate(GpsCamaraDestination.ruta) },
                navegarA_Alertas= { navController.navigate(HistorialAlertasDestination.ruta) },
            )
        }

        composable(route = ClimaDestination.ruta) {
            ClimaScreen(
                navegarArriba = { navController.navigateUp() }
            )
        }

        composable(route = TableroLecturaDestination.ruta) {
            TableroLecturaScreen(
                navegarArriba = { navController.navigateUp() }
            )
        }

        composable(route = TableroInteractivoDestination.ruta) {
            TableroInteractivoScreen(
                navegarArriba = { navController.navigateUp() }
            )
        }

        composable(route = GpsCamaraDestination.ruta) {
            GpsCamaraScreen(navegarArriba = { navController.navigateUp() })
        }

        composable(route = HistorialAlertasDestination.ruta) {
            HistorialAlertasScreen(
                navegarArriba = { navController.navigateUp() }
            )
        }
    }
}
