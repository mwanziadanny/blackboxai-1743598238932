package com.example.danzygram.base

interface BaseMapper<Domain, Entity, Response> {
    fun mapToDomain(entity: Entity): Domain
    fun mapToEntity(domain: Domain): Entity
    fun mapToResponse(domain: Domain): Response
    fun mapFromResponse(response: Response): Domain

    fun mapToDomainList(entities: List<Entity>): List<Domain> {
        return entities.map { mapToDomain(it) }
    }

    fun mapToEntityList(domains: List<Domain>): List<Entity> {
        return domains.map { mapToEntity(it) }
    }

    fun mapToResponseList(domains: List<Domain>): List<Response> {
        return domains.map { mapToResponse(it) }
    }

    fun mapFromResponseList(responses: List<Response>): List<Domain> {
        return responses.map { mapFromResponse(it) }
    }
}

interface DomainMapper<Domain, Entity> {
    fun mapToDomain(entity: Entity): Domain
    fun mapToEntity(domain: Domain): Entity

    fun mapToDomainList(entities: List<Entity>): List<Domain> {
        return entities.map { mapToDomain(it) }
    }

    fun mapToEntityList(domains: List<Domain>): List<Entity> {
        return domains.map { mapToEntity(it) }
    }
}

interface ResponseMapper<Domain, Response> {
    fun mapToResponse(domain: Domain): Response
    fun mapFromResponse(response: Response): Domain

    fun mapToResponseList(domains: List<Domain>): List<Response> {
        return domains.map { mapToResponse(it) }
    }

    fun mapFromResponseList(responses: List<Response>): List<Domain> {
        return responses.map { mapFromResponse(it) }
    }
}

interface EntityMapper<Entity, Response> {
    fun mapToEntity(response: Response): Entity
    fun mapToResponse(entity: Entity): Response

    fun mapToEntityList(responses: List<Response>): List<Entity> {
        return responses.map { mapToEntity(it) }
    }

    fun mapToResponseList(entities: List<Entity>): List<Response> {
        return entities.map { mapToResponse(it) }
    }
}

interface UniDirectionalMapper<From, To> {
    fun map(from: From): To

    fun mapList(from: List<From>): List<To> {
        return from.map { map(it) }
    }
}

interface BiDirectionalMapper<First, Second> {
    fun mapToFirst(second: Second): First
    fun mapToSecond(first: First): Second

    fun mapToFirstList(second: List<Second>): List<First> {
        return second.map { mapToFirst(it) }
    }

    fun mapToSecondList(first: List<First>): List<Second> {
        return first.map { mapToSecond(it) }
    }
}

abstract class BaseMapperImpl<Domain, Entity, Response> : BaseMapper<Domain, Entity, Response> {
    override fun mapToDomainList(entities: List<Entity>): List<Domain> {
        return entities.map { mapToDomain(it) }
    }

    override fun mapToEntityList(domains: List<Domain>): List<Entity> {
        return domains.map { mapToEntity(it) }
    }

    override fun mapToResponseList(domains: List<Domain>): List<Response> {
        return domains.map { mapToResponse(it) }
    }

    override fun mapFromResponseList(responses: List<Response>): List<Domain> {
        return responses.map { mapFromResponse(it) }
    }
}