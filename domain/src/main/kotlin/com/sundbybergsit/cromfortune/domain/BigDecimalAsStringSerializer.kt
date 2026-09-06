package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimalAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val value = if (decoder is JsonDecoder) {
            decoder.decodeJsonElement().jsonPrimitive.content
        } else {
            decoder.decodeString()
        }
        return value.toBigDecimal()
    }
}

