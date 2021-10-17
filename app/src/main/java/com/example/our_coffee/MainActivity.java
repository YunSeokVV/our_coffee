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
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;


import com.example.our_coffee.Utils.MyNotification;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.Notification;
import com.example.our_coffee.Utils.Team;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements NotificationFragment.OnTimePickerSetListener{

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

    // 나의 팀 목록을 표현하는 리사이클러뷰를 표현하기 위한 리스트
    ArrayList<Myteam> myteamArrayList;
    Team team=new Team();

    String TAG="MainActivity";

    // DB에서 필요한 데이터를 다운받기 전까지 화면에 다운중이라는 사실을 알려주기 위환 dialog 다.
    ProgressDialog dialog;

    Bundle bundle;

    // DB에서 자신의 팀 목록을 표현할 때 필요로 하는 데이터를 받아 올 때 마지막으로 받았을때의 인덱스를 표현해주는 변수다.
    int team_last_receive=0;
    int notification_last_receive=0;

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




        DocumentReference doc=db.collection("team3").document("test");
        doc.update("member_name", FieldValue.arrayUnion("대체왜"));

        Download_dialog();


        GetNotificationData();
        //GetMyTeamFragent();



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
    //앱이 가장 먼저 실행됐을때 호출된다. 사용자가 팀 초대 수락을 받아서 팀이 추가될 때는 이 메소드가 호출되지 않는다.
    public void GetNotificationData(){
        System.out.println("GetNotificationData 호출됨");
        Notification notification=new Notification();

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
                            System.out.println("아무에게도 초대 안받음");


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

                                        myNotificationArrayList.add(0,data2);
                                        notification.setMyNotifications(myNotificationArrayList);

                                        if(notification_last_receive==list.size()-1){

                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
                                            bundle.putParcelable("my_notification_list",notification);
                                            bundle.putString("user_Email",currentUser.getEmail());
                                            fragment_notification.setArguments(bundle);
                                            //dialog.dismiss();
                                            GetMyTeamFragent();
                                        }
                                        notification_last_receive++;

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

    // 초대 목록에서 새로운 팀을 초대 수락 받은 경우 DB에서 초대 목록 데이터를 새로 갖고온다.
    public void GetAdditionalNotificationData(){
        System.out.println("GetAdditionalNotificationData 호출됨");
        Notification notification=new Notification();

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

                                        myNotificationArrayList.add(0,data2);
                                        notification.setMyNotifications(myNotificationArrayList);

                                        if(notification_last_receive==list.size()-1){

                                            transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
                                            bundle.putParcelable("my_notification_list",notification);
                                            bundle.putString("user_Email",currentUser.getEmail());
                                            fragment_notification.setArguments(bundle);

                                        }
                                        notification_last_receive++;

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //이미지를 storage 에서 불러오는데 실패한 경우
                                    }
                                });
                            }






                    }
                    else{

                    }
                }



            }       //onComplete
        });
    }

    // 알림Fragment 화면에서 필요로 하는 데이터를 보내기 위한 메소드다. value 로 list 가 들어간다.
    public void MyNotification_Fragment_data(String key,ArrayList<String> list){
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment_notification).commitAllowingStateLoss();
        bundle.putStringArrayList(key,list);
        fragment_notification.setArguments(bundle);

    }

    // 나의 팀 목록 화면을 표현할 때 필요한 데이터를 DB에서 갖고오기 위해 사용하는 함수다.
    public void GetMyTeamFragent(){
        System.out.println("GetMyTeamFragent 호출됨");

        //DB에서 갖고온 사용자의 팀이미지 url을 담는다
        ArrayList<String> team_list_imgurl = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        myteamArrayList = new ArrayList<>();;

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
                            transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                        }

                        for(int i=0;i<team_list_imgurl.size();i++){
                            Myteam data;
                            data = new Myteam("work","work","work");
                            myteamArrayList.add(i,data);
                        }


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

                                    // 절차1-4.사용자들의 팀 정보를 표현하기 위해서 team_list_name 에 담긴 팀명을 사용한다.
                                    data2 = new Myteam(tmp,uri.toString(),team_list_imgurl.get(finalI));

                                    //원본코드
                                    //myteamArrayList.add(0,data2);

                                    myteamArrayList.set(finalI,data2);

                                    team.setTeam(myteamArrayList);

                                    // 마지막으로 Storage 에서 데이터를 받아왔을 때 실행되는 조건문이다.
                                    if(team_last_receive==team_list_imgurl.size()-1){
                                        Log.v(TAG,"마지막 반복문 수행됨");
                                        transaction = fragmentManager.beginTransaction();
                                        transaction.replace(R.id.frameLayout, fragment_myteam).commitAllowingStateLoss();
                                        bundle.putParcelable("my_team_list",team);
                                        bundle.putString("team_exist","yes");
                                        bundle.putString("user_Email",currentUser.getEmail());
                                        fragment_myteam.setArguments(bundle);
                                        dialog.dismiss();
                                    }
                                    team_last_receive++;

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
    public void Download_dialog(){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터 확인중");
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG,"onActivityResult 호출");
        Log.v(TAG,"requestCode "+requestCode);
        Log.v(TAG,"resultCode "+resultCode);


        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Log.v(TAG,"상황1");
            Log.v(TAG, String.valueOf(requestCode));
            Log.v(TAG, String.valueOf(resultCode));
            Log.v(TAG,data.getStringExtra("reload"));

            // 이 변수를 통해 리사이클러뷰 새로고침 여부를 파악한다.
            String reload="nothing";
            String team_pid;
            String team_name;
            reload=data.getStringExtra("reload");
            team_pid=data.getStringExtra("team_pid");
            team_name=data.getStringExtra("team_name");
            Log.v("MyTeamFragment","reload 의 값 : "+reload);
            Log.v("MyTeamFragment","team_pid 의 값 : "+team_pid);
            Log.v("MyTeamFragment","team_name 의 값 : "+team_name);

        }

        // Make_newteam 액티비티에서 새로운 팀을 생성하고 나면 팀목록을 표현하는 리사이클러뷰에 새로 추가된 팀을 추가한다.
        //원래는 resultCode 를 임의로 설정해서 이 상황을 처리하려고 했지만 어째서인지 갑자기 되지 않는다.
        else{
            Log.v(TAG,"상황2");
            String reload="nothing";
            String team_pid;
            String team_name;
            reload=data.getStringExtra("reload");
            team_pid=data.getStringExtra("team_pid");
            team_name=data.getStringExtra("team_name");
            Log.v(TAG,"reload 의 값 : "+reload);
            Log.v(TAG,"team_pid 의 값 : "+team_pid);
            Log.v(TAG,"team_name 의 값 : "+team_name);

            if(reload.equals("reload_data")){
                StorageReference storageRef = storage.getReference();
                storageRef.child("team_profile/"+team_pid+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Myteam data2;
                        data2 = new Myteam(team_name,uri.toString(),team_pid);

                        //myteamArrayList.add(0,data2);
                        myteamArrayList.add(data2); //마지막 줄에 삽입
                        team.setTeam(myteamArrayList);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //이미지를 storage 에서 불러오는데 실패한 경우
                    }
                });
            }

            // Make_newteam 에서 뒤로가기 버튼을 누른 경우의 조건문. 이렇게 예외 처리를 안하면 reload 로 받는 값이 null 이여서 앱이 튕긴다.
            else if(reload.equals("back_btn")){

            }


        }

        //todo : 21.10.15 개발을 할 때 까지만 해도 Make_newteam 액티비티에서 팀을 생성하고 finish 가 된 다음에 이 조건문이 실행 됐었다. 하지만 다음날인 16일 부터는 왜인지 이 조건문이 타지지가 않았다.
        //이 조건문에 대한 자세한 설명을 확인하고 싶다면 16일의 에버노트 내용을 참고할 것.
        if(resultCode==3135678){
            Log.v(TAG,"상황3");
            Log.v(TAG, String.valueOf(requestCode));
            Log.v(TAG, String.valueOf(resultCode));

            String reload="nothing";
            String team_pid;
            String team_name;
            reload=data.getStringExtra("reload");
            team_pid=data.getStringExtra("team_pid");
            team_name=data.getStringExtra("team_name");
            Log.v(TAG,"reload 의 값 : "+reload);
            Log.v(TAG,"team_pid 의 값 : "+team_pid);
            Log.v(TAG,"team_name 의 값 : "+team_name);


            StorageReference storageRef = storage.getReference();
            // 절차1-3.사용자들의 팀 정보를 표현하기 위해서 team_list_imgurl 에 담긴 uid 값을 바탕으로 FireBase storgae 에서 이미지를 불러온다.
            storageRef.child("team_profile/"+team_pid+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Myteam data2;


                    data2 = new Myteam(team_name,uri.toString(),team_pid);

                    // 만약 이 조건문이 실행된다면 아래의 add 의 인덱스 값을 가장 마지막으로 설정하기.
                    //myteamArrayList.add(0,data2);
                    //team.setTeam(myteamArrayList);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //이미지를 storage 에서 불러오는데 실패한 경우
                }
            });
        }
    }


    @Override
    public void onTimePickerSet(String a, String b) {
        Log.v(TAG,"onTimePickerSet");
        GetMyTeamFragent();
        GetNotificationData();
        Log.v(TAG,a);
        Log.v(TAG,b);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}