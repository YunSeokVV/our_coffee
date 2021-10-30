package com.example.our_coffee;
// 커피의 상세 옵션을 설정하는 액티비티다.
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class CoffeeDetailOptionActivity extends AppCompatActivity {

    public static String TAG = "CoffeeDetailOptionActivity";
    Toolbar toolbar;

    AppCompatButton hot_btn;
    AppCompatButton ice_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_detail_option);


        hot_btn=(AppCompatButton)findViewById(R.id.hot_btn);
        ice_btn=(AppCompatButton)findViewById(R.id.ice_btn);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle("커피 설정");

        hot_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                ChooseHot();
            }
        });

        ice_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                ChooseIced();
            }
        });

    }



    private void ChooseHot(){
        hot_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.hot_button_radius));
        hot_btn.setTextColor(getResources().getColor(R.color.white));

        ice_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.iced_button_white));
        ice_btn.setTextColor(getResources().getColor(R.color.black));

    }

    private void ChooseIced(){
        hot_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.hot_button_white));
        hot_btn.setTextColor(getResources().getColor(R.color.black));

        ice_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.iced_button_radius));
        ice_btn.setTextColor(getResources().getColor(R.color.white));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home/*지정한 id*/){
            finish();
            return true;
        }
        else{
            return true;
        }

    }

}