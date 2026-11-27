package com.duocuc.corpalivapp.data.network // Paquete de las pruebas de la capa de red.

import org.junit.Assert.assertNotNull // Comprueba que el servicio se haya creado.
import org.junit.Assert.assertSame // Comprueba que se reutiliza la misma instancia.
import org.junit.Test // Marca cada método como prueba de JUnit.

/**
 * Clientes Retrofit: los servicios se crean en el primer uso (by lazy) y se reutilizan.
 * Crear el servicio no abre conexiones, por eso se prueba en la JVM sin servidor.
 */
class ClientesRetrofitTest {

    @Test
    fun servicioDeOportunidades_seCreaUnaSolaVez() {
        val servicio = RetrofitClient.apiService // Primer uso: Retrofit implementa ApiService.

        assertNotNull(servicio)
        assertSame(servicio, RetrofitClient.apiService) // El segundo uso entrega la misma instancia.
    }

    @Test
    fun serviciosDeAuthYAportes_seCreanConElMismoCliente() {
        assertNotNull(RetrofitClient.authApi) // auth-service (puerto 8083).
        assertNotNull(RetrofitClient.aportesApi) // aportes-service (puerto 8082).
    }

    @Test
    fun apiExterna_seCreaSeparadaDelBackend() {
        val servicio = ExternalApiClient.api // JSONPlaceholder, sin el token de Corpaliv.

        assertNotNull(servicio)
        assertSame(servicio, ExternalApiClient.api)
    }
}
