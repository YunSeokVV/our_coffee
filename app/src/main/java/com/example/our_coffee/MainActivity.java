package com.example.our_coffee;
//이 파일은 사용자가 로그인한 뒤 사용자의 팀 목록, 마이 페이지, 알림 화면 Fragment 를 담는 액티비티다
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.example.our_coffee.Utils.Myteam;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    FirebaseFirestore db;

    // 프래그먼트의 트랜젝션을 수행을 수행하기 위해서는 FragmentTrasaction에서 가져온 API.
    private FragmentManager fragmentManager;
    private MyTeamFragement fragment_myteam;
    private NotificationFragment fragment_notification;
    private MyPageFragment fragment_mypage;

    //앞으로 참조 객체가 될 친구다.
    private FragmentTransaction transaction;
    Toolbar toolbar;

    // 초대해준 팀의 pid 값을 담는 list 다.
    ArrayList<String> inviteteam_pid_list = new ArrayList<String>();

    // 초대해준 사람의 이메일을 담는 변수다.
    ArrayList<String> inviter_list = new ArrayList<String>();

    // 초대해준 사람의 닉네임을 담는 변수다.
    ArrayList<String> inviter_nickname_list = new ArrayList<String>();

    String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("나의 팀 목록");
        toolbar.setTitleTextColor(Color.BLACK);

        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        //참조 객체를 얻기 위해서는 getFragmentManager() 함수 호출
        fragmentManager = getSupportFragmentManager();

        fragment_myteam = new MyTeamFragement();
        fragment_notification = new NotificationFragment();
        fragment_mypage = new MyPageFragment();

        //FragmentTrasaction 참조 객체를 얻기 위해서는 FragmentManager의 beginTransaction() 함수를 사용.
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();

        db = FirebaseFirestore.getInstance();

        GetNotificationData();



    }

    public void clickHandler(View view)
    {
        Log.i("코드의 흐름","clickHandler 메소드 호출");
        transaction = fragmentManager.beginTransaction();

        switch(view.getId())
        {
            case R.id.btn_fragment_team_list:
                toolbar.setTitle("나의 팀 목록");
                //commitAllowingStateLoss : Like {@link #commit} but allows the commit to be executed after an activity's state is saved.
                transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                break;
            case R.id.btn_fragment_mypage:
                toolbar.setTitle("마이페이지");
                transaction.replace(R.id.frameLayout, fragment_mypage).commitAllowingStateLoss();
                break;
            case R.id.btn_fragment_notify:
                toolbar.setTitle("알림");
                transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
                break;
        }
    }

    // 알림화면에서 리사이클러뷰를(누가 사용자를 초대해줬는지) 표현하기위해 필요한 데이터들을 이 메소드에서 정리한다.
    public void GetNotificationData(){
        DocumentReference docRef = db.collection("users3").document(currentUser.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List list = (List) document.getData().get("invited_team");

                        //아래 반복문은 invited_team 데이터를 DB로 부터 받아 왔을 때 문자열을 분리해주는 반복문이다. '_' 를 기준으로 나눠서 리스트에 담는다.
                        for(int i=0;i<list.size();i++){
                            int idx = list.get(i).toString().indexOf("_");
                            String team_pid = list.get(i).toString().substring(0, idx);
                            String user_email = list.get(i).toString().substring(idx+1);
                            inviteteam_pid_list.add(team_pid);
                            inviter_list.add(user_email);
                        }

                        for(int i=0;i<inviter_list.size();i++){
                            DocumentReference docRef = db.collection("users3").document(inviter_list.get(i));
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            inviter_nickname_list.add(String.valueOf(document.get("nick_name")));
                                        }
                                    }
                                }       //onComplete



                            });
                        }


                    }
                }
            }       //onComplete
        });
    }



}