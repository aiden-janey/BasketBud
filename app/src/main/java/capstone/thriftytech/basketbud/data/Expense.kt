package capstone.thriftytech.basketbud.data

class Expense() {
    var imageUrl: String? = null
    var purchaseDate: String? = null
    var purchaseTotal: Double? = null
    var store: String? = null
    var userId: String? = null

    init {
        imageUrl = "IMAGE_URL"
        purchaseDate = "99/99/9999"
        purchaseTotal = 99999.99
        store = "STORE"
        userId = "USER_ID"
    }
}