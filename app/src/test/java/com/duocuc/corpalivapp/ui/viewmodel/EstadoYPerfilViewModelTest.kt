package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import android.net.Uri // Dirección de la foto de perfil.
import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.data.local.EstadoDataStore // DataStore real del modo especial, en una carpeta temporal.
import io.mockk.mockk // Crea el Uri simulado.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.flow.first // Espera el primer valor que cumpla una condición.
import kotlinx.coroutines.runBlocking // Deja el DataStore en un estado conocido antes de cada prueba.
import kotlinx.coroutines.test.advanceUntilIdle // Ejecuta las tareas pendientes (incluidos los delay simulados).
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertFalse // Comprueba una condición falsa.
import org.junit.Assert.assertNull // Comprueba que un valor sea null.
import org.junit.Before // Preparación antes de cada prueba.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G12 y G13: modo especial persistente (EstadoViewModel) y foto de perfil (PerfilViewModel). */
@OptIn(ExperimentalCoroutinesApi::class)
class EstadoYPerfilViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val app = AplicacionDePrueba.crear() // Application simulado con filesDir temporal.

    @Before
    fun preparar() {
        runBlocking { EstadoDataStore(app).guardarEstado(false) } // Cada prueba parte con el modo desactivado.
    }

    @Test
    fun modoEspecial_cargaElValorGuardadoYLoAlterna() = runTest {
        val viewModel = EstadoViewModel(app)
        assertNull(viewModel.activo.value) // Mientras carga, el valor es null (se muestra el loader).

        assertEquals(false, viewModel.activo.first { it != null }) // Lee lo guardado tras el delay simulado.

        viewModel.alternarEstado() // El usuario toca el interruptor.
        assertEquals(true, viewModel.activo.first { it == true })
        assertEquals(true, viewModel.mostrarMensaje.first { it }) // Se muestra la confirmación.
        assertEquals(true, EstadoDataStore(app).obtenerEstado().first()) // Quedó guardado en DataStore.

        advanceUntilIdle() // Pasan los 2 segundos del mensaje.
        assertFalse(viewModel.mostrarMensaje.value) // El mensaje se oculta.
    }

    @Test
    fun perfil_actualizaYQuitaLaImagen() {
        val viewModel = PerfilViewModel()
        val galeria = mockk<Uri>() // Foto elegida desde la galería.
        val camara = mockk<Uri>() // Foto tomada con la cámara.

        viewModel.actualizarDesdeGaleria(galeria)
        assertEquals(galeria, viewModel.imagenUri.value)

        viewModel.actualizarDesdeCamara(camara)
        assertEquals(camara, viewModel.imagenUri.value)

        viewModel.quitarImagen() // Vuelve al ícono por defecto.
        assertNull(viewModel.imagenUri.value)
    }
}
