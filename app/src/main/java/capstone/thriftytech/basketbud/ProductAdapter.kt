package capstone.thriftytech.basketbud

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import capstone.thriftytech.basketbud.data.Product

class ProductAdapter(var context: Context, var productsList: ArrayList<Product>) :
    RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    fun setFilteredList(productsList: ArrayList<Product>) {
        this.productsList = productsList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.product, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (buy_date, prod_name, prod_price, store_id) = productsList[position]
        holder.buyDate.text = buy_date
        holder.productName.text = prod_name
        holder.productPrice.text = prod_price
        holder.storeName.text = store_id
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var buyDate: TextView
        var productName: TextView
        var productPrice: TextView
        var storeName: TextView

        init {
            buyDate = itemView.findViewById(R.id.buyDate)
            productName = itemView.findViewById(R.id.productName)
            productPrice = itemView.findViewById(R.id.productPrice)
            storeName = itemView.findViewById(R.id.storeName)
        }
    }
}