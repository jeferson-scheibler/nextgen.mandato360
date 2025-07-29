// app/src/main/java/com/nextgen/mandato360/ui/RoleScreen.kt
package com.nextgen.mandato360.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.nextgen.mandato360.UserSessionViewModel
import com.nextgen.mandato360.data.Cabinet
import com.nextgen.mandato360.data.FirestoreRepository
import com.nextgen.mandato360.data.User
import com.nextgen.mandato360.ui.theme.BlueMandato
import com.nextgen.mandato360.ui.theme.TealMandato
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleScreen(
    navController: NavController,
    session: UserSessionViewModel = viewModel()
) {
    // Firebase Auth e CoroutineScope
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    // Estados de UI
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var teamCode by remember { mutableStateOf("") }
    var creatingNewTeam by remember { mutableStateOf(false) }
    var primaryColor by remember { mutableStateOf(TealMandato) }
    var secondaryColor by remember { mutableStateOf(BlueMandato) }
    var showPrimaryDialog by remember { mutableStateOf(false) }
    var showSecondaryDialog by remember { mutableStateOf(false) }

    val roles = listOf(
        "Vereador", "Deputado Estadual", "Deputado Federal",
        "Senador", "Prefeito", "Secretário", "Assessor Parlamentar", "Outros"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Qual sua função no gabinete?", style = MaterialTheme.typography.headlineSmall)

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            TextField(
                value = selectedRole.orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Selecione sua função") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            selectedRole = role
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = teamCode,
            onValueChange = { teamCode = it },
            label = { Text("Código do gabinete") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !creatingNewTeam,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = creatingNewTeam, onCheckedChange = { creatingNewTeam = it })
            Spacer(Modifier.width(8.dp))
            Text("Criar novo gabinete")
        }

        if (creatingNewTeam) {
            Text("Personalize a identidade visual:", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showPrimaryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Primária", color = Color.White) }
                Button(
                    onClick = { showSecondaryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) { Text("Secundária", color = Color.White) }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (uid == null || selectedRole.isNullOrEmpty()) return@Button
                session.role = selectedRole!!
                session.generatedTeamCode = if (creatingNewTeam) {
                    teamCode.takeIf { it.isNotBlank() }
                        ?: UUID.randomUUID().toString().substring(0,6)
                } else teamCode
                session.primaryColor = primaryColor
                session.secondaryColor = secondaryColor

                // Salvar gabinete e usuário no Firestore
                scope.launch {
                    val cabinetToSave = Cabinet(
                        code = session.generatedTeamCode,
                        role = session.role,
                        primaryColor = primaryColor.value.toLong(),
                        secondaryColor = secondaryColor.value.toLong(),
                        members = listOf(uid)
                    )
                    FirestoreRepository.saveCabinet(cabinetToSave)

                    val existingUser = FirestoreRepository.getUser(uid) ?: User(uid = uid)
                    existingUser.apply {
                        cabinetCode = session.generatedTeamCode
                        role = session.role
                        lastLogin = System.currentTimeMillis()
                    }
                    FirestoreRepository.saveUser(existingUser)

                    navController.navigate("dashboard") {
                        popUpTo("role") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continuar")
        }
    }

    if (showPrimaryDialog) {
        ColorPickerDialog(
            title = "Escolha cor primária",
            colors = listOf(TealMandato, BlueMandato, Color.Magenta, Color.Cyan),
            onColorSelected = { primaryColor = it; showPrimaryDialog = false },
            onDismiss = { showPrimaryDialog = false }
        )
    }
    if (showSecondaryDialog) {
        ColorPickerDialog(
            title = "Escolha cor secundária",
            colors = listOf(BlueMandato, TealMandato, Color.Magenta, Color.Cyan),
            onColorSelected = { secondaryColor = it; showSecondaryDialog = false },
            onDismiss = { showSecondaryDialog = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    title: String,
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, shape = CircleShape)
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {}
    )
}