// TiendaApi.kt
package com.example.inventariolt.interfaces

import com.example.inventariolt.model.login.TiendaRequestDTO
import com.example.inventariolt.model.login.TiendaResponseDTO
import okhttp3.ResponseBody
import retrofit2.http.*

interface TiendaApi {

    @POST("/api/tiendas")
    suspend fun createTienda(
        @Body request: TiendaRequestDTO
    ): TiendaResponseDTO

    // Devuelve ResponseBody para poder ver la respuesta cruda
    @GET("/api/tiendas")
    suspend fun getAllTiendas(): ResponseBody

    // Devuelve ResponseBody para poder ver la respuesta cruda
    @GET("/api/tiendas/{id}")
    suspend fun getTiendaById(
        @Path("id") id: Long
    ): ResponseBody  // ← Cambiar a ResponseBody

    @PUT("/api/tiendas/{id}")
    suspend fun updateTienda(
        @Path("id") id: Long,
        @Body request: TiendaRequestDTO
    ): TiendaResponseDTO

    @DELETE("/api/tiendas/{id}")
    suspend fun deleteTienda(
        @Path("id") id: Long
    ): retrofit2.Response<Unit>
}