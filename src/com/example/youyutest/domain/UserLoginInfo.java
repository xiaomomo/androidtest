package com.example.youyutest.domain;

public class UserLoginInfo {

	private String email;
	private String password;
	private String token;
	private String userId;
	private String expire;
	//下载高品质音乐时用
	private String baiduCookie;
	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getExpire() {
		return expire;
	}
	public void setExpire(String expire) {
		this.expire = expire;
	}
	public String getBaiduCookie() {
		return baiduCookie;
	}
	public void setBaiduCookie(String baiduCookie) {
		this.baiduCookie = baiduCookie;
	}
	
	
	
}
