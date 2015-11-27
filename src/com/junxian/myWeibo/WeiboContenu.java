package com.junxian.myWeibo;

public class WeiboContenu {

	public WeiboContenu() {
		super();
	}

	public String username;
	public String usericon;
	public String text;
	public String bmiddle_pic;
	public String original_pic;
	public String comments;
	public String reposts;
	public String thumbnail_pic;
	public boolean hasPic = false;

	
	
	
	@Override
	public String toString() {
		return "WeiboContenu [username=" + username + ", usericon=" + usericon
				+ ", text=" + text + ", bmiddle_pic=" + bmiddle_pic
				+ ", original_pic=" + original_pic + ", comments=" + comments
				+ ", reposts=" + reposts + ", thumbnail_pic=" + thumbnail_pic
				+ ", hasPic=" + hasPic + "]";
	}

	public boolean isHasPic() {
		return hasPic;
	}

	public void setHasPic(boolean hasPic) {
		this.hasPic = hasPic;
	}

	public String getBmiddle_pic() {
		return bmiddle_pic;
	}

	public void setBmiddle_pic(String bmiddle_pic) {
		this.bmiddle_pic = bmiddle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}

	public void setOriginal_pic(String original_pic) {
		this.original_pic = original_pic;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsericon() {
		return usericon;
	}

	public void setUsericon(String usericon) {
		this.usericon = usericon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getReposts() {
		return reposts;
	}

	public void setReposts(String reposts) {
		this.reposts = reposts;
	}

	public void setThumbnail_pic(String thumbnail_pic) {
		this.thumbnail_pic = thumbnail_pic;
	}

	public String getThumbnail_pic() {
		return thumbnail_pic;
	}

}
