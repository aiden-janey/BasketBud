package capstone.thriftytech.basketbud.data

//Defines the Store object's structure for CRUD operations with Firestore
data class Store(
    val store_address: String? = null,
    val store_city: String? = null,
    val store_name: String? = null,
    val store_prov: String? = null
)
