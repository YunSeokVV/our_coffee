package com.example.our_coffee.Utils;
// 자신에게 온 초대 알림을 리사이클러뷰로 표현해 주기 위한 클래스다.
public class MyNotification {
    // 자신의 팀을 표현하는 변수다
    String team_name;

    // 팀의 이미지 url 을 표현하는 변수다
    String image_url;

    // 초대자를 표현하기위한 변수다
    String inviter;

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

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public MyNotification(String team_name, String image_url, String inviter) {
        this.team_name = team_name;
        this.image_url = image_url;
        this.inviter = inviter;
    }
}
