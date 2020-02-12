package ch.frequenzdieb.api.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate

@Configuration
class GridFsConfiguration : AbstractReactiveMongoConfiguration() {
    @Value("\${spring.data.mongodb.database}")
    lateinit var databaseNameProperty: String

    override fun getDatabaseName(): String {
        return databaseNameProperty
    }

    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create("mongodb://admin:admin@localhost:27017")
    }

    @Bean
    fun reactiveGridFsTemplate(): ReactiveGridFsTemplate {
        return ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter())
    }
}
