package com.capstone.locker.register;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.view.DeviceSearchActivity;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
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

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        checkBlueTooth();

//        if(ApplicationController.getInstance().mDeviceAddress == null)
//            ApplicationController.getInstance().mDeviceAddress = "";

        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){
            Log.i("myTag","true");
            checkConnectBLE = true;
            Log.i("myTag",ApplicationController.getInstance().mDeviceAddress);
            connectBLE.setText(ApplicationController.getInstance().mDeviceAddress);
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
