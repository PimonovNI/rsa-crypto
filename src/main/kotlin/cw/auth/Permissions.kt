package cw.auth

data class CatalogPermission(
    val catalog: String,
    val permissions: Set<Permission>
)

enum class Permission {
    W, R, E, A
}
