package ch.frequenzdieb.api.services.payment.datatrans

import ch.frequenzdieb.api.services.payment.Payment
import com.fasterxml.jackson.annotation.JsonProperty

data class DatatransPayment(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var merchantId: String?,

    val amount: String,

    val currency: String,

    val reference: String,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var signature: String?
) : Payment
