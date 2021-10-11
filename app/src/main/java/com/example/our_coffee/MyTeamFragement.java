package com.example.our_coffee;
// 사용자의 팀 목록을 표한하기 위한 Framgnet 다.
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
import com.example.our_coffee.Utils.Team;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

//기능1 : 사용자의 팀목록을 표현하기위해서 아래와 같은 절차를 따른다.  (이 알고리즘은 더이상 사용하지 않는다.)
// 절차1-1.로그인한 사용자의 팀목록을 표현하는 uid 값을 FireStore 에서 갖고 와서 team_list_imgurl 에 담는다.
// 절차1-2.로그인한 사용자의 팀목록을 표현하는 uid 값을 FireStore 에서 갖고 와서 team_list_imgurl 에 담는다.
// 절차1-3.사용자들의 팀 정보를 표현하기 위해서 team_list_imgurl 에 담긴 uid 값을 바탕으로 FireBase storgae 에서 이미지를 불러온다.
// 절차1-4.사용자들의 팀 정보를 표현하기 위해서 team_list_name 에 담긴 팀명을 사용한다.

public class MyTeamFragement extends Fragment {

    public static String TAG="MyTeamFragment";
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //리사이클러뷰 관련 코드
    private ArrayList<Myteam> myteamArrayList;
    private MyTeamAdapter myTeamAdapter;

    Team team;

    @Override
    //Called to have the fragment instantiate(예시하다) its user interface view. This is optional, and non-graphical fragments can return null.
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("MyTeamFragment", "onCreaeView");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();





        //ListView에 띄우기 위해서 ArrayList<String> 생성
        //str_persons = new ArrayList<>();


        // -- Inflate the layout for this fragment --
        return inflater.inflate(R.layout.fragment_myteam, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //사용자가 팀을 추가하기위해 누르는 버튼
        ImageView imageView=(ImageView)view.findViewById(R.id.imageView);
        RecyclerView my_team=(RecyclerView)view.findViewById(R.id.my_team);

        //사용자의 팀 목록을 표현해주기 위한 리사이클러뷰다
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        my_team.setLayoutManager(mLinearLayoutManager);
        myteamArrayList = new ArrayList<>();
        myTeamAdapter = new MyTeamAdapter(myteamArrayList,getContext());
        my_team.setAdapter(myTeamAdapter);


        //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
        RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(60);
        my_team.addItemDecoration(decoration_height);

        //RequestActivity에서 전달한 번들 저장
        Bundle bundle = getArguments();
        team = bundle.getParcelable("my_team_list");

        //System.out.println("되지 않을까?");
        for(Myteam myteam:team.getMyteam()){
            String test = "팀명 " + myteam.getTeam_name() + "  이미지url " + myteam.getImage_url()+"  팀 pid "+myteam.getTeam_pid();
            System.out.println(test);

            Myteam data2;
            // 절차1-4.사용자들의 팀 정보를 표현하기 위해서 team_list_name 에 담긴 팀명을 사용한다.
            data2 = new Myteam(myteam.getTeam_name(),myteam.getImage_url(),myteam.getTeam_pid());

            myteamArrayList.add(0,data2); // RecyclerView의 마지막 줄에 삽입
            //myteamArrayList.add(data2); //마지막 줄에 삽입
            myTeamAdapter.notifyDataSetChanged();

        }

        myTeamAdapter.setOnItemClickListener(new MyTeamAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyTeamAdapter.CustomViewHolder_MyTeam holder, View view, int position) {
                Intent intent = new Intent(getContext(), Team_member.class);
                intent.putExtra("team_pid",myteamArrayList.get(position).getTeam_pid());

                startActivity(intent);

            }
        });



        //사용자가 새로운 팀을 생성하기위해 화면을 전환.
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Make_newteam.class);
                startActivity(intent);
            }
        });

    }       //onViewCreated end

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("MyTeamFragment", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyTeamFragment","onCreate");




    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("MyTeamFragment","onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MyTeamFragment","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MyTeamFragment","onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MyTeamFragment", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("MyTeamFragment", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("MyTeamFragment", "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("MyTeamFragment", "onDetach");
    }
}