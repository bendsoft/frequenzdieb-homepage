package ch.frequenzdieb.api.services.ticketing.payment.datatrans.model

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java-Klasse für uppCustomerType.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 *
 * <pre>
 * &lt;simpleType name="uppCustomerType">
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="P"/>
 * &lt;enumeration value="C"/>
 * &lt;/restriction>
 * &lt;/simpleType>
</pre> *
 *
 */
@XmlType(name = "uppCustomerType")
@XmlEnum
enum class UppCustomerType {
    P, C;

    fun value(): String {
        return name
    }

    companion object {
        fun fromValue(v: String?): UppCustomerType {
            return valueOf(v!!)
        }
    }
}
