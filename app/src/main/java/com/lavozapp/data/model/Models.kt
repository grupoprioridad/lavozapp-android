package com.lavozapp.data.model

data class AuthLoginResponse(
    val success: Boolean,
    val message: String? = null
)

data class AuthVerifyResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: LVPUser? = null
)

data class PerfilResponse(
    val success: Boolean,
    val usuario: LVPUser? = null,
    val beneficios: List<Beneficio>? = null
)

data class LVPUser(
    val id: Int,
    val email: String,
    val nombre: String? = null,
    val foto: String? = null,
    val token: String = "",
    val suscripcion_activa: Boolean = false,
    val suscripcion_expira: String? = null,
    val qr_data: String? = null
)

data class Beneficio(
    val id: Int,
    val titulo: String,
    val comercio: String,
    val descripcion: String? = null,
    val imagen: String? = null,
    val stock: Int = 0
)
