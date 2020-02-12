package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java-Klasse für status.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 *
 * <pre>
 * &lt;simpleType name="status">
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="accepted"/>
 * &lt;enumeration value="error"/>
 * &lt;/restriction>
 * &lt;/simpleType>
</pre> *
 *
 */
@XmlType(name = "status")
@XmlEnum
enum class Status(private val value: String) {
    ACCEPTED("accepted"), ERROR("error");

    fun value(): String {
        return value
    }

    companion object {
        fun fromValue(v: String): Status {
            for (c in values()) {
                if (c.value == v) {
                    return c
                }
            }
            throw IllegalArgumentException(v)
        }
    }

}
