package ch.frequenzdieb.payment.datatrans

import ch.frequenzdieb.payment.Payment
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.index.Indexed
import javax.validation.constraints.Min
import javax.validation.constraints.Size

data class DatatransPayment(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var merchantId: String?,

    @field:Min(0, message = "INVALID_AMOUNT")
    override val amount: Int,

    @field:Size(min = 2, max = 3, message = "INVALID_CURRENCY")
    override val currency: String,

    @Indexed
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var reference: String?,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var signature: String?
) : Payment
