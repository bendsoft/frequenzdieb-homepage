package ch.frequenzdieb.common

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException

object RequestParamReader {
    fun ServerRequest.readQueryParam(paramName: String): String =
        queryParam(paramName)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "{ code: \"MISSING_PARAMETER\", details: { name: \"$paramName\" } }"
                )
            }
}
