package com.john.inkwell.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One timestamped block of writing. A day's DiaryEntry is just the
 * collection of Blocks whose [entryDate] matches that day — there is
 * no separate "entry" table because grouping by date is enough.
 *
 * The [timestamp] is fixed the moment the block is created (when the
 * user taps "+") and never changes, even if [text] is edited later —
 * that's what keeps the running-log feeling honest.
 *
 * [id] is a client-generated UUID rather than an auto-incrementing Long.
 * That matters once Drive sync is involved: two different phones each
 * autoincrementing from 1 would produce colliding ids the moment their
 * backups merge. A UUID makes every block globally unique regardless
 * of which device (or how many devices) created it.
 */
@Entity(tableName = "blocks")
data class Block(
    @PrimaryKey val id: String,
    /** yyyy-MM-dd, local date. Groups blocks into a day's entry. */
    val entryDate: String,
    /** Epoch millis, set once at creation. */
    val timestamp: Long,
    val text: String,
    val tag: String? = null,
    val mood: String? = null,
    /** Epoch millis of the last text edit; used to resolve sync merges. */
    val editedAt: Long = timestamp
)
