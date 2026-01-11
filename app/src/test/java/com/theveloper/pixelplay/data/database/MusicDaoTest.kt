package com.theveloper.pixelplay.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MusicDaoTest {

    private lateinit var musicDao: MusicDao
    private lateinit var db: PixelPlayDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PixelPlayDatabase::class.java)
            .allowMainThreadQueries() // Permite consultas en el hilo principal para tests
            .build()
        musicDao = db.musicDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSongs() = runTest {
        val songList = listOf(
            SongEntity(1L, "Song A", "Artist 1", 101L, null, "Album X", 201L, "uri_a", "art_uri_a", 180000, "Pop", "/path/a", "/parent/path", false, null, 1, 2020, System.currentTimeMillis(), "audio/mpeg", 320000, 44100),
            SongEntity(2L, "Song B", "Artist 2", 102L, null, "Album Y", 202L, "uri_b", "art_uri_b", 240000, "Rock", "/path/b", "/parent/path", false, null, 2, 2021, System.currentTimeMillis(), "audio/mpeg", 320000, 44100)
        )
        musicDao.insertSongs(songList)

        val retrievedSongs = musicDao.getSongs(emptyList(), false).first()
        assertThat(retrievedSongs).hasSize(2)
        assertThat(retrievedSongs).containsExactlyElementsIn(songList.sortedBy { it.title })
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetAlbums() = runTest {
        val albumList = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Album Y", "Artist 2", 102L, "art_uri_y", 8, 2021)
        )
        musicDao.insertAlbums(albumList)
        val retrievedAlbums = musicDao.getAlbums(emptyList(), false).first()
        assertThat(retrievedAlbums).hasSize(2)
        assertThat(retrievedAlbums).containsExactlyElementsIn(albumList.sortedBy { it.title })
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetArtists() = runTest {
        val artistList = listOf(
            ArtistEntity(101L, "Artist 1", 10),
            ArtistEntity(102L, "Artist 2", 15)
        )
        musicDao.insertArtists(artistList)
        val retrievedArtists = musicDao.getArtists(emptyList(), false).first()
        assertThat(retrievedArtists).hasSize(2)
        assertThat(retrievedArtists).containsExactlyElementsIn(artistList.sortedBy { it.name })
    }

    @Test
    @Throws(Exception::class)
    fun insertMusicData_clearsOldAndInsertsNew() = runTest {
        val oldSong = SongEntity(1L, "Old Song", "Old Artist", 1L, null, "Old Album", 1L, "old_uri", null, 100, "Genre", "/old/path", "/old/parent", false, null, 0, 2020, System.currentTimeMillis(), "audio/mpeg", 128000, 44100)
        musicDao.insertSongs(listOf(oldSong))

        val songs = listOf(
            SongEntity(10L, "Song A", "Artist 1", 101L, null, "Album X", 201L, "uri_a", "art_uri_a", 180000, "Pop", "/path/a", "/parent/path", false, null, 1, 2020, System.currentTimeMillis(), "audio/mpeg", 320000, 44100)
        )
        val albums = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 1, 2020)
        )
        val artists = listOf(
            ArtistEntity(101L, "Artist 1", 1)
        )

        musicDao.insertMusicData(songs, albums, artists)

        assertThat(musicDao.getSongById(1L).first()).isNull() // Old song should be gone
        assertThat(musicDao.getSongById(10L).first()).isNotNull()
        assertThat(musicDao.getAlbumById(201L).first()).isNotNull()
        assertThat(musicDao.getArtistById(101L).first()).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun searchSongs_returnsMatchingSongs() = runTest {
        val songs = listOf(
            SongEntity(1L, "Cool Song", "Artist A", 101L, null, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/parent", false, null, 1, 2020, System.currentTimeMillis(), "audio/mpeg", 320000, 44100),
            SongEntity(2L, "Another Song", "Artist B", 102L, null, "Album Y", 202L, "uri2", null, 200, "Rock", "/p2", "/parent", false, null, 2, 2021, System.currentTimeMillis(), "audio/mpeg", 320000, 44100),
            SongEntity(3L, "Coolest Song Ever", "Artist C", 103L, null, "Album Z", 203L, "uri3", null, 220, "Pop", "/p3", "/parent", false, null, 3, 2022, System.currentTimeMillis(), "audio/mpeg", 320000, 44100)
        )
        musicDao.insertSongs(songs)

        val results = musicDao.searchSongs("Cool", emptyList(), false).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Cool Song", "Coolest Song Ever")
    }

    // TODO: Add more tests for other DAO methods:
    // - getSongsByIds, getSongsByAlbumId, getSongsByArtistId, getSongCount
    // - getAlbumById, searchAlbums, getAlbumCount, getAlbumsByArtistId
    // - getArtistById, searchArtists, getArtistCount
    // - getSongsByGenre, getUniqueGenres
    // - getAllUniqueAlbumArtUrisFromSongs
    // - Test pagination (offset, pageSize) thoroughly
    // - Test onConflictStrategy (REPLACE) for inserts
}
