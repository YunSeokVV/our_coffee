package com.example.our_coffee;
//이 파일은 사용자가 로그인한 뒤 사용자의 팀 목록, 마이 페이지, 알림 화면 Fragment 를 담는 액티비티다
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;


import com.bumptech.glide.Glide;
import com.example.our_coffee.Utils.MyNotification;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.Notification;
import com.example.our_coffee.Utils.Team;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements NotificationFragment.InvitationAcceptedListener,NotificationFragment.NotificationRefreshListener,MyPageFragment.UserProfileChanged,MyTeamFragement.MyTeamRefresh,NotificationFragment.InvitationDeniedListener{




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

    // 나의 팀 목록을 표현하는 리사이클러뷰에 담기는 리스트를 담는 객체다.
    Team team=new Team();

    // 나의 알람 목록을 표현하는 리사이클러뷰에 담기는 리스트를 담는 객체다.
    Notification notification=new Notification();

    String TAG="MainActivity";

    // DB에서 필요한 데이터를 다운받기 전까지 화면에 다운중이라는 사실을 알려주기 위환 dialog 다.
    ProgressDialog dialog;

    BottomNavigationView bottomNavigationView;

    Bundle bundle;

    // DB에서 데이터를 처음 불러올 때 사용자에게 화면을 보이게 하기 위해서 프레그먼트를 딱 한번 commit 해줘야 한다. 이 값이 변경되면 다시 실행되지 않는다.
    String myteam_commit="yes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("나의 팀 목록");
        toolbar.setTitleTextColor(Color.BLACK);

        bottomNavigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        bundle=new Bundle();

        //참조 객체를 얻기 위해서는 getFragmentManager() 함수 호출
        fragmentManager = getSupportFragmentManager();

        fragment_myteam = new MyTeamFragement();
        fragment_mypage = new MyPageFragment();
        fragment_notification = new NotificationFragment();






        //FragmentTrasaction 참조 객체를 얻기 위해서는 FragmentManager의 beginTransaction() 함수를 사용.
//        transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();

//        transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.frameLayout, fragment_mypage).commitAllowingStateLoss();
//
//        transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();


        Download_dialog("데이터 확인중");

        GetMyPageData();


        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction= fragmentManager.beginTransaction();;
                        switch (item.getItemId()) {
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
                        return true;
                    }
                });


    }




    // 알림화면에서 리사이클러뷰를(누가 사용자를 초대해줬는지) 표현하기위해 필요한 데이터들을 이 메소드에서 정리한다.
    //앱이 가장 먼저 실행됐을때 호출된다. 사용자가 팀 초대 수락을 받아서 팀이 추가될 때는 이 메소드가 호출되지 않는다.
    public void GetNotificationData(){
        Log.v(TAG,"GetNotificationData called");
        //Notification notification=new Notification();

        //DB에서 갖고온 사용자의 pid 값을 담는다. 프로필 사진을 갖고올 때 사용된다.
        ArrayList<String> team_list_pid = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        //DB에서 갖고온 사용자의 이메일을 담는다
        ArrayList<String> team_list_Email = new ArrayList<String>();

        // 나의 알람 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<MyNotification> myNotificationArrayList = new ArrayList<>();;

        DocumentReference docRef = db.collection("users3").document(currentUser.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //만약 초대를 아직 아무에게도 받지 않은 경우다.
                        if(String.valueOf(document.get("invited_team")).equals("[]")){
                            Log.v(TAG,"아무에게도 초대 안받음");

                            bundle.putString("notification_exist","no");
                            fragment_notification.setArguments(bundle);
                            GetMyTeamFragent();
                        }

                        //사용자가 초대를 받은 적이 있는 경우
                        else{
                            System.out.println("초대 받음");

                            List list = (List) document.getData().get("invited_team");

                            //아래 반복문은 invited_team 데이터를 DB로 부터 받아 왔을 때 문자열을 분리해주는 반복문이다. '_' 를 기준으로 나눠서 리스트에 담는다.
                            for(int i=0;i<list.size();i++){
                                String[] array = list.get(i).toString().split("_");
                                System.out.println("i의 값 : "+i);
                                for(int j=0;j<array.length;j++){
                                    System.out.println(array[j]);
                                    // 팀의 pid 값을 리스트에 저장.
                                    if(j==0){
                                        team_list_pid.add(array[j]);
                                    }
                                    else if(j==1){
                                        team_list_name.add(array[j]);
                                    }
                                    else if(j==2){
                                        team_list_Email.add(array[j]);
                                    }
                                }
                            }

                            //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                            final int[] last_idx = {0};

                            for(int i=0;i<list.size();i++){
                                MyNotification data;
                                data = new MyNotification("test","test","test","test");
                                myNotificationArrayList.add(i,data);
                            }

                            // 사용자의 알림 목록을 리사이클러뷰로 표현해주는 코드
                            for(int i=0;i<list.size();i++){
                                int finalI = i;

                                String tmp=team_list_name.get(i);
                                StorageReference storageRef = storage.getReference();
                                storageRef.child("team_profile/"+team_list_pid.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        MyNotification data2;

                                        data2 = new MyNotification(tmp,uri.toString(),team_list_Email.get(finalI),team_list_pid.get(finalI));

                                        myNotificationArrayList.set(finalI,data2);
                                        notification.setMyNotifications(myNotificationArrayList);

                                        if(last_idx[0]==list.size()-1){

                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_notification,"NoticifationFragemnt").commitAllowingStateLoss();
                                            bundle.putParcelable("my_notification_list",notification);
                                            bundle.putString("user_Email",currentUser.getEmail());
                                            bundle.putString("notification_exist","yes");
                                            fragment_notification.setArguments(bundle);
                                            //dialog.dismiss();
                                            GetMyTeamFragent();
                                        }
                                        last_idx[0]++;

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //이미지를 storage 에서 불러오는데 실패한 경우
                                    }
                                });
                            }



                        }


                    }
                    else{

                    }
                }



            }       //onComplete
        });
    }

    // 초대 목록을 새로 고침 했을 때 DB에서 초대 목록 데이터를 새로 갖고 온다.
    // 초대 목록에서 특정 초대를 거절했을 때 초대 목록 데이터를 새로 갖고 온다.
    // 초대 목록에서 새로운 팀을 초대 수락 받은 경우 DB에서 초대 목록 데이터를 새로 갖고 온다. x
    public void GetAdditionalNotificationData(){
        Log.v(TAG,"GetAdditionalNotificationData 호출됨");

        //DB에서 갖고온 사용자의 pid 값을 담는다. 프로필 사진을 갖고올 때 사용된다.
        ArrayList<String> team_list_pid = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        //DB에서 갖고온 사용자의 이메일을 담는다
        ArrayList<String> team_list_Email = new ArrayList<String>();

        // 나의 알람 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<MyNotification> myNotificationArrayList = new ArrayList<>();;

        DocumentReference docRef = db.collection("users3").document(currentUser.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.v(TAG,"GetAdditionalNotificationData 1");
                if (task.isSuccessful()) {
                    Log.v(TAG,"GetAdditionalNotificationData 2");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.v(TAG,"GetAdditionalNotificationData 3");
                        List list = (List) document.getData().get("invited_team");
                        Log.v(TAG,"list 크기 : "+list.size());

                            //아래 반복문은 invited_team 데이터를 DB로 부터 받아 왔을 때 문자열을 분리해주는 반복문이다. '_' 를 기준으로 나눠서 리스트에 담는다.
                            for(int i=0;i<list.size();i++){
                                String[] array = list.get(i).toString().split("_");
                                Log.v(TAG,"GetAdditionalNotificationData i의 값 : "+i);
                                for(int j=0;j<array.length;j++){
                                    System.out.println(array[j]);
                                    // 팀의 pid 값을 리스트에 저장.
                                    if(j==0){
                                        team_list_pid.add(array[j]);
                                    }
                                    else if(j==1){
                                        team_list_name.add(array[j]);
                                    }
                                    else if(j==2){
                                        team_list_Email.add(array[j]);
                                    }
                                }
                            }

                            // 사용자의 초대 목록에 아무런 데이터가 없는 경우다.
                            if(list.size()==0){
                                Log.v(TAG,"사용자의 초대 목록에 아무런 데이터가 없는 경우");
                                transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.frameLayout, fragment_notification,"NoticifationFragemnt").commitAllowingStateLoss();
                                bundle.putString("notification_exist","no");
                                //check point
                                fragment_notification.setArguments(bundle);

                                UseMyTeamFragmentFunction().Load_existing_notification();
                                UseMyTeamFragmentFunction().swipeRefreshLayout.setRefreshing(false);
                            }

                            // 사용자의 초대 목록에 데이터가 1개 이상 있는 경우다.
                            else{

                                //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                                final int[] last_idx = {0};

                                for(int i=0;i<list.size();i++){
                                    MyNotification data;
                                    data = new MyNotification("test","test","test","test");
                                    myNotificationArrayList.add(i,data);
                                }

                                Log.v(TAG,"사용자의 초대 목록에 데이터가 있는 경우");
                                Log.v(TAG, String.valueOf(list.size()));
                                // 사용자의 알림 목록을 리사이클러뷰로 표현해주는 코드
                                for(int i=0;i<list.size();i++){
                                    int finalI = i;

                                    String tmp=team_list_name.get(i);
                                    StorageReference storageRef = storage.getReference();
                                    storageRef.child("team_profile/"+team_list_pid.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            MyNotification data2;

                                            data2 = new MyNotification(tmp,uri.toString(),team_list_Email.get(finalI),team_list_pid.get(finalI));

                                            myNotificationArrayList.set(finalI,data2);
                                            notification.setMyNotifications(myNotificationArrayList);

                                            // 사용자의 초대 목록에 데이터가 1개 이상인 경우다.
                                            if(last_idx[0]==list.size()-1){
                                                Log.v(TAG,"사용자의 초대목록에 데이터가 1개 이상인 경우");
                                                transaction = fragmentManager.beginTransaction();
                                                transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
                                                bundle.putParcelable("my_notification_list",notification);
                                                bundle.putString("user_Email",currentUser.getEmail());
                                                bundle.putString("notification_exist","yes");
                                                fragment_notification.setArguments(bundle);

                                                UseMyTeamFragmentFunction().Load_existing_notification();
                                                UseMyTeamFragmentFunction().swipeRefreshLayout.setRefreshing(false);
                                                last_idx[0]=0;
                                            }
                                            last_idx[0]++;

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            //이미지를 storage 에서 불러오는데 실패한 경우
                                        }
                                    });
                                }
                            }
                    }

                    //DB에 문서가 없는 경우
                    else{
                        Log.v(TAG,"GetAdditionalNotificationData document 가 없는 경우");
                    }
                }
                //task is not successful
                else{
                    Log.v(TAG,"GetAdditionalNotificationData task not successful"+task.isCanceled());
                }



            }       //onComplete
        });
    }

    // 초대 목록에서 새로운 팀을 초대 수락 받은 경우 DB에서 초대 목록 데이터를 새로 갖고 온다.
    //프레그먼트 데이터를 갖고 와서 객체에 저장 한다.
    public void InvitationAcceptAdditionalNotificationData(){
        Log.v(TAG,"InvitationAcceptAdditionalNotificationData 호출됨");

        //DB에서 갖고온 사용자의 pid 값을 담는다. 프로필 사진을 갖고올 때 사용된다.
        ArrayList<String> team_list_pid = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        //DB에서 갖고온 사용자의 이메일을 담는다
        ArrayList<String> team_list_Email = new ArrayList<String>();

        // 나의 알람 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<MyNotification> myNotificationArrayList = new ArrayList<>();;

        DocumentReference docRef = db.collection("users3").document(currentUser.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List list = (List) document.getData().get("invited_team");
                        Log.v(TAG,"list 크기 : "+list.size());

                        //아래 반복문은 invited_team 데이터를 DB로 부터 받아 왔을 때 문자열을 분리해주는 반복문이다. '_' 를 기준으로 나눠서 리스트에 담는다.
                        for(int i=0;i<list.size();i++){
                            String[] array = list.get(i).toString().split("_");
                            Log.v(TAG,"GetAdditionalNotificationData i의 값 : "+i);
                            for(int j=0;j<array.length;j++){
                                System.out.println(array[j]);
                                // 팀의 pid 값을 리스트에 저장.
                                if(j==0){
                                    team_list_pid.add(array[j]);
                                }
                                else if(j==1){
                                    team_list_name.add(array[j]);
                                }
                                else if(j==2){
                                    team_list_Email.add(array[j]);
                                }
                            }
                        }

                        // 사용자의 초대 목록에 아무런 데이터가 없는 경우다.
                        if(list.size()==0){
                            Log.v(TAG,"사용자의 초대 목록에 아무런 데이터가 없는 경우2");
                            transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frameLayout, fragment_notification,"NoticifationFragemnt").commitAllowingStateLoss();
                            bundle.putString("notification_exist","no");
                            //check point
                            fragment_notification.setArguments(bundle);

                            UseMyTeamFragmentFunction().Load_existing_notification();
                            dialog.dismiss();
                        }

                        // 사용자의 초대 목록에 데이터가 1개 이상 있는 경우다.
                        else{

                            //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                            final int[] last_idx = {0};

                            for(int i=0;i<list.size();i++){
                                MyNotification data;
                                data = new MyNotification("test","test","test","test");
                                myNotificationArrayList.add(i,data);
                            }

                            Log.v(TAG,"사용자의 초대 목록에 데이터가 있는 경우2");
                            Log.v(TAG, String.valueOf(list.size()));
                            // 사용자의 알림 목록을 리사이클러뷰로 표현해주는 코드
                            for(int i=0;i<list.size();i++){
                                int finalI = i;

                                String tmp=team_list_name.get(i);
                                StorageReference storageRef = storage.getReference();
                                storageRef.child("team_profile/"+team_list_pid.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        MyNotification data2;

                                        data2 = new MyNotification(tmp,uri.toString(),team_list_Email.get(finalI),team_list_pid.get(finalI));

                                        myNotificationArrayList.set(finalI,data2);
                                        notification.setMyNotifications(myNotificationArrayList);

                                        // 사용자의 초대 목록에 데이터가 1개 이상인 경우다.
                                        if(last_idx[0]==list.size()-1){
                                            Log.v(TAG,"사용자의 초대목록에 데이터가 1개 이상인 경우2");
                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
                                            bundle.putParcelable("my_notification_list",notification);
                                            bundle.putString("user_Email",currentUser.getEmail());
                                            bundle.putString("notification_exist","yes");
                                            fragment_notification.setArguments(bundle);

                                            UseMyTeamFragmentFunction().Load_existing_notification();
                                            dialog.dismiss();
                                            last_idx[0]=0;
                                        }
                                        last_idx[0]++;

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //이미지를 storage 에서 불러오는데 실패한 경우
                                    }
                                });
                            }
                        }
                    }

                    //DB에 문서가 없는 경우
                    else{
                        Log.v(TAG,"InvitationAcceptAdditionalNotificationData document 가 없는 경우");
                    }
                }
                //task is not successful
                else{
                    Log.v(TAG,"InvitationAcceptAdditionalNotificationData task not successful"+task.isCanceled());
                }



            }       //onComplete
        });
    }

//    // 초대 목록에서 새로 고침을 한 경우 DB에서 초대 목록 데이터를 새로 갖고 온다.
//    public void GetAdditionalNotificationData_Refresh(){
//        Log.v(TAG,"GetAdditionalNotificationData_Refresh 호출됨");
//        Notification notification=new Notification();
//
//        //DB에서 갖고온 사용자의 pid 값을 담는다. 프로필 사진을 갖고올 때 사용된다.
//        ArrayList<String> team_list_pid = new ArrayList<String>();
//
//        //DB에서 갖고온 사용자의 팀명을 담는다
//        ArrayList<String> team_list_name = new ArrayList<String>();
//
//        //DB에서 갖고온 사용자의 이메일을 담는다
//        ArrayList<String> team_list_Email = new ArrayList<String>();
//
//        // 나의 알람 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
//        ArrayList<MyNotification> myNotificationArrayList = new ArrayList<>();;
//
//        DocumentReference docRef = db.collection("users3").document(currentUser.getEmail());
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        List list = (List) document.getData().get("invited_team");
//                        Log.v(TAG,"list 크기 : "+list.size());
//
//                        //아래 반복문은 invited_team 데이터를 DB로 부터 받아 왔을 때 문자열을 분리해주는 반복문이다. '_' 를 기준으로 나눠서 리스트에 담는다.
//                        for(int i=0;i<list.size();i++){
//                            String[] array = list.get(i).toString().split("_");
//                            //Log.v(TAG,"GetAdditionalNotificationData_Refresh i의 값 : "+i);
//                            for(int j=0;j<array.length;j++){
//                                System.out.println(array[j]);
//                                // 팀의 pid 값을 리스트에 저장.
//                                if(j==0){
//                                    team_list_pid.add(array[j]);
//                                }
//                                else if(j==1){
//                                    team_list_name.add(array[j]);
//                                }
//                                else if(j==2){
//                                    team_list_Email.add(array[j]);
//                                }
//                            }
//                        }
//
//                        // 사용자의 초대 목록에 아무런 데이터가 없는 경우다.
//                        if(list.size()==0){
//                            Log.v(TAG,"사용자의 초대 목록에 아무런 데이터가 없는 경우 2");
//
//                            transaction = fragmentManager.beginTransaction();
//                            transaction.replace(R.id.frameLayout, fragment_notification,"NoticifationFragemnt").commitAllowingStateLoss();
//                            bundle.putString("notification_exist","no");
//                            //check point
//                            fragment_notification.setArguments(bundle);
//
//
//                            UseMyTeamFragmentFunction().myNotificationArrayList.clear();
//                            UseMyTeamFragmentFunction().Load_existing_notification();
//                            UseMyTeamFragmentFunction().myNotificationAdapter.notifyDataSetChanged();
//                            UseMyTeamFragmentFunction().swipeRefreshLayout.setRefreshing(false);
//                            //check point
//                        }
//
//                        // 사용자의 초대 목록에 데이터가 1개 이상 있는 경우다.
//                        else{
//                            Log.v(TAG,"사용자의 초대 목록에 데이터가 있는 경우2");
//                            // 사용자의 알림 목록을 리사이클러뷰로 표현해주는 코드
//                            for(int i=0;i<list.size();i++){
//                                int finalI = i;
//                                Log.v(TAG,"notification_refresh_last : "+notification_refresh_last);
//
//                                String tmp=team_list_name.get(i);
//                                StorageReference storageRef = storage.getReference();
//                                storageRef.child("team_profile/"+team_list_pid.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        MyNotification data2;
//
//                                        data2 = new MyNotification(tmp,uri.toString(),team_list_Email.get(finalI),team_list_pid.get(finalI));
//
//                                        myNotificationArrayList.add(0,data2);
//                                        notification.setMyNotifications(myNotificationArrayList);
//
//                                        // 사용자의 초대 목록에 데이터가 1개 이상인 경우다.
//                                        if(notification_refresh_last==list.size()-1){
//                                            Log.v(TAG,"사용자의 초대목록에 데이터가 1개 이상인 경우 2");
//                                            transaction = fragmentManager.beginTransaction();
//                                            transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
//                                            bundle.putParcelable("my_notification_list",notification);
//                                            bundle.putString("user_Email",currentUser.getEmail());
//                                            bundle.putString("notification_exist","yes");
//                                            fragment_notification.setArguments(bundle);
//
//                                            UseMyTeamFragmentFunction().myNotificationArrayList.clear();
//                                            UseMyTeamFragmentFunction().Load_existing_notification();
//                                            UseMyTeamFragmentFunction().myNotificationAdapter.notifyDataSetChanged();
//                                            UseMyTeamFragmentFunction().swipeRefreshLayout.setRefreshing(false);
//                                            notification_refresh_last=0;
//                                        }
//                                        notification_refresh_last++;
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception exception) {
//                                        //이미지를 storage 에서 불러오는데 실패한 경우
//                                    }
//                                });
//                            }
//                        }
//                    }
//
//                    //DB에 문서가 없는 경우
//                    else{
//                        Log.v(TAG,"GetAdditionalNotificationData document 가 없는 경우");
//                    }
//                }
//                //task is not successful
//                else{
//                    Log.v(TAG,"GetAdditionalNotificationData task not successful"+task.isCanceled());
//                }
//
//
//
//            }       //onComplete
//        });
//    }


    // 앱을 처음 실행했을 때 나의 팀 목록 화면을 표현할 때 필요한 데이터를 DB에서 갖고오기 위해 사용하는 함수다.
    public void GetMyTeamFragent(){
        Log.v(TAG,"GetMyTeamFragent 호출됨");


        //DB에서 갖고온 사용자의 팀이미지 url을 담는다
        ArrayList<String> team_list_imgurl = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        // 나의 팀 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<Myteam> myteamArrayList;
        myteamArrayList = new ArrayList<>();

        db.collection("users3").document(currentUser.getEmail()).collection("team").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //사용자의 팀목록을 데이터를 불러오는데 성공하면 list 에 담는다
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //절차1-1.로그인한 사용자의 팀목록을 표현하는 uid 값을 FireStore 에서 갖고 와서 team_list_imgurl 에 담는다.
                                team_list_imgurl.add(document.getId());
                                // 절차1-2.로그인한 사용자의 팀목록을 표현하는 uid 값을 FireStore 에서 갖고 와서 team_list_name 에 담는다.
                                team_list_name.add(document.getString("team_name"));
                            }
                        }
                        //데이터를 불러오는데 실패한 경우
                        else {
                        }

                        //만약 유저가 이 앱을 처음 사용해서 어떠한 팀에도 소속되어 있지 않은경우의 조건문이다. 기능을 다 구현하고 예외처리를 반드시 할 것.
                        if(team_list_imgurl.size()==0){
                            Log.v(TAG,"현재 아무런 팀에도 소속되어 있지 않다.");
                            bundle.putString("team_exist","no");
                            dialog.dismiss();
                            bundle.putString("user_Email",currentUser.getEmail());
                            fragment_myteam.setArguments(bundle);
                            if(myteam_commit.equals("yes")){
                                transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                                myteam_commit="no";
                                Log.v(TAG,"myteam_commit");
                                Log.v(TAG,myteam_commit);
                            }

                        }

                        for(int i=0;i<team_list_imgurl.size();i++){
                            Myteam data;
                            data = new Myteam("work","work","work");
                            myteamArrayList.add(i,data);
                        }

                        //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                        final int[] last_idx = {0};

                        // 사용자의 팀 목록을 리사이클러뷰로 표현해주는 코드
                        for(int i=0;i<team_list_imgurl.size();i++){
                            int finalI = i;
                            //리사이클러뷰에 담을 아이템 객체 Myteam 을 생성할 때 리스트 안의 값을 바로 담으면 inner class 가 되서 에러가 뜬다. 그래서 임시로 이 변수를 사용.
                            String tmp=team_list_name.get(i);
                            StorageReference storageRef = storage.getReference();
                            // 절차1-3.사용자들의 팀 정보를 표현하기 위해서 team_list_imgurl 에 담긴 uid 값을 바탕으로 FireBase storgae 에서 이미지를 불러온다.
                            storageRef.child("team_profile/"+team_list_imgurl.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Myteam data2;
                                    data2 = new Myteam(tmp,uri.toString(),team_list_imgurl.get(finalI));
                                    myteamArrayList.set(finalI,data2);
                                    team.setTeam(myteamArrayList);

                                    // 마지막으로 Storage 에서 데이터를 받아왔을 때 실행되는 조건문이다.
                                    if(last_idx[0]==team_list_imgurl.size()-1){
                                        Log.v(TAG,"마지막 반복문 수행됨");
                                        Log.v(TAG,myteam_commit);
                                        if(myteam_commit.equals("yes")){
                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                                            myteam_commit="no";
                                        }
                                        bundle.putParcelable("my_team_list",team);
                                        bundle.putString("team_exist","yes");
                                        bundle.putString("user_Email",currentUser.getEmail());
                                        fragment_myteam.setArguments(bundle);
                                        dialog.dismiss();
                                        last_idx[0]=0;

                                    }
                                    last_idx[0]++;

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //이미지를 storage 에서 불러오는데 실패한 경우
                                }
                            });
                        }


                    }       //onComplete end
                });
    }

    // 나의 팀 목록 화면을 새로고침 했을 때 데이터를 DB에서 갖고오기 위해 사용하는 함수다.
    //사용자가 새로운 팀을 개설할때도 이 메소드가 사용된다.
    //사용자가 초대 목록에서 특정 팀의 초대를 수락 받아서, 사용자의 팀이 늘어날 때도 이 메소드가 호출된다. x
    public void RefreshGetMyTeamFragent(){
        Log.v(TAG,"RefreshGetMyTeamFragent 호출됨");

        //DB에서 갖고온 사용자의 팀이미지 url을 담는다
        ArrayList<String> team_list_imgurl = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        // 나의 팀 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<Myteam> myteamArrayList;
        myteamArrayList = new ArrayList<>();

        db.collection("users3").document(currentUser.getEmail()).collection("team").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //사용자의 팀목록을 데이터를 불러오는데 성공하면 list 에 담는다
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                team_list_imgurl.add(document.getId());
                                team_list_name.add(document.getString("team_name"));

                            }

                        }
                        //데이터를 불러오는데 실패한 경우
                        else {


                        }

                        //만약 유저가 이 앱을 처음 사용해서 어떠한 팀에도 소속되어 있지 않은경우의 조건문이다. 기능을 다 구현하고 예외처리를 반드시 할 것.
                        if(team_list_imgurl.size()==0){
                            Log.v(TAG,"현재 아무런 팀에도 소속되어 있지 않다.");
                            bundle.putString("team_exist","no");
                            dialog.dismiss();
                            bundle.putString("user_Email",currentUser.getEmail());
                            fragment_myteam.setArguments(bundle);
                            if(myteam_commit.equals("yes")){
                                transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                                myteam_commit="no";
                                Log.v(TAG,"myteam_commit");
                                Log.v(TAG,myteam_commit);
                            }

                        }

                        for(int i=0;i<team_list_imgurl.size();i++){
                            Myteam data;
                            data = new Myteam("work","work","work");
                            myteamArrayList.add(i,data);
                        }

                        //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                        final int[] last_idx = {0};

                        // 사용자의 팀 목록을 리사이클러뷰로 표현해주는 코드
                        for(int i=0;i<team_list_imgurl.size();i++){

                            int finalI = i;
                            String tmp=team_list_name.get(i);
                            StorageReference storageRef = storage.getReference();
                            storageRef.child("team_profile/"+team_list_imgurl.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Myteam data2;
                                    data2 = new Myteam(tmp,uri.toString(),team_list_imgurl.get(finalI));
                                    myteamArrayList.set(finalI,data2);
                                    team.setTeam(myteamArrayList);
                                    // 마지막으로 Storage 에서 데이터를 받아왔을 때 실행되는 조건문이다.
                                    if(last_idx[0] ==team_list_imgurl.size()-1){
                                        Log.v(TAG,"마지막 반복문 수행됨2");
                                        Log.v(TAG,myteam_commit);
                                        if(myteam_commit.equals("yes")){
                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                                            myteam_commit="no";
                                            Log.v(TAG,"myteam_commit");
                                            Log.v(TAG,myteam_commit);
                                        }
                                        bundle.putParcelable("my_team_list",team);
                                        bundle.putString("team_exist","yes");
                                        bundle.putString("user_Email",currentUser.getEmail());
                                        fragment_myteam.setArguments(bundle);
                                        dialog.dismiss();

                                        UseMyMyTeamFragmentFunction().myteamArrayList.clear();
                                        UseMyMyTeamFragmentFunction().Load_existing_team();
                                        UseMyMyTeamFragmentFunction().myTeamAdapter.notifyDataSetChanged();
                                        UseMyMyTeamFragmentFunction().swipeRefreshLayout.setRefreshing(false);

                                    }
                                    last_idx[0]++;



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //이미지를 storage 에서 불러오는데 실패한 경우
                                }
                            });
                        }


                    }       //onComplete end
                });
    }

    // 사용자가 팀목록 화면에서 초대수락을 눌렀을때 호출되는 함수. DB에서 새로운 팀목록, 초대목록 데이터를 갖고 온 뒤에 각 프레그먼트를 표현하는 객체에 저장하고,
    //리사이클러뷰에 표현해준다. 이 메소드에서는 팀 목록 데이터를 갖고 와서 객체에 저장 해주는 것 까지 한다. 프레그먼트 데이터를 갖고 와서 객체에 저장 하는 것은
    //InvitationAcceptAdditionalNotificationData 메소드에서 진행한다.
    public void AcceptInvitation(){
        Log.v(TAG,"AcceptInvitation 호출됨");

        //DB에서 갖고온 사용자의 팀이미지 url을 담는다
        ArrayList<String> team_list_imgurl = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        // 나의 팀 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
        ArrayList<Myteam> myteamArrayList;
        myteamArrayList = new ArrayList<>();

        db.collection("users3").document(currentUser.getEmail()).collection("team").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //사용자의 팀목록을 데이터를 불러오는데 성공하면 list 에 담는다
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                team_list_imgurl.add(document.getId());
                                team_list_name.add(document.getString("team_name"));

                            }

                        }
                        //데이터를 불러오는데 실패한 경우
                        else {


                        }

                        for(int i=0;i<team_list_imgurl.size();i++){
                            Myteam data;
                            data = new Myteam("work","work","work");
                            myteamArrayList.add(i,data);
                        }

                        //DB에서 마지막으로 데이티를 받아온 시점을 확인하기 위해 사용하는 변수다.
                        final int[] last_idx = {0};

                        // 사용자의 팀 목록을 리사이클러뷰로 표현해주는 코드
                        for(int i=0;i<team_list_imgurl.size();i++){

                            int finalI = i;
                            String tmp=team_list_name.get(i);
                            StorageReference storageRef = storage.getReference();
                            storageRef.child("team_profile/"+team_list_imgurl.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Myteam data2;
                                    data2 = new Myteam(tmp,uri.toString(),team_list_imgurl.get(finalI));
                                    myteamArrayList.set(finalI,data2);
                                    team.setTeam(myteamArrayList);
                                    // 마지막으로 Storage 에서 데이터를 받아왔을 때 실행되는 조건문이다.
                                    if(last_idx[0] ==team_list_imgurl.size()-1){
                                        Log.v(TAG,"마지막 반복문 수행됨3");
                                        Log.v(TAG,myteam_commit);

                                        bundle.putParcelable("my_team_list",team);
                                        bundle.putString("team_exist","yes");
                                        bundle.putString("user_Email",currentUser.getEmail());
                                        fragment_myteam.setArguments(bundle);

                                        UseMyMyTeamFragmentFunction().Load_existing_team();
                                        InvitationAcceptAdditionalNotificationData();
                                    }
                                    last_idx[0]++;

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //이미지를 storage 에서 불러오는데 실패한 경우
                                }
                            });
                        }


                    }       //onComplete end
                });
    }

    // 화면에 필요한 정보들을 보여주기 위해서 DB에서 데이터를 받아올 때 까지 로딩중이라는 사실을 알려주는 다이얼로그 메소드다.
    public void Download_dialog(String showing_text){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(showing_text);
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG,"onActivityResult 호출");
        Log.v(TAG,"requestCode "+requestCode);
        Log.v(TAG,"resultCode "+resultCode);

        if(resultCode==3135678){
            Log.v(TAG,"사용자가 새로운 팀을 생성함");
            String team_pid;
            String team_name;
            //reload=data.getStringExtra("reload");
            team_pid=data.getStringExtra("team_pid");
            team_name=data.getStringExtra("team_name");
            //Log.v(TAG,"reload 의 값 : "+reload);
            Log.v(TAG,"team_pid 의 값 : "+team_pid);
            Log.v(TAG,"team_name 의 값 : "+team_name);

            RefreshGetMyTeamFragent();

        }



    }       //onActivityResult end

    // 앱이 처음 실행 됐을 때 마이페이지에 필요한 데이터를 DB에서 불러온다.
    public void GetMyPageData(){
        Log.v(TAG,"GetMyPageData 호출");
        StorageReference storageRef = storage.getReference();
        storageRef.child("user_profile/"+currentUser.getEmail()+"_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v(TAG,"uri "+uri.toString());
                bundle.putString("image_uri",uri.toString());
                //사용자가 이전에 설정한 자신의 정보들을 DB에서 불러온다.
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef=db.collection("users3").document(currentUser.getEmail());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                bundle.putString("user_nick_name",document.get("nick_name").toString());
                                bundle.putString("user_frequently_coffee",document.get("my_coffee").toString());
                                bundle.putString("user_coffee_detail_option",document.get("my_coffee_option").toString());
                                fragment_mypage.setArguments(bundle);
                                Log.v(TAG,"GetMyPageData 끝");
                                GetNotificationData();
                            } else {
                            }
                        } else {
                        }
                    }
                });

                //Glide.with(getContext()).load(uri).into(team_profile_url);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(TAG,"onFailure occured");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef=db.collection("users3").document(currentUser.getEmail());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                bundle.putString("user_nick_name",document.get("nick_name").toString());
                                bundle.putString("user_frequently_coffee",document.get("my_coffee").toString());
                                bundle.putString("user_coffee_detail_option",document.get("my_coffee_option").toString());
                                fragment_mypage.setArguments(bundle);
                                Log.v(TAG,"GetMyPageData 끝");
                                GetNotificationData();
                            } else {
                            }
                        } else {
                        }
                    }
                });
            }
        });
    }

    // 마이페이지에서 업데이트 된 데이터들을 새로 DB에서 불러온다.
    public void UpdateMyPageData(){
        Log.v(TAG,"UpdateMyPageData 호출");
        StorageReference storageRef = storage.getReference();
        storageRef.child("user_profile/"+currentUser.getEmail()+"_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v(TAG,"uri "+uri.toString());
                bundle.putString("image_uri",uri.toString());
                //사용자가 이전에 설정한 자신의 정보들을 DB에서 불러온다.
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef=db.collection("users3").document(currentUser.getEmail());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                bundle.putString("user_nick_name",document.get("nick_name").toString());
                                bundle.putString("user_frequently_coffee",document.get("my_coffee").toString());
                                bundle.putString("user_coffee_detail_option",document.get("my_coffee_option").toString());
                                fragment_mypage.setArguments(bundle);
                                Log.v(TAG,"UpdateMyPageData 끝");
                                UseMyPageFragmentFunction().dialog.dismiss();
                            } else {
                            }
                        } else {
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();


    }

    // 알림 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림 프레그먼트가 MainActivity 에게 사용자가 팀 초대를 수락 받았다는 사실을 전달 해야하기 때문에 이 메소드가 존재한다.
    @Override
    public void InvitationAccepted() {
        Log.v(TAG,"InvitationAccepted");

        //RefreshGetMyTeamFragent();
        //GetAdditionalNotificationData();

        Download_dialog("잠시만 기다려주세요! :)");
        AcceptInvitation();

    }

    // 알림 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림 프레그먼트가 MainActivity 에게 리사이클러뷰를 새로고침 했다는 사실을 전달 하기 위해 이 메소드가 존재한다..
    @Override
    public void NotificationRefreshOccured() {
        Log.v(TAG,"NotificationRefreshOccured");

        GetAdditionalNotificationData();
        //notification_refresh_last=0;
        //GetAdditionalNotificationData_Refresh();

    }

    // 마이페이지 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //마이페이지 프레그먼트가 MainActivity 에게 프로필 정보를 수정했다는 사실을 알리기 위해서 이 인터페이스를 사용했다.
    @Override
    public void UserProfileChanged() {
        Log.v(TAG,"UserProfileChanged");
        UpdateMyPageData();
        UseMyPageFragmentFunction().dialog.dismiss();
    }

    // Notification 에 있는 메소드를 엑티비티에서 사용하기위해 만든 메소드다.
    public NotificationFragment UseMyTeamFragmentFunction(){

        transaction=fragmentManager.beginTransaction();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_notification,"NoticifationFragemnt").commit();
        getSupportFragmentManager().executePendingTransactions();
        FragmentManager fm=getSupportFragmentManager();
        NotificationFragment fragment=(NotificationFragment)fm.findFragmentByTag("NoticifationFragemnt");

        return fragment;
    }

    // Mypage 에 있는 메소드를 엑티비티에서 사용하기위해 만든 메소드다.
    public MyPageFragment UseMyPageFragmentFunction(){

        transaction=fragmentManager.beginTransaction();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_mypage,"MyPageFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        FragmentManager fm=getSupportFragmentManager();
        MyPageFragment fragment=(MyPageFragment)fm.findFragmentByTag("MyPageFragment");

        return fragment;
    }

    // MyTeamFragment 에 있는 메소드를 엑티비티에서 사용하기위해 만든 메소드다.
    public MyTeamFragement UseMyMyTeamFragmentFunction(){

        transaction=fragmentManager.beginTransaction();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_myteam,"MyTeamFragement").commit();
        getSupportFragmentManager().executePendingTransactions();
        FragmentManager fm=getSupportFragmentManager();
        MyTeamFragement fragment=(MyTeamFragement)fm.findFragmentByTag("MyTeamFragement");

        return fragment;
    }




    // 팀목록 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //팀목록 프레그먼트가 MainActivity 에게 사용자가 새로고침을 했다는 사실을 전달 해야하기 때문에 이 메소드가 존재한다.
    @Override
    public void RefreshMyTeam() {
        Log.v(TAG,"RefreshMyTeam 메소드 호출");
        RefreshGetMyTeamFragent();
    }

    // 알림목록 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림목록 프레그먼트가 MainActivity 에게 사용자가 초대를 거절 했다는 사실을 전달 해야하기 때문에 이 메소드가 존재한다.
    @Override
    public void InvitationDenied() {
        Log.v(TAG,"InvitationDenied 메소드 호출");

        GetAdditionalNotificationData();
    }
}