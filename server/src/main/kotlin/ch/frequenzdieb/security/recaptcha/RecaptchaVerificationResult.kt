package ch.frequenzdieb.security.recaptcha

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class RecaptchaVerificationResult(
    val success: Boolean,

    @JsonProperty("challenge_ts")
    val challengeDate: LocalDateTime? = null,
    val hostname: String? = null,

    @JsonProperty("error-codes")
    val errorCodes: List<String> = emptyList()
)
