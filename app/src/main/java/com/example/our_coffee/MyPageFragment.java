package com.example.our_coffee;
// 마이페이지를 표현한 Fragmnet 이다. 마이페이지 기능이 작동하기 위해서는 Figma 에서 적었던 기획서를 확인하면 된다.
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.our_coffee.Utils.SelectCoffeeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class MyPageFragment extends Fragment {

    //MainActivity 에서 온 데이터를 받기위해 bundle 객체 사용
    Bundle bundle;

    // MainActivity 에 사용자의 프로필 정보가 바꼈다는 사실을 전달하기 위한 객체다.
    UserProfileChanged userProfileChanged;

    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    //FireBase 에 이미지를 저장하기위해 선언한 인스턴스
    FirebaseStorage storage = FirebaseStorage.getInstance();
    public static String TAG="MyPageFragment";

    //서버에서 사용자가 이전에 설정한 값들을 받아왔을 때 담는 변수들이다. (사용자가 자주 마시는 커피, 사용자 닉네임, 사용자 커피 옵션, 사용자 프로필사진 uri)
    String user_nick_name;
    String user_frequently_coffee;
    String user_coffee_detail_option;
    String img_uri;

    //갤러리에서 이미지를 갖고오는데 필요한 변수
    final int GET_GALLERY_IMAGE = 200;

    // 아래 두 코드는 FireBase Storage 에 사용자의 팀 프로필 이미지를 등록하기위해 사용한 코드다.
    StorageReference user_team_img;


    // 사용자가 자신의 프로필 사진을 변경했는지 여부를 판단해주는 변수다. 이 변수 값이 비어 있다면 사용자는 자신의 프로필 사진을 변경하지 않은 거다.
    String profile_path="";

    CircleImageView team_profile_url;
    Button save_btn;
    EditText coffee_detail_option;
    EditText frequently_coffee;
    EditText nick_name;

    // DB에서 사용자의 정보를 업데이트 하기 전까지 dialog 수행.
    ProgressDialog dialog;

    @Override
    //프래그먼트가 자신의 인터페이스를 처음 그리기 위해 호출합니다. View를 반환해야 합니다. 
    // 이 메서드는 프래그먼트의 레이아웃 루트이기 때문에 UI를 제공하지 않는 경우에는 null을 반환하면 됩니다.
    //참고.  onCreateView()에서 view가 초기화중이기 때문에 충돌이 일어날 수 있습니다. 그래서 onCreateView()에서 레이아웃을 inflate하지만 findViewById 등을
    //사용하여 초기화 하지는 않아야 합니다. 왜나하면 몇가지의 뷰들은 적절히 초기화 되어있지 않았을 수 있기 때문입니다. 그래서 View가 완전히 생성되었을때 호출되는
    // onViewCreated() 메서드에서 findViewById를 사용하여 초기화 해야 합니다. onViewCreated는 완전히 View가 생성되었음을 확신시켜줍니다.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bundle = getArguments();
        if(bundle!=null){
            Log.v(TAG,"bundle null 아님");

            user_nick_name=bundle.getString("user_nick_name");
            user_frequently_coffee=bundle.getString("user_frequently_coffee");
            user_coffee_detail_option=bundle.getString("user_coffee_detail_option");
            img_uri=bundle.getString("image_uri");

//            Log.v(TAG,user_nick_name);
//            Log.v(TAG,user_frequently_coffee);
//            Log.v(TAG,user_coffee_detail_option);
//            Log.v(TAG,img_uri);
        }
        else{
            Log.v(TAG,"bundle null 임");
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // 여기서 뷰를 반환하네. 만약 반환하지 않으면 null 값을 반환하는거구만. ㅇㅋ+
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //사용자가 자신의 닉네임을 설정할 수 있다.
        nick_name=(EditText) view.findViewById(R.id.nick_name);
        //사용자가 자신이 자주 먹는 커피를 표현하는 EditText 다.
        frequently_coffee=(EditText) view.findViewById(R.id.frequently_coffee);
        //사용자가 커피를 살때 커피 옵션을 설정한다.
        coffee_detail_option=(EditText) view.findViewById(R.id.coffee_detail_option);
        //변경사항을 설정한뒤 사용자가 설정사항을 저장시키기 위한 버튼이다.
        save_btn=(Button) view.findViewById(R.id.save_btn);
        //사용자가 자신의 프로필 사진을 설정하기위한 이미지뷰다.
        team_profile_url=(CircleImageView) view.findViewById(R.id.team_profile_url);

        //사용자가 이전에 설정한 자신의 정보들을 DB에서 불러온다.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //사용자가 자신의 프로필 사진을 DB에서 불러오기 위한 코드다.
        StorageReference storageRef = storage.getReference();

        if(img_uri==null){
            Log.v(TAG,"img_uri null 임");
            team_profile_url.setImageResource(R.drawable.basic_profile);
            nick_name.setText(user_nick_name);
            frequently_coffee.setText(user_frequently_coffee);
            coffee_detail_option.setText(user_coffee_detail_option);
        }
        else{
            Log.v(TAG,"img_uri null 아님");
            Glide.with(getContext()).load(img_uri).into(team_profile_url);
            nick_name.setText(user_nick_name);
            frequently_coffee.setText(user_frequently_coffee);
            coffee_detail_option.setText(user_coffee_detail_option);
        }

        frequently_coffee.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG,"닉네임 변경");

                Intent intent = new Intent(getContext(), SelectCoffeeActivity.class);
                startActivity(intent);
            }
        });



        //사용자가 자신의 팀 프로필 사진을 설정한다
        team_profile_url.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG,"이미지뷰 클릭");

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.putExtra("reload", "back_btn");
                //setResult(RESULT_OK,returnIntent);
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });





    //자신의 프로필을 수정한 사용자가 저장할 때 사용하는 버튼.
    save_btn.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
            Log.v(TAG,"저장하기 버튼 클릭");
            Upload_dialog();


            // 만약 사용자가 자신의 닉네임을 변경했다면 DB에 수정된 내용을 반영한다.
            db.collection("users3").document(currentUser.getEmail()).update("nick_name",nick_name.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // 만약 사용자가 자신이 자주 먹는 커피를 변경했다면 DB에 수정된 내용을 반영한다.
                    db.collection("users3").document(currentUser.getEmail()).update("my_coffee",frequently_coffee.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // 만약 사용자가 자신의 커피 옵션을 변경했다면 DB에 수정된 내용을 반영한다.
                            db.collection("users3").document(currentUser.getEmail()).update("my_coffee_option",coffee_detail_option.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // 사용자가 자신의 프로필 사진을 변경하지 않은 경우
                                    if(profile_path.equals("")){
                                        System.out.println("프로필 사진을 변경하지 않았다.");
                                        dialog.dismiss();
                                    }
                                    // 사용자가 자신의 프로필 사진을 변경한 경우
                                    else if(!profile_path.equals("")){
                                        Log.v(TAG,"프로필 사진을 변경했다.");
                                        user_team_img = storageRef.child("user_profile/"+currentUser.getEmail()+"_profile.jpg");
                                        Upload_my_profile();
                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.v(TAG, "Error writing document", e);
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v(TAG, "Error writing document", e);
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.v(TAG, "Error writing document", e);
                }
            });



            //todo : 자주 먹는 커피, 자주 먹는 커피 옵션을 수정하게끔 하는 기능은 아직 구현하지 못했다. 하셈.

        } });







    }       //onViewCreated end

    @Override
    //프래그먼트가 액티비티와 연결되어 있었던 경우 호출됩니다. 여기서 액티비티가 전달됩니다.
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof UserProfileChanged){
            userProfileChanged=(UserProfileChanged)context;
        }else{
            throw new RuntimeException(context.toString()+"must implent userProfileChanged");
        }
    }

    //프래그먼트를 생성할 때 호출합니다.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FragmentB","onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("FragmentB","onActivityCreated");
    }

    @Override
    //액티비티가 시작됨 상태에 들어가면 이 메서드를 호출합니다. 사용자에게 프래그먼트가 보이게 되고, 이 메서드에서 UI를 관리하는 코드를 초기화 합니다.
    // 이 메서드는 매우 빠르게 완료되고, 완료되면 Resumed(재개)상태로 들어가 onResume() 메서드를 호출합니다.
    public void onStart() {
        super.onStart();
        Log.d("FragmentB","onStart");
    }

    @Override
    //이 상태에 들어갔을 때 사용자와 상호작용 합니다. 어떤 이벤트가 발생하여 포커스가 떠날 때 까지 이 상태에 머무릅니다.
    // 프로그램이 일시정지되어 onPause()를 호출하고 다시 재개되면 onResume() 메서드를 다시 호출합니다. 재개 상태로 전환될때마다 필요한 초기화 작업들을 수행해야 합니다.
    public void onResume() {
        super.onResume();
        Log.d("FragmentB","onResume");
    }

    @Override
    //사용자가 프래그먼트를 떠나면 첫번 째로 이 메서드를 호출합니다. 사용자가 돌아오지 않을 수도 있으므로 여기에 현재 사용자 세션을 넘어 지속되어야 하는 변경사항을 저장합니다.
    public void onPause() {
        super.onPause();
        Log.d("FragmentB", "onPause");
    }

    @Override
    //onStop()을 사용하여 CPU를 많이 소모하는 종료 작업을 실행해야 합니다. 예를 들면 그 작업은 데이터 베이스에 저장할 시기가 될 수도 있습니다.
    public void onStop() {
        super.onStop();
        Log.d("FragmentB", "onStop");
    }

    @Override
    //프래그먼트와 연결된 View Layer가 제거되는 중일 때 호출됩니다. 여기서 view 를 제거.
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("FragmentB", "onDestroyView");
    }

    @Override
    //Called when the fragment is no longer in use. This is called after onStop() and before onDetach().
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    //프래그먼트가 액티비티와 연결이 끊어지는 중일 때 호출됩니다.
    public void onDetach() {
        super.onDetach();
        userProfileChanged=null;
    }

    @Override
    //주의 : onActivityResult 는 Framgent 클래스에서 deprecated 됐지만 여전히 사용되는 메소드다. 일단 참고.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,"onActivityResult 로그");


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            team_profile_url.setImageURI(selectedImageUri);

            //사용자가 자신의 프로필 사진을 설정한 경우 프로필 사진을 변경할 수 있다.
            profile_path=selectedImageUri.getPath();


        }

    }

    //FireBase 에 이미지를 업로드하는 메소드. 사용자의 프로필 사진을 저장한다. 염창근교수님의 구글 계정에 Storage 를 확인하면 이미지가 등록된 것을 확인할 수 있다.
    public void Upload_my_profile(){
        // Get the data from an ImageView as bytes
        team_profile_url.setDrawingCacheEnabled(true);
        team_profile_url.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) team_profile_url.getDrawable()).getBitmap();
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
                userProfileChanged.UserProfileChanged();
                Toast myToast = Toast.makeText(getContext(),"프로필 정보를 수정하였습니다 :)", Toast.LENGTH_SHORT);
                myToast.show();

                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    // 마이페이지 프레그먼트 화면에서 메인액티비티로 데이터를 전달하기 위해 사용했다.
    //마이페이지 프레그먼트가 MainActivity 에게 프로필 정보를 수정했다는 사실을 알리기 위해서 이 인터페이스를 사용했다.
    public interface  UserProfileChanged{
        void UserProfileChanged();
    }

    // 프로필 정보를 수정하고 DB에 데이터가 안정적으로 저장될 때 까지 다이얼로그 진행
    public void Upload_dialog(){
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("프로필 변경중");
        dialog.setCancelable(false);
        dialog.show();
    }

}