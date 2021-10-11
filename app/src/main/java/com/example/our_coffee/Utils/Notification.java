package com.example.our_coffee.Utils;

// 사용자의 팀 목록을 표현하는데 사용되는 리사이클러뷰 아이템값들을 객체 형태로 사용하기위해 존재하는 파일이다.
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Notification implements Parcelable {
    ArrayList<MyNotification> myNotifications;
    public Notification(){ }

    protected Notification(Parcel in) {
        myNotifications = new ArrayList<>();
        in.readTypedList(myNotifications,MyNotification.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };

    public ArrayList<MyNotification> getMyNotifications() {
        return myNotifications;
    }

    public void setMyNotifications(ArrayList<MyNotification> myNotifications) {
        this.myNotifications = myNotifications;
    }
}


