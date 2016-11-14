package com.capstone.locker.modify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.main.view.MainActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModifyActivity extends AppCompatActivity {

    @BindView(R.id.icon1)
    ImageView icon1;
    @BindView(R.id.icon2)
    ImageView icon2;
    @BindView(R.id.icon3)
    ImageView icon3;
    @BindView(R.id.pushOn)
    LinearLayout pushOnLayout;
    @BindView(R.id.pushOff)
    LinearLayout pushOffLayout;
    @BindView(R.id.pushOnText)
    TextView pushOnText;
    @BindView(R.id.pushOffText)
    TextView pushOffText;
    @BindView(R.id.moduleNickname)
    TextView moduleNickname;
    @BindView(R.id.pushArea)
    LinearLayout pushArea;
    @BindView(R.id.modifyOwnerPwd)
    EditText modifyOwnerPwd;
    @BindView(R.id.modifyGuestPwd)
    EditText modifyGuestPwd;
    @BindView(R.id.requestAuth)
    TextView requestAuth;
    @BindView(R.id.authPwd)
    EditText authPwd;
    @BindView(R.id.moduleIdentiName)
    TextView textModuleidentiName;

    DbOpenHelper mDbOpenHelper;
    Boolean authCheck = false;
    Boolean bleCheck = false;

    String moduleId;
    int chooseIcon = 1;  // 1,2,3
    int choosePushCheck = 0; // 0 : on , 1 : off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_modify);

        ButterKnife.bind(this);


        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)) {

            bleCheck = true;

            if(ApplicationController.getInstance().mDeviceName == null)
                textModuleidentiName.setText(R.string.unknown_device);
            else
                textModuleidentiName.setText(ApplicationController.getInstance().mDeviceName);

        }
        else{
            textModuleidentiName.setText("연결된 장치 없음");
            bleCheck = false;
        }



        Intent intent = getIntent();
        moduleId = intent.getExtras().getString("id");

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;
        ItemData getItem = mDbOpenHelper.DbDetail(moduleId);


        moduleNickname.setText(getItem.nickName);

        if (getItem.icon==1){
            chooseIcon = 1;
            icon1.setBackgroundResource(R.drawable.border_circle_background_empty);
        }
        else if (getItem.icon==2){
            chooseIcon = 2;
            icon2.setBackgroundResource(R.drawable.border_circle_background_empty);
        }
        else if (getItem.icon==3){
            chooseIcon = 3;
            icon3.setBackgroundResource(R.drawable.border_circle_background_empty);
        }

        if(getItem.qualificaion.equals("Owner")){
            if(getItem.pushcheck == 0){
                choosePushCheck = 0;
                pushOnLayout.setBackgroundResource(R.drawable.border_circle_background);
                pushOnText.setTextColor(Color.parseColor("#ffffff"));
                pushOffLayout.setBackgroundResource(0);
                pushOffText.setTextColor(Color.parseColor("#000000"));
            }
            else{
                choosePushCheck = 1;
                pushOnLayout.setBackgroundResource(0);
                pushOnText.setTextColor(Color.parseColor("#000000"));
                pushOffLayout.setBackgroundResource(R.drawable.border_circle_background);
                pushOffText.setTextColor(Color.parseColor("#ffffff"));
            }
        }
        else{
            pushArea.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }


    @OnClick(R.id.requestAuth)
    public void requestAuth(){

        if(authPwd.getText().length() == 0){
            Toast.makeText(getApplicationContext(),"비밀번호 입력해주세요",Toast.LENGTH_SHORT).show();
        }
        else{

            if(bleCheck == false){
                Toast.makeText(getApplicationContext(),"연결된 장치가 없습니다.\n연결 후 시도해주세요",Toast.LENGTH_SHORT).show();
            }
            else{
                //성공 시
                authCheck = true;
                requestAuth.setText("인증완료");
            }
        }

    }

    @OnClick(R.id.completeRegister)
    public void completeRegisterMethod(){
        if(modifyOwnerPwd.length() != 0 || modifyGuestPwd.length() != 0 ){
            if(authCheck == false){
                Toast.makeText(getApplicationContext(),"비밀번호를 변경하기 위해서는 인증이 필요합니다.",Toast.LENGTH_SHORT).show();
            }
            else{
                //비밀번호를 포함하여 변경 시

                AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

                // 여기서 부터는 알림창의 속성 설정
                builder.setMessage("수정하시겠습니까?")        // 메세지 설정
                        .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton){
                                ItemData item = new ItemData();

                                item.nickName = moduleNickname.getText().toString();
                                item.icon = chooseIcon;
                                item.pushcheck = choosePushCheck;
                                item.ownerPwd = modifyOwnerPwd.getText().toString();
                                item.guestPwd = modifyGuestPwd.getText().toString();

                                mDbOpenHelper.DbUpdate(moduleId,item);

                                Toast.makeText(getApplicationContext(),"수정 완료",Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ModifyActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton){
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기


            }
        }
        else{
            // 비밀번호를 제외한 정보만 변경 시

            AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

            // 여기서 부터는 알림창의 속성 설정
            builder.setMessage("수정하시겠습니까?")        // 메세지 설정
                    .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                    .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        // 확인 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton){
                            ItemData item = new ItemData();

                            item.nickName = moduleNickname.getText().toString();
                            item.icon = chooseIcon;
                            item.pushcheck = choosePushCheck;

                            mDbOpenHelper.DbUpdate(moduleId,item);

                            Toast.makeText(getApplicationContext(),"수정 완료",Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ModifyActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                        // 취소 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton){
                            dialog.cancel();
                        }
                    });

            AlertDialog dialog = builder.create();    // 알림창 객체 생성
            dialog.show();    // 알림창 띄우기

        }

    }

    @OnClick(R.id.icon1)
    public void chooseicon1(){
        chooseIcon = 1;
        icon1.setBackgroundResource(R.drawable.border_circle_background_empty);
        icon2.setBackgroundResource(0);
        icon3.setBackgroundResource(0);
    }

    @OnClick(R.id.icon2)
    public void chooseicon2(){
        chooseIcon = 2;
        icon1.setBackgroundResource(0);
        icon2.setBackgroundResource(R.drawable.border_circle_background_empty);
        icon3.setBackgroundResource(0);
    }

    @OnClick(R.id.icon3)
    public void chooseicon3(){
        chooseIcon = 3;
        icon1.setBackgroundResource(0);
        icon2.setBackgroundResource(0);
        icon3.setBackgroundResource(R.drawable.border_circle_background_empty);
    }

    @OnClick(R.id.pushOn)
    public void pushOnCheck(){
        choosePushCheck = 0;
        pushOnLayout.setBackgroundResource(R.drawable.border_circle_background);
        pushOnText.setTextColor(Color.parseColor("#ffffff"));
        pushOffLayout.setBackgroundResource(0);
        pushOffText.setTextColor(Color.parseColor("#000000"));
    }

    @OnClick(R.id.pushOff)
    public void pushOFFCheck(){
        choosePushCheck = 1;
        pushOnLayout.setBackgroundResource(0);
        pushOnText.setTextColor(Color.parseColor("#000000"));
        pushOffLayout.setBackgroundResource(R.drawable.border_circle_background);
        pushOffText.setTextColor(Color.parseColor("#ffffff"));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        builder.setMessage("수정을 취소하시겠습니까?")        // 메세지 설정
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }
}
