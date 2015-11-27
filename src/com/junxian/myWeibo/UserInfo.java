package com.junxian.myWeibo;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public class UserInfo implements Serializable {

	private static final long serialVersionUID = 8825186846021795496L;
	
	private int id;// User表中的ID  
    private String name;// 用户姓名  
    private String nickname;  //
    private String userid;//网络中的id  
    private Drawable icon;// 用户头像，用二进制字符串存储在数据库中  
    private String type;// 用户类型，例如SINA，QQ，RENREN等  
    private String access_token;// OAuth认证token  
    private String location;// OAuth认证key（secrete）  
    private String expires_in;

	public String getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", name=" + name + ", nickname="
				+ nickname + ", userid=" + userid + ", icon=" + icon
				+ ", type=" + type + ", access_token=" + access_token
				+ ", location=" + location + ", expires_in=" + expires_in + "]";
	}

	
}
