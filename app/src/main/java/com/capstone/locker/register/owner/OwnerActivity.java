package com.capstone.locker.register.owner;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OwnerActivity extends AppCompatActivity {

    @BindView(R.id.registerDate)
    TextView register_date;
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
    @BindView(R.id.moduleIdentiName)
    TextView textModuleidentiName;
    @BindView(R.id.ownerPwd)
    EditText editOwnerPwd;
    @BindView(R.id.moduleNickname)
    EditText editNickname;



    Boolean authCheck = false;
    int chooseIcon = 1;  // 1,2,3
    int choosePushCheck = 0; // 0 : on , 1 : off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        ButterKnife.bind(this);

        // 시스템으로부터 현재시간(ms) 가져오기
        long now = System.currentTimeMillis();
        // Data 객체에 시간을 저장한다.
        Date date = new Date(now);
        // 각자 사용할 포맷을 정하고 문자열로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss");
        String strNow = sdfNow.format(date);

        register_date.setText(strNow);
    }


    @OnClick(R.id.requestAuth)
    public void requestAuth(){
        // TODO: 2016. 10. 5. 블루투스로 부터 인증
        if(editOwnerPwd.length() == 0)
            Toast.makeText(getApplicationContext(),"패스워드를 입력해주세요.",Toast.LENGTH_SHORT).show();
        else{
            //성공 시
            authCheck = true;
        }
    }

    @OnClick(R.id.icon1)
    public void chooseicon1(){
        chooseIcon = 1;
        icon1.setBackgroundResource(R.drawable.icon_background);
        icon2.setBackgroundResource(0);
        icon3.setBackgroundResource(0);
    }

    @OnClick(R.id.icon2)
    public void chooseicon2(){
        chooseIcon = 2;
        icon1.setBackgroundResource(0);
        icon2.setBackgroundResource(R.drawable.icon_background);
        icon3.setBackgroundResource(0);
    }

    @OnClick(R.id.icon3)
    public void chooseicon3(){
        chooseIcon = 3;
        icon1.setBackgroundResource(0);
        icon2.setBackgroundResource(0);
        icon3.setBackgroundResource(R.drawable.icon_background);
    }

    @OnClick(R.id.pushOn)
    public void pushOnCheck(){
        choosePushCheck = 0;
        pushOnLayout.setBackgroundColor(Color.rgb(226, 202, 174));
        pushOffLayout.setBackgroundColor(0);
    }

    @OnClick(R.id.pushOff)
    public void pushOFFCheck(){
        choosePushCheck = 1;
        pushOnLayout.setBackgroundColor(0);
        pushOffLayout.setBackgroundColor(Color.rgb(226, 202, 174));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        builder.setMessage("등록을 취소하시겠습니까?")        // 메세지 설정
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

    @OnClick(R.id.completeRegister)
    public void registerOwner(){
        if (authCheck == true){
            Log.i("MyTag",textModuleidentiName.getText().toString());
            Log.i("MyTag",editOwnerPwd.getText().toString());
            Log.i("MyTag",editNickname.getText().toString());
            Log.i("MyTag", String.valueOf(chooseIcon));
            Log.i("MyTag",register_date.getText().toString());
            Log.i("MyTag",String.valueOf(choosePushCheck));

            ItemData insertData = new ItemData();

            insertData.identName = textModuleidentiName.getText().toString();
            insertData.identNum  = "123dmdf"; // 임시로

            if (editNickname.length() == 0)
                insertData.nickName = textModuleidentiName.getText().toString();
            else
                insertData.nickName = editNickname.getText().toString();

            insertData.qualificaion = "Owner";
            insertData.ownerPwd = editOwnerPwd.getText().toString();
            insertData.questPwd = "";
            insertData.icon = chooseIcon;
            insertData.created = register_date.getText().toString();
            insertData.pushcheck = choosePushCheck;


            DbOpenHelper mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;
            mDbOpenHelper.DbInsert(insertData);

            Toast.makeText(getApplicationContext(),"등록 성공!!",Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            Toast.makeText(getApplicationContext(),"계정 인증은 필수 사항입니다",Toast.LENGTH_SHORT).show();
        }
    }
}
