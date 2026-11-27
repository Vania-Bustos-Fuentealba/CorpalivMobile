package com.duocuc.corpalivapp.util // Paquete de las pruebas de utilidades.

import android.content.Context // Contexto que se simula con MockK.
import io.mockk.mockk // Crea el contexto simulado.
import org.junit.Assert.assertFalse // Comprueba una condición falsa.
import org.junit.Test // Marca cada método como prueba de JUnit.

/** G13: abrirWhatsApp no intenta abrir nada si el teléfono no tiene dígitos. */
class ContactoUtilsTest {

    @Test
    fun telefonoSinDigitos_retornaFalseSinAbrirWhatsApp() {
        val context = mockk<Context>() // Mock estricto: si se llamara a startActivity, la prueba fallaría.

        assertFalse(abrirWhatsApp(context, "sin número", "Hola")) // Se cancela antes de armar el enlace.
    }

    @Test
    fun telefonoVacio_retornaFalse() {
        assertFalse(abrirWhatsApp(mockk(), "", "Hola")) // Sin número no hay chat que abrir.
    }
}
