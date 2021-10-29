package com.example.our_coffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.example.our_coffee.Utils.Coffee;
import com.example.our_coffee.Utils.CoffeeAdapter;
import com.example.our_coffee.Utils.CoffeeCategory;
import com.example.our_coffee.Utils.CoffeeCategoryAdapter;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ConfigureCoffeeActivity extends AppCompatActivity {

    public static String TAG = "ConfigureCoffeeActivity";
    Toolbar toolbar;
    //사용자가 이전 화면에서 고른 마시고 싶은 음료 카테고리다.
    String category;

    //리사이클러뷰 관련 코드
    // 카테고리를 리사이클러뷰로 표현하기 위한 코드
    ArrayList<Coffee> coffee;
    CoffeeAdapter coffeeAdapter;

    // 음료의 카테고리를 표현하는 리사이클러뷰
    RecyclerView coffee_list;

    FirebaseFirestore db;

    // DB에 있는 음료들의 이름을 담는 리스트다.
    ArrayList<String> drinks_name = new ArrayList();

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // DB에서 필요한 데이터를 다운받기 전까지 화면에 다운중이라는 사실을 알려주기 위환 dialog 다.
    ProgressDialog dialog;

    // 새로고침을 안하면 이미지들이 공백으로 나온다. 그래서 강제로 새로고침 시켰다.
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_coffee);
        coffee_list=(RecyclerView)findViewById(R.id.coffee_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_drinks);
        setSupportActionBar(toolbar);
        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        category=intent.getExtras().getString("category");

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle(category);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        coffee_list.setLayoutManager(mLinearLayoutManager);
        coffee=new ArrayList<>();

        coffeeAdapter = new CoffeeAdapter(coffee,getApplicationContext());
        coffee_list.setAdapter(coffeeAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(90);
        coffee_list.addItemDecoration(decoration_height);

//        String loading="https://firebasestorage.googleapis.com/v0/b/matchingapp-tinder.appspot.com/o/coffee_profile%2F%ED%94%8C%EB%A0%88%EC%9D%B8%EC%9A%94%EA%B1%B0%ED%8A%B8%EC%8A%A4%EB%AC%B4%EB%94%94.jpg?alt=media&token=bb1a82d9-68e3-40dd-a2ae-de9b0ed52685";
//        Coffee data;
//        data = new Coffee(String.valueOf("wow"),loading);
//        for(int i=0;i<30;i++){
//            coffee.add(data);
//        }
//        coffeeAdapter.notifyDataSetChanged();


        Download_dialog("데이터 로드중");
        LoadDrinkMenu();


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);

            }
        });


    }       //onCreate end

    public void LoadDrinkMenu(){
        db = FirebaseFirestore.getInstance();
        StorageReference storageRef = storage.getReference();

        db.collection("coffee_menu3").whereEqualTo("drink_type", category).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.v(TAG,"Check");
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.v(TAG, document.getId() + " => " + document.getData());
                                Log.v(TAG, "음료 "+document.get("menu_name"));
                                drinks_name.add(String.valueOf(document.get("menu_name")));
                                Coffee data;
                                data = new Coffee(String.valueOf(document.get("menu_name")),"loading");
                                coffee.add(data);
                            }

                            final int[] last = {0};
                            for(int i=0;i<drinks_name.size();i++){
                                int finalI = i;
                                storageRef.child("coffee_profile/"+drinks_name.get(i)+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Coffee data;
                                        Log.v(TAG,"음료 명 "+drinks_name.get(finalI));
                                        Log.v(TAG,"uri "+uri.toString());
                                        data = new Coffee(String.valueOf(drinks_name.get(finalI)),uri.toString());
                                        coffee.set(finalI,data);
                                        coffeeAdapter.notifyDataSetChanged();

                                        if(last[0]==drinks_name.size()-1){
                                            Log.v(TAG,"마지막으로 데이터 받아옴");

                                            coffeeAdapter.notifyDataSetChanged();
                                            dialog.dismiss();



                                        }

                                        last[0]++;
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //이미지를 storage 에서 불러오는데 실패한 경우
                                        Log.v(TAG,"excetion!! "+exception);
                                    }
                                });
                            }

                        } else {
                            Log.v(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // 화면에 필요한 정보들을 보여주기 위해서 DB에서 데이터를 받아올 때 까지 로딩중이라는 사실을 알려주는 다이얼로그 메소드다.
    public void Download_dialog(String showing_text){
        dialog = new ProgressDialog(ConfigureCoffeeActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(showing_text);
        dialog.setCancelable(false);
        dialog.show();
    }

}