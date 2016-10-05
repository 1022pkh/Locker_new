package com.capstone.locker.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.nickname)
    TextView textviewNickname;
    @BindView(R.id.moduleIdentiname)
    TextView textviewModuleName;
    @BindView(R.id.qualificaion)
    TextView textviewQualification;
    @BindView(R.id.registerDate)
    TextView textviewDate;
    @BindView(R.id.ownerPwd)
    TextView textviewOwnerPwd;
    @BindView(R.id.guestPwd)
    TextView textviewGuestPwd;

    DbOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;


        Intent intent = getIntent();
        String moduleId = intent.getExtras().getString("id");

        ItemData getItem = mDbOpenHelper.DbDetail(moduleId);

        textviewNickname.setText(getItem.nickName);
        textviewModuleName.setText(getItem.identName);
        textviewQualification.setText(getItem.qualificaion);
        textviewDate.setText(getItem.created);
        textviewOwnerPwd.setText(getItem.ownerPwd);
        textviewGuestPwd.setText(getItem.questPwd);

    }

    @OnClick(R.id.closeBtn)
    public void closePage(){
        finish();
    }
}
