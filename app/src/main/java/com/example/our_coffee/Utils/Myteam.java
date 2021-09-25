package com.example.our_coffee.Utils;
// 자신의 팀목록을 리사이클러뷰로 표현해주기위한 클래스다
public class Myteam {
    // 자신의 팀을 표현하는 변수다
    String team_name;

    // 팀의 이미지 url 을 표현하는 변수다
    String image_url;

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

    public Myteam(String team_name, String image_url) {
        this.team_name = team_name;
        this.image_url = image_url;
    }
}
