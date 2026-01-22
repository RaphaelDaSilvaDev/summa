package com.omna.summa.data.repository

import com.omna.summa.data.local.dao.UserDao
import com.omna.summa.data.local.entity.UserEntity
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    suspend fun getUser(): Boolean = dao.getUser()?.isPro ?: false

    suspend fun saveUser(user: UserEntity) = dao.saveUser(user)

    suspend fun clearUser() = dao.clearUser()
}