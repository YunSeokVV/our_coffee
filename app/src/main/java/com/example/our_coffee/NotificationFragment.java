package com.example.our_coffee;
// 이 프레그먼트는 다른팀으로부터 초대 메세지를 확인하기 위한 Frament 다.
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.our_coffee.Utils.MyTeamAdapter;
import com.example.our_coffee.Utils.Myteam;
import com.example.our_coffee.Utils.RecyclerDecoration_Height;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationFragment extends Fragment {

    //리사이클러뷰 관련 코드
    private ArrayList<Myteam> myteamArrayList;
    private MyTeamAdapter myTeamAdapter;

    // 초대해준 팀의 pid 값을 담는 list 다.
    ArrayList<String> inviteteam_pid_list = new ArrayList<String>();


    // 초대해준 사람의 이메일을 담는 변수다.
    ArrayList<String> inviter_list = new ArrayList<String>();

    // 초대해준 사람의 닉네임을 담는 변수다.
    ArrayList<String> inviter_nickname_list = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        Bundle bundle=getArguments();
        inviteteam_pid_list=bundle.getStringArrayList("team_pid");
//        inviter_list=bundle.getStringArrayList("Email");
//        inviter_nickname_list=bundle.getStringArrayList("nick_name");

        System.out.println("아오");
        System.out.println(inviteteam_pid_list.size());
        for(int i=0;i<inviteteam_pid_list.size();i++){
            System.out.println(inviteteam_pid_list.get(i));
        }


        return inflater.inflate(R.layout.fragmnet_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {



        //사용자가 팀을 추가하기위해 누르는 버튼
        RecyclerView my_team=(RecyclerView)view.findViewById(R.id.my_notification);

        //사용자의 팀 목록을 표현해주기 위한 리사이클러뷰다
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        my_team.setLayoutManager(mLinearLayoutManager);
        myteamArrayList = new ArrayList<>();
        myTeamAdapter = new MyTeamAdapter(myteamArrayList,getContext());
        my_team.setAdapter(myTeamAdapter);


        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_team.addItemDecoration(decoration_height);



        myTeamAdapter.setOnItemClickListener(new MyTeamAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyTeamAdapter.CustomViewHolder_MyTeam holder, View view, int position) {



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
}
