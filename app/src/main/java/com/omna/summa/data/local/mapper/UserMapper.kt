package com.omna.summa.data.local.mapper

import com.google.firebase.Timestamp
import com.omna.summa.data.local.entity.UserEntity
import com.omna.summa.domain.model.User

fun UserEntity.toDomain(): User =
    User(
        uid = this.uid,
        email = this.email,
        isPro = this.isPro,
        proExpiration = this.proExpiration?.let { Timestamp(it / 1000, 0) },
        googlePurchaseToken = this.googlePurchaseToken
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        uid = this.uid,
        email = this.email,
        isPro = this.isPro,
        proExpiration = this.proExpiration?.toDate()?.time,
        googlePurchaseToken = this.googlePurchaseToken
    )