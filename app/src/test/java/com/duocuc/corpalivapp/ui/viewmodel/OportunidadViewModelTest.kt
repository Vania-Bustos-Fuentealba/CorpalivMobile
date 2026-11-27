package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.data.model.OportunidadModel // Oportunidad del listado.
import com.duocuc.corpalivapp.data.model.ResultadoLogin // Resultado de la autenticación simulada (Guía 5).
import com.duocuc.corpalivapp.data.network.ApiService // Servicio Retrofit del listado.
import com.duocuc.corpalivapp.data.network.RetrofitClient // Cliente Retrofit que se simula con mockkObject.
import com.duocuc.corpalivapp.data.repository.OportunidadesRepository // Repositorio de eliminación (Parcial 4).
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.every // Configura respuestas para funciones normales.
import io.mockk.mockk // Crea los mocks.
import io.mockk.mockkObject // Permite reemplazar propiedades de un object de Kotlin.
import io.mockk.unmockkObject // Restaura el object original.
import java.io.IOException // Error de red.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.test.advanceUntilIdle // Ejecuta las tareas pendientes (incluidos los delay simulados).
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.After // Limpieza después de cada prueba.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertNull // Comprueba que un valor sea null.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Before // Preparación antes de cada prueba.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** Listado de oportunidades: carga desde la API, respaldo local, eliminación de administrador y Guía 5. */
@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val api = mockk<ApiService>() // Servicio del listado simulado.
    private val repositorio = mockk<OportunidadesRepository>() // Repositorio de eliminación simulado.

    private val remota = OportunidadModel( // Oportunidad que entregaría oportunidades-service.
        id = 10, titulo = "Programa remoto", categoria = "RSE Empresarial", descripcion = "d", impactoSocial = "i",
        imagenUrl = null, contactoEmail = null, metaFinanciamiento = 100, montoRecaudado = 10
    )

    @Before
    fun preparar() {
        mockkObject(RetrofitClient) // El ViewModel usa RetrofitClient.apiService directamente.
        every { RetrofitClient.apiService } returns api // Se entrega el servicio simulado en lugar del real.
    }

    @After
    fun limpiar() {
        unmockkObject(RetrofitClient) // Evita afectar a otras clases de prueba.
    }

    @Test
    fun cargaExitosa_asignaUnResponsableACadaOportunidad() = runTest {
        coEvery { api.getOportunidades() } returns listOf(remota) // El servidor responde con una oportunidad.

        val viewModel = OportunidadViewModel(repositorio) // El init carga el listado.
        advanceUntilIdle()

        val estado = viewModel.uiState.value as OportunidadUiState.Success // Debe quedar en éxito.
        assertEquals(listOf(10), estado.oportunidades.map { it.id }) // Llegan los datos del servidor, no el respaldo.
        assertEquals("Coordinador de Proyectos CORPALIV", estado.oportunidades[0].responsable?.puesto) // G4: responsable por defecto.
    }

    @Test
    fun sinConexion_muestraLosDatosDeRespaldo() = runTest {
        coEvery { api.getOportunidades() } throws IOException("sin red") // oportunidades-service apagado.

        val viewModel = OportunidadViewModel(repositorio)
        advanceUntilIdle()

        val estado = viewModel.uiState.value as OportunidadUiState.Success // El respaldo se muestra como éxito.
        assertEquals(2, estado.oportunidades.size) // Las dos oportunidades locales.
        assertNull(estado.oportunidades[1].contactoEmail) // G2: el segundo respaldo no tiene correo.
    }

    @Test
    fun eliminarConExito_avisaYRecargaElListado() = runTest {
        coEvery { api.getOportunidades() } returns listOf(remota)
        coEvery { repositorio.eliminar(10) } returns Result.success(Unit) // El servidor elimina.
        val viewModel = OportunidadViewModel(repositorio)
        advanceUntilIdle()

        viewModel.eliminar(10) // El administrador confirma la eliminación.
        advanceUntilIdle()

        assertEquals("Oportunidad eliminada", viewModel.aviso.value) // Aviso para la pantalla.
        coVerify(exactly = 2) { api.getOportunidades() } // Carga inicial + recarga después de eliminar.

        viewModel.consumirAviso() // La pantalla ya mostró el aviso.
        assertNull(viewModel.aviso.value)
    }

    @Test
    fun eliminarConError_muestraElMotivoSinRecargar() = runTest {
        coEvery { api.getOportunidades() } returns listOf(remota)
        coEvery { repositorio.eliminar(10) } returns Result.failure(Exception("La oportunidad ya no existe"))
        val viewModel = OportunidadViewModel(repositorio)
        advanceUntilIdle()

        viewModel.eliminar(10)
        advanceUntilIdle()

        assertEquals("La oportunidad ya no existe", viewModel.aviso.value)
        coVerify(exactly = 1) { api.getOportunidades() } // Solo la carga inicial.
    }

    @Test
    fun autenticarUsuario_guia5_validaLasCredencialesDeDemostracion() = runTest {
        coEvery { api.getOportunidades() } returns emptyList()
        val viewModel = OportunidadViewModel(repositorio)

        val exito = viewModel.autenticarUsuario("admin", "1234") // Credenciales de demostración.
        val error = viewModel.autenticarUsuario("usuario", "0000") // Credenciales incorrectas.

        assertTrue(exito is ResultadoLogin.Exito)
        assertEquals("admin@corpaliv.cl", (exito as ResultadoLogin.Exito).perfil.email)
        assertEquals(ResultadoLogin.Error("Credenciales incorrectas"), error)
    }
}
