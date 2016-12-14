package com.capstone.locker.splash.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.presenter.SampleGattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.main.view.MainActivity;
import com.capstone.locker.order.OrderActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SplashActivity extends AppCompatActivity implements SplashView{

    Boolean buletoothCheck = false;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public BluetoothLeService mBluetoothLeService;

    String mDeviceAddress="";
    String mDeviceName;


    //    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private BluetoothGattCharacteristic mNotifyCharacteristic;


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    Handler handler;
    Runnable runnable;

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            Log.i("myTag","onServiceConnected");

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.

            handler = new Handler();

            runnable = new Runnable() {
                @Override
                public void run() {

                    ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                    ApplicationController.getInstance().editor.commit();

                    Intent move = new Intent(getApplicationContext(), MainActivity.class);
                    move.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    move.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(move);
                    finish();
                }
            };


            if(mDeviceAddress==""){
                handler.postDelayed(runnable,2000);
            }
            else {
                handler.postDelayed(runnable, 4000);
                mBluetoothLeService.connect(mDeviceAddress);
            }
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
                Log.i("myTag","splash BroadcastReceiver 연결성공");
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i("myTag","splash BroadcastReceiver 연결 해제");
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i("myTag","splash BroadcastReceiver GATT 서비스 발견");

                displayGattServices(mBluetoothLeService.getSupportedGattServices());



                handler.removeCallbacks(runnable);

                /**
                 * 우리 어플리케이션을 이용할 수 있는 GATT 서비스가 있는 지 체크
                 */
                //있을 경우
                if (mGattCharacteristics.size() > 0){


//                    BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(0).get(0);
//                    final int charaProp = characteristic.getProperties();
//
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                        // If there is an active notification on a characteristic, clear
//                        // it first so it doesn't update the data field on the user interface.
//                        Log.i("myTag3", "---- GATT 1----");
//                        if (mNotifyCharacteristic != null) {
//                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
//                            mNotifyCharacteristic = null;
//                        }
//                        mBluetoothLeService.readCharacteristic(characteristic);
//                    }
//
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                        Log.i("myTag3", "---- GATT 2----");
//                        mNotifyCharacteristic = characteristic;
//                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//                    }




                    ItemData getItem = ApplicationController.getInstance().mDbOpenHelper.DbFindMoudle(ApplicationController.getInstance().mDeviceAddress);

                    if(getItem.identNum == null)
                    {
                        ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                        ApplicationController.getInstance().editor.commit();
                        Intent move = new Intent(getApplicationContext(), MainActivity.class);
                        move.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        move.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(move);
                        finish();
                    }
                    else{
                        ApplicationController.getInstance().editor.putBoolean("Connect_check", true);
                        ApplicationController.getInstance().editor.commit();

                        Intent move = new Intent(getApplicationContext(), OrderActivity.class);
                        startActivity(move);
                        finish();
                    }
                }

                else{
                    ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                    ApplicationController.getInstance().editor.commit();
                    Intent move = new Intent(getApplicationContext(), MainActivity.class);
                    move.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    move.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(move);
                    finish();
                }

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= 21) {   //상태바 색
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        /**
         * BLE 서비스를 등록
         */
        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        /**
         * Splash 에서 확인해야할 것
         * 1. 블루투스 연결 설정 여부
         * 2. 마지막으로 연결한 모델 연결
         */

        // 1. 블루투스 연결 설정 여부 확인
        checkBlueTooth();



    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            if(uuid.equals( "0003cbbb-0000-1000-8000-00805f9b0131")){

                currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put(
                            LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put(LIST_UUID, uuid);
                    gattCharacteristicGroupData.add(currentCharaData);
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
        }


    }



    @Override
    public void checkBlueTooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 장치가 블루투스 지원하지 않는 경우
            buletoothCheck = false;
            Toast.makeText(getApplicationContext(),"해당 디바이스는 블루투스를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();   // 어플리케이션 종료
                }
            }, 2000);

        } else {
            // 장치가 블루투스 지원하는 경우
            if (!mBluetoothAdapter.isEnabled()) {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요첨
                buletoothCheck = false;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // 블루투스를 지원하며 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택.
                buletoothCheck = true;
                //moveMainPage();
                lastBLEConnect();

            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    public void moveMainPage() {
//        Toast.makeText(getApplicationContext(),"블루투스 연결 확인",Toast.LENGTH_SHORT).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    @Override
    public void moveOrderPage() {
//        Toast.makeText(getApplicationContext(),"블루투스 연결 확인",Toast.LENGTH_SHORT).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    @Override
    public void noConnectBluetooth() {
        Toast.makeText(getApplicationContext(),"블루투스 연결 실패 \n블루투스 연결 설정을 확인 후 다시 실행해주세요",Toast.LENGTH_SHORT).show();
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
    public void lastBLEConnect() {

        mDeviceAddress = ApplicationController.getInstance().mDeviceAddress;
        mDeviceName = ApplicationController.getInstance().mDeviceName;

        Log.i("myTag","마지막으로 연결했던 장치 검색 후 연결 시");
        Log.i("myTag","last BLE name : " + mDeviceName);
        Log.i("myTag","last BLE address : " + mDeviceAddress);


        if(mDeviceAddress != null && mDeviceAddress.length() > 0){

            Log.i("myTag","splash in1");

            if (mBluetoothLeService != null) {
                Log.i("myTag","not null");


            }
            else{
                Log.i("myTag","null");

                ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                ApplicationController.getInstance().editor.commit();
//                moveMainPage();
            }

        }
        else{

            Log.i("myTag","splash in2");

            ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
            ApplicationController.getInstance().editor.commit();
            moveMainPage();
            Log.i("myTag","init unconnected");
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // 블루투스가 활성 상태로 변경됨
                    buletoothCheck = true;
                    moveMainPage();

                } else if (resultCode == RESULT_CANCELED) {
                    // 블루투스가 비활성 상태임
                    Toast.makeText(getApplicationContext(), "블루투스 비활성화로 앱종료", Toast.LENGTH_SHORT).show();
                    finish();  //  어플리케이션 종료

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);

    }
}
