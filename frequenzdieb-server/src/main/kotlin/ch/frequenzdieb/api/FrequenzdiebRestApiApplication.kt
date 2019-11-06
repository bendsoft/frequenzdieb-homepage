package ch.frequenzdieb.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableMongoRepositories
@EnableAsync
class FrequenzdiebRestApiApplication

fun main(args: Array<String>) {
    runApplication<FrequenzdiebRestApiApplication>(*args)
}
