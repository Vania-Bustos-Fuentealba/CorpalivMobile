package com.duocuc.corpalivapp.navigation // Paquete de las pruebas de navegación.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.ui.viewmodel.MainViewModel // ViewModel que emite los eventos de navegación.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.flow.toList // Junta los eventos emitidos en una lista.
import kotlinx.coroutines.launch // Inicia el colector de eventos.
import kotlinx.coroutines.test.UnconfinedTestDispatcher // Ejecuta el colector de inmediato.
import kotlinx.coroutines.test.advanceUntilIdle // Ejecuta las tareas pendientes.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G10: rutas de Screen y eventos que emite MainViewModel (incluida la ruta del formulario de oportunidades). */
@OptIn(ExperimentalCoroutinesApi::class)
class NavegacionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    @Test
    fun rutas_sonEstablesYLaDeDetalleReemplazaElParametro() {
        assertEquals("oportunidad_form_page", Screen.OportunidadForm.buildRoute()) // Ruta del CRUD de administrador.
        assertEquals("dashboard_page", Screen.Dashboard.route) // Ruta del Dashboard.
        assertEquals("detail_page/42", Screen.Detail("42").buildRoute()) // La ruta con parámetro queda lista para navegar.
    }

    @Test
    fun mainViewModel_emiteLosEventosEnOrden() = runTest {
        val viewModel = MainViewModel() // ViewModel bajo prueba.
        val eventos = mutableListOf<NavigationEvent>() // Eventos recibidos.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.navigationEvents.toList(eventos) } // Escucha antes de emitir.

        viewModel.navigateTo(Screen.OportunidadForm, popUpToRoute = Screen.Oportunidades, singleTop = true) // Abre el formulario.
        viewModel.navigateBack() // Vuelve al listado.
        viewModel.navigateUp() // Sube en la jerarquía.
        advanceUntilIdle() // Ejecuta las emisiones pendientes.

        assertEquals(
            listOf(
                NavigationEvent.NavigateTo(Screen.OportunidadForm, Screen.Oportunidades, inclusive = false, singleTop = true),
                NavigationEvent.PopBackStack,
                NavigationEvent.NavigateUp
            ),
            eventos
        )
    }
}
