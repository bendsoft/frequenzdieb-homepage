package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java-Klasse für error complex type.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="error">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="errorDetail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="uppTransactionId" type="{}uppTransactionId" minOccurs="0"/>
 * &lt;element name="acqErrorCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "error", propOrder = ["errorCode", "errorMessage", "errorDetail", "uppTransactionId", "acqErrorCode"])
class Error {
    /**
     * Ruft den Wert der errorCode-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der errorCode-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(required = true)
    var errorCode: String? = null
    /**
     * Ruft den Wert der errorMessage-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der errorMessage-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(required = true)
    var errorMessage: String? = null
    /**
     * Ruft den Wert der errorDetail-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der errorDetail-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(required = true)
    var errorDetail: String? = null
    /**
     * Ruft den Wert der uppTransactionId-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der uppTransactionId-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    var uppTransactionId: String? = null
    /**
     * Ruft den Wert der acqErrorCode-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der acqErrorCode-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    var acqErrorCode: String? = null

}
