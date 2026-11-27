package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import android.content.res.AssetManager // Gestor de assets que se simula para contar las lecturas.
import io.mockk.every // Configura las respuestas del mock.
import io.mockk.mockk // Crea el gestor de assets simulado.
import io.mockk.verify // Comprueba las llamadas al mock.
import java.io.ByteArrayInputStream // Simula el contenido de un archivo de assets.
import java.io.IOException // Simula un archivo que no existe.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.flow.first // Espera el primer estado que cumpla una condición.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertFalse // Comprueba una condición falsa.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G10: lectura de los textos institucionales desde assets (InstitucionViewModel e InicioViewModel). */
@OptIn(ExperimentalCoroutinesApi::class)
class InstitucionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val assets = mockk<AssetManager>() // Gestor de assets simulado; cada prueba define qué archivos existen.
    private val app = AplicacionDePrueba.crear().also { every { it.assets } returns assets } // Application simulado que entrega esos assets.

    private fun archivo(texto: String) = ByteArrayInputStream(texto.toByteArray()) // Contenido simulado de un .md.

    @Test
    fun alIniciar_muestraLaEscuela() = runTest {
        every { assets.open("institucion/escuela_jan_van_dijk.md") } returns archivo("# Escuela")

        val viewModel = InstitucionViewModel(app)
        val estado = viewModel.estado.first { it.contenido != null } // Espera la lectura en Dispatchers.IO.

        assertEquals(SeccionInstitucion.ESCUELA, estado.seccion)
        assertEquals("# Escuela", estado.contenido)
        assertFalse(estado.error)
    }

    @Test
    fun archivoInexistente_marcaError_yLaSeccionLeidaSeGuardaEnCache() = runTest {
        every { assets.open("institucion/escuela_jan_van_dijk.md") } returns archivo("# Escuela")
        every { assets.open("institucion/alba_lab.md") } throws IOException("no existe")
        val viewModel = InstitucionViewModel(app)
        viewModel.estado.first { it.contenido != null }

        viewModel.seleccionar(SeccionInstitucion.ALBA_LAB) // Archivo que falla.
        val conError = viewModel.estado.first { it.seccion == SeccionInstitucion.ALBA_LAB && it.error }
        assertTrue(conError.error)

        viewModel.seleccionar(SeccionInstitucion.ESCUELA) // Vuelve a la escuela: sale del caché.
        assertEquals("# Escuela", viewModel.estado.value.contenido) // Se muestra de inmediato.
        verify(exactly = 1) { assets.open("institucion/escuela_jan_van_dijk.md") } // No se volvió a leer el archivo.
    }

    @Test
    fun inicio_leeElTextoDeCorpaliv() = runTest {
        every { assets.open("institucion/corpaliv.md") } returns archivo("# CORPALIV\n\nMisión")

        val viewModel = InicioViewModel(app)

        assertEquals("# CORPALIV\n\nMisión", viewModel.contenido.first { it != null })
    }

    @Test
    fun inicio_sinArchivo_muestraUnTextoDeRespaldo() = runTest {
        every { assets.open("institucion/corpaliv.md") } throws IOException("no existe")

        val viewModel = InicioViewModel(app)

        assertEquals(
            "# CORPALIV\n\nNo se pudo cargar la información institucional.",
            viewModel.contenido.first { it != null }
        )
    }
}
