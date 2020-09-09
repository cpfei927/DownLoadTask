package com.xueersi.downloadtask.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.xueersi.downloadtask.downloader.FileInfo;

import java.util.ArrayList;
import java.util.List;


/*
 * 项目名:    DownLoaderManger
 * 包名       com.azhong.downloader.db
 * 文件名:    DbHelper
 * 创建者:    ZSY
 * 创建时间:  2017/2/14 on 15:20
 * 描述:     TODO 保存下载信息
 */
public class DbHelper extends SQLiteOpenHelper {

    public static String TABLE = "file";//表名

    public DbHelper(Context context) {
        super(context, "download.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table file(fileName varchar,url varchar,length integer,finished integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入一条下载信息
     */
    public void insertData(SQLiteDatabase db, FileInfo info) {
        ContentValues values = new ContentValues();
        values.put("fileName", info.getFileName());
        values.put("url", info.getUrl());
        values.put("length", info.getLength());
        values.put("finished", info.getFinished());
        db.insert(TABLE, null, values);
        db.close();
    }

    /**
     * 更新一条下载信息
     */
    public void updateData(SQLiteDatabase db, FileInfo info) {
        ContentValues values = new ContentValues();
        values.put("fileName", info.getFileName());
        values.put("length", info.getLength());
        values.put("finished", info.getFinished());
        db.update(TABLE, values, "url = ?", new String[]{info.getUrl()});
        db.close();
    }

    /**
     * 删除一条下载信息
     */
    public void deleteData(SQLiteDatabase db, String url) {
        Cursor cursor = db.query(TABLE, null, "url = ?", new String[]{url}, null, null, null, null);
        //如果数据库中没有这条数据
        if (cursor != null) {
            db.delete(TABLE, "url = ?", new String[]{url});
            cursor.close();
        }
        db.close();
    }

    /**
     * 恢复一条下载信息
     */
    public void resetData(SQLiteDatabase db, String url) {
        ContentValues values = new ContentValues();
        values.put("finished", 0);
        values.put("length", 0);
        db.update(TABLE, values, "url = ?", new String[]{url});
        db.close();
    }

    /**
     * 查询所有下载信息
     */
    public List<FileInfo> queryAllData(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE, null, null, null, null, null, null, null);
        List<FileInfo> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileInfo fileInfo = new FileInfo();
                String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                int length = cursor.getInt(cursor.getColumnIndex("length"));
                int finished = cursor.getInt(cursor.getColumnIndex("finished"));
                fileInfo.setFileName(fileName);
                fileInfo.setUrl(url);
                fileInfo.setLength(length);
                fileInfo.setFinished(finished);
                list.add(fileInfo);
            }
            cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * 是否已经插入这条数据
     */
    public boolean isExist(SQLiteDatabase db, FileInfo info) {
        Cursor cursor = db.query(TABLE, null, "url = ?", new String[]{info.getUrl()}, null, null, null, null);
        boolean exist = cursor.moveToNext();
        cursor.close();
        db.close();
        return exist;
    }

    /**
     * 查询已经存在的一条信息
     */
    public FileInfo queryData(SQLiteDatabase db, String url) {
        Cursor cursor = db.query(TABLE, null, "url = ?", new String[]{url}, null, null, null, null);
        FileInfo info = new FileInfo();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                int length = cursor.getInt(cursor.getColumnIndex("length"));
                int finished = cursor.getInt(cursor.getColumnIndex("finished"));
                info.setStop(false);
                info.setFileName(fileName);
                info.setUrl(url);
                info.setLength(length);
                info.setFinished(finished);
            }
            cursor.close();
            db.close();
        }
        return info;
    }
}
