package com.example.our_coffee.Utils;
// 커피 설정화면에서 보여줄 리사이클러뷰의 아이템을 표현하는 클래스 파일.
public class Coffee {
    //음료명
    String drink_name;
    //음료 이미지 주소
    String drink_url;

    public String getDrink_name() {
        return drink_name;
    }

    public void setDrink_name(String drink_name) {
        this.drink_name = drink_name;
    }

    public String getDrink_url() {
        return drink_url;
    }

    public void setDrink_url(String drink_url) {
        this.drink_url = drink_url;
    }

    public Coffee(String drink_name, String drink_url) {
        this.drink_name = drink_name;
        this.drink_url = drink_url;
    }
}
