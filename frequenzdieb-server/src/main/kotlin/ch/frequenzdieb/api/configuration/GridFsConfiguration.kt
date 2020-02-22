package ch.frequenzdieb.api.configuration

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.connection.ClusterSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate

@Configuration
class GridFsConfiguration : AbstractReactiveMongoConfiguration() {
    @Value("\${frequenzdieb.mongodb.uri}")
    lateinit var mongoUri: String

    @Value("\${frequenzdieb.mongodb.database}")
    lateinit var mongoDbName: String

    @Value("\${frequenzdieb.mongodb.username}")
    lateinit var mongoUser: String

    @Value("\${frequenzdieb.mongodb.password}")
    lateinit var mongoPsw: String

    override fun getDatabaseName(): String {
        return mongoDbName
    }

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(
            MongoClientSettings.builder()
                .credential(MongoCredential.createCredential(
                    mongoUser,
                    mongoPsw,
                    "admin".toCharArray())
                )
                .applyToClusterSettings {
                    ClusterSettings.builder()
                        .applyConnectionString(
                            ConnectionString(mongoUri)
                        )
                }
                .build()
        )
    }

    @Bean
    fun reactiveGridFsTemplate(): ReactiveGridFsTemplate {
        return ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter())
    }
}
