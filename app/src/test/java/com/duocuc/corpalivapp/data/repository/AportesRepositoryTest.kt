package com.duocuc.corpalivapp.data.repository // Paquete de las pruebas de la capa de repositorios.

import com.duocuc.corpalivapp.data.model.AporteRequestDto // Cuerpo que la app envía a POST /api/aportes.
import com.duocuc.corpalivapp.data.model.EstadoRequestDto // Cuerpo que la app envía a PATCH /api/aportes/{id}/estado.
import com.duocuc.corpalivapp.data.model.RegistroGuardado // Registro que devuelve aportes-service.
import com.duocuc.corpalivapp.data.network.AportesApiService // Servicio Retrofit que se reemplaza por un mock.
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.mockk // Crea el servicio simulado.
import java.io.IOException // Error de red que lanza Retrofit cuando no hay conexión.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import okhttp3.ResponseBody.Companion.toResponseBody // Crea el cuerpo vacío de una respuesta de error.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Test // Marca cada método como prueba de JUnit.
import retrofit2.Response // Respuesta completa de Retrofit (DELETE responde 204 sin cuerpo).

/** PARCIAL 4: comprueba que AportesRepository traduce las respuestas de aportes-service a Result con mensajes legibles. */
class AportesRepositoryTest {

    private val api = mockk<AportesApiService>() // Servicio de red simulado: no se llama al servidor real.
    private val repositorio = AportesRepository(api) // Repositorio bajo prueba con el servicio simulado.

    private val solicitud = AporteRequestDto( // Formulario de aporte de una persona, como lo arma UsuarioViewModel.
        tipo = "PERSONA", motivo = "APORTE", oportunidad = "Taller Alba Lab", nombre = "Camila Silva",
        rut = "12345678-5", correo = "camila@example.com", telefono = "+569 12345678", tipoAporte = "Mensual"
    )
    private val registro = RegistroGuardado(id = 7L, tipo = "PERSONA", nombre = "Camila Silva") // Respuesta simulada del servidor.

    @Test
    fun registrar_exito_entregaElRegistroGuardado() = runTest {
        coEvery { api.crear(solicitud) } returns registro // El servidor responde 201 con el registro.

        val resultado = repositorio.registrar(solicitud) // Envía el formulario.

        assertEquals(registro, resultado.getOrNull()) // El Result trae el registro del servidor.
    }

    @Test
    fun listar_exito_entregaLaLista() = runTest {
        coEvery { api.listar() } returns listOf(registro, registro.copy(id = 8L)) // El servidor devuelve dos registros.

        val resultado = repositorio.listar() // Descarga los registros.

        assertEquals(2, resultado.getOrThrow().size) // Llegan los dos registros.
    }

    @Test
    fun marcarContactado_envíaElEstadoContactado() = runTest {
        coEvery { api.cambiarEstado(7L, any()) } returns registro.copy(estado = ESTADO_CONTACTADO) // El servidor confirma el cambio.

        val resultado = repositorio.marcarContactado(7L) // Marca el registro como contactado.

        assertEquals("CONTACTADO", resultado.getOrThrow().estado) // El registro vuelve con el nuevo estado.
        coVerify { api.cambiarEstado(7L, EstadoRequestDto("CONTACTADO")) } // Se envió exactamente el estado que espera el backend.
    }

    @Test
    fun eliminar_respuesta204_esExito() = runTest {
        coEvery { api.eliminar(7L) } returns Response.success(204, Unit) // El servidor elimina y responde sin cuerpo.

        assertTrue(repositorio.eliminar(7L).isSuccess) // La eliminación se informa como éxito.
    }

    @Test
    fun eliminar_respuesta404_informaQueNoExiste() = runTest {
        coEvery { api.eliminar(9L) } returns Response.error(404, "".toResponseBody(null)) // El registro ya no existe.

        val resultado = repositorio.eliminar(9L) // Intenta eliminar.

        assertEquals("No se encontró el registro", resultado.exceptionOrNull()?.message) // Mensaje legible para la pantalla.
    }

    @Test
    fun error400_informaDatosRechazados() = runTest {
        coEvery { api.crear(any()) } throws httpError(400) // El servidor rechaza el formulario.

        val resultado = repositorio.registrar(solicitud) // Envía el formulario.

        assertEquals("El servidor rechazó los datos. Revisa el formulario", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun error401y403_informanSesionInvalida() = runTest {
        val esperado = "Tu sesión no es válida o expiró. Vuelve a iniciar sesión" // Mismo mensaje para 401 y 403.
        coEvery { api.listar() } throws httpError(401) // Sin token.
        assertEquals(esperado, repositorio.listar().exceptionOrNull()?.message)

        coEvery { api.listar() } throws httpError(403) // Token sin rol de administrador.
        assertEquals(esperado, repositorio.listar().exceptionOrNull()?.message)
    }

    @Test
    fun otroCodigo_informaElCodigo() = runTest {
        coEvery { api.listar() } throws httpError(500) // Error interno del servidor.

        assertEquals("El servidor respondió con un error (500)", repositorio.listar().exceptionOrNull()?.message)
    }

    @Test
    fun sinConexion_informaQueRevisenLosServicios() = runTest {
        coEvery { api.listar() } throws IOException("sin red") // Servicios apagados o sin red.

        assertEquals(
            "No se pudo conectar con el servidor. Revisa que los servicios estén encendidos",
            repositorio.listar().exceptionOrNull()?.message
        )
    }
}
