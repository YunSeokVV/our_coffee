package com.example.our_coffee.Utils;

import android.os.Parcel;
import android.os.Parcelable;

// 자신에게 온 초대 알림을 리사이클러뷰로 표현해 주기 위한 클래스다.
public class MyNotification {
    // 자신의 팀을 표현하는 변수다
    String team_name;

    // 팀의 이미지 url 을 표현하는 변수다
    String image_url;

    // 초대자를 표현하기위한 변수다
    String inviter;

    // 초대한 팀의 pid 값을 담는 변수다
    String team_pid;

    public MyNotification(Parcel in) {
        team_name=in.readString();
        image_url=in.readString();
        inviter=in.readString();
        team_pid=in.readString();
    }

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

    public String getTeam_pid() {
        return team_pid;
    }

    public void setTeam_pid(String team_pid) {
        this.team_pid = team_pid;
    }

    public MyNotification(String team_name, String image_url, String inviter,String team_pid) {
        this.team_name = team_name;
        this.image_url = image_url;
        this.inviter = inviter;
        this.team_pid = team_pid;
    }

    //todo : 얜 뭐지?? 리사이클러뷰의 아이템을 객체 형태로 받기위해 추가한 코드인데 잘 몰라도 원하는 상황이 구현되서 분석은 안함.
    public static final Parcelable.Creator<MyNotification> CREATOR = new Parcelable.Creator<MyNotification>() {
        @Override
        public MyNotification createFromParcel(Parcel in) {
            return new MyNotification(in);
        }

        @Override
        public MyNotification[] newArray(int size) {
            return new MyNotification[size];
        }
    };

}
