package fi.metatavu.rapurc.api.persistence.model

import fi.metatavu.rapurc.api.model.SurveyStatus
import fi.metatavu.rapurc.api.model.SurveyType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing surveys
 *
 * @author Jari Nykänen
 */
@Entity
class Survey: Metadata() {

    @Id
    var id: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SurveyStatus? = null

    @Column(nullable = false)
    var keycloakGroupId: UUID? = null

    @Enumerated(EnumType.STRING)
    var type: SurveyType? = null
    
    @Column
    var dateUnknown: Boolean? = null
    
    @Column
    var startDate: LocalDate? = null

    @Column
    var endDate: LocalDate? = null

    @Column
    var markedAsDone: OffsetDateTime? = null

    @Column
    var additionalInformation: String? = null

    @Column
    var creatorDisplayName: String? = null

}