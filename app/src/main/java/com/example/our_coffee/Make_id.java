package com.example.our_coffee;
//사용자가 회원가입을 하기위한 화면이다
import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Make_id extends Activity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //사용자가 자신의 아이디를 입력한다
    EditText put_id;
    //사용자가 자신의 패스워드를 입력한다
    EditText put_pw;
    //사용자가 자신의 패스워드를 체크한다
    EditText check_pw;

    //사용자가 입력한 값을 토대로 회원가입을 진행한다.
    Button make_id;

    //FireBase 사용자 인증을 하기위한 인스턴스
    private FirebaseAuth mAuth;

    String TAG="Make_id";

    // 회원가입 화면을 종료하기 위한 객체다
    LoginActivity loginActivity =(LoginActivity) LoginActivity.loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maek_id);

        put_id=findViewById(R.id.editText);
        put_pw=findViewById(R.id.editText2);
        check_pw=findViewById(R.id.editText3);
        make_id=findViewById(R.id.make_id);

        //사용자가 입력한 아이디, 패스워드를 기반으로 회원가입을 진행한다.
        make_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사용자가 입력하지 않은 항목이 존재하는 경우
                if(put_id.getText().toString().equals("") || put_pw.getText().toString().equals("") || check_pw.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "입력하지 않은 항목이 존재합니다", Toast.LENGTH_SHORT).show();
                }
                //사용자가 각 항목에 입력을 다 한경우. 회원가입을 진핸한다.
                else{
                    if(put_pw.getText().toString().equals(check_pw.getText().toString())){
                        Make_Account(put_id.getText().toString(),put_pw.getText().toString());

                    }

                    //사용자가 패스워드를 잘못 입력한 경우
                    else if(!put_pw.getText().toString().equals(check_pw.getText().toString())){
                        Toast.makeText(getApplicationContext(), "패스워드가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    }
                }




            }
        });




    }

    //사용자의 계정을 만드는 역할을 해주는 메소드다. FireBase 의 Authentication 을 사용한다. '염창근' 계정의 FireBase에서 MatchingApp-Tinder 에 저장이 된다.
    public void Make_Account(String email,String password){
        System.out.println("email : "+email);
        System.out.println("password : "+password);

        mAuth = FirebaseAuth.getInstance();

        //사용자의 계정은 반드시 email 형태를 맞춰야 하며 정책에 따라 패스워드는 반드시 비밀번호가 6자리 이상이여야 한다.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.v("maek_id", "회원가입 완료");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "회원가입 완료!", Toast.LENGTH_SHORT).show();
                            finish();
                            loginActivity.finish();
                            //사용자의 팀 목록을 보여주는 화면으로 이동한다
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                            //만약 사용자가 로그인한 상태라면 다음 화면으로 자동으로 넘어간다.
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            //DB 에 사용자의 정보를 저장한다. (염창근 교수님의 FireStore 를 사용하며 MatchingApp-Tinder 프로젝트를 사용한다.)
                            Map<String, Object> user_data = new HashMap<>();
                            user_data.put("my_coffee", "아이스 아메리카노");
                            user_data.put("invited_team", Arrays.asList());
                            user_data.put("nick_name", currentUser.getEmail());
                            user_data.put("my_coffee_option","연한 농도");

                            db.collection("users3").document(currentUser.getEmail())
                                    .set(user_data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.v(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.v(TAG, "Error writing document", e);
                                        }
                                    });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("maek_id", "createUserWithEmail:failure", task.getException());
                            System.out.println("에러 : "+task.getException());

                            //만약 사용자가 이미 사용하고 있는 이메일을 사용하려 한 경우
                            if(task.getException().toString().equals("com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.")){
                                Toast.makeText(getApplicationContext(), "이미 사용하고 있는 이메일 계정입니다", Toast.LENGTH_SHORT).show();
                            }

                            //만약 사용자가 6자리 이상의 비밀번호를 사용하려고 하지 않은 경우
                            else if(task.getException().toString().equals("com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]")){
                                Toast.makeText(getApplicationContext(), "6자리 이상의 패스워드를 사용해야 합니다", Toast.LENGTH_SHORT).show();
                            }

                            else if(task.getException().toString().equals("com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.")){
                                Toast.makeText(getApplicationContext(), "이메일 주소가 옳바른 형태가 아닙니다", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });

    }

}