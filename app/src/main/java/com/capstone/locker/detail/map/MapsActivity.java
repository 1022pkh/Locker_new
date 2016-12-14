package com.capstone.locker.detail.map;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.util.GattAttributes;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.detail.model.MarkerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    View marker_root_view;
    ImageView iv_marker;

    @BindView(R.id.resetBtn)
    ImageView resetBtn;

    Boolean LatCheck = false;
    Boolean LngCheck = false;

    double lag;
    double lng;

    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothLeService mBluetoothLeService;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

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


            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String result = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("myTag", ">>>>get2 : " + result);


                if(result.equals("150")){
                    //위도
                    LatCheck = true;
                    lag = Double.valueOf(result);

                }
                else if(result.equals("160")){
                    //경도

                    LngCheck = true;
                    lng = Double.valueOf(result);

                }

                if(LatCheck && LngCheck){
                    LatLng marketPoint = new LatLng(lag,lng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marketPoint,18));


                    setCustomMarkerView();
                    getSampleMarkerItems();
                }
            }

        }
    };

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
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ButterKnife.bind(this);


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
        if(mWriteCharacteristic != null) {
            BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, 0, 0, 0, 30);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
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


    @OnClick(R.id.resetBtn)
    public void reloadLocation(){
        LatCheck = false;
        LngCheck = false;
        BluetoothLeService.writeCharacteristicRGB(mWriteCharacteristic, 0, 0, 0, 80);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng marketPoint = new LatLng(37.5515434,127.0741555);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marketPoint,18));


        setCustomMarkerView();
        getSampleMarkerItems();

    }

    private void setCustomMarkerView() {

        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_maps_tag, null);
        iv_marker = (ImageView) marker_root_view.findViewById(R.id.iv_marker);
    }

    private void getSampleMarkerItems() {

        //임의로 위치 4개 지정
        MarkerItem markerItem = new MarkerItem(37.5515434,127.0741555, R.drawable.ic_picker);
        addMarker(markerItem, false);

    }

    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker) {


        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());

        if (isSelectedMarker) {
            iv_marker.setBackgroundResource(R.drawable.ic_picker);
        } else {
            iv_marker.setBackgroundResource(R.drawable.ic_picker);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));

        return mMap.addMarker(markerOptions);
    }
    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        mCurrentservice = ApplicationController.getInstance().current_characteristic;

        if(mCurrentservice != null) {
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
        }
//        else{
//            Toast.makeText(getApplicationContext(),"연결 확인 후 재시도해주세요.",Toast.LENGTH_SHORT).show();
//        }
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

}
