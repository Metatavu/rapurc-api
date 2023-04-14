package fi.metatavu.rapurc.api.persistence.model

import java.util.*
import javax.persistence.*

/**
 * JPA class for Building
 */
@Entity
class Building: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    var survey: Survey? = null

    @ManyToOne
    var buildingType: BuildingType? = null

    @Column
    var propertyId: String? = null

    @Column
    var buildingId: String? = null

    @Column
    var constructionYear: Int? = null

    @Column
    var space: Int? = null

    @Column
    var volume: Int? = null

    @Column
    var floors: String? = null

    @Column
    var basements: Int? = null

    @Column
    var foundation: String? = null

    @Column
    var supportStructure: String? = null

    @Column
    var facadeMaterial: String? = null

    @Column
    var roofType: String? = null

    @Column
    var propertyName: String? = null
    
    @Column (nullable = false)
    var streetAddress: String? = null

    @Column (nullable = false)
    var city: String? = null

    @Column (nullable = false)
    var postCode: String? = null

}