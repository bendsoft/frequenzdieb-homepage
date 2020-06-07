package ch.frequenzdieb.common

import ch.frequenzdieb.security.SignatureFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import reactor.core.publisher.Mono
import java.util.Optional
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

@Component
class Validators (
    webFluxValidator: Validator,
    signatureFactory: SignatureFactory
) {
    init {
        Companion.webFluxValidator = webFluxValidator
        Companion.signatureFactory = signatureFactory
    }

    companion object {
        lateinit var webFluxValidator: Validator
        lateinit var signatureFactory: SignatureFactory

        inline fun <reified T> Mono<T>.validateEntity(): Mono<T> =
            doOnNext { entity ->
                println("validateEntity called for: $entity")
                BeanPropertyBindingResult(entity, T::class.java.name).let { errors ->
                    webFluxValidator.validate(entity!!, errors)

                    if (errors.hasErrors()) {
                        ValidationError (
                            value = entity,
                            code ="INVALID_ENTITY",
                            nested = errors.allErrors
                                .mapNotNull {
                                    ValidationError(it.defaultMessage)
                                }
                        ).throwAsServerResponse()
                    }
                }
            }

        fun <T : BaseEntity> Mono<T>.checkSignature(
            signature: String,
            vararg additionalValuesInSignature: String
        ) =
            validateWith("INVALID_SIGNATURE") {
                !it.id.isNullOrEmpty()
                    && signatureFactory.createSignature(it.id!!, *additionalValuesInSignature) != signature
            }

        fun <T> Mono<T>.validateAsyncWith(
            errorMessage: String? = null,
            vararg errorDetails: Pair<String, Any>,
            asyncPredicate: (param: T) -> Mono<Boolean>
        ): Mono<T> =
            zipToPairWhen { asyncPredicate(it) }
            .flatMap { (entity, checkResult) ->
                println("validateAsyncWith called for: $entity")

                entity.executeValidation(
                    errorMessage = errorMessage,
                    errorDetails = errorDetails,
                    predicate = { checkResult }
                )

                Mono.just(entity)
            }

        fun <T> Mono<T>.validateWith(
            errorMessage: String? = null,
            validationError: ValidationError? = null,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            vararg errorDetails: Pair<String, Any>,
            predicate: (param: T) -> Boolean
        ): Mono<T> =
            doOnNext {
                println("validateWith called for: $it")
                it.executeValidation(
                    errorMessage = errorMessage,
                    validationError = validationError,
                    httpStatus = httpStatus,
                    errorDetails = errorDetails,
                    predicate = predicate
                )
            }.log()

        private fun <T> T.executeValidation(
            errorMessage: String? = null,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            errorDetails: Array<out Pair<String, Any>>,
            validationError: ValidationError? = null,
            predicate: (param: T) -> Boolean
        ) {
            println("validation executed for: $this")
            if (!predicate(this)) {
                println("throw error, because: $errorMessage")

                Optional.ofNullable(validationError)
                    .orElse(ValidationError(
                        code = errorMessage,
                        details = errorDetails
                            .takeIf { it.isNotEmpty() }
                            ?.let { mapOf(*it) }
                    ))
                    .throwAsServerResponse(httpStatus)
            }
        }

        fun Mono<String>.validateEMail(): Mono<String> =
            doOnNext {
                try {
                    InternetAddress(it).apply {
                        validate()
                    }
                } catch (addressException: AddressException) {
                    ValidationError(
                        "INVALID_EMAIL",
                        mapOf("reason" to addressException.localizedMessage)
                    ).throwAsServerResponse()
                }
            }
    }
}
