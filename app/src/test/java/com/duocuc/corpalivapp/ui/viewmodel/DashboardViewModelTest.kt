package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.data.local.RegistroDataStore // DataStore real, en una carpeta temporal.
import com.duocuc.corpalivapp.data.model.RegistroGuardado // Registro de aporte o postulación.
import com.duocuc.corpalivapp.data.repository.AportesRepository // Repositorio que se reemplaza por un mock.
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.mockk // Crea el repositorio simulado.
import kotlinx.coroutines.CompletableDeferred // Simula una descarga que todavía no termina.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.flow.first // Espera el primer estado que cumpla una condición.
import kotlinx.coroutines.runBlocking // Limpia el DataStore antes de cada prueba.
import kotlinx.coroutines.test.advanceUntilIdle // Deja terminar la corrutina de sincronización antes de repetirla.
import kotlinx.coroutines.test.runCurrent // Ejecuta solo las tareas del instante actual.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Before // Preparación antes de cada prueba.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/**
 * G12 y PARCIAL 4: Dashboard del administrador. Sincroniza con aportes-service, filtra, cuenta,
 * elimina y marca como contactado. Usa el DataStore real sobre una carpeta temporal.
 *
 * DataStore escribe en su propio hilo, por eso las pruebas esperan el estado con first { ... }
 * en lugar de suponer que ya se escribió.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val app = AplicacionDePrueba.crear() // Application simulado con filesDir temporal.
    private val repositorio = mockk<AportesRepository>() // aportes-service simulado.

    private val servidor = listOf( // Lo que devolvería GET /api/aportes.
        RegistroGuardado(id = 1L, tipo = "PERSONA", nombre = "Camila Silva", motivo = "APORTE"),
        RegistroGuardado(id = 2L, tipo = "EMPRESA", nombre = "Empresa Demo SpA", motivo = "APORTE"),
        RegistroGuardado(id = 3L, tipo = "PERSONA", nombre = "Diego Rojas", motivo = "POSTULACION"),
        RegistroGuardado(id = 4L, tipo = "PERSONA", nombre = "Ana Pérez", motivo = "APORTE")
    )

    @Before
    fun preparar() {
        runBlocking { RegistroDataStore(app).reemplazarTodos(emptyList()) } // Cada prueba parte con el DataStore vacío.
    }

    @Test
    fun sincronizar_guardaLaListaYCalculaLosTotales() = runTest {
        coEvery { repositorio.listar() } returns Result.success(servidor) // El servidor entrega cuatro registros.
        val viewModel = DashboardViewModel(app, repositorio)

        viewModel.sincronizar() // El administrador abre el Dashboard.
        val estado = viewModel.estado.first { it.registros.size == 4 } // Espera a que se escriban en DataStore.

        assertEquals(2, estado.totalPersonas) // Personas que aportan (sin contar la postulación).
        assertEquals(1, estado.totalEmpresas)
        assertEquals(1, estado.totalPostulaciones)
        assertEquals(null, estado.error)
    }

    @Test
    fun cambiarFiltro_muestraSoloLosRegistrosDelTipo() = runTest {
        coEvery { repositorio.listar() } returns Result.success(servidor)
        val viewModel = DashboardViewModel(app, repositorio)
        viewModel.sincronizar()
        viewModel.estado.first { it.registros.size == 4 }

        viewModel.cambiarFiltro(FiltroDashboard.PERSONAS)
        assertEquals(listOf(1L, 4L), viewModel.estado.first { it.filtro == FiltroDashboard.PERSONAS }.registros.map { it.id })

        viewModel.cambiarFiltro(FiltroDashboard.EMPRESAS)
        assertEquals(listOf(2L), viewModel.estado.first { it.filtro == FiltroDashboard.EMPRESAS }.registros.map { it.id })

        viewModel.cambiarFiltro(FiltroDashboard.POSTULACIONES)
        assertEquals(listOf(3L), viewModel.estado.first { it.filtro == FiltroDashboard.POSTULACIONES }.registros.map { it.id })

        viewModel.cambiarFiltro(FiltroDashboard.TODOS)
        assertEquals(4, viewModel.estado.first { it.filtro == FiltroDashboard.TODOS }.registros.size)
        assertEquals("Postulaciones", FiltroDashboard.POSTULACIONES.etiqueta) // Etiqueta del chip.
    }

    @Test
    fun sincronizarConError_muestraElAvisoYConservaLosDatos() = runTest {
        coEvery { repositorio.listar() } returnsMany listOf( // Primero funciona; después el servidor se cae.
            Result.success(servidor),
            Result.failure(Exception("No se pudo conectar con el servidor. Revisa que los servicios estén encendidos"))
        )
        val viewModel = DashboardViewModel(app, repositorio)
        viewModel.sincronizar()
        viewModel.estado.first { it.registros.size == 4 }
        advanceUntilIdle() // La primera sincronización debe terminar del todo, o la segunda se ignoraría.

        viewModel.sincronizar() // Segundo intento sin conexión.
        val estado = viewModel.estado.first { it.error != null }

        assertEquals("No se pudo conectar con el servidor. Revisa que los servicios estén encendidos", estado.error)
        assertEquals(4, estado.registros.size) // Se conservan los últimos datos descargados.
    }

    @Test
    fun sincronizarDosVeces_mientrasDescarga_consultaUnaSolaVez() = runTest {
        val respuesta = CompletableDeferred<Result<List<RegistroGuardado>>>() // Descarga que el test completa después.
        coEvery { repositorio.listar() } coAnswers { respuesta.await() }
        val viewModel = DashboardViewModel(app, repositorio)

        viewModel.sincronizar() // Primera descarga.
        runCurrent()
        viewModel.sincronizar() // Se ignora: ya hay una en curso.
        respuesta.complete(Result.success(servidor))
        viewModel.estado.first { it.registros.size == 4 }

        coVerify(exactly = 1) { repositorio.listar() }
    }

    @Test
    fun eliminarConExito_vuelveASincronizar() = runTest {
        coEvery { repositorio.listar() } returnsMany listOf(Result.success(servidor), Result.success(servidor.drop(1)))
        coEvery { repositorio.eliminar(1L) } returns Result.success(Unit) // El servidor elimina el registro 1.
        val viewModel = DashboardViewModel(app, repositorio)
        viewModel.sincronizar()
        viewModel.estado.first { it.registros.size == 4 }
        advanceUntilIdle() // Deja terminar la primera sincronización antes de eliminar.

        viewModel.eliminar(1L) // El administrador confirma la eliminación.
        val estado = viewModel.estado.first { it.registros.size == 3 } // La lista se refresca sin el registro.

        assertEquals(listOf(2L, 3L, 4L), estado.registros.map { it.id })
        coVerify(exactly = 2) { repositorio.listar() }
    }

    @Test
    fun eliminarConError_muestraElMotivo() = runTest {
        coEvery { repositorio.eliminar(9L) } returns Result.failure(Exception("No se encontró el registro"))
        val viewModel = DashboardViewModel(app, repositorio)

        viewModel.eliminar(9L)

        assertEquals("No se encontró el registro", viewModel.estado.first { it.error != null }.error)
        coVerify(exactly = 0) { repositorio.listar() } // No se recarga si falló.
    }

    @Test
    fun marcarContactado_actualizaElEstadoDesdeElServidor() = runTest {
        val contactado = servidor.map { if (it.id == 2L) it.copy(estado = "CONTACTADO") else it } // Respuesta tras el PATCH.
        coEvery { repositorio.listar() } returns Result.success(contactado)
        coEvery { repositorio.marcarContactado(2L) } returns Result.success(contactado[1])
        val viewModel = DashboardViewModel(app, repositorio)

        viewModel.marcarContactado(2L) // Se abrió WhatsApp con la empresa.
        val estado = viewModel.estado.first { it.registros.size == 4 }

        assertEquals("CONTACTADO", estado.registros.first { it.id == 2L }.estado)
    }

    @Test
    fun marcarContactadoConError_muestraElMotivo() = runTest {
        coEvery { repositorio.marcarContactado(2L) } returns
            Result.failure(Exception("Tu sesión no es válida o expiró. Vuelve a iniciar sesión"))
        val viewModel = DashboardViewModel(app, repositorio)

        viewModel.marcarContactado(2L)

        assertEquals(
            "Tu sesión no es válida o expiró. Vuelve a iniciar sesión",
            viewModel.estado.first { it.error != null }.error
        )
    }

    @Test
    fun guardar_agregaElRegistroAlInicio() = runTest {
        coEvery { repositorio.listar() } returns Result.success(servidor.take(1))
        val viewModel = DashboardViewModel(app, repositorio)
        viewModel.sincronizar()
        viewModel.estado.first { it.registros.size == 1 }

        viewModel.guardar(RegistroGuardado(id = 99L, tipo = "EMPRESA", nombre = "Nueva SpA")) // Copia local tras enviar el formulario.
        val estado = viewModel.estado.first { it.registros.size == 2 }

        assertEquals(99L, estado.registros.first().id) // Los más recientes primero.
    }
}
