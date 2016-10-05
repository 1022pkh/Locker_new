package com.capstone.locker.splash.view;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.capstone.locker.main.view.MainActivity;
import com.capstone.locker.R;

public class SplashActivity extends AppCompatActivity implements SplashView{

    private Boolean buletoothCheck = false;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * Splash 에서 확인해야할 것
         * 1. 블루투스 연결 설정 여부
         * 2. 마지막으로 연결한 모델 연결
         */

        // 1. 블루투스 연결 설정 여부 확인
        checkBlueTooth();


        // TODO: 2016. 10. 4. 2. 마지막으로 연결했던 모듈 확인 후 연결
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
            }, 4000);

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
                moveMainPage();

            }
        }
    }

    @Override
    public void moveMainPage() {
        Toast.makeText(getApplicationContext(),"블루투스 연결 확인",Toast.LENGTH_SHORT).show();

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
    public void noConnectBluetooth() {
        Toast.makeText(getApplicationContext(),"블루투스 연결 실패 \n블루투스 연결 설정을 확인 후 다시 실행해주세요",Toast.LENGTH_SHORT).show();
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
