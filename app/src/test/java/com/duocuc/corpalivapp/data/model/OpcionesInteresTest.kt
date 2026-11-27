package com.duocuc.corpalivapp.data.model // Paquete de las pruebas de los modelos de datos.

import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G11: opciones fijas del formulario de registro (motivos y frecuencias de donación). */
class OpcionesInteresTest {

    @Test
    fun motivos_sonAportarEIngresarAPrograma_enEseOrden() {
        assertEquals(listOf(MOTIVO_APORTAR, MOTIVO_INGRESAR), motivosRegistro.map { it.etiqueta }) // Orden del desplegable.
    }

    @Test
    fun cadaMotivo_tieneTextosYUnaConfirmacionConLaOportunidad() {
        motivosRegistro.forEach { motivo ->
            assertTrue(motivo.descripcion.isNotBlank()) // Explica qué implica el motivo.
            assertTrue(motivo.agradecimiento.isNotBlank()) // Título del resumen.
            assertTrue(motivo.confirmacion.contains("%s")) // Se reemplaza por el título de la oportunidad.
        }
        assertEquals(
            "Registramos tu postulación a «Escuela».",
            motivosRegistro[1].confirmacion.format("Escuela") // La plantilla se completa con String.format.
        )
    }

    @Test
    fun frecuencias_ofrecenLasTresOpcionesDeDonacion() {
        assertEquals(listOf("Donación única", "Mensual", "Anual"), frecuenciasDonacion)
    }
}
