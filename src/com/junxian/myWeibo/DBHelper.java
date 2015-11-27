package com.junxian.myWeibo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private String create_usertb;


	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context, String name) {
		this(context, name, VERSION);
	}

	public DBHelper(Context context, String name, String create_usertb ) {
		this(context, name, VERSION);
		this.create_usertb = create_usertb;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(create_usertb);// ����û���Ϣ��
		System.out.println("���user��");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		System.out.print("��ݿ�汾" + newVersion);
		db.execSQL(create_usertb);// �����û���Ϣ��
		System.out.println("���user��");
		
	}

}