package com.nextgen.mandato360

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nextgen.mandato360.data.FirestoreRepository
import com.nextgen.mandato360.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()
    val session: UserSessionViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()  // retorna kotlinx.coroutines.CoroutineScope

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Configuração do Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }
    val launcher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val acct = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(acct.idToken!!, null)
            isLoading = true
            auth.signInWithCredential(credential).addOnCompleteListener { task2 ->
                isLoading = false
                if (task2.isSuccessful) {
                    handlePostLogin(auth, prefs, session, navController, coroutineScope)
                } else {
                    Toast.makeText(context, "Erro: ${task2.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in falhou: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                handlePostLogin(auth, prefs, session, navController, coroutineScope)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Entrar")
            }

            Spacer(Modifier.height(12.dp))
            Text("Ou")
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { launcher.launch(googleClient.signInIntent) },
                Modifier.fillMaxWidth()
            ) {
                Text("Entrar com Google")
            }
        }
    }
}

/**
 * Pós-login:
 * - Guarda timestamp em SharedPreferences
 * - Cria/atualiza User no Firestore
 * - Decide navegação para Role ou Dashboard
 */
private fun handlePostLogin(
    auth: FirebaseAuth,
    prefs: android.content.SharedPreferences,
    session: UserSessionViewModel,
    navController: NavController,
    coroutineScope: CoroutineScope
) {
    val uid = auth.currentUser!!.uid
    val now = System.currentTimeMillis()
    prefs.edit().putLong("lastLogin", now).apply()

    coroutineScope.launch {
        val existing = FirestoreRepository.getUser(uid)
        if (existing == null) {
            // primeiro acesso
            val newUser = User(uid = uid, lastLogin = now)
            FirestoreRepository.saveUser(newUser)
            navController.navigate("role") { popUpTo("login") { inclusive = true } }
        } else {
            // já existia: atualiza timestamp
            existing.lastLogin = now
            FirestoreRepository.saveUser(existing)
            if (existing.cabinetCode.isNullOrBlank()) {
                navController.navigate("role") { popUpTo("login") { inclusive = true } }
            } else {
                // já vinculado
                session.role = existing.role.orEmpty()
                session.generatedTeamCode = existing.cabinetCode!!
                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
            }
        }
    }
}
