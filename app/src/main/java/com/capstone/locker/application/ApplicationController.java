package com.capstone.locker.application;

import android.app.Application;
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
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.presenter.SampleGattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.tsengvn.typekit.Typekit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplicationController extends Application {

    private static ApplicationController instance;
    public DbOpenHelper mDbOpenHelper;

    public static SharedPreferences connectInfo;
    public static SharedPreferences.Editor editor;

    public String mDeviceAddress="";
    public String mDeviceName="";

    public BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics
            = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mReadGattCharacteristics
            = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    public BluetoothGattService current_characteristic;
    public BluetoothGattService read_characteristic;

    private boolean mConnected = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    // Code to manage Service lifecycle.


    /**
     * 서비스가 살아있는 지에 대한 객체
     */
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
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

                Log.i("myTag__","Application class -- 연결성공");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                Log.i("myTag__","Application class -- ACTION_GATT_SERVICES_DISCOVERED 연결성공");

                /**
                 * 우리 어플리케이션을 이용할 수 있는 GATT 서비스가 있는 지 체크
                 */
                //있을 경우
                displayGattServices(mBluetoothLeService.getSupportedGattServices());

                if (mGattCharacteristics.size() > 0){
                    ItemData getItem = ApplicationController.getInstance().mDbOpenHelper.DbFindMoudle(mDeviceAddress);

                    BluetoothGattCharacteristic characteristic = mReadGattCharacteristics.get(0).get(0);

//                    Log.i("myTag_",">>>>>"+characteristic.getUuid());

                    final int charaProp = characteristic.getProperties();

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        Log.i("myTag_", "---- GATT 1----");
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(characteristic);
                    }

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.i("myTag_", "---- GATT 2----");
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }


                }

            }

        }
    };



    public static ApplicationController getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationController.instance = this;
        this.buildDB();

        /**
         * 폰트 설정
         */
        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "OTF_R.otf"))
                .addBold(Typekit.createFromAsset(this, "OTF_B.otf"))
                .addCustom1(Typekit.createFromAsset(this, "OTF_L.otf"));

        /**
         * SharedPreference 설정
         */
        connectInfo = getSharedPreferences("connect_info", 0);
        editor= connectInfo.edit();


        /**
         *
         */

        mDeviceName = connectInfo.getString("mDeviceName", "");
        mDeviceAddress = connectInfo.getString("mDeviceAddress", "");

        if(mDeviceName == null)
            mDeviceName = "";

        if(mDeviceAddress == null)
            mDeviceAddress = "";


        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        mReadGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            HashMap<String, String> readServiceData = new HashMap<String, String>();



            uuid = gattService.getUuid().toString();

            Log.i("myTag_",">>> "+uuid);

            if(uuid.equals( "0003cbbb-0000-1000-8000-00805f9b0131")){

                current_characteristic = gattService;

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
            else if(uuid.equals("0003cab5-0000-1000-8000-00805f9b0131")){

                read_characteristic = gattService;

                readServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                readServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(readServiceData);

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
                mReadGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);

            }

        }


    }

    public void unBindBLEService(){
        Log.i("myTag","unBindBLEService");

        try{
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);


            mBluetoothLeService.close();
        }catch(IllegalArgumentException e){

        }

        editor.putBoolean("Connect_check", false);
        editor.commit();

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BootLoaderUtils.ACTION_OTA_STATUS);
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


    public void buildDB() {
        // DB Create and Open
        mDbOpenHelper = new DbOpenHelper(this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
