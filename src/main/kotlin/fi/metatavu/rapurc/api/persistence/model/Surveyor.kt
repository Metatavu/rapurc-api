package fi.metatavu.rapurc.api.persistence.model

import com.sun.istack.Nullable
import java.time.LocalDate
import java.util.*
import javax.persistence.*

/**
 * JPA class for Surveyor
 */
@Entity
class Surveyor: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    var survey: Survey? = null

    @Column(nullable = false)
    var firstName: String? = null

    @Column(nullable = false)
    var lastName: String? = null

    @Column(nullable = false)
    var company: String? = null

    @Column
    var role: String? = null

    @Column(nullable = false)
    var phone: String? = null

    @Column
    var email: String? = null

    @Column
    var reportDate: LocalDate? = null
    
    @Column
    var visits: String? = null
}