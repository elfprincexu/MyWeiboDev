package com.junxian.myWeibo;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;

public class DBOperate {

	// ��ݿ����SNSHelper
	// user�?weibotext��
	private static final String DB_NAME = "SNSHelper.db";
	private static final String CREATE_USERTB = "create table if not exists user"
			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,userid varchar(50),name varchar(20),type varchar(20),nickname varchar(20),"
			+ "access_token varchar(50),expires_in varchar(30),location varchar(50),icon blob)";
	
	private Context context;

	public DBOperate(Context context) {
		this.context = context;
	}

	public Boolean initDB() {// ��ʼ����ݿ⣬�����һ������ʱ����
		DBHelper dbhelper = new DBHelper(context, DB_NAME, CREATE_USERTB);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		if (db.isOpen()) {
			db.close();
			return true;
		} else {
			return false;
		}
	}

	public Long insertUser(ContentValues values) {
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Long row_id = db.insert("user", null, values);
		db.close();
		return row_id;

	}

	public Boolean isExist(ContentValues values) {
		List<UserInfo> userlist= findAllUser();
		System.out.println("userlist size: " + userlist.size() );
		if (userlist.size() == 0) {
			return false;
		} 
		else {
			for (int i = 0; i < userlist.size(); i++) {
				//WRONG HERE? TO BE DONE
				if ( userlist.get(i).getUserid().equals( values.get("userid"))) {
					return true;
				}
			}
			return false;
		}
	}

	public List<UserInfo> findAllUser() {
		List<UserInfo> userlist = new ArrayList<UserInfo>();
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("user", null, null, null, null, null,"id desc");
		cursor.moveToFirst();
		int i = 1;
		System.out.println("++++++++++ find all user+++++++++");
		while ((!cursor.isAfterLast()) && (cursor.getString(1) != null)) {
			UserInfo user = new UserInfo();
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			user.setId(id);
			String userid = cursor.getString(cursor.getColumnIndex("userid"));
			user.setUserid(userid);
			String name = cursor.getString(cursor.getColumnIndex("name"));
			user.setName(name);
			String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
			user.setNickname(nickname);
			String type = cursor.getString(cursor.getColumnIndex("type"));
			user.setType(type);
			String access_token = cursor.getString(cursor.getColumnIndex("access_token"));
			user.setAccess_token(access_token);
			String expires_in = cursor.getString(cursor.getColumnIndex("expires_in"));
			user.setExpires_in(expires_in);
			String location = cursor.getString(cursor.getColumnIndex("location"));
			user.setLocation(location);
			ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("icon")));
			Drawable icon = Drawable.createFromStream(stream, "img_" + nickname);
			user.setIcon(icon);
			userlist.add(user);
			cursor.moveToNext();
			System.out.println("user number: " + i++);
			System.out.println("++++++++++" + userid + "+++++++++");
			System.out.println("++++++++++" + name + "+++++++++");
			System.out.println("++++++++++" + type + "+++++++++");
			System.out.println("++++++++++" + access_token + "+++++++++");
			System.out.println("++++++++++" + expires_in + "+++++++++");
			System.out.println("++++++++++" + location + "+++++++++");
		}
		System.out.println("++++++++++ find all user+++++++++");
		cursor.close();
		db.close();
		return userlist;

	}

	public UserInfo findUserByNickname(String userNickName) {// select the user according to the userid
		UserInfo user = new UserInfo();
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("user", null, "nickname=?",new String[]{userNickName}, null, null,null);
		Boolean isFind = cursor.moveToFirst();
		if (isFind) {
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			user.setId(id);
			String name = cursor.getString(cursor.getColumnIndex("name"));
			user.setName(name);
			String userid = cursor.getString(cursor.getColumnIndex("userid"));
			user.setUserid(userid);
			String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
			user.setNickname(nickname);
			String type = cursor.getString(cursor.getColumnIndex("type"));
			user.setType(type);
			String token = cursor.getString(cursor.getColumnIndex("access_token"));
			user.setAccess_token(token);
			String expires_in = cursor.getString(cursor.getColumnIndex("expires_in"));
			user.setExpires_in(expires_in);
			String location = cursor.getString(cursor.getColumnIndex("location"));
			user.setLocation(location);
			ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("icon")));
			Drawable icon = Drawable.createFromStream(stream, "img_" + name);
			user.setIcon(icon);
			cursor.close();
			db.close();
			System.out.println("++++find the user in the data base++++");
			System.out.println("++++++++++" + userid + "+++++++++");
			System.out.println("++++++++++" + name + "+++++++++");
			System.out.println("++++++++++" + type + "+++++++++");
			System.out.println("++++++++++" + token + "+++++++++");
			System.out.println("++++++++++" + expires_in + "+++++++++");
			System.out.println("++++++++++" + location + "+++++++++");
			System.out.println("++++find the user in the data base++++");
			return user;
		} else {
			cursor.close();
			db.close();
			System.out.println("the user is not in the list");
			return null;
		}

	}
	
	public UserInfo findUserByUserId(String userId) {// select the user according to the userid
		UserInfo user = new UserInfo();
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("user", null, "userid=?",new String[]{userId}, null, null,null);
		Boolean isFind = cursor.moveToFirst();
		if (isFind) {
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			user.setId(id);
			String name = cursor.getString(cursor.getColumnIndex("name"));
			user.setName(name);
			String userid = cursor.getString(cursor.getColumnIndex("userid"));
			user.setUserid(userid);
			String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
			user.setNickname(nickname);
			String type = cursor.getString(cursor.getColumnIndex("type"));
			user.setType(type);
			String token = cursor.getString(cursor.getColumnIndex("access_token"));
			user.setAccess_token(token);
			String expires_in = cursor.getString(cursor.getColumnIndex("expires_in"));
			user.setExpires_in(expires_in);
			String location = cursor.getString(cursor.getColumnIndex("location"));
			user.setLocation(location);
			ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("icon")));
			Drawable icon = Drawable.createFromStream(stream, "img_" + name);
			user.setIcon(icon);
			cursor.close();
			db.close();
			return user;
		} else {
			cursor.close();
			db.close();
			System.out.println("the user is not in the list");
			return null;
		}

	}

	public int deleteUser(String nickName) {// delet the user according to the usernickname
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		int row = db.delete("user", "nickname=?", new String[]{nickName});
		db.close();
		return row;
	}
	
	
	public int updateUser(ContentValues values, String userid){
		DBHelper dbhelper = new DBHelper(context, DB_NAME);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		int row=db.update("user", values, "userid=?", new String[]{userid});
		return row;
		
	}
	
	
}