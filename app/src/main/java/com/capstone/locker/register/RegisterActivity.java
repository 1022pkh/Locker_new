package com.capstone.locker.register;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.view.DeviceSearchActivity;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.register.guest.GuestActivity;
import com.capstone.locker.register.owner.OwnerActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.findModule)
    Button findModule;
    @BindView(R.id.registerOwner)
    Button registerOwnerBtn;
    @BindView(R.id.registerGuest)
    Button registerGuestBtn;
    @BindView(R.id.connectBLE)
    TextView connectBLE;

    Boolean checkBluetooth = false;
    Boolean checkConnectBLE = false;
    Boolean registerCheck = false;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;



    private BluetoothLeService mBluetoothLeService;


    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i("myTag","application onServiceDisconnected");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i("myTag","BroadcastReceiver 연결성공");
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i("myTag","BroadcastReceiver 연결 해제");
                invalidateOptionsMenu();


                // 연결 실패시 연결로 이어지도록 조건 변경
                Log.i("myTag","현재 블루투스 연결 상태 : false");
                checkConnectBLE = false;
                connectBLE.setText("");

                ApplicationController.editor.putBoolean("Connect_check", false);
                ApplicationController.editor.commit();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i("myTag", "BroadcastReceiver GATT 서비스 발견");

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        checkBlueTooth();


        /**
         * BLE 서비스를 등록
         */
        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());



        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){
            Log.i("myTag","현재 블루투스 연결 상태 : true");
            checkConnectBLE = true;
            Log.i("myTag",ApplicationController.getInstance().mDeviceAddress);
            connectBLE.setText(ApplicationController.getInstance().mDeviceAddress);

            registerBLECheck();

        }

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_CAROUSEL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_OTA);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECT_OTA);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_OTA);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_ERROR);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESS);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_FAILED);
        intentFilter.addAction(BluetoothLeService.ACTION_PAIR_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_COMPLETED);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            unbindService(mServiceConnection);
            unregisterReceiver(mGattUpdateReceiver);
        }catch(IllegalArgumentException e){

        }
    }

    public void registerBLECheck(){
        ItemData getItem = ApplicationController.getInstance().mDbOpenHelper.DbFindMoudle(ApplicationController.getInstance().mDeviceAddress);

        // 등록된 장치
        if(getItem.identNum != null){
            registerCheck = true;
        }
        else{
            registerCheck = false;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.findModule)
    public void connectModule(){
        // TODO: 2016. 10. 5. 블루투스 목록 검색을 통해 등록되지않은 잠금장치 연결

        Intent intent = new Intent(getApplicationContext(), DeviceSearchActivity.class);
//            startActivity(intent);
        startActivityForResult(intent, 1); // requestCode

    }

    @OnClick(R.id.registerOwner)
    public void moveOwnerPage(){

        if(registerCheck)
        {
            Toast.makeText(getApplicationContext(),"이미 등록된 장치입니다.",Toast.LENGTH_SHORT).show();
            return ;
        }


        if(checkBluetooth == true){
            if(checkConnectBLE == true){
                Intent intent = new Intent(getApplicationContext(), OwnerActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(),"장치를 먼저 연결해주세요.",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"블루투스 설정 및 잠금장치 연결을 확인해주세요",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.registerGuest)
    public void moveGuestPage(){

        if(registerCheck)
        {
            Toast.makeText(getApplicationContext(),"이미 등록된 장치입니다.",Toast.LENGTH_SHORT).show();
            return ;
        }

        if(checkBluetooth == true){
            if(checkConnectBLE == true){
                Intent intent = new Intent(getApplicationContext(), GuestActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(),"장치를 먼저 연결해주세요.",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"블루투스 설정 및 잠금장치 연결을 확인해주세요",Toast.LENGTH_SHORT).show();
        }
    }

    public void checkBlueTooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 장치가 블루투스 지원하지 않는 경우
            Toast.makeText(getApplicationContext(),"해당 디바이스는 블루투스를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();   // 어플리케이션 종료
                }
            }, 4000);

        } else {
            // 장치가 블루투스 지원하는 경우
            if (!mBluetoothAdapter.isEnabled()) {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요첨
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // 블루투스를 지원하며 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택.
                checkBluetooth = true;

            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // 블루투스가 활성 상태로 변경됨
                    checkBluetooth = true;

                } else if (resultCode == RESULT_CANCELED) {
                    // 블루투스가 비활성 상태임
                    Toast.makeText(getApplicationContext(), "블루투스 비활성화로 앱종료", Toast.LENGTH_SHORT).show();
                    finish();  //  어플리케이션 종료

                }
                break;

            case RESULT_OK:
                Toast.makeText(getApplicationContext(), "ddd", Toast.LENGTH_SHORT).show();

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
