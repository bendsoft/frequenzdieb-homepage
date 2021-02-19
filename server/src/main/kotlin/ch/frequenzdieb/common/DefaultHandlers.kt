package ch.frequenzdieb.common

import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.RouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI

object DefaultHandlers {
    inline fun <reified T : ImmutableEntity> RouterFunctionDsl.createDefaultRoutes(
        repository: ReactiveMongoRepository<T, String>
    ) = run {
        GET("/") { repository.getAll() }
        GET("/{id}") { repository.getById(it) }
        POST("/") { repository.create(it) }
        PUT("/{id}") { repository.update(it) }
        DELETE("/{id}") { repository.delete(it) }
    }

    inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.getById(request: ServerRequest) =
        findById(request.pathVariable("id"))
            .returnOne()

    inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.getAll() =
        findAll().returnList()

    inline fun <reified T: ImmutableEntity> Mono<T>.returnOne() =
        flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    inline fun <reified T: ImmutableEntity> Flux<T>.returnList() =
        collectList()
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.create(
        request: ServerRequest
    ) =
        request.bodyToMono(T::class.java).validateEntity()
            .flatMap { insert(it) }
            .flatMap {
                created(URI.create("${request.path()}/${it.id}"))
                    .bodyValue(it)
            }

    inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.update(
        request: ServerRequest
    ) =
        request.bodyToMono(T::class.java).validateEntity()
            .validateWith { !it.id.isNullOrEmpty() }
            .validateAsyncWith(
                httpStatus = HttpStatus.NOT_FOUND
            ) { existsById(request.pathVariable("id")) }
            .flatMap { save(it) }
            .flatMap { ok().bodyValue(it) }

    inline fun <reified T : ImmutableEntity> ReactiveMongoRepository<T, String>.delete(
        request: ServerRequest
    ) =
        noContent().build(deleteById(request.pathVariable("id")))
}
