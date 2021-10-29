package com.example.our_coffee.Utils;
// 사용자가 원하는 종류의 음료를 고르기 위한 카테고리를 표현하는 리사이클러뷰 아이템이다.
public class CoffeeCategory {

    // 음료의 카테고리 명을 표현한다.
    String category_name;
    int img_url;

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getImg_url() {
        return img_url;
    }

    public void setImg_url(int img_url) {
        this.img_url = img_url;
    }

    public CoffeeCategory(String category_name, int img_url) {
        this.category_name = category_name;
        this.img_url = img_url;
    }
}
