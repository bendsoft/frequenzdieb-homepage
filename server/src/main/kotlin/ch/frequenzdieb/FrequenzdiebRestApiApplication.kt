package ch.frequenzdieb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoAuditing
class FrequenzdiebRestApiApplication

fun main(args: Array<String>) {
    runApplication<FrequenzdiebRestApiApplication>(*args)
}
