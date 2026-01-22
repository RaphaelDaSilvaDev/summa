package com.omna.summa.domain.model

import com.google.firebase.Timestamp

data class User(
    val uid: String,
    val email: String?,
    val isPro: Boolean = false,
    val proExpiration : Timestamp?,
    val googlePurchaseToken : String?
)
