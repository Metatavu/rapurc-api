package fi.metatavu.rapurc.api.test.functional.auth

import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.*

/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    accessTokenProvider: AccessTokenProvider
): AuthorizedTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider
    val surveys: SurveyTestBuilderResource = SurveyTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val surveyors: SurveyorTestBuilderResource = SurveyorTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val owners: OwnerInformationTestBuilderResource = OwnerInformationTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val buildings: BuildingTestBuilderResource = BuildingTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val materials: ReusableMaterialTestBuilderResource = ReusableMaterialTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val reusables: SurveyReusableTestBuilderResource = SurveyReusableTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val wasteCategories: WasteCategoryTestBuilderResource = WasteCategoryTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val wasteMaterials: WasteMaterialTestBuilderResource = WasteMaterialTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val usages: UsageTestBuilderResource = UsageTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val wastes: SurveyWasteTestBuilderResource = SurveyWasteTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val hazMaterials: HazardousMaterialTestBuilderResource = HazardousMaterialTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val wasteSpecifiers: WasteSpecifierTestBuilderResource = WasteSpecifierTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val hazWastes: SurveyHazardousWasteTestBuilderResource = SurveyHazardousWasteTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val buildingTypes: BuildingTypeTestBuilderResource = BuildingTypeTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val attachments: AttachmentTestBuilderResource = AttachmentTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val userGroups: UserGroupTestBuilderResource = UserGroupTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val groupJounRequests: GroupJoinRequestTestBuilderResource = GroupJoinRequestTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    /**
     * Creates a API client
     *
     * @param accessToken access token
     * @return API client
     */
    override fun createClient(accessToken: String): ApiClient {
        val result = ApiClient(testBuilder.settings.apiBasePath)
        ApiClient.accessToken = accessToken
        return result
    }

}