package com.example.our_coffee.Utils;

// 우리팀의 음료를 모아 볼 때 리사이클러뷰의 아이템을 표현하는 클래스다.
public class GatherTeam {

    // 이 변수에서 음료의 이름과 해당 음료를 선택한 사람의 인원수 까지 포함 시킨다.
    String menu;

    public GatherTeam(String menu) {
        this.menu = menu;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }
}
