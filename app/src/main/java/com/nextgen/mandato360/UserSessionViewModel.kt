// app/src/main/java/com/nextgen/mandato360/UserSessionViewModel.kt
package com.nextgen.mandato360

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.mandato360.data.Cabinet
import com.nextgen.mandato360.data.FirestoreRepository
import com.nextgen.mandato360.ui.theme.TealMandato
import com.nextgen.mandato360.ui.theme.BlueMandato
import kotlinx.coroutines.launch

/**
 * Mantém o estado da sessão do usuário,
 * incluindo função, código de gabinete e identidade visual.
 */
class UserSessionViewModel : ViewModel() {
    /** Função selecionada pelo usuário. */
    var role by mutableStateOf("")

    /** Código do gabinete gerado ou informado. */
    var generatedTeamCode by mutableStateOf("")

    /** Indica se está criando um novo gabinete. */
    var creatingNewTeam by mutableStateOf(false)

    /** Cor primária da identidade visual. */
    var primaryColor by mutableStateOf(TealMandato)

    /** Cor secundária da identidade visual. */
    var secondaryColor by mutableStateOf(BlueMandato)

    /**
     * Persiste o gabinete atual no Firestore.
     */
    fun persistCabinet() {
        viewModelScope.launch {
            val cabinet = Cabinet(
                code = generatedTeamCode,
                role = role,
                primaryColor = primaryColor.value.toLong(),
                secondaryColor = secondaryColor.value.toLong(),
                members = emptyList()
            )
            FirestoreRepository.saveCabinet(cabinet)
        }
    }

    /**
     * Carrega dados do gabinete pelo código e invoca callback com o resultado.
     */
    fun loadCabinet(code: String, onLoaded: (Cabinet?) -> Unit) {
        viewModelScope.launch {
            val cabinet = FirestoreRepository.getCabinet(code)
            onLoaded(cabinet)
        }
    }
}
