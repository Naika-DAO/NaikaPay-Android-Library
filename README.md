-

Android In-Dapp Crypto Payment SDK for [Naika](https://naika.io) Ecosystem.

## Getting Started

To start working with NaikaPay, you need to add its dependency into your `build.gradle` file:

### Dependency

```groovy
dependencies {
    implementation "com.github.naika.naikapay:naikapay:[latest_version]"
}
```

Then you need to add jitpack as your maven repository in `build.gradle`  file:

```groovy
repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
```

### How to use

For more information regarding the usage of Poolakey, please check out
the [wiki](https://github.com/naika-DAO/NaikaPay/wiki) page.

### Sample

There is a fully functional sample Android Dapp that demonstrates the usage of NaikaPay, all you
have to do is cloning the project and running
the [app](https://github.com/naika-DAO/naikapay/tree/master/app) module. Sample Dapp is a coin toss
game that operates in Ethereum Goerli test network. [Here]() is the coin toss Dapp Smart Contract.

Coin toss Dapp is a simple game, you can buy a chance with your ETH in your wallet. Each chance let
you toss the coin 3 times. you should predict the coin toss result before every toss. If you can
predict all results correctly, you will win the 3 time of your investment in ETH otherwise you may
want to buy another chance to play again.

Coin toss Dapp game crypto features that enables using NaikaPay SDK:

1. Connect Wallet functionality.
2. Sign a Smart Contract transaction to Buy a chance with ETH in your connected wallet.
3. Sign a Smart Contract transaction to Claim your winning rewards to your wallet.

## Usage

### Initialize NaikaPay SDK

```kotlin
private val payment by lazy(LazyThreadSafetyMode.NONE) {
    Payment(context = this)
}

private lateinit var paymentConnection: Connection

paymentConnection = payment.initialize(NetworkType.ETH_MAIN) {
    connectionSucceed {
        Log.d("Payment", "connectionSucceed")
    }
    connectionFailed {
        //You can find out why the connection fails.
        Log.d("Payment", it.message.toString())
    }
    disconnected {
        Log.d("Payment", "disconnected")
    }
}
```

### Disconnect from NaikaPay SDK and free up resources

```kotlin
override fun onDestroy() {
    paymentConnection.disconnect()
    super.onDestroy()
}
```

### Connect Wallet Functionality

```kotlin
if (paymentConnection.getState() == ConnectionState.Connected) {
    connectWallet()
}
private fun connectWallet() {
    payment.connectWallet(
        registry = activityResultRegistry
    ) {
        connectWalletSucceed { accountInfo ->
            Log.d("Payment", "connectWalletSucceed")
        }
        connectWalletCanceled {
            Log.d("Payment", "connectWalletCanceled")
        }
        connectWalletFailed {
            Log.d("Payment", it.message!!)
        }
    }
}
```
