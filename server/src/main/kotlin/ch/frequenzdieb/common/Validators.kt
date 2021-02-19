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
                BeanPropertyBindingResult(entity, T::class.java.name).let { errors ->
                    webFluxValidator.validate(entity!!, errors)

                    if (errors.hasErrors()) {
                        ValidationError (
                            value = entity,
                            code = ErrorCode.ENTITY_INVALID,
                            details = mapOf(
                                "reason" to errors.allErrors
                            )
                        ).throwAsServerResponse()
                    }
                }
            }

        fun <T : ImmutableEntity> Mono<T>.checkSignature(
            signature: String,
            vararg additionalValuesInSignature: String
        ) =
            validateWith(ErrorCode.SIGNATURE_INVALID) {
                !it.id.isNullOrEmpty()
                    && signatureFactory.createSignature(it.id!!, *additionalValuesInSignature) != signature
            }

        fun <T> Mono<T>.validateAsyncWith(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            vararg errorDetails: Pair<String, Any>,
            asyncPredicate: (param: T) -> Mono<Boolean>
        ): Mono<T> =
            zipToPairWhen { asyncPredicate(it) }
            .flatMap { (entity, checkResult) ->
                entity.executeValidation(
                    errorCode = errorCode,
                    httpStatus = httpStatus,
                    errorDetails = errorDetails,
                    predicate = { checkResult }
                )

                Mono.just(entity)
            }

        fun <T> Mono<T>.validateWith(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            validationError: ValidationError? = null,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            vararg errorDetails: Pair<String, Any>,
            predicate: (param: T) -> Boolean
        ): Mono<T> =
            doOnNext {
                it.executeValidation(
                    errorCode = errorCode,
                    validationError = validationError,
                    httpStatus = httpStatus,
                    errorDetails = errorDetails,
                    predicate = predicate
                )
            }.log()

        fun <T> T.executeValidation(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            errorDetails: Array<out Pair<String, Any>> = emptyArray(),
            validationError: ValidationError? = null,
            predicate: (param: T) -> Boolean
        ) {
            if (!predicate(this)) {
                Optional.ofNullable(validationError)
                    .orElse(ValidationError(
                        code = errorCode,
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
                        ErrorCode.EMAIL_INVALID,
                        mapOf("reason" to addressException.localizedMessage)
                    ).throwAsServerResponse()
                }
            }
    }
}
