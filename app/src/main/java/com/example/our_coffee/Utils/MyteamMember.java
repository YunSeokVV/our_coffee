package com.example.our_coffee.Utils;
// 자신의 팀원들을 립사이클러뷰로 표현하기위한 클래스다
public class MyteamMember {
    // 자신의 팀원의 이름을 표현하는 변수다
    String team_name;

    // 팀원들의 프로필 사진을 표현하는 이미지url 을 표현하는 변수다
    String image_url;

    // 팀원의 커피메뉴를 표현하기위한 변수다
    String coffee_menu;

    // 팀원의 커피 추가 옵션을 표현한기위한 변수다
    String coffee_option;

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getCoffee_menu() {
        return coffee_menu;
    }

    public void setCoffee_menu(String coffee_menu) {
        this.coffee_menu = coffee_menu;
    }

    public String getCoffee_option() {
        return coffee_option;
    }

    public void setCoffee_option(String coffee_option) {
        this.coffee_option = coffee_option;
    }

    public MyteamMember(String team_name, String image_url, String coffee_menu, String coffee_option) {
        this.team_name = team_name;
        this.image_url = image_url;
        this.coffee_menu = coffee_menu;
        this.coffee_option = coffee_option;
    }
}
