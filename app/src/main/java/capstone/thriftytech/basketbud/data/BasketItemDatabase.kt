package capstone.thriftytech.basketbud.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//persistent database for shopping list items
@Database(entities = [BasketItem::class], version = 1, exportSchema = false)
abstract class BasketItemDatabase: RoomDatabase() {
    //interaction with DAO for query
    abstract fun basketItemDao(): BasketItemDao
    //companion object for accessing methods
    companion object {
        //variable to reference db
        @Volatile   //changes to 1 thread is visible to other threads, operations in main mem
        private var INSTANCE: BasketItemDatabase? = null
        //get database method
        fun getDatabase(context: Context): BasketItemDatabase {
            //ensures only 1 database is initialized, if INSTANCE is null run synchronized block
            return INSTANCE ?: synchronized(this) {
                //create db using databaseBuilder
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BasketItemDatabase::class.java,
                    "item_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}