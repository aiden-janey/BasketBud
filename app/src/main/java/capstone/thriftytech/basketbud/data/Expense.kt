package capstone.thriftytech.basketbud.data

data class Expense(
    val imageUrl: String? = null,
    val purchaseDate: String? = null,
    val purchaseTotal: Double? = null,
    val store: String? = null,
    val userID: String? = null,
)