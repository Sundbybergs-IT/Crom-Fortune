package com.sundbybergsit.cromfortune.ui.home

import com.google.gson.*
import java.lang.reflect.Type

class StockOrderTypeAdapter : JsonSerializer<StockOrder>, JsonDeserializer<StockOrder> {

    private val ATTRIBUTE_NAME = "name"
    private val ATTRIBUTE_PRICE = "price"
    private val ATTRIBUTE_QUANTITY = "quantity"

    override fun serialize(src: StockOrder, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val result = JsonObject()
        result.addProperty(ATTRIBUTE_NAME, src.name)
        result.addProperty(ATTRIBUTE_PRICE, src.price)
        result.addProperty(ATTRIBUTE_QUANTITY, src.quantity)
        return result
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StockOrder {
        val jsonObject = json.asJsonObject
        val name: String = jsonToString(jsonObject, ATTRIBUTE_NAME)
        val price: Double = jsonToDouble(jsonObject, ATTRIBUTE_PRICE)
        val quantity: Int = jsonToInt(jsonObject, ATTRIBUTE_QUANTITY)
        return StockOrder(name, price, quantity)
    }

    private fun jsonToDouble(jsonObject: JsonObject, propertyName: String): Double {
        return jsonObject[propertyName].asString.toDouble()
    }

    private fun jsonToInt(jsonObject: JsonObject, propertyName: String): Int {
        return jsonObject[propertyName].asString.toInt()
    }

    private fun jsonToString(jsonObject: JsonObject, propertyName: String): String {
        return jsonObject[propertyName].asString
    }

}
