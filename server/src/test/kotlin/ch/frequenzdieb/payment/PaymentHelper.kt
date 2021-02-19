package ch.frequenzdieb.payment

import ch.frequenzdieb.payment.datatrans.paymentRoute
import ch.frequenzdieb.security.SignatureFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient

@Component
@AutoConfigureDataMongo
internal class PaymentHelper {
    @Value("\${payment.datatrans.merchantId}") lateinit var datatransMerchantId: String
    @Autowired lateinit var signatureFactory: SignatureFactory
    @Autowired lateinit var restClient: WebTestClient

    fun createXMLForSuccessfulTransaction(
        reference: String,
        amount: Int = 10,
        currency: String = "CHF"
    ) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <uppTransactionService version="1">
          <body merchantId="$datatransMerchantId" testOnly="yes">
            <transaction refno="$reference" status="success">
              <uppTransactionId>180710155401925243</uppTransactionId>
              <amount>$amount</amount>
              <currency>$currency</currency>
              <pmethod>VIS</pmethod>
              <reqtype>CAA</reqtype>
              <language>en</language>
              <success>
                <authorizationCode>417485393</authorizationCode>
                <acqAuthorizationCode>155417</acqAuthorizationCode>
                <responseMessage>Authorized</responseMessage>
                <responseCode>01</responseCode>
              </success>
              <userParameters>
                <parameter name="sign">${signatureFactory.createPaymentSignature(
                    datatransMerchantId,
                    amount.toString(),
                    currency,
                    reference
                )}</parameter>       
                <parameter name="responseCode">01</parameter>
                <parameter name="expy">21</parameter>
                <parameter name="expm">12</parameter>
              </userParameters>
            </transaction>
          </body>
        </uppTransactionService>""".trimIndent()

    fun insertTransaction(
        reference: String,
        amount: Int = 10,
        currency: String = "CHF"
    ) =
        restClient
            .post().uri("$paymentRoute/datatrans")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(createXMLForSuccessfulTransaction(
                reference,
                amount,
                currency
            ))
            .exchange()
            .expectStatus().isOk
}
