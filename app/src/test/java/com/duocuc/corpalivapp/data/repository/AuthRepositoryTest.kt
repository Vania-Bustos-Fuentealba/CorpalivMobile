package com.duocuc.corpalivapp.data.repository // Paquete de las pruebas de la capa de repositorios.

import com.duocuc.corpalivapp.data.model.LoginRequestDto // Cuerpo que la app envía a POST /api/auth/login.
import com.duocuc.corpalivapp.data.model.SesionRemotaDto // Respuesta del login.
import com.duocuc.corpalivapp.data.network.AuthApiService // Servicio Retrofit que se reemplaza por un mock.
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.mockk // Crea el servicio simulado.
import java.io.IOException // Error de red que lanza Retrofit cuando no hay conexión.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** PARCIAL 4: comprueba que AuthRepository entrega la sesión o un mensaje claro para cada falla del login. */
class AuthRepositoryTest {

    private val api = mockk<AuthApiService>() // Servicio de autenticación simulado.
    private val repositorio = AuthRepository(api) // Repositorio bajo prueba.

    private val sesion = SesionRemotaDto( // Sesión ficticia: no son credenciales reales.
        token = "token-simulado", tipoToken = "Bearer", nombre = "Cuenta de prueba",
        correo = "prueba@example.com", rol = "ADMINISTRADOR", rolEtiqueta = "Administrador", inicio = 1L
    )

    @Test
    fun loginCorrecto_recortaElCorreoYEntregaLaSesion() = runTest {
        coEvery { api.login(any()) } returns sesion // El servidor acepta las credenciales.

        val resultado = repositorio.login("  prueba@example.com  ", "clave") // Correo con espacios, como lo podría escribir el usuario.

        assertEquals(sesion, resultado.getOrNull()) // Llega la sesión con su token.
        coVerify { api.login(LoginRequestDto(correo = "prueba@example.com", clave = "clave")) } // El correo viaja sin espacios.
    }

    @Test
    fun error401_informaCredencialesIncorrectas() = runTest {
        coEvery { api.login(any()) } throws httpError(401) // Correo o contraseña incorrectos.

        assertEquals("Correo o contraseña incorrectos", repositorio.login("a@b.cl", "x").exceptionOrNull()?.message)
    }

    @Test
    fun otroCodigo_informaElCodigo() = runTest {
        coEvery { api.login(any()) } throws httpError(503) // Servicio no disponible.

        assertEquals("El servidor respondió con un error (503)", repositorio.login("a@b.cl", "x").exceptionOrNull()?.message)
    }

    @Test
    fun sinConexion_informaQueRevisenLosServicios() = runTest {
        coEvery { api.login(any()) } throws IOException("sin red") // auth-service apagado.

        assertEquals(
            "No se pudo conectar con el servidor. Revisa que los servicios estén encendidos",
            repositorio.login("a@b.cl", "x").exceptionOrNull()?.message
        )
    }
}
