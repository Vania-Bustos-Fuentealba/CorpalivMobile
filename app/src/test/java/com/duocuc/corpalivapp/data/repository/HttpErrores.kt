package com.duocuc.corpalivapp.data.repository // Paquete de las pruebas de la capa de repositorios.

import okhttp3.ResponseBody.Companion.toResponseBody // Crea un cuerpo HTTP vacío para las respuestas de error simuladas.
import retrofit2.HttpException // Excepción que lanza Retrofit cuando el servidor responde con un código de error.
import retrofit2.Response // Respuesta completa de Retrofit, usada para fabricar el error.

/** Fabrica la HttpException que lanzaría Retrofit si el servidor respondiera con [codigo]. */
fun httpError(codigo: Int): HttpException = // Función auxiliar compartida por las pruebas de los repositorios.
    HttpException(Response.error<Any>(codigo, "".toResponseBody(null))) // Respuesta de error sin cuerpo, igual que un 4xx/5xx real.
