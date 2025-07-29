// app/src/main/java/com/nextgen/mandato360/ui/UploadPhotoScreen.kt
package com.nextgen.mandato360.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.nextgen.mandato360.data.FirestoreRepository
import com.nextgen.mandato360.data.User
import kotlinx.coroutines.launch

/**
 * Tela para o usuário fazer upload de sua foto de perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPhotoScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher para escolher imagem da galeria
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upload de Foto", fontSize = 20.sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(16.dp))
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagem selecionada",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma imagem", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selecionar Foto")
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (uid == null) {
                        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    val uri = imageUri
                    if (uri == null) {
                        Toast.makeText(context, "Selecione uma imagem primeiro", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    isUploading = true
                    // Upload para Firebase Storage
                    val storageRef = FirebaseStorage.getInstance()
                        .reference.child("profile_images/$uid.jpg")
                    storageRef.putFile(uri)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception!!
                            storageRef.downloadUrl
                        }
                        .addOnCompleteListener { task ->
                            isUploading = false
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()
                                // Atualiza User no Firestore
                                coroutineScope.launch {
                                    val user = FirestoreRepository.getUser(uid)
                                        ?: User(uid = uid)
                                    user.apply {
                                        this.photoUrl = downloadUrl
                                    }
                                    FirestoreRepository.saveUser(user)
                                    Toast.makeText(context, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro no upload: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Fazer Upload")
            }
        }
    }
}