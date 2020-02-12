package ch.frequenzdieb.api.services.ticketing.payment.datatrans.model

import java.util.*
import javax.xml.bind.annotation.*

/**
 *
 * Java-Klasse für anonymous complex type.
 *
 *
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="body">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;all>
 * &lt;element name="transaction">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="uppTransactionId" type="{}uppTransactionId"/>
 * &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="currency" type="{}currency"/>
 * &lt;element name="pmethod" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="reqtype" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;choice>
 * &lt;element name="success">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="authorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="acqAuthorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="responseMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="responseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;element name="error">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;element name="errorDetail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/choice>
 * &lt;element name="userParameters">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="parameter" maxOccurs="unbounded" minOccurs="0">
 * &lt;complexType>
 * &lt;simpleContent>
 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/extension>
 * &lt;/simpleContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/sequence>
 * &lt;attribute name="refno" use="required">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;minLength value="1"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/attribute>
 * &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/all>
 * &lt;attribute name="merchantId" use="required" type="{}merchantIdType" />
 * &lt;attribute name="testOnly" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/sequence>
 * &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = ["body"])
@XmlRootElement(name = "uppTransactionService")
class UppTransactionService {
    /**
     * Ruft den Wert der body-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [UppTransactionService.Body]
     */
    /**
     * Legt den Wert der body-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [UppTransactionService.Body]
     */
    @XmlElement(required = true)
    lateinit var body: Body
    /**
     * Ruft den Wert der version-Eigenschaft ab.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Legt den Wert der version-Eigenschaft fest.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "version", required = true)
    var version: String? = null

    /**
     *
     * Java-Klasse für anonymous complex type.
     *
     *
     * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;all>
     * &lt;element name="transaction">
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="uppTransactionId" type="{}uppTransactionId"/>
     * &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="currency" type="{}currency"/>
     * &lt;element name="pmethod" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="reqtype" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;choice>
     * &lt;element name="success">
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="authorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="acqAuthorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="responseMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="responseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;/sequence>
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
     * &lt;/element>
     * &lt;element name="error">
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;element name="errorDetail" type="{http://www.w3.org/2001/XMLSchema}string"/>
     * &lt;/sequence>
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
     * &lt;/element>
     * &lt;/choice>
     * &lt;element name="userParameters">
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="parameter" maxOccurs="unbounded" minOccurs="0">
     * &lt;complexType>
     * &lt;simpleContent>
     * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     * &lt;/extension>
     * &lt;/simpleContent>
     * &lt;/complexType>
     * &lt;/element>
     * &lt;/sequence>
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
     * &lt;/element>
     * &lt;/sequence>
     * &lt;attribute name="refno" use="required">
     * &lt;simpleType>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     * &lt;minLength value="1"/>
     * &lt;/restriction>
     * &lt;/simpleType>
     * &lt;/attribute>
     * &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
     * &lt;/element>
     * &lt;/all>
     * &lt;attribute name="merchantId" use="required" type="{}merchantIdType" />
     * &lt;attribute name="testOnly" type="{http://www.w3.org/2001/XMLSchema}string" />
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
    </pre> *
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = [])
    class Body {
        /**
         * Ruft den Wert der transaction-Eigenschaft ab.
         *
         * @return
         * possible object is
         * [UppTransactionService.Body.Transaction]
         */
        /**
         * Legt den Wert der transaction-Eigenschaft fest.
         *
         * @param value
         * allowed object is
         * [UppTransactionService.Body.Transaction]
         */
        @XmlElement(required = true)
        lateinit var transaction: Transaction
        /**
         * Ruft den Wert der merchantId-Eigenschaft ab.
         *
         * @return
         * possible object is
         * [String]
         */
        /**
         * Legt den Wert der merchantId-Eigenschaft fest.
         *
         * @param value
         * allowed object is
         * [String]
         */
        @XmlAttribute(name = "merchantId", required = true)
        var merchantId: String? = null
        /**
         * Ruft den Wert der testOnly-Eigenschaft ab.
         *
         * @return
         * possible object is
         * [String]
         */
        /**
         * Legt den Wert der testOnly-Eigenschaft fest.
         *
         * @param value
         * allowed object is
         * [String]
         */
        @XmlAttribute(name = "testOnly")
        var testOnly: String? = null

        /**
         *
         * Java-Klasse für anonymous complex type.
         *
         *
         * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         *
         * <pre>
         * &lt;complexType>
         * &lt;complexContent>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         * &lt;sequence>
         * &lt;element name="uppTransactionId" type="{}uppTransactionId"/>
         * &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="currency" type="{}currency"/>
         * &lt;element name="pmethod" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="reqtype" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;choice>
         * &lt;element name="success">
         * &lt;complexType>
         * &lt;complexContent>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         * &lt;sequence>
         * &lt;element name="authorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="acqAuthorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="responseMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="responseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;/sequence>
         * &lt;/restriction>
         * &lt;/complexContent>
         * &lt;/complexType>
         * &lt;/element>
         * &lt;element name="error">
         * &lt;complexType>
         * &lt;complexContent>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         * &lt;sequence>
         * &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;element name="errorDetail" type="{http://www.w3.org/2001/XMLSchema}string"/>
         * &lt;/sequence>
         * &lt;/restriction>
         * &lt;/complexContent>
         * &lt;/complexType>
         * &lt;/element>
         * &lt;/choice>
         * &lt;element name="userParameters">
         * &lt;complexType>
         * &lt;complexContent>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         * &lt;sequence>
         * &lt;element name="parameter" maxOccurs="unbounded" minOccurs="0">
         * &lt;complexType>
         * &lt;simpleContent>
         * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         * &lt;/extension>
         * &lt;/simpleContent>
         * &lt;/complexType>
         * &lt;/element>
         * &lt;/sequence>
         * &lt;/restriction>
         * &lt;/complexContent>
         * &lt;/complexType>
         * &lt;/element>
         * &lt;/sequence>
         * &lt;attribute name="refno" use="required">
         * &lt;simpleType>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         * &lt;minLength value="1"/>
         * &lt;/restriction>
         * &lt;/simpleType>
         * &lt;/attribute>
         * &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
         * &lt;/restriction>
         * &lt;/complexContent>
         * &lt;/complexType>
        </pre> *
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = ["uppTransactionId", "amount", "currency", "pmethod", "reqtype", "language", "success", "error", "userParameters"])
        class Transaction {
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
            @XmlElement(required = true)
            lateinit var uppTransactionId: String
            /**
             * Ruft den Wert der amount-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der amount-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(required = true)
            lateinit var amount: String
            /**
             * Ruft den Wert der currency-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der currency-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(required = true)
            lateinit var currency: String
            /**
             * Ruft den Wert der pmethod-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der pmethod-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(required = true)
            lateinit var pmethod: String
            /**
             * Ruft den Wert der reqtype-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der reqtype-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(required = true)
            lateinit var reqtype: String
            /**
             * Ruft den Wert der language-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der language-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(required = true)
            lateinit var language: String
            /**
             * Ruft den Wert der success-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [UppTransactionService.Body.Transaction.Success]
             */
            /**
             * Legt den Wert der success-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [UppTransactionService.Body.Transaction.Success]
             */
            var success: Success? = null
            /**
             * Ruft den Wert der error-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [UppTransactionService.Body.Transaction.Error]
             */
            /**
             * Legt den Wert der error-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [UppTransactionService.Body.Transaction.Error]
             */
            var error: Error? = null
            /**
             * Ruft den Wert der userParameters-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [UppTransactionService.Body.Transaction.UserParameters]
             */
            /**
             * Legt den Wert der userParameters-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [UppTransactionService.Body.Transaction.UserParameters]
             */
            @XmlElement(required = true)
            lateinit var userParameters: UserParameters
            /**
             * Ruft den Wert der refno-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der refno-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlAttribute(name = "refno", required = true)
            lateinit var refno: String
            /**
             * Ruft den Wert der status-Eigenschaft ab.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Legt den Wert der status-Eigenschaft fest.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlAttribute(name = "status")
            var status: String? = null

            /**
             *
             * Java-Klasse für anonymous complex type.
             *
             *
             * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType>
             * &lt;complexContent>
             * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             * &lt;sequence>
             * &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;element name="errorDetail" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;/sequence>
             * &lt;/restriction>
             * &lt;/complexContent>
             * &lt;/complexType>
            </pre> *
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = ["errorCode", "errorMessage", "errorDetail"])
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

            }

            /**
             *
             * Java-Klasse für anonymous complex type.
             *
             *
             * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType>
             * &lt;complexContent>
             * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             * &lt;sequence>
             * &lt;element name="authorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;element name="acqAuthorizationCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;element name="responseMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;element name="responseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
             * &lt;/sequence>
             * &lt;/restriction>
             * &lt;/complexContent>
             * &lt;/complexType>
            </pre> *
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = ["authorizationCode", "acqAuthorizationCode", "responseMessage", "responseCode"])
            class Success {
                /**
                 * Ruft den Wert der authorizationCode-Eigenschaft ab.
                 *
                 * @return
                 * possible object is
                 * [String]
                 */
                /**
                 * Legt den Wert der authorizationCode-Eigenschaft fest.
                 *
                 * @param value
                 * allowed object is
                 * [String]
                 */
                @XmlElement(required = true)
                lateinit var authorizationCode: String
                /**
                 * Ruft den Wert der acqAuthorizationCode-Eigenschaft ab.
                 *
                 * @return
                 * possible object is
                 * [String]
                 */
                /**
                 * Legt den Wert der acqAuthorizationCode-Eigenschaft fest.
                 *
                 * @param value
                 * allowed object is
                 * [String]
                 */
                @XmlElement(required = true)
                lateinit var acqAuthorizationCode: String
                /**
                 * Ruft den Wert der responseMessage-Eigenschaft ab.
                 *
                 * @return
                 * possible object is
                 * [String]
                 */
                /**
                 * Legt den Wert der responseMessage-Eigenschaft fest.
                 *
                 * @param value
                 * allowed object is
                 * [String]
                 */
                @XmlElement(required = true)
                lateinit var responseMessage: String
                /**
                 * Ruft den Wert der responseCode-Eigenschaft ab.
                 *
                 * @return
                 * possible object is
                 * [String]
                 */
                /**
                 * Legt den Wert der responseCode-Eigenschaft fest.
                 *
                 * @param value
                 * allowed object is
                 * [String]
                 */
                @XmlElement(required = true)
                lateinit var responseCode: String
            }

            /**
             *
             * Java-Klasse für anonymous complex type.
             *
             *
             * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType>
             * &lt;complexContent>
             * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             * &lt;sequence>
             * &lt;element name="parameter" maxOccurs="unbounded" minOccurs="0">
             * &lt;complexType>
             * &lt;simpleContent>
             * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
             * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
             * &lt;/extension>
             * &lt;/simpleContent>
             * &lt;/complexType>
             * &lt;/element>
             * &lt;/sequence>
             * &lt;/restriction>
             * &lt;/complexContent>
             * &lt;/complexType>
            </pre> *
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = ["parameter"])
            class UserParameters {
                /**
                 * Gets the value of the parameter property.
                 *
                 *
                 *
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the parameter property.
                 *
                 *
                 *
                 * For example, to add a new item, do as follows:
                 * <pre>
                 * getParameter().add(newItem);
                </pre> *
                 *
                 *
                 *
                 *
                 * Objects of the following type(s) are allowed in the list
                 * [UppTransactionService.Body.Transaction.UserParameters.Parameter]
                 *
                 *
                 */
                var parameter: List<Parameter> = ArrayList()

                /**
                 *
                 * Java-Klasse für anonymous complex type.
                 *
                 *
                 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 *
                 * <pre>
                 * &lt;complexType>
                 * &lt;simpleContent>
                 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
                 * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
                 * &lt;/extension>
                 * &lt;/simpleContent>
                 * &lt;/complexType>
                </pre> *
                 *
                 *
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = ["value"])
                class Parameter {
                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     *
                     * @return
                     * possible object is
                     * [String]
                     */
                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     *
                     * @param value
                     * allowed object is
                     * [String]
                     */
                    @XmlValue
                    var value: String? = null
                    /**
                     * Ruft den Wert der name-Eigenschaft ab.
                     *
                     * @return
                     * possible object is
                     * [String]
                     */
                    /**
                     * Legt den Wert der name-Eigenschaft fest.
                     *
                     * @param value
                     * allowed object is
                     * [String]
                     */
                    @XmlAttribute(name = "name")
                    var name: String? = null

                }
            }
        }
    }
}
