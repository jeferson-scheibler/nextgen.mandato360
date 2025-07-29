package com.nextgen.mandato360

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.nextgen.mandato360.data.FirestoreRepository

@Composable
fun SplashScreen(navController: NavController) {
    // Sessão & Auth
    val auth = FirebaseAuth.getInstance()
    val session: UserSessionViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // Efeito de inicialização / navegação
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
        } else {
            val uid = auth.currentUser!!.uid
            val user = FirestoreRepository.getUser(uid)
            if (user?.cabinetCode.isNullOrBlank()) {
                navController.navigate("role") { popUpTo("splash") { inclusive = true } }
            } else {
                session.generatedTeamCode = user.cabinetCode!!
                session.role = user.role.orEmpty()
                navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
            }
        }
    }

    // UI do splash com fundo cinza e logo centralizado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_mandato), // seu logo em drawable/logo.png
            contentDescription = "Logo Mandato360",
            modifier = Modifier
                .size(200.dp)
        )
    }
}
