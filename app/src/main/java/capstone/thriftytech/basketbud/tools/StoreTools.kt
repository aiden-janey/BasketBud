package capstone.thriftytech.basketbud.tools

class StoreTools {
    val addresses = arrayOf("499 MAIN STREET SOUTH", "160 MAIN STREET SOUTH", "50 QUARRY EDGE DR",
        "15 RESOLUTION DR", "100 BISCAYNE CRES", "345 MAIN ST N", "1 BARTLEY BULL PKWY")
    val stores = arrayOf("GIANT TIGER", "METRO", "WALMART", "FOOD BASICS", "NO FRILLS", "SHOPPERS",
        "DOLLARAMA", "COSTCO", "OCEANS", "OCEANS FRESH FOOD MARKET")
    val cities = arrayOf("BRAMPTON", "MISSISSAUGA", "TORONTO", "OAKVILLE", "HAMILTON")
    val provinces = arrayOf("ON", "ONT")

    fun findAddress(text: String): String{
        var store_address = "No Address Found"
        for(addr in addresses)
            if(text.contains(addr, true))
                store_address = addr
        return store_address
    }

    fun findStore(text: String): String{
        var store_name = "No Store Found"
        for(store in stores)
            if(text.contains(store, true))
                store_name = store
        return store_name
    }

    fun findCity(text: String): String{
        var store_city = "No City Found"
        for(city in cities)
            if(text.contains(city, true))
                store_city = city
        return store_city
    }

    fun findProv(text: String): String{
        var store_prov = "No Province Found"
        for(prov in provinces)
            if(text.contains(prov, true))
                store_prov = prov
        return store_prov
    }
}
