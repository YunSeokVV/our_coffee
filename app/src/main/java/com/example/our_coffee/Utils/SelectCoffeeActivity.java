package com.example.our_coffee.Utils;
// 자신이 자주 먹기위한 커피를 고르기 위해 카테고리를 선택하는 화면이다.
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Window;

import com.example.our_coffee.R;

public class SelectCoffeeActivity extends AppCompatActivity {

    public static String TAG = "SelectCoffeeActivity";
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coffee);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle("커피 설정");

    }
}