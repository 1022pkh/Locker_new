package com.capstone.locker.splash.view;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.main.view.MainActivity;
import com.capstone.locker.R;
import com.capstone.locker.order.OrderActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

public class SplashActivity extends AppCompatActivity implements SplashView{

    private Boolean buletoothCheck = false;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public BluetoothLeService mBluetoothLeService;

    String mDeviceAddress;
    String mDeviceName;

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i("myTag","application onServiceDisconnected");
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
         * Splash 에서 확인해야할 것
         * 1. 블루투스 연결 설정 여부
         * 2. 마지막으로 연결한 모델 연결
         */

        // 1. 블루투스 연결 설정 여부 확인
        checkBlueTooth();



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

    @Override
    public void lastBLEConnect() {
        // TODO: 2016. 10. 4. 2. 마지막으로 연결했던 모듈 확인 후 연결

        mDeviceAddress = ApplicationController.getInstance().mDeviceAddress;
        mDeviceName = ApplicationController.getInstance().mDeviceName;

        Log.i("myTag","마지막으로 연결했던 장치 검색 후 연결 시");
        Log.i("myTag","last BLE name : " + mDeviceName);
        Log.i("myTag","last BLE address : " + mDeviceAddress);

        if(mDeviceAddress != null && mDeviceAddress.length() > 0){


            Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


            if (ApplicationController.getInstance().mBluetoothLeService != null) {
                final boolean result = ApplicationController.getInstance().mBluetoothLeService.connect(mDeviceAddress);
                Log.i("myTag", "Connect request result=" + result);

                if(result){
                    ApplicationController.getInstance().editor.putBoolean("Connect_check", true);
                    ApplicationController.getInstance().editor.commit();
                    moveOrderPage();
                }
                else{
                    ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                    ApplicationController.getInstance().editor.commit();
                    moveMainPage();
                }
            }
            else{
                ApplicationController.getInstance().editor.putBoolean("Connect_check", false);
                ApplicationController.getInstance().editor.commit();
                moveMainPage();
            }

        }
        else{
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
}
