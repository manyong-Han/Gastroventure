package com.teamproject.gastroventure.vo;

/**
 * Created by hanman-yong on 2020/03/16.
 */
public class UserInfo {
    public String user_key;
    public String id;
    public String pwd;
    public String name;
    public String nickname;
    public String tel;

    public UserInfo() {
    }

    // 회원가입시
    public UserInfo(String id, String pwd, String name, String nickname, String tel) {
        this.id = id;
        this.pwd = pwd;
        this.name = name;
        this.nickname = nickname;
        this.tel = tel;
    }

    // 아이디 찾기
    public UserInfo(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }

    // 비밀번호 찾기
    public UserInfo(String id, String name, String nickname) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
    }
    // 수정
    public UserInfo(String pwd, String name, String nickname, String tel) {
        this.pwd = pwd;
        this.name = name;
        this.nickname = nickname;
        this.tel = tel;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
