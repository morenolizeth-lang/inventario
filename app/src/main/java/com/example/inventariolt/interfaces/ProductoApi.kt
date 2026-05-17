package com.example.inventariolt.interfaces


import com.example.inventariolt.model.inventario.ProductoRequestDTO
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import retrofit2.Response
import retrofit2.http.*


interface ProductoApi {
    @POST("api/productos")
    suspend fun create(@Body request: ProductoRequestDTO): ProductoResponseDTO

    @GET("api/productos")
    suspend fun getAll(): List<ProductoResponseDTO>

    @GET("api/productos/{id}")
    suspend fun getById(@Path("id") id: Long): ProductoResponseDTO

    @PUT("api/productos/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: ProductoRequestDTO): ProductoResponseDTO

    @DELETE("api/productos/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
