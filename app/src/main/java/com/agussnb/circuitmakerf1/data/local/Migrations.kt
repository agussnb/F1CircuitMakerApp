package com.agussnb.circuitmakerf1.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Versión 1 → 2:
 * - `track` suma la columna `sportType` (los circuitos existentes quedan como ciclismo).
 * - Tablas nuevas: `track_point` (trazado), `session` y `lap` (vueltas cronometradas).
 *
 * El SQL está copiado del esquema que exporta Room (`app/schemas/.../2.json`), porque al abrir
 * la base Room verifica que las tablas sean exactamente las que esperan las entidades.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `track` ADD COLUMN `sportType` TEXT NOT NULL DEFAULT 'CYCLING'")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `track_point` (`trackId` INTEGER NOT NULL, " +
                    "`position` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, " +
                    "PRIMARY KEY(`trackId`, `position`), FOREIGN KEY(`trackId`) REFERENCES `track`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_point_trackId` ON `track_point` (`trackId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `session` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`trackId` INTEGER NOT NULL, `startedAtMillis` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`trackId`) REFERENCES `track`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_trackId` ON `session` (`trackId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `lap` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`sessionId` INTEGER NOT NULL, `lapNumber` INTEGER NOT NULL, " +
                    "`sector1Millis` INTEGER NOT NULL, `sector2Millis` INTEGER NOT NULL, " +
                    "`sector3Millis` INTEGER NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lap_sessionId` ON `lap` (`sessionId`)")
    }
}
