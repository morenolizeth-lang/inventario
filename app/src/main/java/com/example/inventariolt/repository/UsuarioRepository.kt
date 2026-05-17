package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.login.UsuarioRequestDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import okhttp3.MultipartBody

class UsuarioRepository {
    suspend fun getUsuarioById(id: Long): Result<UsuarioResponseDTO> {
        return try {
            val response = RetrofitClient.usuarioApi.getById(id)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun updateUsuario(id: Long, request: UsuarioRequestDTO): Result<UsuarioResponseDTO> {
        return try {
            val response = RetrofitClient.usuarioApi.update(id, request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun uploadFotoPerfil(id: Long, file: MultipartBody.Part): Result<UsuarioResponseDTO> {
        return try {
            val response = RetrofitClient.usuarioApi.uploadFotoPerfil(id, file)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}