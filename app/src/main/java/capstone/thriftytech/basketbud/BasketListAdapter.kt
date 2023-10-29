package capstone.thriftytech.basketbud

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import capstone.thriftytech.basketbud.data.BasketItem
import capstone.thriftytech.basketbud.databinding.BasketListItemBinding

//adapter class for recyclerView
class BasketListAdapter(private val onItemClicked: (BasketItem) -> Unit) : ListAdapter<BasketItem, BasketListAdapter.BasketItemViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BasketListAdapter.BasketItemViewHolder {
        return BasketItemViewHolder(BasketListItemBinding.inflate(LayoutInflater.from(parent.context)))
    }
    //bind onClickListener to current position
    override fun onBindViewHolder(holder: BasketListAdapter.BasketItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }
    //ItemViewHolder inflates from cardView
    class BasketItemViewHolder(private var binding: BasketListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BasketItem) {
            //bind textView with Item data
            binding.apply {
                itemName.text = item.itemName
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
}