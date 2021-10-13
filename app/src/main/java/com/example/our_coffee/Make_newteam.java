package com.example.our_coffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Make_newteam extends AppCompatActivity {

    public static String TAG="Make_newteam";

    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // users3 컬렉션속 team 컬렉션의 문서 pid 값이다.
    String team_pid;
    Button make_team;
    EditText team_name;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //사용자가 팀 프로필 사진을 설정하기위한 뷰다
    CircleImageView team_profile;

    //갤러리에서 이미지를 갖고오는데 필요한 변수
    final int GET_GALLERY_IMAGE = 200;

    // 아래 두 코드는 FireBase Storage 에 사용자의 팀 프로필 이미지를 등록하기위해 사용한 코드다.
    StorageReference storageRef = storage.getReference();
    StorageReference user_team_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_newteam);


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("나의 팀 이름");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        make_team=(Button)findViewById(R.id.make_team);
        team_name=(EditText)findViewById(R.id.team_name);

        team_profile=(CircleImageView)findViewById(R.id.team_profile_url);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //팀 생성 버튼. 사용자가 설정한 팀 프로필사진, 팀명을 DB 에 저장한다.
        make_team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //만약 팀명을 입력하지 않았으면 입력하라고 Toast 메세지를 띄운다.
                if(team_name.getText().toString().equals("")){
                    Toast myToast = Toast.makeText(Make_newteam.this,"팀명을 입력하세요", Toast.LENGTH_SHORT);
                    myToast.show();
                }

                //사용자가 새로운 팀을 생성한다. (DB에 데이터 추가)
                else{
                    Toast myToast = Toast.makeText(Make_newteam.this,"팀 생성을 완료했습니다! 새로운 팀원들을 초대해보세요 :)", Toast.LENGTH_SHORT);
                    myToast.show();



                    Map<String, Object> data = new HashMap<>();
                    //data.put("team_member_name", Arrays.asList(currentUser.getEmail()));
                    data.put("team_name",team_name.getText().toString());
                    //users3 컬렉션에 새로운 팀의 정보를 추가한다.
                    db.collection("users3").document(currentUser.getEmail()).collection("team").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                user_team_img = storageRef.child("team_profile/"+documentReference.getId()+"/"+"team_profile.jpg");

                                Upload_team_profile();
                                team_pid=documentReference.getId();

                                Map<String, Object> team_data = new HashMap<>();
                                team_data.put("team_member_name", "none");

                                //팀을 만든다.
                                db.collection("team3").document(team_pid).set(team_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.v(TAG, "DocumentSnapshot successfully written!");

                                        //만들어진 팀에 나 자신을 추가한다.
                                        DocumentReference doc=db.collection("team3").document(team_pid);
                                        doc.update("team_member_name", FieldValue.arrayUnion(currentUser.getEmail()));
                                        finish();
                                    }})
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.v(TAG, "Error writing document", e);
                                        }
                                    });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });





                }

            }
        });

        //사용자가 자신의 팀 프로필 사진을 설정한다
        team_profile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });


    }       //onCreate end

    @Override
    //좌측 상단의 왼쪽으로 향하는 화살표 버튼. 이전 화면으로 되돌아 간다.
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home/*지정한 id*/){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            team_profile.setImageURI(selectedImageUri);

        }

    }

    //FireBase 에 이미지를 업로드하는 메소드. 팀의 프로필 사진을 저장시킨다. 염창근교수님의 구글 계정에 Storage 를 확인하면 이미지가 등록된 것을 확인할 수 있다.
    public void Upload_team_profile(){
        // Get the data from an ImageView as bytes
        team_profile.setDrawingCacheEnabled(true);
        team_profile.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) team_profile.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = user_team_img.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

}