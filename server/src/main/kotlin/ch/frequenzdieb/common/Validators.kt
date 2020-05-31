package ch.frequenzdieb.common

import ch.frequenzdieb.security.SignatureFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import reactor.core.publisher.Mono
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
                BeanPropertyBindingResult(entity, T::class.java.name).let { errors ->
                    webFluxValidator.validate(entity!!, errors)

                    if (errors.hasErrors()) {
                        ValidationError (
                            value = entity,
                            code ="INVALID_ENTITY",
                            nested = errors.allErrors.mapNotNull {
                                ValidationError(it.defaultMessage)
                            }
                        ).throwAsServerResponse()
                    }
                }
            }

        fun <T : BaseEntity> Mono<T>.checkSignature(signature: String, vararg additionalValuesInSignature: String) =
            validateWith("INVALID_SIGNATURE")
                { !it.id.isNullOrEmpty() && signatureFactory.createSignature(it.id!!, *additionalValuesInSignature) != signature }

        inline fun <T> Mono<T>.validateAsyncWith(
            errorMessage: String? = null,
            vararg errorDetails: Pair<String, Any>,
            crossinline asyncPredicate: (param: T) -> Mono<Boolean>
        ): Mono<T> =
            validateWith(errorMessage, *errorDetails) {
                asyncPredicate(it).blockOptional().orElse(false)
            }

        inline fun <T> Mono<T>.validateWith(
            errorMessage: String? = null,
            vararg errorDetails: Pair<String, Any>,
            crossinline predicate: (param: T) -> Boolean
        ): Mono<T> =
            validateWith(
                validationError = ValidationError(errorMessage, mapOf(*errorDetails)),
                predicate = predicate
            )

        inline fun <T> Mono<T>.validateWith(
            validationError: ValidationError = ValidationError(),
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            crossinline predicate: (param: T) -> Boolean
        ): Mono<T> =
            doOnNext {
                if (!predicate(it)) {
                    validationError.throwAsServerResponse(httpStatus)
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
