package com.craw

import com.craw.schema.graph.Graph
import kotlinx.coroutines.flow.Flow

interface GraphApplication {
    fun subscribe(): Flow<Graph>
}