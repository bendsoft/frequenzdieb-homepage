package ch.frequenzdieb.api.concert

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import javax.validation.constraints.Future
import javax.validation.constraints.Size

@Document(collection = "concerts")
@TypeAlias("model.concert")
data class Concert(
    @Id val id: String,
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String = "",
    @Future val date: LocalDate,
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val location: String
)
