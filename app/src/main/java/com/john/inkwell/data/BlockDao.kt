package com.john.inkwell.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {

    @Query("SELECT * FROM blocks WHERE entryDate = :date ORDER BY timestamp ASC")
    fun blocksForDate(date: String): Flow<List<Block>>

    @Query("SELECT DISTINCT entryDate FROM blocks ORDER BY entryDate DESC")
    fun allEntryDates(): Flow<List<String>>

    @Query("SELECT entryDate, COUNT(*) as count FROM blocks GROUP BY entryDate ORDER BY entryDate DESC")
    fun entryDateCounts(): Flow<List<EntryDateCount>>

    @Query(
        "SELECT * FROM blocks WHERE text LIKE '%' || :query || '%' " +
            "OR tag LIKE '%' || :query || '%' OR mood LIKE '%' || :query || '%' " +
            "ORDER BY timestamp DESC"
    )
    fun search(query: String): Flow<List<Block>>

    @Query("SELECT * FROM blocks")
    suspend fun allBlocksOnce(): List<Block>

    @Insert
    suspend fun insert(block: Block)

    @Update
    suspend fun update(block: Block)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(blocks: List<Block>)
}

data class EntryDateCount(val entryDate: String, val count: Int)
