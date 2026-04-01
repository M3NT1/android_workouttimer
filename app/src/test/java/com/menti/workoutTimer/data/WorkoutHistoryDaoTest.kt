package com.menti.workoutTimer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.menti.workoutTimer.model.WorkoutHistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Instrumentation tests for WorkoutHistoryDao using an in-memory database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WorkoutHistoryDaoTest {

    private lateinit var database: WorkoutHistoryDatabase
    private lateinit var dao: WorkoutHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            WorkoutHistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.workoutHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertEntry_returnsPositiveId() = runTest {
        val entry = createTestEntry()
        val id = dao.insertEntry(entry)
        assertTrue(id > 0)
    }

    @Test
    fun getAllEntries_returnsInsertedEntries() = runTest {
        val entry = createTestEntry()
        dao.insertEntry(entry)

        val entries = dao.getAllEntriesSync()
        assertEquals(1, entries.size)
        assertEquals(entry.workoutDuration, entries[0].workoutDuration)
    }

    @Test
    fun getAllEntries_returnsEntriesOrderedByDateDescending() = runTest {
        val entry1 = createTestEntry(id = 1L, date = 1000L)
        val entry2 = createTestEntry(id = 2L, date = 2000L)
        val entry3 = createTestEntry(id = 3L, date = 3000L)

        dao.insertEntry(entry1)
        dao.insertEntry(entry2)
        dao.insertEntry(entry3)

        val entries = dao.getAllEntriesSync()
        assertEquals(3, entries.size)
        assertEquals(3000L, entries[0].date)
        assertEquals(2000L, entries[1].date)
        assertEquals(1000L, entries[2].date)
    }

    @Test
    fun getEntriesByDate_returnsOnlyEntriesInRange() = runTest {
        val entry1 = createTestEntry(id = 1L, date = 1000L)
        val entry2 = createTestEntry(id = 2L, date = 5000L)
        val entry3 = createTestEntry(id = 3L, date = 10000L)

        dao.insertEntry(entry1)
        dao.insertEntry(entry2)
        dao.insertEntry(entry3)

        val entries = dao.getEntriesByDateSync(2000L, 8000L)
        assertEquals(1, entries.size)
        assertEquals(5000L, entries[0].date)
    }

    @Test
    fun getTotalWorkoutCount_returnsCorrectCount() = runTest {
        dao.insertEntry(createTestEntry(id = 1L))
        dao.insertEntry(createTestEntry(id = 2L))
        dao.insertEntry(createTestEntry(id = 3L))

        val count = dao.getTotalWorkoutCount().first()
        assertEquals(3, count)
    }

    @Test
    fun getAverageCompletionRate_returnsCorrectAverage() = runTest {
        dao.insertEntry(createTestEntry(id = 1L, rounds = 4, completedRounds = 4)) // 100%
        dao.insertEntry(createTestEntry(id = 2L, rounds = 4, completedRounds = 2)) // 50%
        dao.insertEntry(createTestEntry(id = 3L, rounds = 4, completedRounds = 3)) // 75%

        val average = dao.getAverageCompletionRate().first()
        assertNotNull(average)
        assertEquals(0.75f, average!!, 0.01f)
    }

    @Test
    fun getRecentEntries_returnsLimitedEntries() = runTest {
        for (i in 1..10) {
            dao.insertEntry(createTestEntry(id = i.toLong(), date = i.toLong() * 1000))
        }

        val recent = dao.getRecentEntries(3).first()
        assertEquals(3, recent.size)
        assertEquals(10000L, recent[0].date)
    }

    @Test
    fun deleteEntry_removesEntry() = runTest {
        val entry = createTestEntry()
        dao.insertEntry(entry)

        val inserted = dao.getAllEntriesSync()
        assertEquals(1, inserted.size)

        val deleted = dao.deleteEntry(inserted[0])
        assertEquals(1, deleted)

        val afterDelete = dao.getAllEntriesSync()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun deleteAllEntries_removesAllEntries() = runTest {
        dao.insertEntry(createTestEntry(id = 1L))
        dao.insertEntry(createTestEntry(id = 2L))
        dao.insertEntry(createTestEntry(id = 3L))

        val deleted = dao.deleteAllEntries()
        assertEquals(3, deleted)

        val afterDelete = dao.getAllEntriesSync()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun insertEntryWithSameId_replacesEntry() = runTest {
        val entry1 = createTestEntry(id = 1L, workoutDuration = 30)
        val entry2 = createTestEntry(id = 1L, workoutDuration = 60)

        dao.insertEntry(entry1)
        dao.insertEntry(entry2)

        val entries = dao.getAllEntriesSync()
        assertEquals(1, entries.size)
        assertEquals(60, entries[0].workoutDuration)
    }

    @Test
    fun getTotalWorkoutTime_returnsSumOfAllTimes() = runTest {
        dao.insertEntry(createTestEntry(id = 1L, totalTime = 100000))
        dao.insertEntry(createTestEntry(id = 2L, totalTime = 200000))
        dao.insertEntry(createTestEntry(id = 3L, totalTime = 300000))

        val totalTime = dao.getTotalWorkoutTime().first()
        assertNotNull(totalTime)
        assertEquals(600000L, totalTime!!)
    }

    private fun createTestEntry(
        id: Long = System.currentTimeMillis(),
        date: Long = System.currentTimeMillis(),
        workoutDuration: Int = 30,
        restDuration: Int = 15,
        rounds: Int = 4,
        completedRounds: Int = 4,
        totalTime: Long = 180000,
        completed: Boolean = true
    ): WorkoutHistoryEntry {
        return WorkoutHistoryEntry(
            id = id,
            date = date,
            workoutDuration = workoutDuration,
            restDuration = restDuration,
            rounds = rounds,
            completedRounds = completedRounds,
            totalTime = totalTime,
            completed = completed
        )
    }
}
