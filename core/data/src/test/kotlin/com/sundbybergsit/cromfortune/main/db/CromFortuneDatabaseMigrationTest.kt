package com.sundbybergsit.cromfortune.main.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.CromTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class CromFortuneDatabaseMigrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    @Test
    fun `MIGRATION_1_2 - migrates database from version 1 to 2 without losing data`() {
        val dbName = "migration_test_db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `portfolios` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")
                        db.execSQL("INSERT INTO `portfolios` (`name`) VALUES ('Default')")
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                })
                .build()
        )
        val v1Db = helper.writableDatabase
        v1Db.version = 1
        v1Db.close()

        val roomDb = Room.databaseBuilder(context, CromFortuneDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val portfolios = roomDb.portfolioDao().getAllPortfoliosList()
        assertTrue(portfolios.contains("Default"))

        roomDb.close()
        context.deleteDatabase(dbName)
    }
}
