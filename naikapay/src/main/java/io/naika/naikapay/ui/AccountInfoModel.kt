package io.naika.naikapay.ui

import org.ethereum.geth.Account
import org.ethereum.geth.BigInt

data class AccountInfoModel
    (
    val name: String = "",
    val account: Account,
    val balance: BigInt?,
    val chances: Int = 0
            )