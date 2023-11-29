package capstone.thriftytech.basketbud.data

//Defines the Product objects structure for CRUD operations with Firestore
data class Product(
    val buy_date: String? = null,
    val prod_name: String? = null,
    val prod_price: String? = null,
    val store_id: String? = null,
    val user_id: String? = null
)
