package shiroi.top.traits

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

class JsonUtil {
    fun toJsonParse(json: String): String {
        return try {
            val jsonParser = JsonParser()
            val je = jsonParser.parse(json)
            val gson = GsonBuilder().setPrettyPrinting().create()
            gson.toJson(je)
        } catch (e: Exception) {
            json
        }
    }
}