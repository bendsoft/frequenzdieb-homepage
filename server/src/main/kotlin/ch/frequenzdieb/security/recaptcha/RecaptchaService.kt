package ch.frequenzdieb.security.recaptcha

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class RecaptchaService (
    @Value("\${recaptcha.key}") private val recaptchaKey: String
) {
    private val client = WebClient.create("https://www.google.com/recaptcha/api")

    fun recaptchaVerify(responseToken: String) =
        client.post()
            .uri { uriBuilder ->
                uriBuilder.path("/siteverify")
                    .queryParam("secret", recaptchaKey)
                    .queryParam("response", responseToken)
                    .build()
            }
            .accept(MediaType.APPLICATION_FORM_URLENCODED)
            .exchange()
            .flatMap {
                it.bodyToMono(RecaptchaVerificationResult::class.java)
            }
}
