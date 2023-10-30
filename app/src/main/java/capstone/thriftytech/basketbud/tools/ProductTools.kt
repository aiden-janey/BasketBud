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
                //Assume Current Date & Time
                var date = Date()
                buy_date = format.format(date).toString()
            }
        return buy_date
    }

    fun findPrice(line: String): String{
        var price = "No Price Found"
        if(line.contains(pricePattern)){
            price = pricePattern.find(line).toString()
        }
        return price
    }

    fun findName(line: String): String{
        var prodName = "No Product Found"
        var price = ""
        if(line.contains(pricePattern)){
            prodName = line.substring(0, (line.length-5))
        }
        return prodName
    }
}
