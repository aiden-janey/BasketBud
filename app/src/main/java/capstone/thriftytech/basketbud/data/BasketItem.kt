package capstone.thriftytech.basketbud.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//class for storing item obj to room DB
@Entity(tableName = "basketitem")
data class BasketItem (
    //primary key, unique
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    //shopping list item name
    @ColumnInfo(name = "name")
    val itemName: String,
)