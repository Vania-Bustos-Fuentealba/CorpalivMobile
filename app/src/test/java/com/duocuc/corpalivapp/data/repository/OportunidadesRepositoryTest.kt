package com.duocuc.corpalivapp.data.repository // Paquete de las pruebas de la capa de repositorios.

import com.duocuc.corpalivapp.data.model.OportunidadModel // Oportunidad que devuelve oportunidades-service.
import com.duocuc.corpalivapp.data.model.OportunidadRequestDto // Cuerpo de POST y PUT /api/oportunidades.
import com.duocuc.corpalivapp.data.network.ApiService // Servicio Retrofit que se reemplaza por un mock.
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

/** PARCIAL 4: comprueba el CRUD de administrador de OportunidadesRepository y sus mensajes de error. */
class OportunidadesRepositoryTest {

    private val api = mockk<ApiService>() // Servicio de oportunidades simulado.
    private val repositorio = OportunidadesRepository(api) // Repositorio bajo prueba.

    private val solicitud = OportunidadRequestDto( // Datos válidos de una oportunidad nueva.
        titulo = "Taller Alba Lab", categoria = "RSE Empresarial", descripcion = "Capacitación",
        impactoSocial = "12 jóvenes", metaFinanciamiento = 5_000_000, montoRecaudado = 0
    )
    private val creada = OportunidadModel( // Lo que respondería el servidor con el id asignado.
        id = 3, titulo = "Taller Alba Lab", categoria = "RSE Empresarial", descripcion = "Capacitación",
        impactoSocial = "12 jóvenes", imagenUrl = null, contactoEmail = null,
        metaFinanciamiento = 5_000_000, montoRecaudado = 0
    )

    @Test
    fun crear_exito_entregaLaOportunidadConId() = runTest {
        coEvery { api.crearOportunidad(solicitud) } returns creada // El servidor responde 201.

        assertEquals(3, repositorio.crear(solicitud).getOrThrow().id) // Llega el id asignado por el servidor.
    }

    @Test
    fun actualizar_exito_usaElIdIndicado() = runTest {
        coEvery { api.actualizarOportunidad(3, solicitud) } returns creada // El servidor responde 200.

        assertTrue(repositorio.actualizar(3, solicitud).isSuccess) // Se informa como éxito.
        coVerify { api.actualizarOportunidad(3, solicitud) } // Se actualizó la oportunidad correcta.
    }

    @Test
    fun eliminar_respuesta204_esExito() = runTest {
        coEvery { api.eliminarOportunidad(3) } returns Response.success(204, Unit) // Eliminada sin cuerpo.

        assertTrue(repositorio.eliminar(3).isSuccess)
    }

    @Test
    fun eliminar_respuesta404_informaQueYaNoExiste() = runTest {
        coEvery { api.eliminarOportunidad(9) } returns Response.error(404, "".toResponseBody(null)) // Otra persona ya la eliminó.

        assertEquals("La oportunidad ya no existe", repositorio.eliminar(9).exceptionOrNull()?.message)
    }

    @Test
    fun error400_informaDatosRechazados() = runTest {
        coEvery { api.crearOportunidad(any()) } throws httpError(400) // Validación del servidor.

        assertEquals("El servidor rechazó los datos. Revisa el formulario", repositorio.crear(solicitud).exceptionOrNull()?.message)
    }

    @Test
    fun error401y403_informanSesionInvalida() = runTest {
        val esperado = "Tu sesión no es válida o expiró. Vuelve a iniciar sesión" // Mismo mensaje para 401 y 403.
        coEvery { api.actualizarOportunidad(any(), any()) } throws httpError(401) // Sin token.
        assertEquals(esperado, repositorio.actualizar(1, solicitud).exceptionOrNull()?.message)

        coEvery { api.actualizarOportunidad(any(), any()) } throws httpError(403) // Sin rol de administrador.
        assertEquals(esperado, repositorio.actualizar(1, solicitud).exceptionOrNull()?.message)
    }

    @Test
    fun otroCodigo_informaElCodigo() = runTest {
        coEvery { api.crearOportunidad(any()) } throws httpError(500) // Error interno.

        assertEquals("El servidor respondió con un error (500)", repositorio.crear(solicitud).exceptionOrNull()?.message)
    }

    @Test
    fun sinConexion_informaQueRevisenLosServicios() = runTest {
        coEvery { api.eliminarOportunidad(any()) } throws IOException("sin red") // oportunidades-service apagado.

        assertEquals(
            "No se pudo conectar con el servidor. Revisa que los servicios estén encendidos",
            repositorio.eliminar(1).exceptionOrNull()?.message
        )
    }
}
