package cw.server

class Server {
    private val catalogController = CatalogController()
    private val authController = AuthController()
    private val adminController = AdminController()

    fun catalog(): CatalogController = catalogController
    fun auth(): AuthController = authController
    fun admin(): AdminController = adminController
}
