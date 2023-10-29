package capstone.thriftytech.basketbud.tools

class StoreTools {
    val addresses = arrayOf("499 MAIN STREET SOUTH")
    val stores = arrayOf("GIANT TIGER", "METRO", "WALMART", "FOOD BASICS", "NO FRILLS")
    val cities = arrayOf("BRAMPTON", "MISSISSAUGA", "TORONTO", "OAKVILLE", "HAMILTON")
    val provinces = arrayOf("ON", "ONT")

    fun findAddress(text: String): String{
        var store_address = ""
        for(addr in addresses)
            if(text.contains(addr))
                store_address = addr
        return store_address
    }

    fun findStore(text: String): String{
        var store_name = ""
        for(store in stores)
            if(text.contains(store))
                store_name = store
        return store_name
    }

    fun findCity(text: String): String{
        var store_city = ""
        for(city in cities)
            if(text.contains(city))
                store_city = city
        return store_city
    }

    fun findProv(text: String): String{
        var store_prov = ""
        for(prov in provinces)
            if(text.contains(prov))
                store_prov = prov
        return store_prov
    }
}