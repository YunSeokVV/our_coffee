package com.example.our_coffee;
//팀원들 목록을 보여주는 화면이다.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.our_coffee.Utils.GatherTeam;
import com.example.our_coffee.Utils.GatherTeamAdapter;
import com.example.our_coffee.Utils.MyNotificationAdapter;
import com.example.our_coffee.Utils.MyTeamAdapter;
import com.example.our_coffee.Utils.MyTeamMemberAdapter;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.MyteamMember;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Team_member extends AppCompatActivity {

    //리사이클러뷰 관련 코드
    //ArrayList<MyteamMember> myteamMemberArrayList;
    // 팀원들의 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
    ArrayList<MyteamMember> team_memberArrayList;
    MyTeamMemberAdapter myTeamMemberAdapter;

    public static String TAG = "Team_member";
    Toolbar toolbar;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //팀원들의 목록을 보여주기 위한 리사이클러뷰다.
    RecyclerView my_team_member;

    // 팀원들이 원하는 음료를 정리해서 볼 수 있다.
    Button show_organized;

    // DB에서 필요한 데이터를 다운받기 전까지 화면에 다운중이라는 사실을 알려주기 위환 dialog 다.
    ProgressDialog dialog;

    // 사용자들의 음료를 모아봐서 보기 편하게 해줄 떄 사용하는 다이얼로그 창이다.
    Dialog gather_drink;

    FirebaseFirestore db;

    //DB에서 갖고온 팀원들의 이메일을 담는다
    List<String> team_member_email;

    //DB에서 갖고온 팀원들의 커피 메뉴를 담는다
    List<String> team_member_coffee = new ArrayList<String>();;

    //DB에서 갖고온 팀원들의 커피 옵션을 담는다
    List<String> member_coffee_option = new ArrayList<String>();;

    //DB에서 갖고온 팀원들의 닉네임을 담는다
    List<String> member_nick_name = new ArrayList<String>();;

    //DB에서 갖고온 커피메뉴 데이터를 담는다
    List<String> coffee_menu_list=new ArrayList<>();

    //각 커피의 종류별로 몇개나 사람들이 원하는지 표현해주는 리스트다.
    List<Integer> coffee_number = new ArrayList<Integer>();


    // 현재 화면에서 표현하고 있는 팀의 고유 pid 값이다
    String team_pid;

    // 현재 화면에서 표현하고 있는 팀의 이름이다.
    String team_name;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member);
        Log.v(TAG,"onCreate");

        my_team_member=findViewById(R.id.my_team_member);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        show_organized=(Button)findViewById(R.id.button);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_member_list);

        //사용자의 팀원 목록을 표현해주기 위한 리사이클러뷰다
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        my_team_member.setLayoutManager(mLinearLayoutManager);
        team_memberArrayList=new ArrayList<>();

        myTeamMemberAdapter = new MyTeamMemberAdapter(team_memberArrayList,getApplicationContext());
        my_team_member.setAdapter(myTeamMemberAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_team_member.addItemDecoration(decoration_height);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        setSupportActionBar(toolbar);

        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 다이얼로그에 대한 설정을 해준다.
        gather_drink=new Dialog(Team_member.this);
        gather_drink.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gather_drink.setContentView(R.layout.gather_menu_dialog);

        Intent intent = getIntent(); /*데이터 수신*/
        team_pid = intent.getExtras().getString("team_pid"); /*String형*/
        team_name = intent.getExtras().getString("team_name");

        Log.v(TAG,"team_pid "+team_pid);
        Log.v(TAG,"team_name "+team_name);

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle(team_name);

        // 팀안의 모든 유저들의 이메일 정보를 DB에서 불러온다.
        db = FirebaseFirestore.getInstance();

        Download_dialog("데이터 확인중");
        LoadUsersData();

        show_organized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG,"버튼 클릭함");
                ShowGatherDrinkDialog(coffee_number,coffee_menu_list);
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                team_member_email.clear();
                team_member_coffee.clear();
                member_coffee_option.clear();
                member_nick_name.clear();
                coffee_menu_list.clear();
                coffee_number.clear();
                team_memberArrayList.clear();
                myTeamMemberAdapter.notifyDataSetChanged();
                LoadUsersData();

            }
        });

    }       //onCreate end

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_member_tool_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home/*지정한 id*/){
            finish();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.add_new_member:
                Intent intent = new Intent(getApplicationContext(), Add_new_member.class);
                intent.putExtra("invite_team_pid",team_pid);
                intent.putExtra("invite_team_name",team_name);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 팀에 있는 팀원들을 리사이클러뷰로 표현하기 위한 데이터들을 DB에서 불러온다.
    public void LoadUsersData(){

        // DB에서 같은 팀원들의 데이터를 마지막으로 받아 왔다는 사실을 알려주는 인덱스다.
        final int[] last = {0};

        DocumentReference docRef = db.collection("team3").document(team_pid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        List list = (List) document.getData().get("team_member_name");
                        Log.v(TAG,"데이터 확인 좀 하자 :"+list.size());

                        team_member_email = (List) document.getData().get("team_member_name");
                        for(int i=0;i<team_member_email.size();i++){

                            Log.i("TEST", "data["+i+"] > " + team_member_email.get(i).toString());

                        }

                        for(int i=0;i<team_member_email.size();i++){
                            Log.v(TAG,"데이터가 추가됨");
                            MyteamMember data;
                            data = new MyteamMember("work","work","work","work");
                            team_memberArrayList.add(i,data);
                            myTeamMemberAdapter.notifyDataSetChanged();
                        }



                        // 팀원들의 닉네임, 자주먹는 커피, 커피 옵션 값들을 DB에서 갖고온다.
                        for(int i=0;i<team_member_email.size();i++){
                            DocumentReference docRef = db.collection("users3").document(team_member_email.get(i));
                            int finalI = i;
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();

                                        // 현재 팀에 사람들이 있는 경우
                                        if (document.exists()) {
                                            Log.v(TAG,"팀원들 정보 확인");
                                            Log.v(TAG, String.valueOf(document.get("my_coffee")));
                                            Log.v(TAG, String.valueOf(document.get("my_coffee_option")));
                                            Log.v(TAG, String.valueOf(document.get("nick_name")));
                                            Log.v(TAG, String.valueOf(team_member_email.get(finalI)));


                                            team_member_coffee.add(String.valueOf(document.get("my_coffee")));
                                            member_coffee_option.add(String.valueOf(document.get("my_coffee_option")));
                                            member_nick_name.add(String.valueOf(document.get("nick_name")));

                                            MyteamMember data;
                                            data = new MyteamMember(String.valueOf(document.get("nick_name")),"work",String.valueOf("원하는 음료 : "+document.get("my_coffee")),String.valueOf(document.get("my_coffee_option")));
                                            team_memberArrayList.set(finalI,data);

                                            //DB 에서 데이터를 마지막으로 받아온 상황이다.
                                            if(last[0] ==team_member_email.size()-1){
                                                Log.v(TAG,"마지막으로 데이터를 받아옴");

                                                for(int i=0;i<team_member_coffee.size();i++){
                                                    Log.v(TAG,team_member_coffee.get(i));
                                                    Log.v(TAG,member_coffee_option.get(i));
                                                    Log.v(TAG,member_nick_name.get(i));
                                                }


                                                ShowMemberRecyclerView();
                                                last[0] =0;
                                            }
                                            last[0]++;

                                        }

                                        // 현재 팀에 사람들이 없는 경우 (있을 수 없음)
                                        else {
                                            Log.d(TAG, "No such document");
                                        }
                                    }
                                    // DB를 제대로 불러오지 못함
                                    else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        }

                        //myTeamMemberAdapter.notifyDataSetChanged();


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    // 팀원들의 데이터를 정리된 형태로 리사이클러뷰에 보여준다.
    public void ShowMemberRecyclerView(){
        Log.v(TAG,"ShowMemberRecyclerView");

        //현재 팀의 팀원들을 리사이클러뷰로 표현해주는 코드다.
        for(int i=0;i<team_member_coffee.size();i++){
            Log.v(TAG,"반복문이 실행 되었습니다. "+i);
            Log.v(TAG,team_member_email.get(i));

            int finalI = i;
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            storageRef.child("user_profile/"+team_member_email.get(i)+"_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.v(TAG,"team_member_email.get(i) "+team_member_email.get(finalI));
                    Log.v(TAG,"데이터 받아오기 성공 "+finalI);
                    Log.v(TAG,"member_nick_name "+member_nick_name.get(finalI));
                    Log.v(TAG,"uri.toString "+uri.toString());
                    Log.v(TAG,"음료 "+team_member_coffee.get(finalI));
                    Log.v(TAG,"옵션 "+member_coffee_option.get(finalI));

                    team_memberArrayList.get(finalI).setImage_url(uri.toString());
                    myTeamMemberAdapter.notifyDataSetChanged();

                    // 마지막으로 DB에서 데이터를 받아왔을 때 실행되는 조건문.
                    if(finalI==team_member_coffee.size()-1){
                        Log.v(TAG,"마지막 반복문이 실행되었습니다.");
                        //Collections.reverse(myteamArrayList);
                        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
                        myTeamMemberAdapter.notifyDataSetChanged();

                        LoadCoffeeMenu();
                    }

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //이미지를 storage 에서 불러오는데 실패한 경우
                    Log.v(TAG,"excetion log "+exception);

                }
            });
        }
    }

    // DB에 저장되어 있는 커피 메뉴를 불러오는 메소드다. 팀원들이 어떤 음료 메뉴를 몇개나 선택했는지 확인헐 때 사용한다.
    public void LoadCoffeeMenu(){
        Log.v(TAG,"LoadCoffeeMenu");
        db.collection("coffee_menu3").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.v(TAG, document.getId() + " => " + document.getData());
                                Log.v(TAG, document.getId() + " => " + document.get("menu_name"));
                                coffee_menu_list.add(String.valueOf(document.get("menu_name")));
                            }

                            CountMenu();
                            dialog.dismiss();

                        }

                        else {
                            Log.v(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // 팀원들이 어떤 음료를 몇개나 원하는지 세는 역할을 하는 메소드다. '우리팀 음료 모아 보기' 기능을 통해서 구매 해야하는 커피들의 개수를 보기 편하게 Dialog로 표현한다.
    public void CountMenu(){
        Log.v(TAG,"CountMenu");

        for(int i=0;i<coffee_menu_list.size();i++){
            coffee_number.add(i,Collections.frequency(team_member_coffee, coffee_menu_list.get(i)));
        }

        // 사람들이 고른 음료들의 개수를 로그로 확인
        for(int i=0;i<coffee_number.size();i++){
            Log.v(TAG,"coffee_number "+coffee_number.get(i));
            Log.v(TAG,"coffee_menu_list "+coffee_menu_list.get(i));
        }
        swipeRefreshLayout.setRefreshing(false);
        myTeamMemberAdapter.notifyDataSetChanged();
    }

    // 화면에 필요한 정보들을 보여주기 위해서 DB에서 데이터를 받아올 때 까지 로딩중이라는 사실을 알려주는 다이얼로그 메소드다.
    public void Download_dialog(String showing_text){
        dialog = new ProgressDialog(Team_member.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(showing_text);
        dialog.setCancelable(false);
        dialog.show();
    }

    // 팀의 음료를 모아서 보여주기 위한 다이얼로그를 실행한다.
    public void ShowGatherDrinkDialog(List<Integer> coffee_number,List<String> coffee_menu_list){
        gather_drink.show();

        //리사이클러뷰 관련 코드
        ArrayList<GatherTeam> gatherTeamArrayList=new ArrayList<>();
        GatherTeamAdapter gatherTeamAdapter;
        gatherTeamAdapter = new GatherTeamAdapter(gatherTeamArrayList,getApplicationContext());

        RecyclerView item=gather_drink.findViewById(R.id.recyclerview);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        item.setLayoutManager(mLinearLayoutManager);
        item.setAdapter(gatherTeamAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        item.addItemDecoration(decoration_height);

        GatherTeam data;

        for(int i=0;i<coffee_number.size();i++){
            data=new GatherTeam(coffee_menu_list.get(i)+" : "+coffee_number.get(i)+"명");

            // 1명 이상 선택한 음료의 경우만 보여준다.
            if(coffee_number.get(i)!=0){
                gatherTeamArrayList.add(0,data);
            }
        }
        gatherTeamAdapter.notifyDataSetChanged();

    }

}
