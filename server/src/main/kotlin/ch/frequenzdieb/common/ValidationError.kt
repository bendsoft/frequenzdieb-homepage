package ch.frequenzdieb.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidationError(
    val code: ErrorCode = ErrorCode.VALIDATION_ERROR,
    val details: Map<String, Any>? = null,
    val value: Any? = null,
    val nested: List<ValidationError>? = null
) {
    fun throwAsServerResponse(httpStatus: HttpStatus = HttpStatus.BAD_REQUEST) {
        throw ResponseStatusException(
            httpStatus,
            ObjectMapper().writeValueAsString(this)
        )
    }
}
