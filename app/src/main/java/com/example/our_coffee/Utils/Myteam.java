package com.example.our_coffee.Utils;

import android.os.Parcel;
import android.os.Parcelable;

// 자신의 팀목록을 리사이클러뷰로 표현해주기위한 클래스다
public class Myteam {
    // 자신의 팀을 표현하는 변수다
    String team_name;

    // 팀의 이미지 url 을 표현하는 변수다
    String image_url;

    // 팀의 pid 값을 표현하는 변수다.
    String team_pid;

    public Myteam(Parcel in) {
        team_name=in.readString();
        image_url=in.readString();
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

    public String getTeam_pid() {
        return team_pid;
    }

    public void setTeam_pid(String team_pid) {
        this.team_pid = team_pid;
    }


    public Myteam(String team_name, String image_url, String team_pid) {
        this.team_name = team_name;
        this.image_url = image_url;
        this.team_pid = team_pid;
    }

    //todo : 얜 뭐지?? 리사이클러뷰의 아이템을 객체 형태로 받기위해 추가한 코드인데 잘 몰라도 원하는 상황이 구현되서 분석은 안함.
    public static final Parcelable.Creator<Myteam> CREATOR = new Parcelable.Creator<Myteam>() {
        @Override
        public Myteam createFromParcel(Parcel in) {
            return new Myteam(in);
        }

        @Override
        public Myteam[] newArray(int size) {
            return new Myteam[size];
        }
    };

}
