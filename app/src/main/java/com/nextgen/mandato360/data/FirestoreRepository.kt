// app/src/main/java/com/nextgen/mandato360/data/FirestoreRepository.kt
package com.nextgen.mandato360.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val USERS    = "users"
    private const val CABINETS = "cabinets"

    suspend fun saveCabinet(cabinet: Cabinet) {
        db.collection(CABINETS)
            .document(cabinet.code)
            .set(cabinet)
            .await()
    }

    suspend fun getCabinet(code: String): Cabinet? {
        return db.collection(CABINETS)
            .document(code)
            .get()
            .await()
            .toObject(Cabinet::class.java)
    }

    suspend fun saveUser(user: User) {
        db.collection(USERS)
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun getUser(uid: String): User? {
        return db.collection(USERS)
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun getUsers(uids: List<String>): List<User> {
        if (uids.isEmpty()) return emptyList()
        val snapshots = db.collection(USERS)
            .whereIn(FieldPath.documentId(), uids)
            .get()
            .await()
        return snapshots.toObjects(User::class.java)
    }
}