package com.nextgen.mandato360.data

import com.google.firebase.firestore.PropertyName

/**
 * Representa um gabinete político salvo no Firestore.
 */
data class Cabinet(
    @get:PropertyName("code")       @set:PropertyName("code")       var code: String = "",
    @get:PropertyName("role")       @set:PropertyName("role")       var role: String = "",
    @get:PropertyName("primaryColor")   @set:PropertyName("primaryColor")   var primaryColor: Long = 0L,
    @get:PropertyName("secondaryColor") @set:PropertyName("secondaryColor") var secondaryColor: Long = 0L,
    @get:PropertyName("members")    @set:PropertyName("members")    var members: List<String> = emptyList() // lista de UIDs
)