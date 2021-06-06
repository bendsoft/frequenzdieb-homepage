package ch.frequenzdieb.blog

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class BlogHandler(
    private val repository: BlogRepository
) {
    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(BlogEntry::class.java)
            .flatMap {
                repository.save(it)
                    .flatMap { blogEntry ->
                        created(URI.create("/blogEntry/${blogEntry.id}"))
                            .bodyValue(blogEntry)
                    }
            }
            .switchIfEmpty(badRequest().bodyValue("BlogEntry must be provided"))

    fun deleteById(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap { blogEntry ->
                repository.delete(blogEntry)
                    .thenReturn(noContent().build())
                    .flatMap { it }
            }
            .switchIfEmpty(notFound().build())
}
