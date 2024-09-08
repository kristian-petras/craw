package com.craw.application

import com.craw.schema.graphql.Node
import com.craw.schema.graphql.Query
import com.craw.schema.graphql.WebPage
import com.craw.translator.GraphQLTranslator
import com.expediagroup.graphql.generator.scalars.ID

class GraphQLApplication(private val translator: GraphQLTranslator, private val repository: Repository) : Query {
    override fun websites(): List<WebPage> {
        val records = repository.records()
        return records.map { record -> translator.translate(record) }
    }

    override fun nodes(webPages: List<ID>): List<Node> {
        val ids = webPages.map { it.value }.toSet()
        val records = repository.records()
        return records
            .filter { record -> record.recordId in ids }
            .flatMap { record ->
                val owner = translator.translate(record)
                record.executions.map { execution -> translator.translate(owner, execution) }
            }
    }
}

