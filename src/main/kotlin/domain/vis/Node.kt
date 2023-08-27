package domain.vis

import kotlinx.serialization.Serializable

@Serializable
data class Node(val id: Int, val label: String)