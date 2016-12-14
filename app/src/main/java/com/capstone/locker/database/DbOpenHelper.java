package com.capstone.locker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.capstone.locker.main.model.ListViewItem;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DB에 대한 함수가 정의된 곳
 *
 */
public class DbOpenHelper {

    private static final String DATABASE_NAME = "modulelist.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private ArrayList<ListViewItem> itemDatas = null;
    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);

        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }
    /**
     * DB에 데이터 추가
    */
    public void DbInsert(ItemData itemData ){

        mDB = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ident_name",itemData.identName);
        values.put("ident_num",itemData.identNum);
        values.put("nickname",itemData.nickName);
        values.put("qualificaion",itemData.qualificaion);
        values.put("owner_pwd", itemData.ownerPwd);
        values.put("quest_pwd",itemData.guestPwd);
        values.put("icon",itemData.icon);
        values.put("created",itemData.created);
        values.put("pushcheck",itemData.pushcheck);

        mDB.insert("moduleinfo",null,values);

    }

    /**
     * DB항목 업그레이드 - 수정할 때 사용
     */
    public void DbUpdate(String id, ItemData itemData ){

        ContentValues values = new ContentValues();
        values.put("nickname",itemData.nickName);
        values.put("icon",itemData.icon);
        values.put("pushcheck",itemData.pushcheck);

        if(itemData.ownerPwd != null && itemData.ownerPwd.length() !=0 ){
            values.put("owner_pwd",itemData.ownerPwd);
        }
        if(itemData.guestPwd != null && itemData.guestPwd.length() !=0 ){
            values.put("quest_pwd",itemData.guestPwd);
        }

        mDB.update("moduleinfo", values, "_id=?", new String[]{id});

    }

    /**
     * 항목 삭제하는 함수
     * @param id
     */
    public void DbDelete(String id) {
        mDB.delete("moduleinfo", "_id=?", new String[]{id});
    }


    /**
     * modulelist테이블에 저장되어있는 값들을 반환하는 함수 - 리스트뷰 뿌릴 때 호출
     * @return
     */
    public ArrayList<ListViewItem> DbMainSelect(){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo" , null);

        itemDatas = new ArrayList<ListViewItem>();
//
//        Log.i("myTag" , "갯수 : " + String.valueOf(c.getCount()));

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            String nickName = c.getString(c.getColumnIndex("nickname"));
            String qualificaion = c.getString(c.getColumnIndex("qualificaion"));
            String ownerPwd = c.getString(c.getColumnIndex("owner_pwd"));
            String questPwd = c.getString(c.getColumnIndex("quest_pwd"));
            int icon = c.getInt(c.getColumnIndex("icon"));
            String created = c.getString(c.getColumnIndex("created"));
            int pushcheck = c.getInt(c.getColumnIndex("pushcheck"));


            ListViewItem listViewItem = new ListViewItem(_id,icon,qualificaion,nickName);

//            listViewItem.id = _id;
//            listViewItem.nickName = nickName;
//            listViewItem.qualification = qualificaion;
//            listViewItem.img = icon;

            itemDatas.add(listViewItem);

        }


        return itemDatas;
    }

//    public ArrayList<ListViewItem> DbMainExcept(){
//        SQLiteDatabase getDb;
//        getDb = mDBHelper.getReadableDatabase();
//        Cursor c = getDb.rawQuery( "select * from moduleinfo where ident_num not in " + ApplicationController.getInstance().mDeviceAddress, null);
//
//        itemDatas = new ArrayList<ListViewItem>();
////
////        Log.i("myTag" , "갯수 : " + String.valueOf(c.getCount()));
//
//        while(c.moveToNext()){
//            int _id = c.getInt(c.getColumnIndex("_id"));
//            String identName = c.getString(c.getColumnIndex("ident_name"));
//            String identNum = c.getString(c.getColumnIndex("ident_num"));
//            String nickName = c.getString(c.getColumnIndex("nickname"));
//            String qualificaion = c.getString(c.getColumnIndex("qualificaion"));
//            String ownerPwd = c.getString(c.getColumnIndex("owner_pwd"));
//            String questPwd = c.getString(c.getColumnIndex("quest_pwd"));
//            int icon = c.getInt(c.getColumnIndex("icon"));
//            String created = c.getString(c.getColumnIndex("created"));
//            int pushcheck = c.getInt(c.getColumnIndex("pushcheck"));
//
//
//            ListViewItem listViewItem = new ListViewItem();
//
//            listViewItem.id = _id;
//            listViewItem.nickName = nickName;
//            listViewItem.qualification = qualificaion;
//            listViewItem.img = icon;
//
//            itemDatas.add(listViewItem);
//
//        }
//
//
//        return itemDatas;
//    }



    public ItemData DbDetail(String id){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo where _id = "+id , null);

        ItemData item = new ItemData();
//
//        Log.i("myTag" , "갯수 : " + String.valueOf(c.getCount()));

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            String nickName = c.getString(c.getColumnIndex("nickname"));
            String qualificaion = c.getString(c.getColumnIndex("qualificaion"));
            String ownerPwd = c.getString(c.getColumnIndex("owner_pwd"));
            String guestPwd = c.getString(c.getColumnIndex("quest_pwd"));
            int icon = c.getInt(c.getColumnIndex("icon"));
            String created = c.getString(c.getColumnIndex("created"));
            int pushcheck = c.getInt(c.getColumnIndex("pushcheck"));


            item.identName = identName;
            item.identNum = identNum;
            item.nickName = nickName;
            item.qualificaion = qualificaion;
            item.ownerPwd = ownerPwd;
            item.guestPwd = guestPwd;
            item.icon = icon;
            item.created = created;
            item.pushcheck = pushcheck;

        }


        return item;
    }


    public ItemData DbFindMoudle(String num){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo where ident_num = '"+ num +"'" , null);

        ItemData item = new ItemData();
//
//        Log.i("myTag" , "갯수 : " + String.valueOf(c.getCount()));

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            String nickName = c.getString(c.getColumnIndex("nickname"));
            String qualificaion = c.getString(c.getColumnIndex("qualificaion"));
            String ownerPwd = c.getString(c.getColumnIndex("owner_pwd"));
            String guestPwd = c.getString(c.getColumnIndex("quest_pwd"));
            int icon = c.getInt(c.getColumnIndex("icon"));
            String created = c.getString(c.getColumnIndex("created"));
            int pushcheck = c.getInt(c.getColumnIndex("pushcheck"));

            item.Id = _id;
            item.identName = identName;
            item.identNum = identNum;
            item.nickName = nickName;
            item.qualificaion = qualificaion;
            item.ownerPwd = ownerPwd;
            item.guestPwd = guestPwd;
            item.icon = icon;
            item.created = created;
            item.pushcheck = pushcheck;

        }


        return item;
    }



    public void close(){
        mDB.close();
    }

}
