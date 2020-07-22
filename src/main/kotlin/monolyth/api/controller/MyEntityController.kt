
package monolyth.api.controller

import monolyth.api.MyEntityResponse
import monolyth.api.NewEntityRequest
import monolyth.api.toResponse
import monolyth.service.MyEntityService
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import io.ktor.locations.KtorExperimentalLocationsAPI
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

@KtorExperimentalLocationsAPI
class MyEntityController(
    private val myEntityService: MyEntityService
) : ApiController {

    @Path("{id}")
    class ByIdPath(
        @PathParam("Entity Id") val id: Long
    )

    @Path("by-unique-property/{uniqueProperty}")
    class ByUniquePropertyPath(
        @PathParam("Entity Unique Property") val uniqueProperty: String
    )

    override fun NormalOpenAPIRoute.registerApi() = route("my-entities") {
        post<Unit, MyEntityResponse, NewEntityRequest>(
            info("Submits a new entity")
        ) { _, request ->
            val data2 = request.data2.toLongOrNull()
                ?: throw BadRequestException("Invalid data2: ${request.data2}")

            if (myEntityService.getByUniqueProperty(request.uniqueProperty) != null) {
                throw BadRequestException("There's another entity with the ${request.uniqueProperty} uniqueProperty already")
            }
            val entity = myEntityService.create(request.uniqueProperty, request.data2, data2, request.data3)

            respond(entity.toResponse())
        }
        get<ByIdPath, MyEntityResponse>(
            info("Get the entity for the given id")
        ) { location ->
            val entityId = location.id
            val entity = myEntityService.getById(entityId)
                ?: throw NotFoundException("Entity $entityId does not exist!")
            respond(entity.toResponse())
        }
        get<ByUniquePropertyPath, MyEntityResponse>(
            info("Get the entity for the given id")
        ) { location ->
            val uniqueProperty = location.uniqueProperty
            val entity = myEntityService.getByUniqueProperty(uniqueProperty)
                ?: throw NotFoundException("Entity $uniqueProperty does not exist!")
            respond(entity.toResponse())
        }
    }
}
