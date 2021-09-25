package com.example.our_coffee;
// 팀에서 새로운 팀원을 추가할 때 사용하는 화면이다.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Add_new_member extends AppCompatActivity {

    //사용자가 초대하고 싶은 멤버의 이메일을 입력하는 창이다.
    EditText member_name;
    Toolbar toolbar;
    //검색결과로 나온 사용자의 프로필 사진을 보여주는 이미지뷰.
    ImageView user_profile;
    //검색결과로 나온 사용자의 닉네임을 보여주기 위한 텍스트뷰.
    TextView user_nick_name;
    //검색결과가 없다는 것을 알려주기 위한 텍스트뷰.
    TextView search_not_found;
    //검색한 팀원을 초대하기 위한 버튼.
    Button invite_member;

    FirebaseFirestore db;

    public String TAG="Add_new_member";

    // 초대하고 싶은 사용자의 닉네임을 저장하는 변수다.
    String member_email;

    // 현재 초대하려는 팀의 pid 값이 담겨있다.
    String team_pid;

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_member);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        member_name=(EditText)findViewById(R.id.member_name);
        user_profile=(ImageView)findViewById(R.id.user_profile);
        user_nick_name=(TextView)findViewById(R.id.user_nick_name);
        search_not_found=(TextView)findViewById(R.id.search_not_found);
        invite_member=(Button)findViewById(R.id.invite_member);
        setSupportActionBar(toolbar);
        //툴바의 뒤로가기 버튼을 활성화 한다
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 팀안의 모든 유저들의 이메일 정보를 DB에서 불러온다.
        db = FirebaseFirestore.getInstance();

        user_profile.setVisibility(View.INVISIBLE);
        user_nick_name.setVisibility(View.INVISIBLE);
        search_not_found.setVisibility(View.INVISIBLE);
        invite_member.setVisibility(View.INVISIBLE);

        Intent intent = getIntent(); /*데이터 수신*/
        team_pid= intent.getExtras().getString("invite_team_pid"); /*String형*/

        System.out.println("team_pid 확인");
        System.out.println(team_pid);




        //팀원 초대하기 버튼이다. 초대하고 싶은 사용자의 필드값에 팀의 pid 값을 저장 시킨다.
        invite_member.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) {
            DocumentReference docRef = db.collection("users3").document(member_email);
            docRef.update("invited_team", FieldValue.arrayUnion(team_pid));

            Toast myToast = Toast.makeText(getApplicationContext(),"해당 사용자에게 초대 수락 메세지를 보냈습니다 :)", Toast.LENGTH_SHORT);
            myToast.show();
            finish();
        }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.confirm_tool_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //뒤로가기 버튼(왼쪽 화살표)을 누르면 이전화면으로 간다
        if(item.getItemId()==android.R.id.home/*지정한 id*/){
            finish();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.confirm_button:

                Search_user();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //확인 버튼을 눌렀을떄 메소드
    public void Search_user(){

        // 사용자가 아무런 값도 입력하지 않은 경우
        if(member_name.getText().toString().equals("")){
            Toast myToast = Toast.makeText(this.getApplicationContext(), "찾고싶은 사용자의 이메일을 입력해주세요", Toast.LENGTH_SHORT);
            myToast.show();
        }

        // 검색은 했지만 사용자가 존재하지 않는 경우, 검색 했을때 사용자가 존재하는 경우 두가지 경우가 다 포함됨.
        else {
            DocumentReference docRef = db.collection("users3").document(member_name.getText().toString());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();





                        //검색 결과 사용자가 존재하는 경우
                        if (document.exists()) {

                            user_nick_name.setText(String.valueOf(document.get("nick_name")));

                            //사용자가 자신의 프로필 사진을 DB에서 불러오기 위한 코드다.
                            StorageReference storageRef = storage.getReference();
                            // 절차1-3.사용자들의 팀 정보를 표현하기 위해서 team_list_imgurl 에 담긴 uid 값을 바탕으로 FireBase storgae 에서 이미지를 불러온다.
                            storageRef.child("user_profile/"+member_name.getText().toString()+"_profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    System.out.println("check-data onSuccess");
                                    System.out.println(uri.toString());

                                    Glide.with(getApplicationContext()).load(uri).into(user_profile);
                                    user_profile.setClipToOutline(true);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    System.out.println("check-data onFailure");
                                    System.out.println(exception.toString());


                                }
                            });

                            search_not_found.setVisibility(View.INVISIBLE);
                            user_profile.setVisibility(View.VISIBLE);
                            user_nick_name.setVisibility(View.VISIBLE);
                            invite_member.setVisibility(View.VISIBLE);

                            member_email=member_name.getText().toString();

                        }

                        // 검색결과 사용자가 없는 경우다.
                        else {
                            Toast myToast = Toast.makeText(Add_new_member.this, "존재하지 않는 사용자 입니다.", Toast.LENGTH_SHORT);
                            myToast.show();

                            search_not_found.setVisibility(View.VISIBLE);
                            user_profile.setVisibility(View.INVISIBLE);
                            user_nick_name.setVisibility(View.INVISIBLE);
                            invite_member.setVisibility(View.INVISIBLE);


                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

    }
}