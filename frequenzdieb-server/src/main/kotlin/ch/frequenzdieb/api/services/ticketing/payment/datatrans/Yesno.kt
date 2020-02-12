package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java-Klasse für yesno.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 *
 * <pre>
 * &lt;simpleType name="yesno">
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="yes"/>
 * &lt;enumeration value="no"/>
 * &lt;/restriction>
 * &lt;/simpleType>
</pre> *
 *
 */
@XmlType(name = "yesno")
@XmlEnum
enum class Yesno(private val value: String) {
    YES("yes"), NO("no");

    fun value(): String {
        return value
    }

    companion object {
        fun fromValue(v: String): Yesno {
            for (c in values()) {
                if (c.value == v) {
                    return c
                }
            }
            throw IllegalArgumentException(v)
        }
    }

}
