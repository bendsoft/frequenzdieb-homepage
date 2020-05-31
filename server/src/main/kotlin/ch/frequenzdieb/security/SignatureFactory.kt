package ch.frequenzdieb.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

@Component
class SignatureFactory(
    @Value("\${payment.datatrans.secret}") private val paymentSecret: String,
    @Value("\${secret}") private val secret: String
) {
    private val algorithm = "HmacSHA256"

    private val paymentMac = createMac(paymentSecret)
    private val mac = createMac(secret)

    fun createPaymentSignature(vararg valuesToSign: String): String =
        paymentMac.createSignature(valuesToSign)

    fun createSignature(vararg valuesToSing: String): String =
        mac.createSignature(valuesToSing)

    private fun createMac(secret: String) =
        Mac.getInstance(algorithm).apply {
            init(SecretKeySpec(DatatypeConverter.parseHexBinary(secret), algorithm))
        }

    private fun ByteArray.toHex(): String = DatatypeConverter.printHexBinary(this).toLowerCase()

    private fun Mac.createSignature(valuesToSign: Array<out String>): String =
        doFinal(valuesToSign.joinToString(separator = "").toByteArray()).toHex()
}
