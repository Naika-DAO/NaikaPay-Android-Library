package io.naika.naikapay.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import io.naika.naikapay.AOSPBuildConfig
import io.naika.naikapay.constant.Const.NAIKA_SIGNER_PACKAGE_NAME
import io.naika.naikapay.getPackageInfo
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

internal object Security {

    @OptIn(ExperimentalStdlibApi::class)
    fun verifyNaikaSignerIsInstalled(context: Context): Boolean {

        if (getPackageInfo(context, NAIKA_SIGNER_PACKAGE_NAME) == null) {
            return false
        }

        val packageManager: PackageManager = context.packageManager

        @Suppress("DEPRECATION")
        @SuppressLint("PackageManagerGetSignatures")
        val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = packageManager.getPackageInfo(
                NAIKA_SIGNER_PACKAGE_NAME,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            packageInfo.signingInfo.apkContentsSigners
        } else {
            val packageInfo = packageManager.getPackageInfo(
                NAIKA_SIGNER_PACKAGE_NAME,
                PackageManager.GET_SIGNATURES
            )
            packageInfo.signatures
        }

        var certificateMatch = true
        for (signature in signatures) {
            val input: InputStream = ByteArrayInputStream(signature.toByteArray())
            val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X509")
            val certificate: X509Certificate = certificateFactory
                .generateCertificate(input) as X509Certificate
            val publicKey: PublicKey = certificate.publicKey
            val certificateHex = byte2HexFormatted(publicKey.encoded)
            if (AOSPBuildConfig.NAIKA_HASH != certificateHex) {
                certificateMatch = false
                break
            }
        }

        return certificateMatch
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun byte2HexFormatted(array: ByteArray): String {
        val stringBuilder = StringBuilder(array.size * 2)
        for (index in array.indices) {
            var suggestedHex = Integer.toHexString(array[index].toInt())
            val length = suggestedHex.length
            if (length == 1) {
                suggestedHex = "0$suggestedHex"
            } else if (length > 2) {
                suggestedHex = suggestedHex.substring(length - 2, length)
            }
            stringBuilder.append(suggestedHex.uppercase(Locale.US))
            if (index < array.size - 1) {
                stringBuilder.append(':')
            }
        }
        return stringBuilder.toString()
    }
}