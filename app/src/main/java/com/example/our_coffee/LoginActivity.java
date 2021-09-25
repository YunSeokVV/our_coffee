package com.example.our_coffee;
// 이 파일은 로그인 화면이다. 카카오톡, 네이버, 페이스북 아이디로 로그인 할 수 있다.

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {
    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView make_id;

    //사용자가 로그인하기 위해 자신의 이메일을 입력한다
    EditText put_id;

    //사용자가 로그인 하기 위해 자신의 비밀번호를 입력한다
    EditText put_pw;

    Button login_btn;

    Button button;

    //다른 액티비티에서도 이 액티비티를 종료시키 수 있게끔 하기위해 static 객체를 선언했다
    public static LoginActivity loginActivity;

    public static String TAG="MainActivity";

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginActivity = LoginActivity.this;

        mAuth = FirebaseAuth.getInstance();



        //button.setBackgroundResource(R.drawable.round);

        //만약 사용자가 로그인한 상태라면 다음 화면으로 자동으로 넘어간다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            System.out.println("사용자 정보 확인");
            System.out.println(currentUser.getUid());
            System.out.println(currentUser.getEmail());

            //사용자 팀 목록 화면으로 이동한다.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            //reload();
        }

        put_id=(EditText)findViewById(R.id.editText);
        put_pw=(EditText)findViewById(R.id.editText2);
        make_id=(TextView)findViewById(R.id.make_id);
        login_btn=(Button)findViewById(R.id.login_btn);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //로그인 버튼. 사용자의 입력값을 기반으로 로그인 한다.
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //입력하지 않은 항목이 존재하는 경우
                if(put_id.getText().toString().equals("")||put_pw.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "입력하지 않은 항목이 존재합니다", Toast.LENGTH_SHORT).show();
                }
                //로그인을 진행한다
                else{
                    Login(put_id.getText().toString(),put_pw.getText().toString());

                }
            }
        });


        //회원가입 화면으로 넘어간다
        make_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Make_id.class);
                startActivity(intent);
            }
        });


    }       //onCreate end


    @Override
    public void onStart() {
        super.onStart();

    }       //onStart end

    // 사용자가 앱에 로그인한다. 로그인을 위해서 FireBase 의 Authentication 기능을 사용했다. '염창근' 계정의 MatchingApp-Tinder 를 참조 할 것.
    public void Login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    //로그인 성공
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            //사용자 팀 목록 화면으로 이동한다.
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                            //updateUI(user);
                        }

                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            if(task.getException().toString().equals("com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted.")){
                                Toast.makeText(getApplicationContext(), "존재하지 않는 계정입니다", Toast.LENGTH_SHORT).show();
                            }
                            else if(task.getException().toString().equals("com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.")){
                                Toast.makeText(getApplicationContext(), "잘못된 패스워드 입니다", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }


}
