package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.data.model.ErroresEmpresa // Estructura de errores del formulario de empresa.
import com.duocuc.corpalivapp.data.model.MOTIVO_APORTAR // Motivo que pide frecuencia y se envía como APORTE.
import com.duocuc.corpalivapp.data.model.MOTIVO_INGRESAR // Motivo que se envía como POSTULACION.
import com.duocuc.corpalivapp.data.model.RegistroGuardado // Respuesta que entrega aportes-service.
import com.duocuc.corpalivapp.data.model.TipoRegistro // Tipo de formulario (PERSONA o EMPRESA).
import com.duocuc.corpalivapp.data.model.UsuarioUiState // Estado inicial del formulario.
import com.duocuc.corpalivapp.data.repository.AportesRepository // Repositorio que se simula con MockK.
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.mockk // Crea el repositorio simulado.
import java.io.IOException // Error de red.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.test.advanceUntilIdle // Ejecuta las tareas pendientes (incluidos los delay simulados).
import kotlinx.coroutines.test.runCurrent // Ejecuta solo las tareas listas, sin avanzar el reloj virtual.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertFalse // Comprueba una condición falsa.
import org.junit.Assert.assertNull // Comprueba que un valor sea null.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/**
 * Parcial 4: envío del formulario de registro a aportes-service (UsuarioViewModel).
 *
 * Cubre la solicitud que se arma para cada caso (aporte, postulación y empresa), el envío con
 * barra de carga, los errores del servidor y la revalidación en tiempo real del formulario de empresa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelEnvioTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val repositorio = mockk<AportesRepository>() // aportes-service simulado.

    private val guardado = RegistroGuardado(id = 1L, tipo = "PERSONA", oportunidad = "Programa") // Respuesta mínima del servidor.

    private fun personaQueAporta() = UsuarioViewModel(repositorio).apply { // Persona completa que aporta mensualmente.
        seleccionarOportunidad("Programa Alba Lab") // Se elige antes porque seleccionar deja el formulario vacío.
        onNombreChange(" Benjamín ") // Con espacios para comprobar el trim.
        onApellidoChange("Pérez")
        onRunChange("11.111.111-1")
        onCorreoChange(" correo@ejemplo.cl ")
        onTelefonoChange("12345678")
        onMotivoChange(MOTIVO_APORTAR)
        onFrecuenciaChange("Mensual")
    }

    private fun empresaCompleta() = UsuarioViewModel(repositorio).apply { // Empresa completa sin oportunidad elegida.
        onTipoChange(TipoRegistro.EMPRESA)
        onRazonSocialChange("Fundación Ejemplo SpA ")
        onRutEmpresaChange("76.543.210-3")
        onGiroChange("Servicios de ejemplo")
        onRepresentanteChange("Ana Ejemplo")
        onRutRepresentanteChange("12345678-5")
        onCorreoEmpresaChange("contacto@ejemplo.cl")
        onTelefonoEmpresaChange("212345678")
        onDireccionEmpresaChange("Av. Providencia 1234")
    }

    @Test
    fun personaQueAporta_armaUnaSolicitudDeAporteConFrecuencia() {
        val solicitud = personaQueAporta().construirSolicitud()

        assertEquals("PERSONA", solicitud.tipo)
        assertEquals("APORTE", solicitud.motivo) // Aportar se envía como APORTE.
        assertEquals("Programa Alba Lab", solicitud.oportunidad)
        assertEquals("Benjamín Pérez", solicitud.nombre) // Nombre y apellido sin espacios sobrantes.
        assertEquals("correo@ejemplo.cl", solicitud.correo)
        assertEquals("+569 12345678", solicitud.telefono) // Celular con prefijo.
        assertEquals("Mensual", solicitud.tipoAporte) // Solo quien aporta envía la frecuencia.
        assertNull(solicitud.direccion) // Los campos de empresa no viajan.
    }

    @Test
    fun personaQuePostula_sinOportunidad_usaGeneralYNoEnviaFrecuencia() {
        val viewModel = UsuarioViewModel(repositorio).apply {
            onMotivoChange(MOTIVO_INGRESAR)
            onFrecuenciaChange("Mensual") // Aunque quede escrita, no aplica a una postulación.
        }

        val solicitud = viewModel.construirSolicitud()

        assertEquals("POSTULACION", solicitud.motivo)
        assertEquals("General", solicitud.oportunidad) // El servidor exige una oportunidad.
        assertNull(solicitud.tipoAporte)
    }

    @Test
    fun empresa_armaUnaSolicitudDeAporteConSusDatos() {
        val solicitud = empresaCompleta().construirSolicitud()

        assertEquals("EMPRESA", solicitud.tipo)
        assertEquals("APORTE", solicitud.motivo) // La empresa siempre aporta.
        assertEquals("General", solicitud.oportunidad)
        assertEquals("Fundación Ejemplo SpA", solicitud.nombre)
        assertEquals("+56 212345678", solicitud.telefono) // Teléfono de empresa con prefijo +56.
        assertEquals("Av. Providencia 1234", solicitud.direccion)
        assertEquals("Servicios de ejemplo", solicitud.giro)
        assertEquals("Ana Ejemplo", solicitud.representante)
        assertNull(solicitud.tipoAporte) // La frecuencia es solo de personas.
    }

    @Test
    fun envioExitoso_completaLaBarraYAvisaALaPantalla() = runTest {
        coEvery { repositorio.registrar(any()) } returns Result.success(guardado) // El servidor guarda el registro.
        val viewModel = personaQueAporta()

        viewModel.enviarRegistro()
        runCurrent() // Arranca el envío sin avanzar el reloj.
        assertTrue(viewModel.estado.value.enviando) // La barra de carga está activa.

        advanceUntilIdle() // Completa los 20 pasos y la respuesta del servidor.

        val estado = viewModel.estado.value
        assertFalse(estado.enviando)
        assertTrue(estado.completado) // La pantalla debe navegar al resumen.
        assertEquals(1f, estado.progreso) // La barra llegó al final.
        assertNull(estado.errorEnvio)
        coVerify(exactly = 1) { repositorio.registrar(match { it.motivo == "APORTE" && it.tipoAporte == "Mensual" }) }

        viewModel.confirmarNavegacion() // La pantalla ya navegó.
        assertFalse(viewModel.estado.value.completado) // El aviso se apaga para no repetir la navegación.
    }

    @Test
    fun envioRechazado_muestraElMensajeDelServidor() = runTest {
        coEvery { repositorio.registrar(any()) } returns Result.failure(IOException("Sin conexión con el servidor"))
        val viewModel = empresaCompleta()

        viewModel.enviarRegistro()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.enviando)
        assertFalse(estado.completado) // Se queda en el formulario.
        assertEquals("Sin conexión con el servidor", estado.errorEnvio)
    }

    @Test
    fun envioRechazadoSinMensaje_usaUnTextoPorDefecto() = runTest {
        coEvery { repositorio.registrar(any()) } returns Result.failure(RuntimeException()) // Error sin mensaje.
        val viewModel = personaQueAporta()

        viewModel.enviarRegistro()
        advanceUntilIdle()

        assertEquals("No se pudo enviar el registro", viewModel.estado.value.errorEnvio)
    }

    @Test
    fun envioEnCurso_ignoraUnSegundoToque() = runTest {
        coEvery { repositorio.registrar(any()) } returns Result.success(guardado)
        val viewModel = personaQueAporta()

        viewModel.enviarRegistro()
        runCurrent() // El primer envío ya marcó enviando = true.
        viewModel.enviarRegistro() // Segundo toque mientras se envía.
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.registrar(any()) } // Solo se envió una vez.
    }

    @Test
    fun reiniciarDuranteElEnvio_cancelaYDejaElFormularioVacio() = runTest {
        coEvery { repositorio.registrar(any()) } returns Result.success(guardado)
        val viewModel = personaQueAporta()

        viewModel.enviarRegistro()
        runCurrent()
        viewModel.reiniciar() // El usuario sale del formulario.
        advanceUntilIdle()

        assertEquals(UsuarioUiState(), viewModel.estado.value) // No quedó rastro del envío cancelado.
    }

    @Test
    fun empresa_revalidaEnTiempoRealCadaCampoConError() {
        val viewModel = UsuarioViewModel(repositorio).apply { onTipoChange(TipoRegistro.EMPRESA) }
        assertFalse(viewModel.validarFormulario()) // Marca todos los campos vacíos.

        viewModel.onRazonSocialChange("AB") // Valores cortos o mal formados.
        viewModel.onRutEmpresaChange("123")
        viewModel.onGiroChange("TI")
        viewModel.onRepresentanteChange("Al")
        viewModel.onRutRepresentanteChange("1")
        viewModel.onCorreoEmpresaChange("sin-arroba")
        viewModel.onTelefonoEmpresaChange("2123")
        viewModel.onDireccionEmpresaChange("Av 1")

        val errores = viewModel.estado.value.errores.empresa // Los mensajes cambian mientras se escribe.
        assertEquals("Debe tener al menos 3 caracteres", errores.razonSocial)
        assertEquals("RUT inválido", errores.rut)
        assertEquals("Debe tener al menos 3 caracteres", errores.giro)
        assertEquals("Debe tener al menos 3 caracteres", errores.representante)
        assertEquals("RUT inválido", errores.rutRepresentante)
        assertEquals("Correo inválido", errores.correo)
        assertEquals("Ingresa los 9 dígitos del teléfono", errores.telefono)
        assertEquals("Ingresa una dirección completa", errores.direccion)

        viewModel.onRazonSocialChange("Fundación Ejemplo SpA") // Se corrige cada campo.
        viewModel.onRutEmpresaChange("76.543.210-3")
        viewModel.onGiroChange("Servicios de ejemplo")
        viewModel.onRepresentanteChange("Ana Ejemplo")
        viewModel.onRutRepresentanteChange("12345678-5")
        viewModel.onCorreoEmpresaChange("contacto@ejemplo.cl")
        viewModel.onTelefonoEmpresaChange("212345678")
        viewModel.onDireccionEmpresaChange("Av. Providencia 1234")

        assertEquals(ErroresEmpresa(), viewModel.estado.value.errores.empresa) // Sin errores.
        assertTrue(viewModel.validarFormulario())
    }

    @Test
    fun persona_revalidaNombreYApellidoCortos() {
        val viewModel = UsuarioViewModel(repositorio)
        viewModel.validarFormulario() // Marca los campos vacíos.

        viewModel.onNombreChange("Al")
        viewModel.onApellidoChange("P")

        val errores = viewModel.estado.value.errores.persona
        assertEquals("Debe tener al menos 3 caracteres", errores.nombre)
        assertEquals("Debe tener al menos 2 caracteres", errores.apellido)
    }
}
