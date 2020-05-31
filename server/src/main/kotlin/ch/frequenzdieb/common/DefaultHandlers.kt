package ch.frequenzdieb.common

import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.RouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.net.URI

object DefaultHandlers {
    inline fun <reified T : BaseEntity> RouterFunctionDsl.createDefaultRoutes(
        repository: ReactiveMongoRepository<T, String>
    ) = run {
        GET("/") { repository.getAll() }
        GET("/{id}") { repository.getById(it) }
        POST("/") { repository.create(it) }
        PUT("/") { repository.update(it) }
        DELETE("/{id}") { repository.delete(it) }
    }

    inline fun <reified T : BaseEntity> ReactiveMongoRepository<T, String>.getById(
        request: ServerRequest
    ) = findAllById { request.pathVariable("id") }
        .collectList()
        .flatMap { ok().bodyValue(it) }
        .switchIfEmpty(notFound().build())

    inline fun <reified T : BaseEntity> ReactiveMongoRepository<T, String>.getAll() =
        findAll()
            .collectList()
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    inline fun <reified T : BaseEntity> ReactiveMongoRepository<T, String>.create(
        request: ServerRequest
    ) = request.bodyToMono(T::class.java).validateEntity()
        .flatMap { insert(it) }
        .flatMap {
            created(URI.create("${request.path()}/${it.id}"))
                .bodyValue(it)
        }

    inline fun <reified T : BaseEntity> ReactiveMongoRepository<T, String>.update(
        request: ServerRequest
    ) = request.bodyToMono(T::class.java).validateEntity()
        .validateWith { !it.id.isNullOrEmpty() }
        .flatMap { save(it) }
        .flatMap { ok().bodyValue(it) }
        .switchIfEmpty(notFound().build())

    inline fun <reified T : BaseEntity> ReactiveMongoRepository<T, String>.delete(
        request: ServerRequest
    ) = noContent().build(deleteById(request.pathVariable("id")))
}
