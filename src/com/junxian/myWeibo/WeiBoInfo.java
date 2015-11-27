package com.junxian.myWeibo;

public class WeiBoInfo {

	public void setWeibo_source(String weibo_source) {
		this.weibo_source = weibo_source;
	}
	
	public String thu_pic;
	public String middle_pic;
	public String original_pic;
	
	
	
	public String getMiddle_pic() {
		return middle_pic;
	}

	public void setMiddle_pic(String middle_pic) {
		this.middle_pic = middle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}

	public void setOriginal_pic(String original_pic) {
		this.original_pic = original_pic;
	}

	public String getThu_pic() {
		if (thu_pic == null) return "nothing ";
		return thu_pic;
	}

	public void setThu_pic(String thu_pic) {
		this.thu_pic = thu_pic;
	}

	// ����id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// ������id
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	// ����������
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	// ������ͷ��
	private String userIcon;

	public String getUserIcon() {
		return userIcon;
	}

	public void setUserIcon(String userIcon) {
		this.userIcon = userIcon;
	}

	// ����ʱ��
	private String time;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	// �Ƿ���ͼƬ
	private Boolean haveImage = false;

	public Boolean getHaveImage() {
		return haveImage;
	}

	public void setHaveImage(Boolean haveImage) {
		this.haveImage = haveImage;
	}

	// ��������
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isretweedted = false;

	public boolean isIsretweedted() {
		return isretweedted;
	}

	public void setIsretweedted(boolean isretweedted) {
		this.isretweedted = isretweedted;
	}

	public String reposts_count;
	public String comments_count;
	public String attitudes_count;

	public String getReposts_count() {
		return reposts_count;
	}

	public void setReposts_count(String reposts_count) {
		this.reposts_count = reposts_count;
	}

	public String getComments_count() {
		return comments_count;
	}

	public void setComments_count(String comments_count) {
		this.comments_count = comments_count;
	}

	public String getAttitudes_count() {
		return attitudes_count;
	}

	public void setAttitudes_count(String attitudes_count) {
		this.attitudes_count = attitudes_count;
	}

	public String weibo_source;

	public void setWeiboSource(String weiboSource) {
		this.weibo_source = weiboSource;
	}

	public String getWeibo_source() {
		return weibo_source;
	}
}