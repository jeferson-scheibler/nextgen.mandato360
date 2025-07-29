// app/src/main/java/com/nextgen/mandato360/ui/DashboardScreen.kt
package com.nextgen.mandato360.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.nextgen.mandato360.data.User
import com.nextgen.mandato360.data.FirestoreRepository
import com.nextgen.mandato360.UserSessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController
) {
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid
    val session: UserSessionViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // Carrega gabinete e membros
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(session.generatedTeamCode) {
        val code = session.generatedTeamCode
        if (code.isNotBlank()) {
            val cabinetInfo = FirestoreRepository.getCabinet(code)
            cabinetInfo?.let {
                session.role = it.role
                session.primaryColor = Color(it.primaryColor.toULong())
                session.secondaryColor = Color(it.secondaryColor.toULong())
                // Carrega perfis dos membros
                coroutineScope.launch {
                    members = it.members.mapNotNull { uid ->
                        FirestoreRepository.getUser(uid)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sala do Gabinete", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = session.primaryColor,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info do gabinete com QR
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Código do Gabinete", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                session.generatedTeamCode,
                            style = MaterialTheme.typography.headlineSmall,
                            color = session.primaryColor,
                            fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(16.dp))
                            QrCodeImage(
                                content = session.generatedTeamCode,
                                size = 80.dp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Função: ${session.role}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Seção Equipe
            item {
                Text("Equipe", style = MaterialTheme.typography.titleMedium)
            }
            items(members) { member ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!member.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = member.photoUrl,
                                contentDescription = member.name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.LightGray, CircleShape)
                                    .padding(2.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    member.name.first().uppercaseChar().toString(),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(member.name, style = MaterialTheme.typography.bodyLarge)
                            Text(member.role.orEmpty(), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.weight(1f))
                        if (member.uid == currentUid && member.photoUrl.isNullOrBlank()) {
                            OutlinedButton(onClick = { navController.navigate("uploadPhoto") }) {
                                Text("Upload Foto")
                            }
                        }
                    }
                }
            }

            // Ações Rápidas
            item {
                Text("Ações Rápidas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Protocolos",
                        icon = Icons.Filled.List,
                        primaryColor = session.primaryColor
                    ) { /* TODO */ }
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Financeiro",
                        icon = Icons.Filled.AttachMoney,
                        primaryColor = session.secondaryColor
                    ) { /* TODO */ }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(32.dp), tint = primaryColor)
            Spacer(Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Gera imagem QR Code de um texto.
 */
@Composable
fun QrCodeImage(content: String, size: Dp) {
    if (content.isBlank()) {
        // fallback: ícone ou caixa vazia
        Box(
            modifier = Modifier
                .size(size)
                .background(color = Color.LightGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.QrCode,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    } else {
        val bitmap = remember(content) { generateQrBitmap(content) }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(size)
        )
    }
}

private fun generateQrBitmap(content: String): Bitmap {
    val matrix: BitMatrix = MultiFormatWriter()
        .encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = matrix.width
    val height = matrix.height
    val pixels = IntArray(width * height) { i ->
        val x = i % width
        val y = i / width
        if (matrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}
