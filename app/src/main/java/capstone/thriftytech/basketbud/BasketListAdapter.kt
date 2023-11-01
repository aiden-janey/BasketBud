package capstone.thriftytech.basketbud

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import capstone.thriftytech.basketbud.data.BasketItem
import capstone.thriftytech.basketbud.databinding.BasketListItemBinding


//adapter class for recyclerView
class BasketListAdapter(private val onItemClicked: (BasketItem) -> Unit) : ListAdapter<BasketItem, BasketListAdapter.BasketItemViewHolder>(DiffCallback) {
    //item selection mode flag
    var isSelectable: Boolean = false
    var selectedItems = ArrayList<BasketItem>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BasketListAdapter.BasketItemViewHolder {
        return BasketItemViewHolder(BasketListItemBinding.inflate(LayoutInflater.from(parent.context)))
    }
    //bind onClickListener to current position
    override fun onBindViewHolder(holder: BasketListAdapter.BasketItemViewHolder, position: Int) {
        val current = getItem(position)
        //disable delete button
        holder.itemView.setOnClickListener {
            onItemClicked(current)
            //if selection mode is not enabled
            if (!isSelectable) {
                //search item
                Log.d("Mode", "Search ${current.itemName}")
            } else {
                //selection mode is enabled. toggle selected
                if (holder.itemView.isSelected) {
                    holder.itemView.isSelected = false
                    selectedItems.remove(current)
                    //check if list is empty, disable select mode
                    if (selectedItems.isEmpty()) {
                        isSelectable = false
                    }
                } else {
                    holder.itemView.isSelected = true
                    //add item to selected list
                    selectedItems.add(current)
                }
                isSelectedColor(holder)
                //Log.d("Mode", "Selected: ${holder.itemView.isSelected}")
            }
            //Log.d("Item", "Item flag: ${current.itemSelected}")
        }
        //on long click
        holder.itemView.setOnLongClickListener {
            onItemClicked(current)
            //enable selection mode and set item as selected
            isSelectable = true
            Log.d("Mode", "Select Mode: $isSelectable")
            holder.itemView.isSelected = true
            isSelectedColor(holder)
            selectedItems.add(current)
            return@setOnLongClickListener true
        }
        holder.bind(current)
    }
    //ItemViewHolder inflates from cardView
    class BasketItemViewHolder(private var binding: BasketListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BasketItem) {
            //bind textView with Item data
            binding.apply {
                itemName.text = item.itemName
                selectedBox.visibility = View.GONE
            }
        }
    }
    //companion object to check if input is the same
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BasketItem>() {
            override fun areItemsTheSame(oldItem: BasketItem, newItem: BasketItem): Boolean {
                return oldItem === newItem
            }
            override fun areContentsTheSame(oldItem: BasketItem, newItem: BasketItem): Boolean {
                return oldItem.itemName == newItem.itemName
            }
        }
    }
    //enabling unique identifier
    init {
        setHasStableIds(true)
    }
    @SuppressLint("ResourceAsColor")
    fun isSelectedColor(viewHolder: ViewHolder) {
        if (viewHolder.itemView.isSelected) {
            viewHolder.itemView.setBackgroundColor(R.color.list_default_color)
        } else {
            viewHolder.itemView.setBackgroundColor(R.color.list_transparent_color)
        }
    }
}