package com.teamproject.gastroventure.vo;

/**
 * Created by hanman-yong on 2020/03/17.
 */
public class ReviewImgVo {
    public String review_key;
    public String review_img_key;
    public String menu_image;
    public String menu_image_name;

    public ReviewImgVo() {
    }

    public ReviewImgVo(String review_key, String menu_image) {
        this.review_key = review_key;
        this.menu_image = menu_image;
    }

    public String getReview_key() {
        return review_key;
    }

    public void setReview_key(String review_key) {
        this.review_key = review_key;
    }

    public String getReview_img_key() {
        return review_img_key;
    }

    public void setReview_img_key(String review_img_key) {
        this.review_img_key = review_img_key;
    }

    public String getMenu_image() {
        return menu_image;
    }

    public void setMenu_image(String menu_image) {
        this.menu_image = menu_image;
    }

    public String getMenu_image_name() {
        return menu_image_name;
    }

    public void setMenu_image_name(String menu_image_name) {
        this.menu_image_name = menu_image_name;
    }
}
