package capstone.thriftytech.basketbud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import capstone.thriftytech.basketbud.data.Expense
import com.squareup.picasso.Picasso

class ExpenseAdapter(private val expenses: ArrayList<Expense>): RecyclerView.Adapter<ExpenseAdapter.ViewHolder>(){
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receiptIV: ImageView = itemView.findViewById(R.id.receiptIV)
        val storeTV: TextView = itemView.findViewById(R.id.storeTV)
        val purchaseDateTV: TextView = itemView.findViewById(R.id.purchaseDateTV)
        val purchaseTotalTV: TextView = itemView.findViewById(R.id.purchaseTotalTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.expense_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]

        Picasso.get()
            .load(expense.imageUrl)
            .rotate(90f)
            .into(holder.receiptIV)

        holder.storeTV.text = expense.store
        holder.purchaseDateTV.text = expense.purchaseDate
        holder.purchaseTotalTV.text = expense.purchaseTotal.toString()
    }

    override fun getItemCount(): Int {
        return expenses.size
    }
}