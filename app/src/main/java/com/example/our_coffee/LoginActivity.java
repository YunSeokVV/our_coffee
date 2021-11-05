package com.example.our_coffee;
// 이 파일은 로그인 화면이다. 카카오톡, 네이버, 페이스북 아이디로 로그인 할 수 있다.

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView make_id;

    //사용자가 로그인하기 위해 자신의 이메일을 입력한다
    EditText put_id;

    //사용자가 로그인 하기 위해 자신의 비밀번호를 입력한다
    EditText put_pw;

    Button login_btn;

    //다른 액티비티에서도 이 액티비티를 종료시키 수 있게끔 하기위해 static 객체를 선언했다
    public static LoginActivity loginActivity;

    public static String TAG="MainActivity";

    private FirebaseAuth mAuth;

    Context context;

    //아래 코드들은 네이버아이디로 로그인 API 를 사용하는데 필요한 값들이다.
    //client 정보
    String OAUTH_CLIENT_ID = "3sLZy8l4OFIKQRIxm9p8";
    String OAUTH_CLIENT_SECRET = "BZu3oceTJl";
    String OAUTH_CLIENT_NAME = "우리의 커피";
    OAuthLogin mOAuthLoginModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginActivity = LoginActivity.this;

        mAuth = FirebaseAuth.getInstance();

        context=this;

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

        NaverAPILogin();

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

    //네이버 아이디로 로그인 기능을 진행하는 메소드다.
    private void NaverAPILogin() {
        //Context mContext;

        OAuthLoginButton mOAuthLoginButton;

        //초기화
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(context, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);

        //custom img로 변경시 사용
        mOAuthLoginButton.setBgResourceId(R.drawable.btn_circle_icon);
    }

    OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginModule.getAccessToken(context);
                String refreshToken = mOAuthLoginModule.getRefreshToken(context);
                long expiresAt = mOAuthLoginModule.getExpiresAt(context);
                String tokenType = mOAuthLoginModule.getTokenType(context);
                new RequestApiTask(context, mOAuthLoginModule).execute();

            } else {
                String errorCode = mOAuthLoginModule.getLastErrorCode(context).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(context);
                Toast.makeText(context, "errorCode:" + errorCode
                        + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 네이버 아이디로 로그인이 성공하고 난 뒤에 앱에서 필요로 하는 사용자의 정보를 응답값으로 받아오는 AsyncTask 다.
    //사용자의 이름, 프로필 사진, 이메일 을 필요로 한다.
    public class RequestApiTask extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final OAuthLogin mOAuthLoginModule;
        public RequestApiTask(Context mContext, OAuthLogin mOAuthLoginModule) {
            this.mContext = mContext;
            this.mOAuthLoginModule = mOAuthLoginModule;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginModule.getAccessToken(mContext);
            return mOAuthLoginModule.requestApi(mContext, at, url);
        }

        protected void onPostExecute(String content) {
            try {
                JSONObject loginResult = new JSONObject(content);
                if (loginResult.getString("resultcode").equals("00")){
                    JSONObject response = loginResult.getJSONObject("response");
                    String id = response.getString("id");
                    String email = response.getString("email");
                    String name = response.getString("name");
                    String nickname = response.getString("nickname");
                    String profile_image = response.getString("profile_image");

                    Log.v(TAG,"네이버 아이디로 로그인한 후 받아온 데이터");
                    Log.v(TAG,"id : "+id);
                    Log.v(TAG,"email : "+email);
                    Log.v(TAG,"name : "+name);
                    Log.v(TAG,"nickname : "+nickname);
                    Log.v(TAG,"profile_image : "+profile_image);

                    //check point 여기서 부터 해야할 일을 정의
                    CheckRegister(email,name,profile_image);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 소셜네트워크 로그인을 진행했을 회원가입여부를 묻는 다이얼로그 창
    public void CheckRegister(String email,String name,String profile_image)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("회원 가입을 진행합니다").setMessage("우리의 커피 서비스를 사용하기 위해서는 회원 가입이 필수입니다. 회원가입을 진행하시겠습니까?");

        builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getApplicationContext(), "회원 가입이 완료 되었습니다!!", Toast.LENGTH_SHORT).show();

                Make_Account(email,name,profile_image);

                //사용자 팀 목록 화면으로 이동한다.
                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(intent);

            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //사용자의 계정을 만드는 역할을 해주는 메소드다. FireBase 의 Authentication 을 사용한다. '염창근' 계정의 FireBase에서 MatchingApp-Tinder 에 저장이 된다.
    public void Make_Account(String email,String name,String profile_image){
        Log.v(TAG,"Make_Account");
        Log.v(TAG,email);
        Log.v(TAG,name);
        Log.v(TAG,profile_image);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //사용자의 계정은 반드시 email 형태를 맞춰야 하며 정책에 따라 패스워드는 반드시 비밀번호가 6자리 이상이여야 한다.
        mAuth.createUserWithEmailAndPassword(email, "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            finish();
                            loginActivity.finish();
                            //사용자의 팀 목록을 보여주는 화면으로 이동한다
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                            //만약 사용자가 로그인한 상태라면 다음 화면으로 자동으로 넘어간다.
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            //DB 에 사용자의 정보를 저장한다. (염창근 교수님의 FireStore 를 사용하며 MatchingApp-Tinder 프로젝트를 사용한다.)
                            Map<String, Object> user_data = new HashMap<>();
                            user_data.put("my_coffee", "아메리카노");
                            user_data.put("invited_team", Arrays.asList());
                            user_data.put("nick_name", name);
                            user_data.put("my_coffee_option","연한 농도");

                            db.collection("users3").document(currentUser.getEmail())
                                    .set(user_data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.v(TAG, "DocumentSnapshot successfully written!");

                                            Toast.makeText(getApplicationContext(), "회원가입 완료!", Toast.LENGTH_SHORT).show();
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
