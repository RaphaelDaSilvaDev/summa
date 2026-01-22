package com.omna.summa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "user")
data class UserEntity (
    @PrimaryKey
    val uid: String ,
    val email: String?,
    val isPro: Boolean,
    val proExpiration : Long?,
    val googlePurchaseToken : String?
)