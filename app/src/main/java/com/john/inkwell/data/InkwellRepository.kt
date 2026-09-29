package com.john.inkwell.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class InkwellRepository(private val dao: BlockDao) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun blocksForDate(date: String): Flow<List<Block>> = dao.blocksForDate(date)

    fun allEntryDates(): Flow<List<String>> = dao.allEntryDates()

    fun entryDateCounts(): Flow<List<EntryDateCount>> = dao.entryDateCounts()

    fun search(query: String): Flow<List<Block>> = dao.search(query)

    /** Creates a new block for today, timestamped right now. */
    suspend fun addBlock(text: String, tag: String? = null, mood: String? = null): String {
        val now = Instant.now().toEpochMilli()
        val today = todayKey()
        val block = Block(
            id = UUID.randomUUID().toString(),
            entryDate = today,
            timestamp = now,
            text = text,
            tag = tag,
            mood = mood,
            editedAt = now
        )
        dao.insert(block)
        return block.id
    }

    /** Edits the text/tag/mood of an existing block without touching its original timestamp. */
    suspend fun editBlock(block: Block, newText: String? = null, newTag: String? = null, newMood: String? = null) {
        dao.update(
            block.copy(
                text = newText ?: block.text,
                tag = newTag ?: block.tag,
                mood = newMood ?: block.mood,
                editedAt = Instant.now().toEpochMilli()
            )
        )
    }

    fun todayKey(): String =
        Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)

    // --- Sync support -------------------------------------------------

    suspend fun allBlocksOnce(): List<Block> = dao.allBlocksOnce()

    /**
     * Merges a remote set of blocks into local storage. Since [Block.id] is
     * globally unique (UUID) and blocks are otherwise append-only, this is
     * just: for each remote block, keep it if it's new locally, or if its
     * [Block.editedAt] is newer than the local copy's.
     */
    suspend fun mergeRemote(remoteBlocks: List<Block>) {
        val local = dao.allBlocksOnce().associateBy { it.id }
        val toUpsert = remoteBlocks.filter { remote ->
            val existing = local[remote.id]
            existing == null || remote.editedAt > existing.editedAt
        }
        if (toUpsert.isNotEmpty()) {
            dao.upsertAll(toUpsert)
        }
    }
}
