package com.capstone.locker.register.owner;

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
import com.tsengvn.typekit.TypekitContextWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @BindView(R.id.pushOnText)
    TextView pushOnText;
    @BindView(R.id.pushOffText)
    TextView pushOffText;
    @BindView(R.id.requestAuth)
    TextView requestAuth;


    Boolean authCheck = false;
    int chooseIcon = 1;  // 1,2,3
    int choosePushCheck = 0; // 0 : on , 1 : off

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mWriteCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean mConnected = false;

    private BluetoothLeService mBluetoothLeService;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.i("myTag","연결성공");
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();


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
                else{

                    //성공 시
                    authCheck = false;
                    requestAuth.setText("인증실패");
                }
            }

        }
    };

    // Code to manage Service lifecycle.
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
        setContentView(R.layout.activity_owner);

        ButterKnife.bind(this);

        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)) {

            if(ApplicationController.getInstance().mDeviceName == null)
                textModuleidentiName.setText(R.string.unknown_device);
            else
                textModuleidentiName.setText(ApplicationController.getInstance().mDeviceName);

        }
        else{
            finish();
        }

        // 시스템으로부터 현재시간(ms) 가져오기
        long now = System.currentTimeMillis();
        // Data 객체에 시간을 저장한다.
        Date date = new Date(now);
        // 각자 사용할 포맷을 정하고 문자열로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss");
        String strNow = sdfNow.format(date);

        register_date.setText(strNow);


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

        Log.i("myTag_","read 활성화");
        BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, 0, 0, 0, 30);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.requestAuth)
    public void requestAuth(){
        // TODO: 2016. 10. 5. 블루투스로 부터 인증

        Log.i("myTag_","Auth Request");

        if(authCheck == false){
            if(editOwnerPwd.length() == 0)
                Toast.makeText(getApplicationContext(),"패스워드를 입력해주세요.",Toast.LENGTH_SHORT).show();
            else{

                String temp = editOwnerPwd.getText().toString();

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
            insertData.identNum  = ApplicationController.getInstance().mDeviceAddress; // 임시로

            if (editNickname.length() == 0)
                insertData.nickName = textModuleidentiName.getText().toString();
            else
                insertData.nickName = editNickname.getText().toString();

            insertData.qualificaion = "Owner";
            insertData.ownerPwd = editOwnerPwd.getText().toString();
            insertData.guestPwd = "";
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

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        mCurrentservice = ApplicationController.getInstance().current_characteristic;

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
