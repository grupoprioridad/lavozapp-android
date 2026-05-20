package com.lavozapp.ui.socio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lavozapp.LavozApp
import com.lavozapp.data.model.LVPUser
import com.lavozapp.data.repository.AuthResult
import com.lavozapp.data.repository.PerfilResult
import com.lavozapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SocioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { LavozApp.instance.authRepository }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<LVPUser?>(null) }
    var perfil by remember { mutableStateOf<LVPUser?>(null) }
    var beneficios by remember { mutableStateOf<List<com.lavozapp.data.model.Beneficio>>(emptyList()) }
    var email by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var paso by remember { mutableIntStateOf(0) } // 0=email, 1=codigo, 2=credencial
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        user = repo.getSavedUser()
        if (user != null) { paso = 2; loadPerfil(repo, user!!.token) { p, b -> perfil = p; beneficios = b } }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        when (paso) {
            0 -> LoginEmailStep(
                email = email, onEmailChange = { email = it },
                isLoading = isLoading, error = error, onErrorClear = { error = null },
                onNext = { e ->
                    scope.launch {
                        isLoading = true; error = null
                        when (val r = repo.login(e)) {
                            is AuthResult.Success -> paso = 1
                            is AuthResult.Error -> error = r.message
                        }
                        isLoading = false
                    }
                }
            )
            1 -> LoginCodeStep(
                codigo = codigo, onCodigoChange = { codigo = it },
                email = email, isLoading = isLoading, error = error,
                onErrorClear = { error = null },
                onVerify = { c ->
                    scope.launch {
                        isLoading = true; error = null
                        when (val r = repo.verify(email, c)) {
                            is AuthResult.Success -> {
                                user = repo.getSavedUser()
                                paso = 2
                                user?.let { u -> loadPerfil(repo, u.token) { p, b -> perfil = p; beneficios = b } }
                            }
                            is AuthResult.Error -> error = r.message
                        }
                        isLoading = false
                    }
                },
                onResend = {
                    scope.launch { repo.login(email) }
                }
            )
            2 -> CredencialScreen(
                user = user, perfil = perfil, beneficios = beneficios,
                onLogout = {
                    scope.launch { repo.clearUser() }
                    user = null; perfil = null; beneficios = emptyList(); paso = 0
                }
            )
        }
    }
}

private suspend fun loadPerfil(
    repo: com.lavozapp.data.repository.AuthRepository,
    token: String,
    onResult: (LVPUser?, List<com.lavozapp.data.model.Beneficio>) -> Unit
) {
    when (val r = repo.getPerfil(token)) {
        is PerfilResult.Success -> onResult(r.data.usuario, r.data.beneficios ?: emptyList())
        is PerfilResult.Error -> onResult(null, emptyList())
    }
}

@Composable
private fun LoginEmailStep(
    email: String, onEmailChange: (String) -> Unit,
    isLoading: Boolean, error: String?, onErrorClear: () -> Unit,
    onNext: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(24.dp))
        Icon(Icons.Default.Person, contentDescription = null,
            modifier = Modifier.size(56.dp), tint = LvpRed)
        Spacer(Modifier.height(16.dp))
        Text("Red de Beneficios LVP", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Ingresa tu correo registrado para acceder\na los beneficios exclusivos para socios.",
            fontSize = 14.sp, color = LvpGray500, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email, onValueChange = { onEmailChange(it); onErrorClear() },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        error?.let {
            Text(it, color = LvpRed, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { onNext(email) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = email.isNotEmpty() && !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LvpRed)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White)
            else Text("Enviar código", fontSize = 16.sp)
        }
    }
}

@Composable
private fun LoginCodeStep(
    codigo: String, onCodigoChange: (String) -> Unit,
    email: String, isLoading: Boolean, error: String?, onErrorClear: () -> Unit,
    onVerify: (String) -> Unit, onResend: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(32.dp))
        Text("Código de verificación", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Enviamos un código a $email", fontSize = 12.sp, color = LvpGray500)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = codigo, onValueChange = { if (it.length <= 6) { onCodigoChange(it); onErrorClear() } },
            label = { Text("Código de 6 dígitos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        error?.let {
            Text(it, color = LvpRed, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { onVerify(codigo) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = codigo.length == 6 && !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LvpRed)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White)
            else Text("Verificar", fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onResend) {
            Text("Reenviar código", color = LvpRed)
        }
    }
}

@Composable
private fun CredencialScreen(
    user: LVPUser?, perfil: LVPUser?,
    beneficios: List<com.lavozapp.data.model.Beneficio>,
    onLogout: () -> Unit
) {
    val p = perfil ?: user
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Header toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onLogout) { Text("Salir", color = LvpRed) }
        }

        Spacer(Modifier.height(8.dp))

        // Credencial card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // Red header
                Box(
                    modifier = Modifier.fillMaxWidth().background(LvpRed).padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RADIO LA VOZ DE PUCÓN", fontSize = 9.sp,
                            color = White.copy(alpha = 0.8f), letterSpacing = 2.sp)
                        Text("SOCIOS", fontFamily = FontFamily.Serif,
                            fontSize = 28.sp, fontWeight = FontWeight.Black, color = White)
                    }
                }

                // Photo placeholder
                Box(
                    modifier = Modifier.offset(y = (-40).dp).align(Alignment.CenterHorizontally)
                        .size(80.dp).clip(CircleShape).background(LvpSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null,
                        modifier = Modifier.size(36.dp), tint = LvpGray500)
                }

                Spacer(Modifier.height((-30).dp))

                // Info
                Text(p?.nombre ?: "Socio LVP", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Socio #${String.format("%05d", p?.id ?: 0)}", fontSize = 12.sp, color = LvpGray500)
                Text(p?.email ?: "", fontSize = 12.sp, color = LvpGrayMuted)

                Spacer(Modifier.height(8.dp))

                // Status
                val activa = p?.suscripcion_activa ?: false
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (activa) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (activa) LvpGreen else LvpRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (activa) "MEMBRESÍA ACTIVA" else "MEMBRESÍA INACTIVA",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = if (activa) LvpGreen else LvpRed
                    )
                }

                if (activa && p?.suscripcion_expira != null) {
                    Text("Válida hasta ${formatearFecha(p.suscripcion_expira)}",
                        fontSize = 11.sp, color = LvpGray500,
                        modifier = Modifier.padding(top = 4.dp))
                }

                // QR placeholder
                Box(
                    modifier = Modifier.padding(16.dp).size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp)).background(LvpSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = LvpGray500)
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Beneficios
        if (beneficios.isNotEmpty()) {
            Text("BENEFICIOS DISPONIBLES", fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = LvpRed,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))

            Spacer(Modifier.height(12.dp))

            beneficios.forEach { b ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = LvpSurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                .background(LvpRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null,
                                tint = LvpRed, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(b.titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(b.comercio, fontSize = 12.sp, color = LvpGray500)
                        }
                        Badge(
                            containerColor = if (b.stock > 0) LvpRed else LvpGray500
                        ) {
                            Text("${b.stock}", fontSize = 10.sp, color = White)
                        }
                    }
                }
            }
        } else {
            Text("No hay beneficios disponibles", fontSize = 12.sp,
                color = LvpGray500, modifier = Modifier.padding(24.dp))
        }
    }
}

private fun formatearFecha(fecha: String): String {
    return try {
        val parts = fecha.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else fecha
    } catch (e: Exception) { fecha }
}
