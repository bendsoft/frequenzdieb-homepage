package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java-Klasse für trxStatus.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 *
 * <pre>
 * &lt;simpleType name="trxStatus">
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="error"/>
 * &lt;enumeration value="response"/>
 * &lt;/restriction>
 * &lt;/simpleType>
</pre> *
 *
 */
@XmlType(name = "trxStatus")
@XmlEnum
enum class TrxStatus(private val value: String) {
    ERROR("error"), RESPONSE("response");

    fun value(): String {
        return value
    }

    companion object {
        fun fromValue(v: String): TrxStatus {
            for (c in values()) {
                if (c.value == v) {
                    return c
                }
            }
            throw IllegalArgumentException(v)
        }
    }

}
