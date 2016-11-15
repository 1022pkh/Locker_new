package com.capstone.locker.Buletooth.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.presenter.DeviceAdapter;
import com.capstone.locker.Buletooth.presenter.SampleGattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.order.OrderActivity;
import com.capstone.locker.register.RegisterActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DeviceSearchActivity extends ActionBarActivity {

    @BindView(R.id.listview)
    ListView listView;

    private DeviceAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;



    private String mDeviceName;
    private String mDeviceAddress;
    private final static String TAG = DeviceSearchActivity.class.getSimpleName();
    //    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    private ProgressDialog mProgressDialog;
    private ProgressDialog mConnectDialog;

    Handler progressHanlder;
    Handler connectHanlder;

    Runnable progressRunnable;
    Runnable connectRunnable;


    /**
     * 두 번 클릭되었을 경우 다이얼로그 및 진행다이얼로그가 2개 뜨는 문제를 해결하기위해
     * 만약 하나가 show되었을 경우 안보이도록 설정
     */
    Boolean mProgressCheck = false;
    Boolean mConneckCheck = false;
    Boolean mDialogCheck = false;

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

                Log.i("myTag","BroadcastReceiver 연결성공");

                invalidateOptionsMenu();

                if(mConnectDialog!=null){
                    mConnectDialog.dismiss();
                    mConneckCheck = false;
//                    connectHanlder.removeCallbacks(connectRunnable);
                }


                /**
                 * 이미 등록된 기기는 유효성 검사를 할 필요가 없다.
                 */
                ItemData getItem = ApplicationController.getInstance().mDbOpenHelper.DbFindMoudle(mDeviceAddress);

                // 등록된 장치
                if(getItem.identNum != null){
                    alreadyRegisterBLE();
                }
                // 등록되지 않은 장치
                else{
                    /**
                     * ConnectProgress는 제거해주고
                     * 유효성 검사를 진행한다.
                     */
                    if(mConnectDialog!=null){
                        mConnectDialog.dismiss();
                        mConneckCheck = false;
//                        connectHanlder.removeCallbacks(connectRunnable);
                    }

                    showServiceDiscoveryAlert();
                }


            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i("myTag","BroadcastReceiver 연결 해제");

                if(mConnectDialog!=null){
                    mConnectDialog.dismiss();
                    mConneckCheck = false;
//                    connectHanlder.removeCallbacks(connectRunnable);
                }

                if(mProgressDialog!=null && mProgressCheck==true ){
                    mProgressDialog.dismiss();
                    mProgressCheck = false;
                    Toast.makeText(getApplicationContext(),"유효하지않은 장치입니다.",Toast.LENGTH_SHORT).show();
//                    progressHanlder.removeCallbacks(progressRunnable);
                }


                mConnected = false;
//                updateConnectionState(R.string.disconnected);
                ApplicationController.getInstance().mDeviceAddress = "";
                ApplicationController.getInstance().mDeviceName = "";

//                ApplicationController.editor.putString("mDeviceAddress", "");
//                ApplicationController.editor.putString("mDeviceName", "");
                ApplicationController.editor.putBoolean("Connect_check", false);
                ApplicationController.editor.commit();

                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                Log.i("myTag","BroadcastReceiver GATT 서비스 발견");

                // Show all the supported services and characteristics on the user interface.
                displayGattServices(ApplicationController.getInstance().mBluetoothLeService.getSupportedGattServices());

                if(mConnectDialog!=null && mConneckCheck == true ){
                    mConnectDialog.dismiss();
                    mConneckCheck = false;
//                    connectHanlder.removeCallbacks(connectRunnable);
                }
                if(mProgressDialog!=null && mProgressCheck == true){
                    mProgressDialog.dismiss();
                    mProgressCheck = false;
//                    progressHanlder.removeCallbacks(progressRunnable);
                }


                /**
                 * 우리 어플리케이션을 이용할 수 있는 GATT 서비스가 있는 지 체크
                 */
                //있을 경우
                if (mGattCharacteristics.size() > 0){
                    ItemData getItem = ApplicationController.getInstance().mDbOpenHelper.DbFindMoudle(mDeviceAddress);

                    // 등록된 장치
                    if(getItem.identNum != null){
                        ;
                    }
                    // 등록되지 않은 장치
                    else{
                        notYetRegisterBLE();
                    }
                }
                //없을 경우
                else{
                    Toast.makeText(getApplicationContext(),"해당 앱을 이용할 수 없는 장치입니다.",Toast.LENGTH_SHORT).show();
                    connectHanlder.removeCallbacks(connectRunnable);
                    progressHanlder.removeCallbacks(progressRunnable);
                }


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }


            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDING) {
                    // Bonding...
                    Log.i("myTag","BOND_BONDING");
                } else if (state == BluetoothDevice.BOND_BONDED) {
                    Log.i("myTag","BOND_BONDED");

                } else if (state == BluetoothDevice.BOND_NONE) {
                    Log.i("myTag","BOND_NONE");
                }
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 21) {   //상태바 색
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // ActionBar의 배경색 변경
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF3F51B5));

        getSupportActionBar().setElevation(0); // 그림자 없애기

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_layout, null);

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


        /**
         *
         */

        mProgressDialog = new ProgressDialog(DeviceSearchActivity.this);
        mProgressDialog.setCancelable(false);

        mConnectDialog = new ProgressDialog(DeviceSearchActivity.this);
        mConnectDialog.setCancelable(false);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


    }

    private void showServiceConnectAlert() {

        mConnectDialog.setTitle("연결 중");
        mConnectDialog.setMessage("연결을 시도합니다.");
        mConnectDialog.setIndeterminate(true);
        mConnectDialog.setCancelable(false);
        mConnectDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if(mConneckCheck == false) {
            mConnectDialog.show();
            mConneckCheck = true;
        }

        connectHanlder = new Handler();

        connectRunnable = new Runnable() {
            @Override
            public void run() {
                if(mConnectDialog!=null && mConneckCheck == true){
                    mConnectDialog.dismiss();
                    mConneckCheck = false;
                    Toast.makeText(getApplicationContext(),"Connect Timeout\n다시 시도해주세요.",Toast.LENGTH_SHORT).show();

                }
            }
        };


        connectHanlder.postDelayed(connectRunnable, SERVICE_DISCOVERY_TIMEOUT);


    }



    private void showServiceDiscoveryAlert() {

        mProgressDialog.setTitle("유효성 검사");
        mProgressDialog.setMessage("해당 잠금장치에 대한 유효성 검사를 시작합니다.");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if(mProgressCheck == false){
            mProgressDialog.show();
            mProgressCheck = true;
        }


        progressHanlder = new Handler();

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if(mConnectDialog!=null && mConneckCheck == true){
                    mProgressDialog.dismiss();
                    mProgressCheck = false;
                    Toast.makeText(getApplicationContext(),"validity Timeout\n다시 시도해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        };


        progressHanlder.postDelayed(progressRunnable, SERVICE_DISCOVERY_TIMEOUT);


    }

    public void alreadyRegisterBLE(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSearchActivity.this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        builder.setTitle("등록된 장치입니다.")        // 메세지 설정
                .setMessage("연결하시겠습니까?")
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){

                        ApplicationController.getInstance().mDeviceName = mDeviceName;
                        ApplicationController.getInstance().mDeviceAddress = mDeviceAddress;
                        ApplicationController.editor.putBoolean("Connect_check", true);
                        ApplicationController.editor.commit();


                        moveMainPage();

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                        mDialogCheck = false;
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성

        if(mDialogCheck == false){
            dialog.show();    // 알림창 띄우기
            mDialogCheck = true;
        }
    }


    public void notYetRegisterBLE(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSearchActivity.this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        builder.setTitle("등록되지 않은 장치입니다.")        // 메세지 설정
                .setMessage("등록하시겠습니까?")
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){

                        ApplicationController.getInstance().mDeviceName = mDeviceName;
                        ApplicationController.getInstance().mDeviceAddress = mDeviceAddress;


//                                ApplicationController.editor.putString("mDeviceAddress", mDeviceAddress);
//                                ApplicationController.editor.putString("mDeviceName", mDeviceName);
                        ApplicationController.editor.putBoolean("Connect_check", true);
                        ApplicationController.editor.commit();

                        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                        mDialogCheck = false;
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        if(mDialogCheck == false){
            dialog.show();    // 알림창 띄우기
            mDialogCheck = true;
        }
    }

    public void moveMainPage(){
//        Intent intent = getIntent();
//        setResult(RESULT_OK, intent);
//        finish();
        final Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
        finish();

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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new DeviceAdapter(getApplicationContext());
        listView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);

        /**
         * 검색된 블루투스 디바이스에 대한 클릭이벤트
         */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showServiceConnectAlert();

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

                if (device == null)
                    return;
//                final Intent intent = new Intent(getApplicationContext(), DeviceControlActivity.class);
//
//                Log.i("myTag",String.valueOf(device.getName()));
//                Log.i("myTag",String.valueOf(device.getAddress()));
//
//                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
//                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//
//                if (mScanning) {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    mScanning = false;
//                }
                /**
                 *
                 */
                //startActivity(intent);

                mDeviceName = device.getName();
                mDeviceAddress = device.getAddress();

                ApplicationController.getInstance().mDeviceAddress =  mDeviceAddress;
                ApplicationController.getInstance().mDeviceName = mDeviceName;

//                ApplicationController.editor.putString("mDeviceAddress", mDeviceAddress);
//                ApplicationController.editor.putString("mDeviceName", mDeviceName);
//                ApplicationController.editor.commit();

                Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
                bindService(gattServiceIntent, ApplicationController.getInstance().mServiceConnection, BIND_AUTO_CREATE);



                if (ApplicationController.getInstance().mBluetoothLeService != null) {
                    final boolean result = ApplicationController.getInstance().mBluetoothLeService.connect(mDeviceAddress);
                    Log.i("myTag", "Connect request result=" + result);
                }


            }
        });

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        unregisterReceiver(mGattUpdateReceiver);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.i("myTag","uuid : " + device.getUuids().toString());

                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


}