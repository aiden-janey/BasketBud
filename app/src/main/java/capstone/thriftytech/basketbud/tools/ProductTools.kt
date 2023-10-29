package capstone.thriftytech.basketbud.tools

import java.text.SimpleDateFormat
import java.util.Date

class ProductTools {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
        "Aug", "Sep", "Oct", "Nov", "Dec")
    val pricePattern = Regex(pattern = "[0-9][0-9].[0-9][0-9]")

    fun findDate(line: String): String{
        var buy_date = ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        for(month in months)
            if(line.contains(month))
                buy_date = line
            else{
                var date = Date()
                buy_date = format.format(date).toString()
            }
        return buy_date
    }

    fun findPrice(line: String): String{
        return "0.00"
    }

    fun findName(line: String): String{
        return "product brand"
    }
}