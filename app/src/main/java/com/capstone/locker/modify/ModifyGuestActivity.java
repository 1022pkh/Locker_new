package com.capstone.locker.modify;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.util.GattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.main.view.MainActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModifyGuestActivity extends AppCompatActivity {

    @BindView(R.id.icon1)
    ImageView icon1;
    @BindView(R.id.icon2)
    ImageView icon2;
    @BindView(R.id.icon3)
    ImageView icon3;
    @BindView(R.id.moduleIdentiName)
    TextView textModuleidentiName;
    @BindView(R.id.moduleNickname)
    TextView moduleNickname;
    @BindView(R.id.modifyGuestPwd)
    EditText modifyGuestPwd;

    DbOpenHelper mDbOpenHelper;
    Boolean bleCheck = false;

    String moduleId;
    int chooseIcon = 1;  // 1,2,3
    int choosePushCheck = 0; // 0 : on , 1 : off

    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mWriteCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_modify);

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

        getGattData();

    }
    @Override
    protected void onResume() {
        super.onResume();

        if(mWriteCharacteristic != null){

            Log.i("myTag_","read 활성화");
            BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, 0, 0, 0, 30);
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
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

    @OnClick(R.id.completeRegister)
    public void completeRegisterMethod(){
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
                        item.guestPwd = modifyGuestPwd.getText().toString();

                        mDbOpenHelper.DbUpdate(moduleId,item);

                        Toast.makeText(getApplicationContext(),"수정 완료",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ModifyGuestActivity.this, MainActivity.class);
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

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        mCurrentservice = ApplicationController.getInstance().current_characteristic;

        if(mCurrentservice != null){
            List<BluetoothGattCharacteristic> gattCharacteristics = mCurrentservice.getCharacteristics();



            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String uuidchara = gattCharacteristic.getUuid().toString();
                Log.i("myTag_",uuidchara);

                //0003cbbb-0000-1000-8000-00805f9b0131

//            RGB_LED = "0000cbb1-0000-1000-8000-00805f9b34fb";
//            RGB_LED_CUSTOM = "0003cbb1-0000-1000-8000-00805f9b0131"; << 여기에 해당함.
                if (uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED) || uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED_CUSTOM)) {
                    mWriteCharacteristic = gattCharacteristic;
                    break;
                }
            }
        }


    }

}
