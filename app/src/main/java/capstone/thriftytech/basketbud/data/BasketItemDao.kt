package capstone.thriftytech.basketbud.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//Dao interface for interaction with database
@Dao
interface BasketItemDao {
    //insert function
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: BasketItem)
    //update function
    @Update
    suspend fun update(item: BasketItem)
    //delete function
    @Delete
    suspend fun delete(item: BasketItem)
    //get item by id
    @Query("SELECT * FROM basketitem WHERE ID = :id")
    fun getItem(id: Int): Flow<BasketItem>
    //get all items ordered by Acsending
    @Query("SELECT * FROM basketitem ORDER BY name ASC")
    fun getItems(): Flow<List<BasketItem>>
}