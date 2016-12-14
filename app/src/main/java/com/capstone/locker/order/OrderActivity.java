package com.capstone.locker.order;

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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.presenter.SampleGattAttributes;
import com.capstone.locker.Buletooth.util.GattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.main.view.MainActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderActivity extends AppCompatActivity {
    @BindView(R.id.stateImg)
    ImageView stateImg;
    @BindView(R.id.connectBLEName)
    TextView connectBLEName;
    @BindView(R.id.openOrder)
    TextView openOrder;
    @BindView(R.id.closeOrder)
    TextView closeOrder;


    DbOpenHelper mDbOpenHelper;
    ItemData getItem;

    Boolean firstPwdCheck = false; //
    String requestState = "close";


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


//            Log.i("myTag__","in " + action);

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
            }else if (BluetoothLeService.EXTRA_DATA.equals(action)) {

                /**
                 * 데이터가 왔을 경우
                 */
                String result = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("myTag", ">>>>get : " + result);

                if(result.equals("100")){

                }

//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String result = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("myTag", ">>>>get2 : " + result);

                if(result.equals("100")){

                    if(requestState.equals("open")){
                        stateImg.setImageResource(R.drawable.unlock);
                        openOrder.setBackgroundResource(R.drawable.border_circle_background);
                        openOrder.setTextColor(Color.parseColor("#ffffff"));
                        closeOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
                        closeOrder.setTextColor(Color.parseColor("#000000"));
                    }
                    else{
                        stateImg.setImageResource(R.drawable.lock);
                        closeOrder.setBackgroundResource(R.drawable.border_circle_background);
                        closeOrder.setTextColor(Color.parseColor("#ffffff"));
                        openOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
                        openOrder.setTextColor(Color.parseColor("#000000"));
                    }

                }
                else if(result.equals("200")){
                    firstPwdCheck = false;
                    Toast.makeText(getApplicationContext(),"계정 비밀번호가 틀립니다. 확인 후 이용해주세요.",Toast.LENGTH_SHORT).show();

                    Intent move = new Intent(getApplicationContext(), MainActivity.class);
                    move.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    move.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(move);
                    finish();

                }
                else if(result.equals("130")){ // open state
                    firstPwdCheck = true;

                    stateImg.setImageResource(R.drawable.unlock);
                    openOrder.setBackgroundResource(R.drawable.border_circle_background);
                    openOrder.setTextColor(Color.parseColor("#ffffff"));
                    closeOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
                    closeOrder.setTextColor(Color.parseColor("#000000"));
                }
                else if(result.equals("140")){ // close state
                    firstPwdCheck = true;

                    stateImg.setImageResource(R.drawable.lock);
                    closeOrder.setBackgroundResource(R.drawable.border_circle_background);
                    closeOrder.setTextColor(Color.parseColor("#ffffff"));
                    openOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
                    openOrder.setTextColor(Color.parseColor("#000000"));
                }


            }
            else if(BluetoothLeService.ORDER_OPEN.equals(action)){

                requestOpenOrder();
            }
            else if(BluetoothLeService.ORDER_CLOSE.equals(action)){
                requestCloseOrder();
            }

        }
    };



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            Log.i("myTag", "onServiceConnected");
//
//            if (!mBluetoothLeService.initialize()) {
//                Log.i("myTag", "Unable to initialize Bluetooth");
//                finish();
//            }
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
        setContentView(R.layout.activity_order);

        ButterKnife.bind(this);

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;



        getItem = mDbOpenHelper.DbFindMoudle(ApplicationController.getInstance().mDeviceAddress);


        connectBLEName.setText("명령 페이지");//ApplicationController.getInstance().mDeviceName);

        ApplicationController.editor.putString("mDeviceAddress", ApplicationController.getInstance().mDeviceAddress);
        ApplicationController.editor.putString("mDeviceName", ApplicationController.getInstance().mDeviceName);
        ApplicationController.editor.commit();


        /**
         * BLE 서비스를 등록
         */
        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        /**
         * 서비스 등록
         */
        getGattData();





    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("myTag_","read 활성화");

        if(mWriteCharacteristic != null) {
            BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, 0, 0, 0, 30);
        }


        /**
         * 현재 잠금 상태 체크
         * firstPwdCheck
         */

        firstPwdCheck = true;
//        int mRed;  // pwd 앞 2자리
//        int mGreen;  // pwd 뒷 2자리
//        int mBlue;  // 10:owner, 20:guest
//        int mIntensity = 70;  // 명령 종류  10:해제 , 20: 잠금, 30:read 활성화, 40:read 비활성화 , 50: 인증 , 60:변경 , 70 : 상태체크
//
//        Log.i("myTag",getItem.qualificaion);
//
//        if(getItem.qualificaion.equals("Owner")){
//            mBlue = 10;
//            String temp = getItem.ownerPwd;
//            mRed = Integer.valueOf(temp) / 100;
//            mGreen = Integer.valueOf(temp) % 100;
//
//        }
//        else{
//            mBlue = 20;
//            String temp = getItem.guestPwd;
//            mRed = Integer.valueOf(temp) / 100;
//            mGreen = Integer.valueOf(temp) % 100;
//        }
//
////        Log.i("myTag", String.valueOf(mRed) + " " + String.valueOf(mGreen));
//
//        BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed, mGreen, mBlue, mIntensity);

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


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
        intentFilter.addAction(BluetoothLeService.ORDER_OPEN);
        intentFilter.addAction(BluetoothLeService.ORDER_CLOSE);

        return intentFilter;
    }

    @OnClick(R.id.openOrder)
    public void requestOpenOrder(){
        Log.i("myTag_","OPEN REQUEST");

        if(firstPwdCheck){
            requestState = "open";

            int mRed;  // pwd 앞 2자리
            int mGreen;  // pwd 뒷 2자리
            int mBlue;  // 10:owner, 20:guest
            int mIntensity = 10;  // 명령 종류  10:해제 , 20: 잠금, 30:read 활성화, 40:read 비활성화


            if(getItem.qualificaion.equals("Owner")){
                mBlue = 10;

                String temp = getItem.ownerPwd;
                mRed = Integer.valueOf(temp) / 100;
                mGreen = Integer.valueOf(temp) % 100;

            }
            else{
                mBlue = 20;

                String temp = getItem.guestPwd;
                mRed = Integer.valueOf(temp) / 100;
                mGreen = Integer.valueOf(temp) % 100;

            }


            try {
                BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed, mGreen, mBlue, mIntensity);

            } catch (Exception e) {

            }

            stateImg.setImageResource(R.drawable.unlock);
            openOrder.setBackgroundResource(R.drawable.border_circle_background);
            openOrder.setTextColor(Color.parseColor("#ffffff"));
            closeOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
            closeOrder.setTextColor(Color.parseColor("#000000"));
        }

    }

    @OnClick(R.id.closeOrder)
    public void requestCloseOrder(){
        Log.i("myTag_","CLOSE REQUEST");


        if(firstPwdCheck){
            requestState = "close";

            int mRed;  // pwd 앞 2자리
            int mGreen;  // pwd 뒷 2자리
            int mBlue;  // 10:owner, 20:guest
            int mIntensity = 20;  // 명령 종류  10:해제 , 20: 잠금 , 30:read활성화, 40:read 비활성화


            if(getItem.qualificaion.equals("Owner")){
                mBlue = 10;

                String temp = getItem.ownerPwd;
                mRed = Integer.valueOf(temp) / 100;
                mGreen = Integer.valueOf(temp) % 100;

            }
            else{
                mBlue = 20;

                String temp = getItem.guestPwd;
                mRed = Integer.valueOf(temp) / 100;
                mGreen = Integer.valueOf(temp) % 100;

            }

            try {
                BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, mRed, mGreen, mBlue, mIntensity);

            } catch (Exception e) {

            }


            stateImg.setImageResource(R.drawable.lock);
            closeOrder.setBackgroundResource(R.drawable.border_circle_background);
            closeOrder.setTextColor(Color.parseColor("#ffffff"));
            openOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
            openOrder.setTextColor(Color.parseColor("#000000"));
        }


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
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

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {

        mCurrentservice = ApplicationController.getInstance().current_characteristic;

//        if(mCurrentservice != null) {
            List<BluetoothGattCharacteristic> gattCharacteristics = mCurrentservice.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String uuidchara = gattCharacteristic.getUuid().toString();
                Log.i("myTag_", uuidchara);

                //0003cbbb-0000-1000-8000-00805f9b0131

//            RGB_LED = "0000cbb1-0000-1000-8000-00805f9b34fb";
//            RGB_LED_CUSTOM = "0003cbb1-0000-1000-8000-00805f9b0131"; << 여기에 해당함.
                if (uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED) || uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED_CUSTOM)) {
                    mWriteCharacteristic = gattCharacteristic;
                    break;
                }
            }
//        }
//        else{
//            Toast.makeText(getApplicationContext(),"연결 확인 후 재시도해주세요.",Toast.LENGTH_SHORT).show();
//        }

    }



}
