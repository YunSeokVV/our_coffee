package com.example.our_coffee;
//팀원들 목록을 보여주는 화면이다.
import androidx.annotation.NonNull;
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
import android.view.Window;

import com.example.our_coffee.Utils.MyTeamMemberAdapter;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.MyteamMember;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Team_member extends AppCompatActivity {

    //리사이클러뷰 관련 코드
    private ArrayList<MyteamMember> myteamMemberArrayList;
    private MyTeamMemberAdapter myTeamMemberAdapter;

    public static String TAG = "Team_member";
    Toolbar toolbar;

    // DB에서 같은 팀원들의 데이터를 마지막으로 받아 왔다는 사실을 알려주는 인덱스다.
    int last_receive=0;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //팀원들의 목록을 보여주기 위한 리사이클러뷰다.
    RecyclerView my_team_member;

    FirebaseFirestore db;

    // 팀원들의 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
    ArrayList<MyteamMember> team_memberArrayList;

    //DB에서 갖고온 팀원들의 이메일을 담는다
    List<String> team_member_email;

    //DB에서 갖고온 팀원들의 커피 메뉴를 담는다
    List<String> team_member_coffee = new ArrayList<String>();;

    //DB에서 갖고온 팀원들의 커피 옵션을 담는다
    List<String> member_coffee_option = new ArrayList<String>();;

    //DB에서 갖고온 팀원들의 닉네임을 담는다
    List<String> member_nick_name = new ArrayList<String>();;


    // 현재 화면에서 표현하고 있는 팀의 고유 pid 값이다
    String team_pid;

    // 현재 화면에서 표현하고 있는 팀의 이름이다.
    String team_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member);
        Log.v(TAG,"onCreate");

        team_memberArrayList=new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        my_team_member = (RecyclerView) findViewById(R.id.my_team_member);

        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent(); /*데이터 수신*/
        team_pid = intent.getExtras().getString("team_pid"); /*String형*/
        team_name = intent.getExtras().getString("team_name");

        Log.v(TAG,"team_pid "+team_pid);
        Log.v(TAG,"team_name "+team_name);

        //팀명을 툴바 제목으로 설정한다.
        toolbar.setTitle(team_name);

        // 팀안의 모든 유저들의 이메일 정보를 DB에서 불러온다.
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("team3").document(team_pid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());


                        team_member_email = (List) document.getData().get("team_member_name");
                        for(int i=0;i<team_member_email.size();i++){

                            Log.i("TEST", "data["+i+"] > " + team_member_email.get(i).toString());

                        }

                        for(int i=0;i<team_member_email.size();i++){
                            MyteamMember data;
                            data = new MyteamMember("work","work","work","work");
                            team_memberArrayList.add(i,data);
                        }

                        // 팀원들의 닉네임, 자주먹는 커피, 커피 옵션 값들을 DB에서 갖고온다.
                        for(int i=0;i<team_member_email.size();i++){
                            DocumentReference docRef = db.collection("users3").document(team_member_email.get(i));
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

                                            team_member_coffee.add(String.valueOf(document.get("my_coffee")));
                                            member_coffee_option.add(String.valueOf(document.get("my_coffee_option")));
                                            member_nick_name.add(String.valueOf(document.get("nick_name")));

                                            //DB 에서 데이터를 마지막으로 받아온 상황이다.
                                            if(last_receive==team_member_email.size()-1){
                                                Log.v(TAG,"마지막으로 데이터를 받아옴");

                                                for(int i=0;i<team_member_coffee.size();i++){
                                                    Log.v(TAG,team_member_coffee.get(i));
                                                    Log.v(TAG,member_coffee_option.get(i));
                                                    Log.v(TAG,member_nick_name.get(i));
                                                }

                                                last_receive=0;
                                            }
                                            last_receive++;

                                            //todo : 일단 팀원들을 리사이클러뷰로 표현하는 것은 후순위로 미룬다. 우선 팀원들을 초대하는 기능부터 구현하겠다.

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


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //사용자의 팀 목록을 표현해주기 위한 리사이클러뷰다
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        my_team_member.setLayoutManager(mLinearLayoutManager);
        myteamMemberArrayList = new ArrayList<>();
        myTeamMemberAdapter = new MyTeamMemberAdapter(myteamMemberArrayList, getApplicationContext());
        my_team_member.setAdapter(myTeamMemberAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_team_member.addItemDecoration(decoration_height);

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



}
