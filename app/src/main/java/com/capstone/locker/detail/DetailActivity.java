package com.capstone.locker.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.detail.map.MapsActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

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
    @BindView(R.id.profile_image)
    ImageView profileImg;

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
        textviewGuestPwd.setText(getItem.guestPwd);

        Log.i("MyTag", String.valueOf(getItem.icon));

        if(getItem.icon == 1)
            profileImg.setImageResource(R.drawable.ic_owner);
        else if(getItem.icon == 2)
            profileImg.setImageResource(R.drawable.ic_lock);
        else
            profileImg.setImageResource(R.drawable.ic_cycle);


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }


    @OnClick(R.id.showLocationBtn)
    public void moveMap(){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.closeBtn)
    public void closePage(){
        finish();
    }

}
