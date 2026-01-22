package com.omna.summa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.omna.summa.data.local.entity.UserEntity
import com.omna.summa.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _isPro = MutableStateFlow(false)
    val isPro = _isPro.asStateFlow()

    fun checkUser(){
        viewModelScope.launch {
            _isPro.value = userRepository.getUser()
        }
    }

    fun onPurchaseConfirmed(purchase: Purchase){
        viewModelScope.launch {
            val expirationMillis = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000

            userRepository.saveUser(UserEntity(
                uid = FirebaseAuth.getInstance().currentUser!!.uid,
                email = FirebaseAuth.getInstance().currentUser!!.email,
                isPro = true,
                proExpiration = expirationMillis,
                googlePurchaseToken = purchase.purchaseToken
            ))

            _isPro.value = true
        }
    }
}