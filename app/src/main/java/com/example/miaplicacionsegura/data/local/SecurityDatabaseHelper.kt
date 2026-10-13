package com.example.miaplicacionsegura.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class EventoLog(
    val id: Int,
    val hogar: String,
    val zona: String,
    val timestamp: String,
    val descripcion: String
)

class SecurityDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "seguridad_iot.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_LOGS = "eventos_log"
        private const val COL_ID = "id"
        private const val COL_HOGAR = "hogar"
        private const val COL_ZONA = "zona"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_DESCRIPCION = "descripcion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_LOGS (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_HOGAR TEXT, " +
                "$COL_ZONA TEXT, " +
                "$COL_TIMESTAMP TEXT, " +
                "$COL_DESCRIPCION TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }

    fun insertarEvento(hogar: String, zona: String, timestamp: String, descripcion: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_HOGAR, hogar)
            put(COL_ZONA, zona)
            put(COL_TIMESTAMP, timestamp)
            put(COL_DESCRIPCION, descripcion)
        }
        db.insert(TABLE_LOGS, null, values)
        db.close()
    }

    fun obtenerHistorial(): List<EventoLog> {
        val lista = mutableListOf<EventoLog>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LOGS ORDER BY $COL_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val hogar = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOGAR)) ?: "Mi Hogar"
                val zona = cursor.getString(cursor.getColumnIndexOrThrow(COL_ZONA))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION))
                lista.add(EventoLog(id, hogar, zona, timestamp, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerHistorialPorHogar(hogar: String): List<EventoLog> {
        val lista = mutableListOf<EventoLog>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LOGS WHERE $COL_HOGAR = ? ORDER BY $COL_ID DESC", arrayOf(hogar))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val hg = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOGAR)) ?: "Mi Hogar"
                val zona = cursor.getString(cursor.getColumnIndexOrThrow(COL_ZONA))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION))
                lista.add(EventoLog(id, hg, zona, timestamp, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun limpiarHistorial() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_LOGS")
        db.close()
    }
}
