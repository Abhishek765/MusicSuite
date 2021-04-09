package com.example.musicsuite.databases

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.musicsuite.databases.EchoDatabase.Staticated.TABLE_NAME
import com.example.musicsuite.databases.EchoDatabase.Staticated.TABLE_NAME1
import com.example.musicsuite.databases.EchoDatabase.Staticated.TABLE_NAME2
import com.example.musicsuite.Songs


class EchoDatabase : SQLiteOpenHelper {
    //    Favorite songList
    var _songList = ArrayList<Songs>()

    //    Happy SongList
    var HappysongList = ArrayList<Songs>()
    var SadsongList = ArrayList<Songs>()
//    var _songList = ArrayList<Songs>()
//    var _songList = ArrayList<Songs>()


    object Staticated {
        var DB_VERSION = 3
        val DB_NAME = "FavoriteDatabase"

        //        Favorite table
        val TABLE_NAME = "FavoriteTable"

        // Happy table
        val TABLE_NAME1 = "HappyTable"

        // Sad table
        val TABLE_NAME2 = "SadTable"

        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"


    }

    @SuppressLint("SQLiteString")
    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
//        Creating Favorite table
        sqliteDatabase?.execSQL("CREATE TABLE $TABLE_NAME ( ${Staticated.COLUMN_ID} INTEGER, ${Staticated.COLUMN_SONG_ARTIST} STRING, ${Staticated.COLUMN_SONG_TITLE} STRING, ${Staticated.COLUMN_SONG_PATH} STRING);")

//        Creating Happy table
        sqliteDatabase?.execSQL("CREATE TABLE $TABLE_NAME1 ( ${Staticated.COLUMN_ID} INTEGER, ${Staticated.COLUMN_SONG_ARTIST} STRING, ${Staticated.COLUMN_SONG_TITLE} STRING, ${Staticated.COLUMN_SONG_PATH} STRING);")

        //        Creating Sad table
        sqliteDatabase?.execSQL("CREATE TABLE $TABLE_NAME2 ( ${Staticated.COLUMN_ID} INTEGER, ${Staticated.COLUMN_SONG_ARTIST} STRING, ${Staticated.COLUMN_SONG_TITLE} STRING, ${Staticated.COLUMN_SONG_PATH} STRING);")

    }

    override fun onUpgrade(sqliteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
        sqliteDatabase?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        sqliteDatabase?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME1")
        sqliteDatabase?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME2")
        onCreate(sqliteDatabase)
    }


    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, Staticated.DB_NAME, null, Staticated.DB_VERSION)


    /** Favorite Section */
    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?) {
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>? {
        try {

            val db = this.readableDatabase
            val query_params = "SELECT * FROM $TABLE_NAME"
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0))
                } while (cSor.moveToNext())
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _songList

    }

    fun checkifIdExists(_id: Int): Boolean {
        var storeId = -1090
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME WHERE SongId = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))

            } while (cSor.moveToNext())
        } else {
            return false
        }
        return storeId != -1090
    }

    fun deleteFavourite(_id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, Staticated.COLUMN_ID + "=" + _id, null)
        db.close()
    }

    fun checkSize(): Int {
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM $TABLE_NAME"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                counter += 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }
        return counter
    }


    /** Happy Section */
    fun storeAsHappy(id: Int?, artist: String?, songTitle: String?, path: String?) {
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME1, null, contentValues)
        db.close()
    }

    fun queryDBHappyList(): ArrayList<Songs>? {
        try {

            val db = this.readableDatabase
            val query_params = "SELECT * FROM $TABLE_NAME1"
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    HappysongList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0))
                } while (cSor.moveToNext())
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HappysongList

    }

    fun checkifIdExistsHappy(_id: Int): Boolean {
        var storeId = -1091
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME1 WHERE SongId = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))

            } while (cSor.moveToNext())
        } else {
            return false
        }
        return storeId != -1091
    }

    fun deleteHappy(_id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME1, Staticated.COLUMN_ID + "=" + _id, null)
        db.close()
    }

    fun checkSizeHappy(): Int {
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM $TABLE_NAME1"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                counter += 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }
        return counter
    }


    /** Sad Section **/
    fun storeAsSad(id: Int?, artist: String?, songTitle: String?, path: String?) {
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME2, null, contentValues)
        db.close()
    }

    fun queryDBSadList(): ArrayList<Songs>? {
        try {

            val db = this.readableDatabase
            val query_params = "SELECT * FROM $TABLE_NAME2"
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    SadsongList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0))
                } while (cSor.moveToNext())
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return SadsongList

    }

    fun checkifIdExistsSad(_id: Int): Boolean {
        var storeId = -1092
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME2 WHERE SongId = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))

            } while (cSor.moveToNext())
        } else {
            return false
        }
        return storeId != -1092
    }

    fun deleteSad(_id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME2, Staticated.COLUMN_ID + "=" + _id, null)
        db.close()
    }

    fun checkSizeSad(): Int {
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM $TABLE_NAME2"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                counter += 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }
        return counter
    }


}