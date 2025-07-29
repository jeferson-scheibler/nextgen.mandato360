package com.nextgen.mandato360.data

import com.google.firebase.firestore.PropertyName

/**
 * Representa um usuário autenticado,
 * com vínculo a gabinete, função e avatar.
 */
data class User(
    @get:PropertyName("uid")         @set:PropertyName("uid")         var uid: String = "",
    @get:PropertyName("name")        @set:PropertyName("name")        var name: String = "",
    @get:PropertyName("email")       @set:PropertyName("email")       var email: String = "",
    @get:PropertyName("photoUrl")    @set:PropertyName("photoUrl")    var photoUrl: String? = null,
    @get:PropertyName("cabinetCode") @set:PropertyName("cabinetCode") var cabinetCode: String? = null,
    @get:PropertyName("role")        @set:PropertyName("role")        var role: String? = null,
    @get:PropertyName("lastLogin")   @set:PropertyName("lastLogin")   var lastLogin: Long = 0L
)