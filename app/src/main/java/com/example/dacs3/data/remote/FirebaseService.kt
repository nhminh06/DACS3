package com.example.dacs3.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getFirestore(): FirebaseFirestore = firestore
    fun getAuth(): FirebaseAuth = auth
}
