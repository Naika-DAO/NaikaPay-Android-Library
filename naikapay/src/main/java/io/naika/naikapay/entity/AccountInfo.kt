package io.naika.naikapay.entity

import java.io.Serializable

data class AccountInfo(
    val address: String,
    val balance: Double
) : Serializable