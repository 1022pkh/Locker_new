package com.capstone.locker.modify;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    @BindView(R.id.requestChangeOwer)
    TextView requestChangeOwer;
    @BindView(R.id.requestChangeGuest)
    TextView requestChangeGuest;


    DbOpenHelper mDbOpenHelper;
    Boolean authCheck = false;
    Boolean bleCheck = false;

    String moduleId;
    int chooseIcon = 1;  // 1,2,3
    int choosePushCheck = 0; // 0 : on , 1 : off


    Boolean modOwenrResult = false;
    Boolean modGuestResult = false;

    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mWriteCharacteristic;


    private BluetoothLeService mBluetoothLeService;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                ApplicationController.editor.putBoolean("Connect_check", false);
                ApplicationController.editor.commit();

                Toast.makeText(getApplicationContext(),"연결이 끊겼습니다.\n다시 연결 후 시도해주세요.",Toast.LENGTH_SHORT).show();
                finish();


            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.i("myTag", "BroadcastReceiver GATT 서비스 발견");

            }else if (BluetoothLeService.EXTRA_DATA.equals(action)) {

                /**
                 * 데이터가 왔을 경우
                 */
                String result = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("myTag", ">>>>get1 : " + result);

                if(result.equals("100")){

                    //성공 시
                    authCheck = true;
                    requestAuth.setText("인증성공");
                }
                else{

                    //성공 시
                    authCheck = false;
                    requestAuth.setText("인증실패");
                }

//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String result = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("myTag", ">>>>get2 : " + result);
                if(result.equals("100")){
                    //성공 시
                    authCheck = true;
                    requestAuth.setText("인증성공");
                }
                else if (result.equals("110")){
                    modOwenrResult = true;
                    requestChangeOwer.setText("변경완료");
                }
                else if (result.equals("120")){
                    modGuestResult = true;
                    requestChangeGuest.setText("변경완료");
                }
                else{
                    authCheck = false;
                    requestAuth.setText("인증실패");
                }

            }

        }
    };


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            Log.i("myTag", "onServiceConnected");

            if (!mBluetoothLeService.initialize()) {
                Log.i("myTag", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(ApplicationController.getInstance().mDeviceAddress);


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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
            Log.i("myTag__","push"+ String.valueOf(getItem.pushcheck));
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

        /**
         * BLE 서비스를 등록
         */
        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

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

    @Override
    protected void onPause() {
        super.onPause();
        try{
            unregisterReceiver(mGattUpdateReceiver);
        }catch(IllegalArgumentException e){

        }
        unbindService(mServiceConnection);
    }


    @OnClick(R.id.requestChangeOwer)
    public void requestChangeOwnerPwd(){

        if(authCheck ==false){
            Toast.makeText(getApplicationContext(),"인증 후 이용해주세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(modifyOwnerPwd.getText().length() != 0) {
            int m_owenr = Integer.valueOf(modifyOwnerPwd.getText().toString());

            int mRed_owmer = m_owenr/100;  // pwd 앞 2자리
            int mGreen_owmer = m_owenr%100;  // pwd 뒷 2자리
            int mBlue_owmer = 10;  // 10:owner, 20:guest
            int mIntensity_owner = 60;  // 명령 종류  10:해제 , 20: 잠금, 30:read 활성화, 40:read 비활성화 , 50: 인증 , 60:변경

            BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed_owmer, mGreen_owmer, mBlue_owmer, mIntensity_owner);

        }
        else{
            Toast.makeText(getApplicationContext(),"비밀번호 입력 후 이용해주세요.",Toast.LENGTH_SHORT);
            return;
        }

    }

    @OnClick(R.id.requestChangeGuest)
    public void requestChangeGuestPwd(){

        if(authCheck ==false){
            Toast.makeText(getApplicationContext(),"인증 후 이용해주세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(modifyOwnerPwd.getText().length() != 0) {
            int m_guest = Integer.valueOf(modifyGuestPwd.getText().toString());

            int mRed_guest = m_guest/100;  // pwd 앞 2자리
            int mGreen_guest = m_guest%100;  // pwd 뒷 2자리
            int mBlue_guest = 20;  // 10:owner, 20:guest
            int mIntensity_owner = 60;  // 명령 종류  10:해제 , 20: 잠금, 30:read 활성화, 40:read 비활성화 , 50: 인증 , 60:변경

            BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed_guest, mGreen_guest, mBlue_guest, mIntensity_owner);

        }
        else{
            Toast.makeText(getApplicationContext(),"비밀번호 입력 후 이용해주세요.",Toast.LENGTH_SHORT);
            return;
        }

    }

    @OnClick(R.id.requestAuth)
    public void requestAuth(){
        Log.i("myTag_","Auth Request");


        if(authCheck ==false){

            if(authPwd.getText().length() == 0){
                Toast.makeText(getApplicationContext(),"비밀번호 입력해주세요",Toast.LENGTH_SHORT).show();
            }
            else{
                if(bleCheck == false){
                    Toast.makeText(getApplicationContext(),"연결된 장치가 없습니다.\n연결 후 시도해주세요",Toast.LENGTH_SHORT).show();
                }
                else{

                    String temp = authPwd.getText().toString();

                    int mRed = Integer.valueOf(temp)/100;  // pwd 앞 2자리
                    int mGreen = Integer.valueOf(temp)%100;  // pwd 뒷 2자리
                    int mBlue = 10;  // 10:owner, 20:guest
                    int mIntensity = 50;  // 명령 종류  10:해제 , 20: 잠금, 30:read 활성화, 40:read 비활성화 , 50: 인증

                    try {

                        BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed, mGreen, mBlue, mIntensity);

                    } catch (Exception e) {

                    }

                }
            }


        }

    }


    @OnClick(R.id.completeRegister)
    public void completeRegisterMethod(){


        if(modifyOwnerPwd.getText().length() == 0)
            modOwenrResult = true;

        if(modifyGuestPwd.getText().length() == 0)
            modGuestResult = true;



        if(modifyOwnerPwd.getText().length() != 0 || modifyGuestPwd.getText().length() != 0 ){

            if(modOwenrResult == false || modGuestResult == false){
                Toast.makeText(getApplicationContext(),"비밀번호 변경을 요청 후 이용해주세요",Toast.LENGTH_SHORT).show();

            }
            else{
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

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        mCurrentservice = ApplicationController.getInstance().current_characteristic;
        if(mCurrentservice != null) {

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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);

        return intentFilter;
    }
}
