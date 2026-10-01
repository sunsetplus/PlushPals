package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PlushieDao
import com.example.data.local.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        PostEntity::class,
        CommentEntity::class,
        StoryEntity::class,
        ReelEntity::class,
        MessageEntity::class,
        ChatThreadEntity::class,
        UserAffinityEntity::class,
        NightAiMessageEntity::class,
        ArtistTrackEntity::class,
        ArtistAlbumEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PlushieDatabase : RoomDatabase() {

    abstract fun plushieDao(): PlushieDao

    companion object {
        @Volatile
        private var INSTANCE: PlushieDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PlushieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlushieDatabase::class.java,
                    "plushie_paws_database"
                )
                    .addCallback(PlushieDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PlushieDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureCurrentUserInitialized(database.plushieDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureCurrentUserInitialized(database.plushieDao())
                    }
                }
            }
        }

        suspend fun ensureCurrentUserInitialized(dao: PlushieDao) {
            val existing = dao.getCurrentUserSync()
            if (existing == null) {
                val currentUser = UserProfileEntity(
                    userId = "user_me",
                    displayName = "Alex & Barnaby",
                    plushieName = "Barnaby",
                    plushieBreed = "Golden Retriever",
                    bio = "Proud parent of Barnaby, my emotional support dog plushie.",
                    comfortRole = "Sensory Grounding & Anxiety Relief",
                    profileAvatarUri = "drawable://plushie_hero_dog",
                    isVerified = true,
                    verificationBadgeType = "CERTIFIED_COMPANION",
                    isArtist = false,
                    artistGenre = "Bedtime Lullabies & Ambient Sounds",
                    artistBio = "",
                    joinedDate = System.currentTimeMillis(),
                    followerCount = 0,
                    followingCount = 0,
                    isCurrentUser = true
                )
                dao.insertUser(currentUser)
            }
        }
    }
}
