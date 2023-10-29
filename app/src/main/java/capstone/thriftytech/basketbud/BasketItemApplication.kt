package capstone.thriftytech.basketbud

import android.app.Application
import capstone.thriftytech.basketbud.data.BasketItemDatabase

//extends application class for interaction with database from UI
class BasketItemApplication : Application() {
    //initializes database
    val database: BasketItemDatabase by lazy { BasketItemDatabase.getDatabase(this) }
}