package ch.frequenzdieb.common

import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.URI

object DefaultHandlers {
    inline fun <reified T : ImmutableEntity> CoRouterFunctionDsl.createDefaultRoutes(
        repository: ReactiveMongoRepository<T, String>
    ) = run {
        GET("/") { repository.getAll() }
        GET("/{id}") { repository.getById(it) }
        POST("/") { repository.create(it) }
        PUT("/{id}") { repository.update(it) }
        DELETE("/{id}") { repository.delete(it) }
    }

    suspend fun <T : ImmutableEntity> ReactiveMongoRepository<T, String>.getById(request: ServerRequest): ServerResponse =
        findById(request.pathVariable("id"))
            .awaitSingleOrNull()
            .asServerResponse()

    suspend fun <T : ImmutableEntity> ReactiveMongoRepository<T, String>.getAll(): ServerResponse =
        findAll()
            .asFlow()
            .toList()
            .asServerResponse()

    suspend fun <T> T?.asServerResponse(emptyBody: Boolean = false): ServerResponse =
        if (this !== null)
            if (emptyBody) ok().buildAndAwait()
            else ok().bodyValueAndAwait(this)
        else notFound().buildAndAwait()

    suspend inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.create(
        request: ServerRequest
    ) = request.awaitBody(T::class)
        .validateEntity()
        .let { create(request.path(), it) }

    suspend fun <T : ImmutableEntity> ReactiveMongoRepository<T, String>.create(
        requestPath: String,
        entity: T
    ): ServerResponse = insert(entity)
        .awaitSingle()
        .let {
            created(URI.create("${requestPath}/${it.id}"))
                .bodyValueAndAwait(it)
        }

    suspend inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.update(
        request: ServerRequest
    ): ServerResponse = request.awaitBody(T::class)
        .validateEntity()
        .validateAsyncWith(httpStatus = HttpStatus.NOT_FOUND) {
            existsById(request.pathVariable("id")).awaitSingle()
        }
        .let { save(it) }
        .let { ok().bodyValueAndAwait(it) }

    suspend fun <T : ImmutableEntity> ReactiveMongoRepository<T, String>.delete(
        request: ServerRequest
    ): ServerResponse = deleteById(request.pathVariable("id"))
        .awaitSingle()
        .let {  noContent().buildAndAwait() }
}
