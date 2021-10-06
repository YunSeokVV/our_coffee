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

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    // 초대해준 팀의 pid 값을 담는 리스트다.
    ArrayList<String> inviteteam_pid_list = new ArrayList<String>();

    // 초대해준 사람의 이메일을 담는 리스트다.
    ArrayList<String> inviter_list = new ArrayList<String>();

    // 초대해준 사람의 닉네임을 담는 리스트다.
    ArrayList<String> inviter_nickname_list = new ArrayList<String>();

    // 초대해준 팀의 이름을 담는 리스트다.
    ArrayList<String> inviter_teamname_list = new ArrayList<String>();

    // 초대해준 팀과 초대원의 닉네임을 표현하는 데이터를 DB로 부터 불러와 담는 변수다.
    ArrayList<String> invited_teaam_list = new ArrayList<String>();

    //리사이클러뷰 관련 코드
    ArrayList<MyNotification> myNotificationArrayList;
    MyNotificationAdapter myNotificationAdapter;

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle=getArguments();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        inviteteam_pid_list=bundle.getStringArrayList("team_pid");
        inviter_list=bundle.getStringArrayList("Email");
        inviter_nickname_list=bundle.getStringArrayList("nick_name");
        inviter_teamname_list=bundle.getStringArrayList("team_name");
        invited_teaam_list=bundle.getStringArrayList("invited_team");

        System.out.println("아오");
        //System.out.println(invited_teaam_list.size());
        for(int i=0;i<inviteteam_pid_list.size();i++){
            System.out.println("팀 pid 값");
            System.out.println(inviteteam_pid_list.get(i));
            System.out.println("사용자 이메일");
            System.out.println(inviter_list.get(i));
            System.out.println("사용자 닉네임");
            //System.out.println(inviter_nickname_list.get(i));
            //System.out.println("팀명 목록");
            //System.out.println(inviter_teamname_list.get(i));
            System.out.println("초대한 팀과 사용자 목록");
            System.out.println(invited_teaam_list.get(i));
        }


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

        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_notification.addItemDecoration(decoration_height);



        myNotificationAdapter.setOnItemClickListener(new MyNotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyNotificationAdapter.CustomViewHolder_MyNotification holder, View view, int position) {
                DialogClick();

                System.out.println("자 드가자");
                System.out.println(invited_teaam_list.get(position));
            }
        });


        // 사용자의 팀 목록을 리사이클러뷰로 표현해주는 코드
        for(int i=0;i<inviteteam_pid_list.size();i++){
            //리사이클러뷰에 담을 아이템 객체 MyNotification 을 생성할 때 리스트 안의 값을 바로 담으면 inner class 가 되서 에러가 뜬다. 그래서 임시로 이 변수를 사용.
            StorageReference storageRef = storage.getReference();
            int finalI = i;
            // 절차1-3.사용자들의 팀 정보를 표현하기 위해서 team_list_imgurl 에 담긴 uid 값을 바탕으로 FireBase storgae 에서 이미지를 불러온다.
            storageRef.child("team_profile/"+inviteteam_pid_list .get(i)+"/"+inviter_list.get(i) +"_team.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    MyNotification data2;
                    // 절차1-4.사용자들의 팀 정보를 표현하기 위해서 team_list_name 에 담긴 팀명을 사용한다.
                    data2 = new MyNotification(inviter_teamname_list.get(finalI)+"로 부터 초대 알림이 왔어요!!",uri.toString(),"초대자 : "+inviter_nickname_list.get(finalI));

                    myNotificationArrayList.add(0,data2); // RecyclerView의 마지막 줄에 삽입
                    myNotificationAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //이미지를 storage 에서 불러오는데 실패한 경우
                }
            });
        }


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

    public void DialogClick() {
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
                DocumentReference docRef=db.collection("users3").document(currentUser.getEmail());
                //docRef.update("invited_team", FieldValue.arrayRemove());
            } });
        builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        AlertDialog alertDialog = builder.create(); alertDialog.show();
    }

}
