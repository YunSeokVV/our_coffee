package com.example.our_coffee;
// 사용자의 팀 목록을 표한하기 위한 Framgnet 다.
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Collection;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    Bundle bundle;
    FirebaseFirestore db;

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView my_team;

    @Override
    //Called to have the fragment instantiate(예시하다) its user interface view. This is optional, and non-graphical fragments can return null.
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("MyTeamFragment", "onCreaeView");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();



        //ListView에 띄우기 위해서 ArrayList<String> 생성
        //str_persons = new ArrayList<>();


        // -- Inflate the layout for this fragment --
        return inflater.inflate(R.layout.fragment_myteam, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //사용자가 팀을 추가하기위해 누르는 버튼
        ImageView imageView=(ImageView)view.findViewById(R.id.imageView);
        my_team=(RecyclerView)view.findViewById(R.id.my_team);
        swipeRefreshLayout=view.findViewById(R.id.swipe_team_list);

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
        bundle = getArguments();
        if(bundle!=null){

            Load_existing_user();
        }





        //사용자가 새로운 팀을 생성하기위해 화면을 전환.
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), Make_newteam.class);
                i.putExtra("helloString", "helloString!!");

                startActivityForResult(i, 1);

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myteamArrayList.clear();
                //ReloadMyTeam();
                Load_existing_user();
                myTeamAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("MyTeamFragment","onActivityResult called");
        getActivity();
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //some code
            Log.v("MyTeamFragment",data.getStringExtra("reload"));

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

            if(reload.equals("reload_data")){
                Log.v("MyTeamFragment","조건문이 실행됨");
                AddNewTeam(team_name,team_pid);
            }

        }
    }

    // 팀목록이 있는 사용자가 로그인한 경우 팀목록을 리사이클러뷰로 표현한다.
    public void Load_existing_user(){
        Log.v(TAG,"Load_existing_user 호출됨");

        // 현재 DB에 사용자에게 소속된 팀이 존재하는지 판단해주는 변수다. 있으면 yes, 없으면 no 가 저장된다.
        String team_exist;
        team_exist=bundle.getString("team_exist");
        Log.v(TAG,"team_exist : "+team_exist);
        if(team_exist.equals("yes")){
            team = bundle.getParcelable("my_team_list");

            Log.v(TAG,team.getMyteam().toString());

            for(Myteam myteam:team.getMyteam()){
                String test = "팀명 " + myteam.getTeam_name() + "  이미지url " + myteam.getImage_url()+"  팀 pid "+myteam.getTeam_pid();


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
                    intent.putExtra("team_name",myteamArrayList.get(position).getTeam_name());

                    startActivity(intent);

                }
            });
        }

        else if(team_exist.equals("no")){
            Log.v(TAG,"아무런 팀 데이터가 없음");
        }

    }

    // 나의 팀 목록 화면을 새로고침 한다.
    public void ReloadMyTeam(){
        System.out.println("ReloadMyTeam 호출");
        //DB에서 갖고온 사용자의 팀의 pid값을 담는다
        ArrayList<String> team_list_pid = new ArrayList<String>();

        //DB에서 갖고온 사용자의 팀명을 담는다
        ArrayList<String> team_list_name = new ArrayList<String>();

        db.collection("users3").document(currentUser.getEmail()).collection("team").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //사용자의 팀목록을 데이터를 불러오는데 성공하면 list 에 담는다
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                team_list_pid.add(document.getId());
                                team_list_name.add(document.getString("team_name"));

                            }

                        }
                        //데이터를 불러오는데 실패한 경우
                        else {


                        }

                        //만약 유저가 이 앱을 처음 사용해서 어떠한 팀에도 소속되어 있지 않은경우의 조건문이다.
                        if(team_list_pid.size()==0){

                        }

                        // DB에 사용자의 팀목록 데이터가 존재하는 경우다.
                        else{

                            for(int i=0;i<team_list_pid.size();i++){
                                Myteam data;
                                data = new Myteam("work","work","work");
                                myteamArrayList.add(i,data);
                            }

                            // 사용자의 팀 목록을 리사이클러뷰로 표현해주는 코드
                            for(int i=0;i<team_list_pid.size();i++){

                                int finalI = i;
                                //팀명을 표현하는 변수다.
                                String tmp=team_list_name.get(i);
                                StorageReference storageRef = storage.getReference();

                                Log.v(TAG,"check-data");
                                Log.v(TAG,"team_list_pid.get(i) "+team_list_pid.get(i));
                                Log.v(TAG,"tmp "+tmp);


                                storageRef.child("team_profile/"+team_list_pid.get(i)+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Myteam data2;
                                        data2 = new Myteam(tmp,uri.toString(),team_list_pid.get(finalI));
                                        //myteamArrayList.add(0,data2);
                                        myteamArrayList.set(finalI,data2);

                                        if(team_list_pid.size()==myteamArrayList.size()){
                                            //Collections.reverse(myteamArrayList);
                                            //아래 두 코드는 리사이클러뷰의 아이템 간격을 조절해주는 코드다.
                                            myTeamAdapter.notifyDataSetChanged();
                                            swipeRefreshLayout.setRefreshing(false);
                                        }

                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //이미지를 storage 에서 불러오는데 실패한 경우
                                        Log.v(TAG,"excetion "+exception);
                                    }
                                });
                            }
                        }






                    }       //onComplete end
                });
    }

    // 새로운 팀을 개설했을 때 리사이클러뷰에 새로운 팀을 추가한다.
    public void AddNewTeam(String team_name,String team_pid){
        StorageReference storageRef = storage.getReference();
        Log.v(TAG,"AddNewTeam 메소드 호출");
        storageRef.child("team_profile/"+team_pid+"/"+"team_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v(TAG,"OnSuccess 라는데");
                Myteam data2;
                data2 = new Myteam(team_name,uri.toString(),team_pid);
                myteamArrayList.add(0,data2);
                //myteamArrayList.add(data2); //마지막 줄에 삽입
                myTeamAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지를 storage 에서 불러오는데 실패한 경우
                Log.v(TAG,"excetion "+exception);
            }
        });

    }


}