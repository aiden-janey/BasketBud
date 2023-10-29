package capstone.thriftytech.basketbud

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import capstone.thriftytech.basketbud.data.BasketItem
import capstone.thriftytech.basketbud.data.BasketItemDao
import kotlinx.coroutines.launch

//ViewModel class for handling interaction from UI to Dao>db
class BasketItemViewModel(private val basketItemDao: BasketItemDao) : ViewModel() {
    //initialize list of shopping items
    val allItems: LiveData<List<BasketItem>> = basketItemDao.getItems().asLiveData()
    //insert item using BasketItem entity/object
    private fun insertItem(basketItem: BasketItem) {
        //start coroutine for insert
        viewModelScope.launch {
            basketItemDao.insert(basketItem)
        }
    }
    //get item by id
    fun getItem(id: Int): LiveData<BasketItem> {
        return basketItemDao.getItem(id).asLiveData()
    }
    //update
    fun updateItem(basketItem: BasketItem) {
        viewModelScope.launch {
            basketItemDao.update(basketItem)
        }
    }
    //delete
    fun deleteItem(basketItem: BasketItem) {
        viewModelScope.launch {
            basketItemDao.delete(basketItem)
        }
    }
    //check valid entries
    fun isEntryValid(itemName: String): Boolean {
        return !itemName.isBlank()
    }
}