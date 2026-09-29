package com.john.inkwell.data.drive

import com.john.inkwell.data.Block
import org.json.JSONArray
import org.json.JSONObject

/**
 * Blocks are append-only and each has a stable [Block.id] (UUID), so
 * merging a local set with a remote backup is just "union by id, prefer
 * whichever copy has the newer [Block.editedAt]" — see
 * InkwellRepository.mergeRemote. No text-diff/conflict UI needed.
 */
object BackupCodec {

    fun encode(blocks: List<Block>): String {
        val array = JSONArray()
        blocks.forEach { block ->
            val obj = JSONObject()
            obj.put("id", block.id)
            obj.put("entryDate", block.entryDate)
            obj.put("timestamp", block.timestamp)
            obj.put("text", block.text)
            obj.put("tag", block.tag ?: JSONObject.NULL)
            obj.put("mood", block.mood ?: JSONObject.NULL)
            obj.put("editedAt", block.editedAt)
            array.put(obj)
        }
        val root = JSONObject()
        root.put("version", 1)
        root.put("blocks", array)
        return root.toString()
    }

    fun decode(json: String): List<Block> {
        val root = JSONObject(json)
        val array = root.optJSONArray("blocks") ?: JSONArray()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            val timestamp = obj.getLong("timestamp")
            Block(
                id = obj.getString("id"),
                entryDate = obj.getString("entryDate"),
                timestamp = timestamp,
                text = obj.getString("text"),
                tag = obj.optString("tag", null).takeUnless { it.isNullOrEmpty() },
                mood = obj.optString("mood", null).takeUnless { it.isNullOrEmpty() },
                editedAt = if (obj.has("editedAt")) obj.getLong("editedAt") else timestamp
            )
        }
    }
}
