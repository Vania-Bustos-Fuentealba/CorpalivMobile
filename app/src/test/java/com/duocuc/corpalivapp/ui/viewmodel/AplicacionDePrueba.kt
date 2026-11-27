package com.duocuc.corpalivapp.ui.viewmodel // Paquete de las pruebas de ViewModels.

import android.app.Application // Contexto global que reciben los AndroidViewModel.
import io.mockk.every // Configura las respuestas del mock.
import io.mockk.mockk // Crea el Application simulado.
import java.io.File // Carpeta temporal donde DataStore guarda sus archivos.
import java.nio.file.Files // Crea la carpeta temporal.

/**
 * Application simulado para probar AndroidViewModel en la JVM, sin emulador.
 *
 * DataStore solo necesita applicationContext y filesDir. La carpeta es la misma para todas las
 * pruebas porque el delegado preferencesDataStore crea una única instancia por proceso.
 */
object AplicacionDePrueba {

    /** Carpeta temporal compartida por los DataStore de las pruebas (se borra al terminar la JVM). */
    val carpeta: File by lazy { Files.createTempDirectory("corpaliv-test").toFile().apply { deleteOnExit() } }

    /** Crea un Application cuyo filesDir apunta a [carpeta]. */
    fun crear(): Application {
        val app = mockk<Application>(relaxed = true) // Mock relajado: los métodos no configurados devuelven valores por defecto.
        every { app.applicationContext } returns app // DataStore usa el contexto de la aplicación.
        every { app.filesDir } returns carpeta // DataStore guarda sus archivos en filesDir/datastore.
        return app
    }
}
