package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(
    tableName = "notifications",
    indices = [Index("portfolioName")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateInMillis: Long,
    val message: String,
    val portfolioName: String?,
    val stockSymbol: String?,
    val currencyCode: String?,
    val pricePerStock: Double?,
    val orderAction: String?
) {
    fun toDomain(): com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage =
        com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage(
            dateInMillis = dateInMillis,
            message = message,
            portfolioName = portfolioName,
            stockSymbol = stockSymbol,
            currencyCode = currencyCode,
            pricePerStock = pricePerStock,
            orderAction = orderAction
        )

    companion object {
        fun fromDomain(notification: com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage): NotificationEntity =
            NotificationEntity(
                dateInMillis = notification.dateInMillis,
                message = notification.message,
                portfolioName = notification.portfolioName,
                stockSymbol = notification.stockSymbol,
                currencyCode = notification.currencyCode,
                pricePerStock = notification.pricePerStock,
                orderAction = notification.orderAction
            )
    }
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id OR (dateInMillis = :dateInMillis AND message = :message)")
    fun delete(id: Long, dateInMillis: Long, message: String)

    @Query("DELETE FROM notifications")
    fun deleteAll()
}
