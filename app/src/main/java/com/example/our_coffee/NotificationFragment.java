package com.example.our_coffee;
// 이 프레그먼트는 다른팀으로부터 초대 메세지를 확인하기 위한 Frament 다.
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.our_coffee.Utils.MyNotification;
import com.example.our_coffee.Utils.MyNotificationAdapter;
import com.example.our_coffee.Utils.MyTeamAdapter;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.Notification;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class NotificationFragment extends Fragment {

    String TAG="NoticifationFragemnt";

    //현재 로그인한 유저의 이메일을 담는 변수다
    String login_user;
    FirebaseFirestore db;

    //리사이클러뷰 관련 코드
    ArrayList<MyNotification> myNotificationArrayList;
    MyNotificationAdapter myNotificationAdapter;

    // 초대받은 팀의 pid를 담는 리스트다.
    ArrayList<String> team_pid_list = new ArrayList<String>();

    // 초대받은 팀명을 담는 리스트다.
    ArrayList<String> team_name_list = new ArrayList<String>();

    // 초대한 팀원의 이메일을 담는 리스트다.
    ArrayList<String> team_inviter_list = new ArrayList<String>();

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //MainActivity 에서 알림객체를 받아오기위해 필요하다.
    Notification notification;

    //RequestActivity에서 전달한 번들 저장
    Bundle bundle;

    // MainActivity 에 사용자가 초대를 수락받았다는 내용을 전달하기 위한 객체다.
    InvitationAcceptedListener invitationAcceptedListener;

    // MainActivity 에 사용자가 초대 목록을 새로고침 했다는 내용을 전달하기 위한 객체다.
    NotificationRefreshListener notificationRefreshListener;

    // MainActrivity 에 사용자가 초대를 거절했다는 사실을 전달하기 위한 객체다.
    InvitationDeniedListener invitationDeniedListener;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragmnet_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //사용자가 팀을 추가하기위해 누르는 버튼
        RecyclerView my_notification=(RecyclerView)view.findViewById(R.id.my_notification);
        swipeRefreshLayout=view.findViewById(R.id.swipe_fragment_list);

        //사용자의 팀 목록을 표현해주기 위한 리사이클러뷰다
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        my_notification.setLayoutManager(mLinearLayoutManager);
        myNotificationArrayList = new ArrayList<>();
        myNotificationAdapter = new MyNotificationAdapter(myNotificationArrayList,getContext());
        my_notification.setAdapter(myNotificationAdapter);
        myNotificationArrayList = new ArrayList<>();
        myNotificationAdapter = new MyNotificationAdapter(myNotificationArrayList,getContext());
        my_notification.setAdapter(myNotificationAdapter);

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_notification.addItemDecoration(decoration_height);

        db = FirebaseFirestore.getInstance();

        //RequestActivity에서 전달한 번들 저장
        bundle = getArguments();
        if(bundle!=null){
            System.out.println("로그 확인1");
            Load_existing_notification();
        }
        else{
            System.out.println("번들 null");
        }



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //myNotificationArrayList.clear();
                notificationRefreshListener.NotificationRefreshOccured();
                //Load_existing_user();
//                myNotificationAdapter.notifyDataSetChanged();
//                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }       //onViewCreated end

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof InvitationAcceptedListener){
            invitationAcceptedListener=(InvitationAcceptedListener)context;
        }else{
            throw new RuntimeException(context.toString()+"must implent InvitationAcceptedListener");
        }

        if(context instanceof NotificationRefreshListener){
            notificationRefreshListener=(NotificationRefreshListener)context;
        }else{
            throw new RuntimeException(context.toString()+"must implent NotificationRefreshListener");
        }

        if(context instanceof InvitationDeniedListener){
            invitationDeniedListener=(InvitationDeniedListener)context;
        }else{
            throw new RuntimeException(context.toString()+"must implent InvitationDeniedListener");
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        invitationAcceptedListener=null;
        notificationRefreshListener=null;
        invitationDeniedListener=null;
    }

    public void DialogClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("초대알림").setMessage("해당 팀의 초대를 수락하시겠습니까?");
        builder.setPositiveButton("수락", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                // 초대받을 팀의 pid 값.
                String team_pid;
                team_pid=myNotificationArrayList.get(position).getTeam_pid();
                //초대 받은 팀에 나 자신을 추가한다. (team3 컬렉션의 초대받은 팀 목록에서 나 자신을 추가한다.)
                DocumentReference doc=db.collection("team3").document(team_pid);
                doc.update("team_member_name", FieldValue.arrayUnion(login_user));
                //나의 팀목록에서 초대받은 팀을 추가한다. (users3컬렉션의 team 컬렉션에 새로운 팀pid 문서를 추가.)
                Map<String, Object> data2 = new HashMap<>();
                data2.put("team_name", team_name_list.get(position));
                db.collection("users3").document(login_user).collection("team").document(team_pid).set(data2)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");

                                // 초대목록에서 초대 수락한 팀을 삭제 한다.
                                Accept_invitation(position);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });



            } });
        // 초대를 거절한경우. 초대목록에서 아이템을 제거하고 DB에서도 삭제 시킨다.
        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Delete_invitation(position);
                Toast myToast = Toast.makeText(getContext(),"초대를 거절했습니다", Toast.LENGTH_SHORT);
                myToast.show();
            } });
        builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        AlertDialog alertDialog = builder.create(); alertDialog.show();
    }

    //자신의 초대 목록에서 특정 초대를 거절할 때 사용하는 메소드.
    public void Delete_invitation(int position){
        Log.v(TAG,"Delete_invitation called");
        //DB에서 사용할 값을 잠시 저장하기 위한 변수
        String tmp;
        tmp=team_pid_list.get(position)+"_"+team_name_list.get(position)+"_"+team_inviter_list.get(position);
        //초대 목록에서 거절한 팀을 제외하는 코드
        DocumentReference docRef=db.collection("users3").document(login_user);
        //docRef.update("invited_team", FieldValue.arrayRemove(tmp));
        // 초대 거절한 invited_team 데이터를 없앤 뒤의 내용을 적는다.
        docRef.update("invited_team", FieldValue.arrayRemove(tmp)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.v(TAG,"onComplete");

                invitationDeniedListener.InvitationDenied();
            }
        });
    }

    //자신의 초대 목록에서 특정 초대를 수락할 때 사용하는 메소드.
    public void Accept_invitation(int position){
        Log.v(TAG,"Accept_invitation called");
        //DB에서 사용할 값을 잠시 저장하기 위한 변수
        String tmp;
        tmp=team_pid_list.get(position)+"_"+team_name_list.get(position)+"_"+team_inviter_list.get(position);

        //초대 목록에서 거절한 팀을 제외하는 코드
        DocumentReference docRef=db.collection("users3").document(login_user);
        //docRef.update("invited_team", FieldValue.arrayRemove(tmp));

        // 초대 거절한 invited_team 데이터를 없앤 뒤의 내용을 적는다.
        docRef.update("invited_team", FieldValue.arrayRemove(tmp)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.v(TAG,"onComplete");

                invitationAcceptedListener.InvitationAccepted();
            }
        });
    }

    // 알람 목록이 있는 사용자가 로그인한 경우 알람목록을 리사이클러뷰로 표현한다.
    public void Load_existing_notification(){
        Log.v(TAG,"Load_existing_notification");

        // 현태 알림 목록에 데이터가 존재하는지 표현해주는 변수
        String notify_exist=bundle.getString("notification_exist");

        // 사용자의 초대목록에 아무런 데이터가 없는 경우
        if(notify_exist.equals("no")){
            Log.v(TAG,"알림 데이터 x");

            // 알람목록에서 초대 수락을 했을 때 알람 목록을 지운다.
            myNotificationArrayList.clear();
            myNotificationAdapter.notifyDataSetChanged();
        }

        // 사용자의 초대목록에 데이터가 있는 경우
        else if(notify_exist.equals("yes")){
            Log.v(TAG,"알림 데이터 ㅇ");
            notification = bundle.getParcelable("my_notification_list");
            login_user=bundle.getString("user_Email");
            System.out.println(login_user);
            System.out.println("되지 않을까?");

            myNotificationArrayList.clear();

            for(MyNotification myNotification:notification.getMyNotifications()){
                String test = "팀명 " + myNotification.getTeam_name() + "  이미지url " + myNotification.getImage_url()+"  초대자 메일 "+myNotification.getInviter()+"    초대팀pid "+myNotification.getTeam_pid();
                System.out.println(test);

                MyNotification data2;

                data2 = new MyNotification(myNotification.getTeam_name()+"로 부터 초대 알림이 왔어요!!",myNotification.getImage_url(),"초대자 : "+myNotification.getInviter(),myNotification.getTeam_pid());

                myNotificationArrayList.add(0,data2); // RecyclerView의 마지막 줄에 삽입
                team_pid_list.add(0,myNotification.getTeam_pid());
                team_name_list.add(0,myNotification.getTeam_name());
                team_inviter_list.add(0,myNotification.getInviter());

            }

            // 리사이클러뷰의 아이템 클릭시 실행됨
            myNotificationAdapter.setOnItemClickListener(new MyNotificationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(MyNotificationAdapter.CustomViewHolder_MyNotification holder, View view, int position) {
                    DialogClick(position);

                }
            });

            myNotificationAdapter.notifyDataSetChanged();

        }







    }


    // 알림 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림 프레그먼트가 MainActivity 에게 사용자가 팀 초대를 수락 받았다는 사실을 전달 해야하기 때문에 이 인터페이스가 존재한다.
    public interface  InvitationAcceptedListener{
        void InvitationAccepted();
    }

    // 알림 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림 프레그먼트가 MainActivity 에게 사용자가 팀 초대를 거절했다는 사실을 전달 해야하기 때문에 이 인터페이스가 존재한다.
    public interface  InvitationDeniedListener{
        void InvitationDenied();
    }

    // 알림 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //알림 프레그먼트가 MainActivity 에게 팀목록을 새로고침 했다는 사실을 전달 해야하기 때문에 이 인터페이스가 존재한다.
    public interface  NotificationRefreshListener{
        void NotificationRefreshOccured();
    }




}
