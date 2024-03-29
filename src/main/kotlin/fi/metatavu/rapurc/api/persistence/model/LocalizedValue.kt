package fi.metatavu.rapurc.api.persistence.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

/**
 * Entity class for Localized values
 */
@Entity
class LocalizedValue {

    @Id
    var id: UUID? = null

    @Column
    @NotBlank
    lateinit var language: String

    @Column
    @NotBlank
    lateinit var value: String

    // Links to various other entites that use localization

    @ManyToOne
    var wasteSpecifier: WasteSpecifier? = null

    @ManyToOne
    var wasteCategory: WasteCategory? = null

    @ManyToOne
    var usage: WasteUsage? = null

    @ManyToOne
    var reusableMaterial: ReusableMaterial? = null

    @ManyToOne
    var hazardousMaterial: HazardousMaterial? = null

    @ManyToOne
    var buildingType: BuildingType? = null

    @ManyToOne
    var wasteMaterial: WasteMaterial? = null
}