package com.example.news.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.news.data.local.NewsDatabase
import com.example.news.data.mapper.toBookmarkEntity
import com.example.news.data.mapper.toDomain
import com.example.news.domain.model.Article
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var database: NewsDatabase
    private val article = Article(url = "https://bbc.com/a", title = "A")

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NewsDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun saveTwice_keepsOneRow_andIsBookmarkedFollowsDeletes() = runTest {
        val dao = database.bookmarkDao
        assertFalse(dao.observeIsBookmarked(article.url).first())

        dao.upsert(article.toBookmarkEntity(savedAt = 1L))
        dao.upsert(article.copy(title = "A updated").toBookmarkEntity(savedAt = 2L))

        val saved = dao.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("A updated", saved.single().toDomain().title)
        assertTrue(dao.observeIsBookmarked(article.url).first())

        dao.deleteByUrl(article.url)
        assertFalse(dao.observeIsBookmarked(article.url).first())
    }

    @Test
    fun deleteByUrls_removesOnlyTheGivenBookmarks() = runTest {
        val dao = database.bookmarkDao
        listOf("a", "b", "c").forEachIndexed { index, id ->
            dao.upsert(Article(url = "https://bbc.com/$id").toBookmarkEntity(savedAt = index.toLong()))
        }

        dao.deleteByUrls(listOf("https://bbc.com/a", "https://bbc.com/c"))

        assertEquals(listOf("https://bbc.com/b"), dao.observeAll().first().map { it.url })
    }
}
