package io.naika.naikapay.ui

import org.ethereum.geth.Account
import org.ethereum.geth.BigInt

data class AccountUIModel(val account: Account, var balance: BigInt? = null)