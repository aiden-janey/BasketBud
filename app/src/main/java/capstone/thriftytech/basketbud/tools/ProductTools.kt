package capstone.thriftytech.basketbud.tools

import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

//Just a set of tools to help extract relevant data for the Product from the text extraction
class ProductTools {
    val pricePattern = """(\$?\d{1,3}(?:,?\d{3})*(?:\.\d{2})?)""".toRegex()
    val namePattern = """\\b[a-zA-Z]+\\b""".toRegex()
    val datePattern = "\\b\\p{IsAlphabetic}+ \\d{1,2}, \\d{4} \\d{1,2}:\\d{2}\\b"

    val productNames = arrayOf("LARGE EGGS", "BANANA", "MIX BQT CHARM", "OREO GOLDEN", "EGGPLANT LNG"
    , "TOMATO ROMA", "MEAT", "SPAGHETTI SAUCE", "ASPIRIN REG", "NEUTROGENA WAS", "WHITE POTATO",
        "COLGATE THPASTE", "RUFFLES SOUR CRM", "LAYS KETCHUP", "COCKTAILS", "STARBUCKS", "BIRTHDAY BAG",
        "COUNTRY MUSHROOM", "CATELLI GARDEN SELECT", "FV FOODS UBE", "PINOY DELIGHT SIOPAO PORK")
    fun findDate(text: String): String{
        var buy_date = getCurrentDateTime()
        val pattern = Pattern.compile(datePattern)
        val matcher = pattern.matcher(text)
        while (matcher.find())
            buy_date = matcher.group()
        return buy_date
    }

    fun findPrice(line: String): String{
        var price = "No Price Found"
        val matches = pricePattern.findAll(line)
        for(match in matches)
            price = match.value
        return price
    }

    fun findName(line: String): String{
        var prodName = "No Product Found"
        val matches = namePattern.findAll(line)
        for(match in matches)
            prodName = match.value
        if(prodName == "No Product Found"){
            for(pN in productNames){
                if(line.contains(pN, true))
                    prodName = pN
            }
        }
        return prodName
    }

    fun getCurrentDateTime(): String{
        val currentDateTime = Date()
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm")
        val formattedDateTime = formatter.format(currentDateTime)
        return formattedDateTime.toString()
    }
}
