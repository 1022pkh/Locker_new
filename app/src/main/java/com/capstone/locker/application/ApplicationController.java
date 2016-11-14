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
import com.capstone.locker.Buletooth.util.GattAttributes;
import com.capstone.locker.database.DbOpenHelper;
import com.tsengvn.typekit.Typekit;

import java.sql.SQLException;
import java.util.List;

public class ApplicationController extends Application {

    private static ApplicationController instance;
    public DbOpenHelper mDbOpenHelper;

    public static SharedPreferences connectInfo;
    public static SharedPreferences.Editor editor;

    public String mDeviceAddress="";
    public String mDeviceName="";

    public BluetoothLeService mBluetoothLeService;
    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mReadCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    BluetoothGattCharacteristic characteristic;

    // Code to manage Service lifecycle.
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.

            if(mDeviceAddress.length()>0)
                mBluetoothLeService.connect(mDeviceAddress);
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

                Log.i("myTag","연결성공");

                ApplicationController.editor.putBoolean("Connect_check", true);
                ApplicationController.editor.commit();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

                Log.i("myTag", "BOND_CHECK");

                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDING) {
                    Log.i("myTag", "BOND_BONDING");

                } else if (state == BluetoothDevice.BOND_BONDED) {
                    Log.i("myTag", "BOND_BONDED");
                    getGattData();

                } else if (state == BluetoothDevice.BOND_NONE) {

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
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


    }


//    public void GATTConnect(BluetoothGattCharacteristic characteristic){
//        this.characteristic = characteristic;
//
//        final int charaProp = characteristic.getProperties();
//
//        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//            // If there is an active notification on a characteristic, clear
//            // it first so it doesn't update the data field on the user interface.
//            Log.i("myTag", "---- GATT 1----");
//            if (mNotifyCharacteristic != null) {
//                ApplicationController.getInstance().mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
//                mNotifyCharacteristic = null;
//            }
//            ApplicationController.getInstance().mBluetoothLeService.readCharacteristic(characteristic);
//        }
//
//        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//            Log.i("myTag", "---- GATT 2----");
//            mNotifyCharacteristic = characteristic;
//            ApplicationController.getInstance().mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//        }
//    }


    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        List<BluetoothGattCharacteristic> gattCharacteristics = mCurrentservice.getCharacteristics();
        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
            String uuidchara = gattCharacteristic.getUuid().toString();
            if (uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED) || uuidchara.equalsIgnoreCase(GattAttributes.RGB_LED_CUSTOM)) {
                mReadCharacteristic = gattCharacteristic;
                break;
            }
        }
    }

    public void bindBLEService(final String mDeviceAddress,final String mDeviceName){
        Log.i("myTag","bindBLEService");

        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
        }

    }

    public void unBindBLEService(){
        Log.i("myTag","unBindBLEService");

        unregisterReceiver(mGattUpdateReceiver);
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
