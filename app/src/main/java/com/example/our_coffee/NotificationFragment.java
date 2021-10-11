package com.example.our_coffee;
// 이 프레그먼트는 다른팀으로부터 초대 메세지를 확인하기 위한 Frament 다.
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationFragment extends Fragment {

    //현재 로그인한 유저의 이메일을 담는 변수다
    String login_user;
    FirebaseFirestore db;

    //리사이클러뷰 관련 코드
    ArrayList<MyNotification> myNotificationArrayList;
    MyNotificationAdapter myNotificationAdapter;


    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //MainActivity 에서 알림객체를 받아오기위해 필요하다.
    Notification notification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragmnet_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //사용자가 팀을 추가하기위해 누르는 버튼
        RecyclerView my_notification=(RecyclerView)view.findViewById(R.id.my_notification);

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

        //RequestActivity에서 전달한 번들 저장
        Bundle bundle = getArguments();
        notification = bundle.getParcelable("my_notification_list");
        login_user=bundle.getString("user_Email");


        db = FirebaseFirestore.getInstance();

        System.out.println("되지 않을까?");
        for(MyNotification myNotification:notification.getMyNotifications()){
            String test = "팀명 " + myNotification.getTeam_name() + "  이미지url " + myNotification.getImage_url()+"  초대자 메일 "+myNotification.getInviter()+"    초대팀pid "+myNotification.getTeam_pid();
            System.out.println(test);

            MyNotification data2;

            data2 = new MyNotification(myNotification.getTeam_name(),myNotification.getImage_url(),myNotification.getInviter(),myNotification.getTeam_pid());

            myNotificationArrayList.add(0,data2); // RecyclerView의 마지막 줄에 삽입
            myNotificationAdapter.notifyDataSetChanged();

        }

        myNotificationAdapter.setOnItemClickListener(new MyNotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyNotificationAdapter.CustomViewHolder_MyNotification holder, View view, int position) {
                DialogClick(position);

            }
        });

    }       //onViewCreated end

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

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

    }

    public void DialogClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("초대알림").setMessage("해당 팀의 초대를 수락하시겠습니까?");
        builder.setPositiveButton("수락", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(getContext(), "Yeah!!", Toast.LENGTH_LONG).show(); } });
        // 초대를 거절한경우. 초대목록에서 아이템을 제거하고 DB에서도 삭제 시킨다.
        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //DB에서 저장할 값을 잠시 저장하기 위한 변수
                String tmp;
                tmp=myNotificationArrayList.get(position).getTeam_pid()+"_"+myNotificationArrayList.get(position).getTeam_name()+"_"+myNotificationArrayList.get(position).getInviter();
                System.out.println("데이터 확인!!!");
                System.out.println(tmp);

                DocumentReference docRef=db.collection("users3").document(login_user);
                docRef.update("invited_team", FieldValue.arrayRemove(tmp));

                myNotificationAdapter.RemoveItem(position);
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

}
