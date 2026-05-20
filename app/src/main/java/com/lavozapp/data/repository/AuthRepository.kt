package com.lavozapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.lavozapp.data.api.ApiService
import com.lavozapp.data.model.LVPUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lavoz_prefs")

class AuthRepository(private val context: Context) {
    private val api = ApiService.create()
    private val gson = Gson()

    companion object {
        private val USER_KEY = stringPreferencesKey("user_data")
    }

    suspend fun getSavedUser(): LVPUser? {
        val json = context.dataStore.data.map { it[USER_KEY] }.first() ?: return null
        return try { gson.fromJson(json, LVPUser::class.java) } catch (e: Exception) { null }
    }

    suspend fun saveUser(user: LVPUser) {
        context.dataStore.edit { it[USER_KEY] = gson.toJson(user) }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.remove(USER_KEY) }
    }

    suspend fun login(email: String): AuthResult {
        return try {
            val resp = api.login(mapOf("email" to email))
            if (resp.success) AuthResult.Success
            else AuthResult.Error(resp.message ?: "Error al iniciar sesión")
        } catch (e: Exception) {
            AuthResult.Error("Error de red: ${e.localizedMessage}")
        }
    }

    suspend fun verify(email: String, codigo: String): AuthResult {
        return try {
            val resp = api.verify(mapOf("email" to email, "codigo" to codigo))
            if (resp.success && resp.user != null) {
                val user = resp.user.copy(token = resp.token ?: "")
                saveUser(user)
                AuthResult.Success
            } else {
                AuthResult.Error(resp.message ?: "Código inválido")
            }
        } catch (e: Exception) {
            AuthResult.Error("Error de red: ${e.localizedMessage}")
        }
    }

    suspend fun getPerfil(token: String): PerfilResult {
        return try {
            val resp = api.getPerfil("Bearer $token")
            if (resp.success) PerfilResult.Success(resp)
            else PerfilResult.Error("Error al cargar perfil")
        } catch (e: Exception) {
            PerfilResult.Error("Error de red: ${e.localizedMessage}")
        }
    }
}

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class PerfilResult {
    data class Success(val data: PerfilResponse) : PerfilResult()
    data class Error(val message: String) : PerfilResult()
}
