package com.duocuc.corpalivapp.data.model // Paquete de las pruebas de los modelos de datos.

import org.junit.Assert.assertEquals // Compara el valor esperado con el obtenido.
import org.junit.Assert.assertTrue // Comprueba una condición verdadera.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G2 y G4: cálculos de OportunidadModel y comportamiento de Persona y Empleado. */
class OportunidadModelTest {

    private fun oportunidad(meta: Int, recaudado: Int, correo: String? = null) = OportunidadModel( // Crea una oportunidad con los montos indicados.
        id = 1, titulo = "Taller", categoria = "RSE", descripcion = "d", impactoSocial = "i",
        imagenUrl = null, contactoEmail = correo, metaFinanciamiento = meta, montoRecaudado = recaudado
    )

    @Test
    fun montoFaltante_nuncaEsNegativo() {
        assertEquals(3_800_000, oportunidad(5_000_000, 1_200_000).calcularMontoFaltante()) // Resta normal.
        assertEquals(0, oportunidad(1_000, 2_000).calcularMontoFaltante()) // Meta superada: no hay faltante.
    }

    @Test
    fun porcentajeAvance_usaDivisionDecimalYEvitaDividirPorCero() {
        assertEquals(24, oportunidad(5_000_000, 1_200_000).calcularPorcentajeAvance()) // 1,2 M de 5 M = 24 %.
        assertEquals(0, oportunidad(0, 100).calcularPorcentajeAvance()) // Meta inválida: 0 %.
    }

    @Test
    fun prioridad_segunElRangoDeAvance() {
        assertEquals("Urgencia Alta (Requiere RSE)", oportunidad(100, 29).obtenerPrioridadVinculacion())
        assertEquals("Prioridad Media (En Avance)", oportunidad(100, 30).obtenerPrioridadVinculacion())
        assertEquals("Prioridad Media (En Avance)", oportunidad(100, 75).obtenerPrioridadVinculacion())
        assertEquals("Fase Final de Financiamiento", oportunidad(100, 76).obtenerPrioridadVinculacion())
        assertEquals("Proyecto Completo", oportunidad(100, 100).obtenerPrioridadVinculacion())
    }

    @Test
    fun contactoSeguro_usaElCorreoInstitucionalSiFalta() {
        assertEquals("rse@corpaliv-demo.cl", oportunidad(1, 0, "rse@corpaliv-demo.cl").obtenerContactoSeguro()) // Usa el propio.
        assertEquals("contacto@corpaliv-demo.cl", oportunidad(1, 0, null).obtenerContactoSeguro()) // Null safety.
        assertEquals("contacto@corpaliv-demo.cl", oportunidad(1, 0, "").obtenerContactoSeguro()) // Vacío.
    }

    @Test
    fun empleado_sobrescribePresentarseYMuestraSuPuesto() {
        val persona = Persona("Camila Silva", 28) // Clase base.
        val empleado = Empleado("Roberto Morales", 35, "Coordinador Alba Lab", 850000.0) // Subclase.

        assertEquals("Hola, mi nombre es Camila Silva y tengo 28 años.", persona.presentarse())
        assertTrue(empleado.presentarse().contains("desempeño el cargo de Coordinador Alba Lab")) // Polimorfismo.
        assertEquals("Roberto Morales trabaja como Coordinador Alba Lab en la red de inclusión.", empleado.mostrarPuesto())
        assertTrue(empleado.obtenerFichaResumida().startsWith("Empleado: Roberto Morales | Cargo: Coordinador Alba Lab"))
    }
}
