package com.teamproject.gastroventure.vo;

/**
 * Created by hanman-yong on 2020/03/17.
 */
public class ReviewVo {
    public String review_key;
    public String store_name;
    public String menu;
    public double rating_num;
    public String menu_image;
    public String review_content;
    public String write_user;

    public ReviewVo() {
    }

    public String getReview_key() {
        return review_key;
    }

    public void setReview_key(String review_key) {
        this.review_key = review_key;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public double getRating_num() {
        return rating_num;
    }

    public void setRating_num(double rating_num) {
        this.rating_num = rating_num;
    }

    public String getMenu_image() {
        return menu_image;
    }

    public void setMenu_image(String menu_image) {
        this.menu_image = menu_image;
    }

    public String getReview_content() {
        return review_content;
    }

    public void setReview_content(String review_content) {
        this.review_content = review_content;
    }

    public String getWrite_user() {
        return write_user;
    }

    public void setWrite_user(String write_user) {
        this.write_user = write_user;
    }
}
