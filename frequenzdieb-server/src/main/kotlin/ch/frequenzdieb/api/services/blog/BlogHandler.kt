package ch.frequenzdieb.api.services.blog

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class BlogHandler {
    @Autowired
    lateinit var repository: BlogRepository

    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(Blog::class.java)
            .flatMap {
                repository.save(it)
                    .flatMap { blog ->
                        created(URI.create("/blog/${blog.id}"))
                            .bodyValue(blog)
                    }
            }
            .switchIfEmpty(badRequest().bodyValue("Blog entity must be provided"))

    fun deleteById(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap { blog ->
                repository.delete(blog)
                    .thenReturn(noContent().build())
                    .flatMap { it }
            }
            .switchIfEmpty(notFound().build())
}
