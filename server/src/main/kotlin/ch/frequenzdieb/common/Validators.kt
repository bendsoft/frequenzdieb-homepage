package ch.frequenzdieb.common

import ch.frequenzdieb.security.SignatureFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import java.util.*
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
        private val logger: Logger = LoggerFactory.getLogger(Validators::class.java)

        lateinit var webFluxValidator: Validator
        lateinit var signatureFactory: SignatureFactory

        inline fun <reified T> T.validateEntity(): T =
            apply {
                BeanPropertyBindingResult(this, T::class.java.name).apply {
                    webFluxValidator.validate(this@validateEntity!!, this)

                    if (this.hasErrors()) {
                        ValidationFailure(
                            value = this,
                            code = ErrorCode.ENTITY_INVALID,
                            details = mapOf(
                                "reason" to this.allErrors
                            )
                        ).throwAsServerResponse()
                    }
                }
            }

        fun <T : ImmutableEntity> T.checkSignature(
            signature: String,
            vararg additionalValuesInSignature: String
        ) =
            validateWith(ErrorCode.SIGNATURE_INVALID) {
                it.id.isNotEmpty()
                    && signatureFactory.createSignature(it.id, *additionalValuesInSignature) != signature
            }

        suspend fun <T> T.validateAsyncWith(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            vararg errorDetails: Pair<String, Any>,
            asyncPredicate: suspend (T) -> Boolean
        ): T = apply {
            asyncPredicate(this@validateAsyncWith)
                .let { checkResult ->
                    executeValidation(
                        errorCode = errorCode,
                        httpStatus = httpStatus,
                        errorDetails = errorDetails,
                        predicate = { checkResult }
                    )
                }
        }

        fun <T> T.validateWith(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            validationError: ValidationFailure? = null,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            vararg errorDetails: Pair<String, Any>,
            predicate: (param: T) -> Boolean
        ): T = apply {
            executeValidation(
                errorCode = errorCode,
                validationError = validationError,
                httpStatus = httpStatus,
                errorDetails = errorDetails,
                predicate = predicate
            )
        }

        fun <T> T.executeValidation(
            errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
            httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
            errorDetails: Array<out Pair<String, Any>> = emptyArray(),
            validationError: ValidationFailure? = null,
            predicate: (param: T) -> Boolean
        ): T {
            if (!predicate(this)) {
                Optional.ofNullable(validationError)
                    .orElse(ValidationFailure(
                        code = errorCode,
                        details = errorDetails
                            .takeIf { it.isNotEmpty() }
                            ?.let { mapOf(*it) }
                    ))
                    .apply { logger.warn("Validation failed!")  }
                    .throwAsServerResponse(httpStatus)
            }

            return this
        }

        fun String.validateEMail(): String =
            apply {
                try {
                    InternetAddress(this).apply {
                        validate()
                    }
                } catch (addressException: AddressException) {
                    ValidationFailure(
                        ErrorCode.EMAIL_INVALID,
                        mapOf("reason" to addressException.localizedMessage)
                    ).throwAsServerResponse()
                }
            }
    }
}
