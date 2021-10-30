package com.example.our_coffee;
// 자신이 자주 먹기위한 커피를 고르기 위해 카테고리를 선택하는 화면이다.
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.example.our_coffee.Add_new_member;
import com.example.our_coffee.R;
import com.example.our_coffee.Utils.CoffeeCategory;
import com.example.our_coffee.Utils.CoffeeCategoryAdapter;
import com.example.our_coffee.Utils.MyTeamMemberAdapter;
import com.example.our_coffee.Utils.MyteamMember;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;

import java.util.ArrayList;

public class SelectCategoryActivity extends AppCompatActivity {

    public static String TAG = "SelectCoffeeActivity";
    Toolbar toolbar;

    //리사이클러뷰 관련 코드
    // 카테고리를 리사이클러뷰로 표현하기 위한 코드
    ArrayList<CoffeeCategory> coffeeCategory;
    CoffeeCategoryAdapter coffeeCategoryAdapter;

    // 음료의 카테고리를 표현하는 리사이클러뷰
    RecyclerView coffee_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coffee);

        Log.v(TAG,"onCreate");

        coffee_category=(RecyclerView)findViewById(R.id.coffee_category);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle("커피 설정");

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        coffee_category.setLayoutManager(mLinearLayoutManager);
        coffeeCategory=new ArrayList<>();

        coffeeCategoryAdapter = new CoffeeCategoryAdapter(coffeeCategory,getApplicationContext());
        coffee_category.setAdapter(coffeeCategoryAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(90);
        coffee_category.addItemDecoration(decoration_height);

        CoffeeCategory data;
        data = new CoffeeCategory("커피",R.drawable.coffee);
        coffeeCategory.add(data);
        data = new CoffeeCategory("논커피",R.drawable.non_coffee);
        coffeeCategory.add(data);
        data = new CoffeeCategory("스무디&프라페",R.drawable.smoothe);
        coffeeCategory.add(data);
        data = new CoffeeCategory("에이드&주스",R.drawable.ade);
        coffeeCategory.add(data);
        data = new CoffeeCategory("티",R.drawable.tea);
        coffeeCategory.add(data);
        coffeeCategoryAdapter.notifyDataSetChanged();

        coffeeCategoryAdapter.setOnItemClickListener(new CoffeeCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CoffeeCategoryAdapter.CustomViewHolder_CoffeeCategoryAdapter holder, View view, int position) {
                Log.v(TAG,"아이템을 클릭함 "+position);
                Log.v(TAG,coffeeCategory.get(position).getCategory_name());

                Intent intent = new Intent(getApplicationContext(), ConfigureCoffeeActivity.class);
                intent.putExtra("category", coffeeCategory.get(position).getCategory_name());
                startActivity(intent);
            }
        });



    }       //onCreate end



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