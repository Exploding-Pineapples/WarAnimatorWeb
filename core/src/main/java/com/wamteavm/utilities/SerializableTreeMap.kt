package com.wamteavm.utilities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.TreeMap

/**
 * ChatGPT wrote this
 * A small wrapper around java.util.TreeMap that can be serialized by kotlinx.serialization.
 * Keys must be Comparable so the TreeMap can use natural ordering.
 */
@Serializable(with = SerializableTreeMapSerializer::class)
class SerializableTreeMap<K : Comparable<K>, V>(
    private val _map: TreeMap<K, V> = TreeMap()
) : MutableMap<K, V> by _map {
    fun toTreeMap(): TreeMap<K, V> = TreeMap(_map)
}

class SerializableTreeMapSerializer<K : Comparable<K>, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<SerializableTreeMap<K, V>> {

    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)
    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: SerializableTreeMap<K, V>) {
        mapSerializer.serialize(encoder, value.toTreeMap())
    }

    override fun deserialize(decoder: Decoder): SerializableTreeMap<K, V> {
        val map = mapSerializer.deserialize(decoder)
        val tm = TreeMap<K, V>()
        tm.putAll(map)
        return SerializableTreeMap(tm)
    }
}
