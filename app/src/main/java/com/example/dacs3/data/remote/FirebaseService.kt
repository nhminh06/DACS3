package com.example.dacs3.data.remote

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {
    private val firestore = FirebaseFirestore.getInstance()

    fun getFirestore(): FirebaseFirestore = firestore
}
