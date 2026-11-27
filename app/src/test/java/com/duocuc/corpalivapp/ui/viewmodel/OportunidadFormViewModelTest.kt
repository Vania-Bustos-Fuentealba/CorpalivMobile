package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import com.duocuc.corpalivapp.MainDispatcherRule // Controla Dispatchers.Main para probar el ViewModel sin Android.
import com.duocuc.corpalivapp.data.model.OportunidadModel // Oportunidad que se edita.
import com.duocuc.corpalivapp.data.model.OportunidadRequestDto // Cuerpo que se envía al servidor.
import com.duocuc.corpalivapp.data.repository.OportunidadesRepository // Repositorio que se reemplaza por un mock.
import io.mockk.coEvery // Configura respuestas para funciones suspendidas.
import io.mockk.coVerify // Comprueba las llamadas a funciones suspendidas.
import io.mockk.mockk // Crea el repositorio simulado.
import kotlinx.coroutines.CompletableDeferred // Simula una respuesta del servidor que todavía no llega.
import kotlinx.coroutines.ExperimentalCoroutinesApi // Habilita las API de prueba de corrutinas.
import kotlinx.coroutines.test.advanceUntilIdle // Ejecuta las tareas pendientes.
import kotlinx.coroutines.test.runCurrent // Ejecuta solo las tareas del instante actual.
import kotlinx.coroutines.test.runTest // Ejecuta corrutinas en un entorno de prueba controlado.
import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertFalse // Comprueba una condición falsa.
import org.junit.Assert.assertNull // Comprueba que un valor sea null.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Rule // Aplica la regla del dispatcher principal.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** PARCIAL 4: formulario de oportunidades del administrador (crear, editar, validar y guardar). */
@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sustituye Dispatchers.Main durante cada prueba.

    private val repositorio = mockk<OportunidadesRepository>() // Repositorio simulado: no se llama al servidor.

    private val existente = OportunidadModel( // Oportunidad que viene del listado para editarla.
        id = 4, titulo = "Equipamiento Escuela", categoria = "Donación Directa", descripcion = "Material didáctico",
        impactoSocial = "30 estudiantes", imagenUrl = null, contactoEmail = "escuela@corpaliv-demo.cl",
        metaFinanciamiento = 3_000_000, montoRecaudado = 250_000
    )

    /** ViewModel con un formulario válido ya completado. */
    private fun formularioValido() = OportunidadFormViewModel(repositorio).apply {
        onTituloChange("  Taller Alba Lab  ") // Con espacios para comprobar que se recortan.
        onCategoriaChange("RSE Empresarial")
        onDescripcionChange("Capacitación en oficios")
        onImpactoChange("12 jóvenes")
        onImagenChange("https://img.cl/a.png")
        onCorreoChange("")
        onMetaChange("5.000.000") // Con puntos: solo deben quedar los dígitos.
        onMontoChange("")
    }

    @Test
    fun estadoInicial_esCreacionConMontoCero() {
        val estado = OportunidadFormViewModel(repositorio).estado.value // Formulario recién abierto.

        assertFalse(estado.esEdicion) // No hay id: es una oportunidad nueva.
        assertEquals("0", estado.monto) // El monto parte en 0.
    }

    @Test
    fun editar_cargaLosDatosYNueva_losLimpia() {
        val viewModel = OportunidadFormViewModel(repositorio)

        viewModel.editar(existente) // Abre el formulario desde el botón "Editar".
        val enEdicion = viewModel.estado.value
        assertTrue(enEdicion.esEdicion)
        assertEquals(4, enEdicion.idEdicion)
        assertEquals("", enEdicion.imagenUrl) // La imagen null se muestra como campo vacío.
        assertEquals("3000000", enEdicion.meta)
        assertEquals("250000", enEdicion.monto)

        viewModel.nueva() // Abre el formulario desde "Nueva oportunidad".
        assertEquals(OportunidadFormUiState(), viewModel.estado.value) // Formulario vacío otra vez.
    }

    @Test
    fun montos_soloAceptanDigitosHastaNueve() {
        val viewModel = OportunidadFormViewModel(repositorio)

        viewModel.onMetaChange("$ 1.234.567.890.123") // Texto con símbolos y más de 9 dígitos.
        viewModel.onMontoChange("12a3") // Letras mezcladas.

        assertEquals("123456789", viewModel.estado.value.meta) // Solo dígitos, máximo 9 para no desbordar un Int.
        assertEquals("123", viewModel.estado.value.monto)
    }

    @Test
    fun validar_formularioVacio_marcaLosObligatorios() {
        val errores = OportunidadFormViewModel(repositorio).validar(OportunidadFormUiState()) // Nada completado.

        assertEquals("Campo obligatorio", errores["titulo"])
        assertEquals("Campo obligatorio", errores["categoria"])
        assertEquals("Campo obligatorio", errores["descripcion"])
        assertEquals("Campo obligatorio", errores["impactoSocial"])
        assertEquals("Ingresa una meta mayor a 0", errores["meta"])
        assertNull(errores["imagenUrl"]) // La imagen es opcional.
        assertNull(errores["contactoEmail"]) // El correo es opcional.
    }

    @Test
    fun validar_respetaLosLimitesDelServidor() {
        val viewModel = OportunidadFormViewModel(repositorio)
        val base = OportunidadFormUiState(titulo = "Taller", categoria = "RSE", descripcion = "d", impactoSocial = "i", meta = "1")

        assertTrue(viewModel.validar(base).isEmpty()) // Caso válido mínimo.
        assertEquals("Debe tener al menos 3 caracteres", viewModel.validar(base.copy(titulo = " ab "))["titulo"])
        assertEquals("Máximo 200 caracteres", viewModel.validar(base.copy(titulo = "a".repeat(201)))["titulo"])
        assertEquals("Máximo 80 caracteres", viewModel.validar(base.copy(categoria = "c".repeat(81)))["categoria"])
        assertEquals("Máximo 1000 caracteres", viewModel.validar(base.copy(descripcion = "d".repeat(1001)))["descripcion"])
        assertEquals("Máximo 500 caracteres", viewModel.validar(base.copy(impactoSocial = "i".repeat(501)))["impactoSocial"])
        assertEquals("Debe comenzar con http:// o https://", viewModel.validar(base.copy(imagenUrl = "ftp://img"))["imagenUrl"])
        assertEquals("Máximo 500 caracteres", viewModel.validar(base.copy(imagenUrl = "https://" + "x".repeat(500)))["imagenUrl"])
        assertTrue(viewModel.validar(base.copy(imagenUrl = "http://img.cl/a.png")).isEmpty()) // http también es válido.
        assertEquals("Correo inválido", viewModel.validar(base.copy(contactoEmail = "no-es-correo"))["contactoEmail"])
        assertTrue(viewModel.validar(base.copy(contactoEmail = "rse@corpaliv-demo.cl")).isEmpty())
        assertEquals("Ingresa una meta mayor a 0", viewModel.validar(base.copy(meta = "0"))["meta"])
    }

    @Test
    fun guardarConErrores_noLlamaAlServidorYAlEscribirSeBorraElError() = runTest {
        val viewModel = OportunidadFormViewModel(repositorio)

        viewModel.guardar() // Intenta guardar el formulario vacío.
        advanceUntilIdle()

        assertEquals("Campo obligatorio", viewModel.estado.value.errores["titulo"]) // Se muestran los errores.
        coVerify(exactly = 0) { repositorio.crear(any()) } // No se envió nada.

        viewModel.onTituloChange("Nuevo") // El usuario corrige el título.
        assertNull(viewModel.estado.value.errores["titulo"]) // Su error desaparece.
        assertEquals("Campo obligatorio", viewModel.estado.value.errores["categoria"]) // Los demás se mantienen.
    }

    @Test
    fun guardarNueva_creaConLosTextosLimpios() = runTest {
        coEvery { repositorio.crear(any()) } returns Result.success(existente) // El servidor guarda la oportunidad.
        val viewModel = formularioValido()

        viewModel.guardar() // Pulsa "Guardar".
        advanceUntilIdle()

        coVerify {
            repositorio.crear(
                OportunidadRequestDto(
                    titulo = "Taller Alba Lab", categoria = "RSE Empresarial", descripcion = "Capacitación en oficios",
                    impactoSocial = "12 jóvenes", imagenUrl = "https://img.cl/a.png", contactoEmail = null, // El correo vacío no se envía.
                    metaFinanciamiento = 5_000_000, montoRecaudado = 0 // El monto vacío se envía como 0.
                )
            )
        }
        assertTrue(viewModel.estado.value.guardado) // La pantalla vuelve al listado.
        assertFalse(viewModel.estado.value.guardando)

        viewModel.confirmarGuardado() // La pantalla ya navegó.
        assertFalse(viewModel.estado.value.guardado) // No se repite la navegación.
    }

    @Test
    fun guardarEdicion_actualizaLaOportunidadExistente() = runTest {
        coEvery { repositorio.actualizar(4, any()) } returns Result.success(existente) // El servidor actualiza.
        val viewModel = OportunidadFormViewModel(repositorio).apply { editar(existente) }

        viewModel.guardar()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.actualizar(4, any()) } // Se usó PUT con el id correcto.
        coVerify(exactly = 0) { repositorio.crear(any()) } // No se creó una nueva.
        assertTrue(viewModel.estado.value.guardado)
    }

    @Test
    fun errorDelServidor_quedaEnElFormularioConElMensaje() = runTest {
        coEvery { repositorio.crear(any()) } returns Result.failure(Exception("Tu sesión no es válida o expiró. Vuelve a iniciar sesión"))
        val viewModel = formularioValido()

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals("Tu sesión no es válida o expiró. Vuelve a iniciar sesión", viewModel.estado.value.errorEnvio)
        assertFalse(viewModel.estado.value.guardado) // No vuelve al listado.
        assertFalse(viewModel.estado.value.guardando) // El botón se habilita otra vez.
    }

    @Test
    fun errorSinMensaje_usaUnMensajePorDefecto() = runTest {
        coEvery { repositorio.crear(any()) } returns Result.failure(Exception()) // Falla sin mensaje.
        val viewModel = formularioValido()

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals("No se pudo guardar la oportunidad", viewModel.estado.value.errorEnvio)
    }

    @Test
    fun guardarDosVeces_mientrasEspera_enviaUnaSolaPeticion() = runTest {
        val respuesta = CompletableDeferred<Result<OportunidadModel>>() // Respuesta que el test entrega más tarde.
        coEvery { repositorio.crear(any()) } coAnswers { respuesta.await() }
        val viewModel = formularioValido()

        viewModel.guardar() // Primer toque.
        runCurrent() // La petición queda en curso.
        assertTrue(viewModel.estado.value.guardando)
        viewModel.guardar() // Segundo toque mientras espera.
        respuesta.complete(Result.success(existente)) // Llega la respuesta.
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.crear(any()) } // Solo un envío.
    }
}
