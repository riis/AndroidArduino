package com.riis.androidarduino.bloodpressure;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "bp_Data_Manager";
 
    // Table names
    private static final String TABLE_USERDATA = "user_data";
    private static final String TABLE_USERINFO = "user_info";
 
    // User Data Table Columns names
    private static final String KEY_USERID = "user_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_SYSTOLIC = "systolic";
    private static final String KEY_DIASTOLIC = "diastolic";
    
    // User Info Table Columns names
    private static final String KEY_FIRSTNAME = "first_name";
    private static final String KEY_LASTNAME = "last_name";
    private static final String KEY_AGE = "age";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
 
    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERDATA_TABLE = "CREATE TABLE " + TABLE_USERDATA + "("
                + KEY_USERID + " INTEGER PRIMARY KEY,"
        		+ KEY_DATE + " TEXT,"
                + KEY_SYSTOLIC + " INTEGER,"
                + KEY_DIASTOLIC + " INTEGER" + ")";
        db.execSQL(CREATE_USERDATA_TABLE);
        
        String CREATE_USERINFO_TABLE = "CREATE TABLE " + TABLE_USERINFO + "("
                + KEY_USERID + " INTEGER PRIMARY KEY,"
        		+ KEY_FIRSTNAME + " TEXT,"
                + KEY_LASTNAME + " TEXT,"
                + KEY_AGE + " INTEGER"
                + KEY_WEIGHT + " INTEGER,"
                + KEY_HEIGHT + " INTEGER," + ")";
        db.execSQL(CREATE_USERINFO_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERDATA);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERINFO);
 
        // Create tables again
        onCreate(db);
    }
    
	public void addUserData(UserData data) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERID, data.getUserID());
	    values.put(KEY_DATE, data.getTimestamp());
	    values.put(KEY_SYSTOLIC, data.getSystolic());
	    values.put(KEY_DIASTOLIC, data.getDiastolic());
	 
	    // Inserting Row
	    db.insert(TABLE_USERDATA, null, values);
	    db.close(); // Closing database connection
	}
	
	public void addUseInfo(UserInfo info) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERID, info.getUserID());
	    values.put(KEY_FIRSTNAME, info.getFirstName());
	    values.put(KEY_LASTNAME, info.getLastName());
	    values.put(KEY_AGE, info.getAge());
	    values.put(KEY_WEIGHT, info.getWeight());
	    values.put(KEY_HEIGHT, info.getHeight());
	 
	    // Inserting Row
	    db.insert(TABLE_USERINFO, null, values);
	    db.close(); // Closing database connection
	}
	
    UserInfo getUserInfo(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_USERINFO, new String[] { KEY_USERID,
        		KEY_FIRSTNAME, KEY_LASTNAME, KEY_AGE, KEY_WEIGHT, KEY_HEIGHT}, KEY_USERID + "=?",
                new String[] { String.valueOf(userID) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        UserInfo info = new UserInfo(Integer.parseInt(cursor.getString(0)),
                						cursor.getString(1), 
                						cursor.getString(2),
                						Integer.parseInt(cursor.getString(3)),
                						Integer.parseInt(cursor.getString(4)),
                						Integer.parseInt(cursor.getString(5)));
        return info;
    }
 
    public List<UserData> getAllDataWithUserID(int userID) {
        List<UserData> dataList = new ArrayList<UserData>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERDATA + " WHERE " + KEY_USERID + "='" + userID + "'";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	UserData data = new UserData();
                data.setUserID(Integer.parseInt(cursor.getString(0)));
                data.setTimestamp(cursor.getString(1));
                data.setSystolic(Integer.parseInt(cursor.getString(2)));
                data.setDiastolic(Integer.parseInt(cursor.getString(3)));
                // Adding contact to list
                dataList.add(data);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return dataList;
    }
 
    // Updating single contact
    public int updateUserInfo(UserInfo info) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
	    values.put(KEY_USERID, info.getUserID());
	    values.put(KEY_FIRSTNAME, info.getFirstName());
	    values.put(KEY_LASTNAME, info.getLastName());
	    values.put(KEY_AGE, info.getAge());
	    values.put(KEY_WEIGHT, info.getWeight());
	    values.put(KEY_HEIGHT, info.getHeight());
 
        // updating row
        return db.update(TABLE_USERINFO, values, KEY_USERID + " = ?",
                new String[] { String.valueOf(info.getUserID()) });
    }
 
    public void deleteUser(int userID) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        String DELETE_USERINFO = "DELETE FROM" + TABLE_USERINFO + " " +
        						 "WHERE " + KEY_USERID + "='" + userID + "'";
    	db.execSQL(DELETE_USERINFO);
    	
    	String DELETE_USERDATA = "DELETE FROM" + TABLE_USERDATA + " " +
				 "WHERE " + KEY_USERID + "='" + userID + "'";
    	db.execSQL(DELETE_USERDATA);
        db.close();
    }
 
    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + TABLE_USERINFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }

}
