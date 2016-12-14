package com.capstone.locker.main.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.capstone.locker.Buletooth.presenter.BluetoothLeService;
import com.capstone.locker.Buletooth.view.DeviceSearchActivity;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.database.ItemData;
import com.capstone.locker.detail.DetailActivity;
import com.capstone.locker.main.model.ListViewItem;
import com.capstone.locker.main.presenter.CustomAdapter;
import com.capstone.locker.modify.ModifyActivity;
import com.capstone.locker.modify.ModifyGuestActivity;
import com.capstone.locker.order.OrderActivity;
import com.capstone.locker.register.RegisterActivity;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainView{

    private ArrayList<ListViewItem> itemDatas = null;
    private ArrayList<ListViewItem> connectItemDatas = null;

    @BindView(R.id.listview)
    SwipeMenuListView listView;
    @BindView(R.id.registerBtn)
    ImageView registerModuleBtn;
    @BindView(R.id.connectArea)
    LinearLayout inflatedLayout;

    SwipeMenuListView connectListView;
    CustomAdapter connectCustomAdapter;

    CustomAdapter customAdapter;
    DbOpenHelper mDbOpenHelper;

    int connectModuleDBId = -1;
    String connectModuleNum = "";
    Boolean registerBLE = false;

    //Back 키 두번 클릭 여부 확인
    private final long FINSH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    public BluetoothLeService mBluetoothLeService;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            }


        }
    };

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
////                Log.e(TAG, "Unable to initialize Bluetooth");
////                finish();
//            }
            // Automatically connects to the device upon successful start-up initialization.
//            if(ApplicationController.getInstance().mDeviceAddress.length()>0)
//                mBluetoothLeService.connect(ApplicationController.getInstance().mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;

            ApplicationController.getInstance().mDeviceName = "";
            ApplicationController.getInstance().mDeviceAddress = "";
            ApplicationController.editor.putBoolean("Connect_check", false);
            ApplicationController.editor.commit();

            ChangeConnectArea();

            Toast.makeText(getApplicationContext(),"연결이 해제되었습니다.\n다시 연결해주세요.",Toast.LENGTH_SHORT).show();

            Log.i("myTag","application onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 21) {   //상태바 색
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;


        itemDatas = new ArrayList<ListViewItem>();
        // itemDatas 들어갈 자료를 추가

        itemDatas =  mDbOpenHelper.DbMainSelect();




        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        /**
         * 리스트뷰에 필요한 정보
         * 0. id
         * 1. icon
         * 2. qualification
         * 3. nickname
         */

        // 들어갈 자료를 ListView에 지정
        customAdapter = new CustomAdapter(itemDatas, getApplicationContext());
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("myTag", String.valueOf(itemDatas.get(position).id));
//                Toast.makeText(getApplicationContext(), String.valueOf(itemDatas.get(position).id)+"번 리스트가 클릭 되었습니다.",Toast.LENGTH_SHORT).show();

            }
        });



        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                // set item width
                openItem.setWidth(dp2px(70));

                openItem.setIcon(R.drawable.ic_detail);

                // add to menu
                menu.addMenuItem(openItem);


                // create "modify" item
                SwipeMenuItem modifyItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                modifyItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                // set item width
                modifyItem.setWidth(dp2px(70));

                modifyItem.setIcon(R.drawable.ic_modify);

                // add to menu
                menu.addMenuItem(modifyItem);


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                // set item width
                deleteItem.setWidth(dp2px(70));

                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };


        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
                //.setBackgroundColor(Color.GRAY);
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });


        // set creator
        listView.setMenuCreator(creator);

        // step 2. listener item click event
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {

                Intent intent;
                switch (index) {
                    case 0:
                        //Toast.makeText(getApplicationContext(), "상세 보기",Toast.LENGTH_SHORT).show();

                        intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra("id",String.valueOf(itemDatas.get(position).id));
                        startActivity(intent);

                        break;
                    case 1:
//                        Toast.makeText(getApplicationContext(), "수정",Toast.LENGTH_SHORT).show();

                        if(itemDatas.get(position).qualification.equals("Owner")){
                            intent = new Intent(getApplicationContext(), ModifyActivity.class);
                            intent.putExtra("id",String.valueOf(itemDatas.get(position).id));
                            startActivity(intent);
                        }
                        else{
                            intent = new Intent(getApplicationContext(), ModifyGuestActivity.class);
                            intent.putExtra("id",String.valueOf(itemDatas.get(position).id));
                            startActivity(intent);
                        }


                        break;
                    case 2:
//                        Toast.makeText(getApplicationContext(), "삭제",Toast.LENGTH_SHORT).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);     // 여기서 this는 Activity의 this

                        // 여기서 부터는 알림창의 속성 설정
                        builder.setMessage("삭제하시겠습니까?")        // 메세지 설정
                                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                    // 확인 버튼 클릭시 설정
                                    public void onClick(DialogInterface dialog, int whichButton){
                                        // db 삭제
                                        mDbOpenHelper.DbDelete(String.valueOf(itemDatas.get(position).id));
                                        // 리스트 삭제
                                        // 1. 아이템 삭제
                                        itemDatas.remove(position) ;

                                        // 2. listview 갱신.
                                        customAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                                    // 취소 버튼 클릭시 설정
                                    public void onClick(DialogInterface dialog, int whichButton){
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog dialog = builder.create();    // 알림창 객체 생성
                        dialog.show();    // 알림창 띄우기

                        break;
                }
                return false;
            }
        });


        /**
         * 현재 연결된 장치 체크
         */
        ChangeConnectArea();


    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){
            ApplicationController.getInstance().unBindBLEService();
        }
        else{

        }

//
//        ApplicationController.editor.putBoolean("Connect_check", false);
//        ApplicationController.editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // 넘어갔던 화면에서 되돌아 왔을 때
        if (resultCode == RESULT_OK) {
            //블루투스 맥 주소
            Log.i("myTag",ApplicationController.getInstance().mDeviceAddress);

            /**
             * 연결된 잠금장치는 위로 올려준다
             */
            ChangeConnectArea();

        }
        else{
            Log.i("myTag","onActivityResult failed");

        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

//        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){
//
//        }
//        else{
//
//        }
//
//        ItemData getItem = mDbOpenHelper.DbFindMoudle(ApplicationController.getInstance().mDeviceAddress);
//
//        if(getItem.identNum == null)
//        {
//            ApplicationController.getInstance().unBindBLEService();
//            ApplicationController.editor.putBoolean("Connect_check", false);
//            ApplicationController.editor.commit();
//        }



        itemDatas =  mDbOpenHelper.DbMainSelect();
        customAdapter.setItemDatas(itemDatas);
        ChangeConnectArea();
    }


    @OnClick(R.id.registerBtn)
    public void registerModule(){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.connectArea)
    public void connectEvent(){

        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){

            if(registerBLE){
                // 명령페이지 이동
                Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                startActivity(intent);
            }

        }
        //미연결 상태
        else{
            //연결화면으로
            Intent intent = new Intent(getApplicationContext(), DeviceSearchActivity.class);
//            startActivity(intent);
            startActivityForResult(intent, 1); // requestCode
        }
    }

    public void ChangeConnectArea(){

        inflatedLayout.removeAllViews();


        if(ApplicationController.connectInfo.getBoolean("Connect_check", false)){

            connectModuleNum = ApplicationController.getInstance().mDeviceAddress;
//            Log.i("myTag", connectModuleNum);

            ItemData getItem = mDbOpenHelper.DbFindMoudle(connectModuleNum);


            // 연결은 된 상태 + 등록된 장치
            if(getItem.identNum != null){

                registerBLE = true;

                connectItemDatas = new ArrayList<ListViewItem>();
                connectItemDatas.add(new ListViewItem(getItem.Id,getItem.icon,getItem.qualificaion,getItem.nickName));

                LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // Inflated_Layout.xml로 구성한 레이아웃을 inflatedLayout 영역으로 확장
                inflater.inflate(R.layout.connect_exist_listview, inflatedLayout);



                connectListView = (SwipeMenuListView)findViewById(R.id.connectListview);
                connectListView.setVerticalScrollBarEnabled(false);
                connectListView.setOverScrollMode(View.OVER_SCROLL_NEVER); // 효과 없음


                // 들어갈 자료를 ListView에 지정
                connectCustomAdapter = new CustomAdapter(connectItemDatas, getApplicationContext());
                connectListView.setAdapter(connectCustomAdapter);

                connectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("myTag", String.valueOf(connectItemDatas.get(position).id));

                    }
                });




                SwipeMenuCreator creator = new SwipeMenuCreator() {

                    @Override
                    public void create(SwipeMenu menu) {
                        // create "open" item
                        SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                        // set item background
                        openItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                        // set item width
                        openItem.setWidth(dp2px(70));

                        openItem.setIcon(R.drawable.ic_detail);

                        // add to menu
                        menu.addMenuItem(openItem);


                        // create "modify" item
                        SwipeMenuItem modifyItem = new SwipeMenuItem(getApplicationContext());
                        // set item background
                        modifyItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                        // set item width
                        modifyItem.setWidth(dp2px(70));

                        modifyItem.setIcon(R.drawable.ic_modify);

                        // add to menu
                        menu.addMenuItem(modifyItem);


                        // create "delete" item
                        SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                        // set item background
                        deleteItem.setBackground(new ColorDrawable(Color.rgb(63, 81, 181)));
                        // set item width
                        deleteItem.setWidth(dp2px(70));

                        deleteItem.setIcon(R.drawable.bluetooth);
                        // add to menu
                        menu.addMenuItem(deleteItem);
                    }
                };


                connectListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

                    @Override
                    public void onSwipeStart(int position) {
                        // swipe start
                        //.setBackgroundColor(Color.GRAY);
                    }

                    @Override
                    public void onSwipeEnd(int position) {
                        // swipe end
                    }
                });

                connectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                        intent.putExtra("id",String.valueOf(connectItemDatas.get(position).id));
                        startActivity(intent);
                    }
                });

                // set creator
                connectListView.setMenuCreator(creator);

                // step 2. listener item click event
                connectListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {

                        Intent intent;
                        switch (index) {
                            case 0:
                                intent = new Intent(getApplicationContext(), DetailActivity.class);
                                intent.putExtra("id",String.valueOf(connectItemDatas.get(position).id));
                                startActivity(intent);

                                break;
                            case 1:

                                if(connectItemDatas.get(position).qualification.equals("Owner")){
                                    intent = new Intent(getApplicationContext(), ModifyActivity.class);
                                    intent.putExtra("id",String.valueOf(connectItemDatas.get(position).id));
                                    startActivity(intent);
                                }
                                else{
                                    intent = new Intent(getApplicationContext(), ModifyGuestActivity.class);
                                    intent.putExtra("id",String.valueOf(connectItemDatas.get(position).id));
                                    startActivity(intent);
                                }


                                break;
                            case 2:

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);     // 여기서 this는 Activity의 this

                                // 여기서 부터는 알림창의 속성 설정
                                builder.setMessage("연결해제하시겠습니까?")        // 메세지 설정
                                        .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                            // 확인 버튼 클릭시 설정
                                            public void onClick(DialogInterface dialog, int whichButton){

                                                ApplicationController.getInstance().unBindBLEService();

                                                ApplicationController.getInstance().mDeviceName = "";
                                                ApplicationController.getInstance().mDeviceAddress = "";
                                                ApplicationController.editor.putBoolean("Connect_check", false);
                                                ApplicationController.editor.commit();

                                                ChangeConnectArea();

                                            }
                                        })
                                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                                            // 취소 버튼 클릭시 설정
                                            public void onClick(DialogInterface dialog, int whichButton){
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                                dialog.show();    // 알림창 띄우기

                                break;
                        }
                        return false;
                    }
                });


                connectModuleDBId = getItem.Id;
//                Log.i("myTag", String.valueOf(connectModuleDBId));

                for(int i =0;i<itemDatas.size();i++){
                    if(itemDatas.get(i).id == connectModuleDBId){
                        itemDatas.remove(i) ;
                        break;
                    }
                }

//            itemDatas.remove(connectModuleDBId) ;
//             2. listview 갱신.
                customAdapter.notifyDataSetChanged();

            }
            //연결 + 등록 안된 장치
            else{
                Log.i("myTag","unregister");
                registerBLE = false;

                LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // Inflated_Layout.xml로 구성한 레이아웃을 inflatedLayout 영역으로 확장
                inflater.inflate(R.layout.connect_exist_unregister, inflatedLayout);

            }
        }
        //미연결 상태
        else{
            registerBLE = false;

            LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Inflated_Layout.xml로 구성한 레이아웃을 inflatedLayout 영역으로 확장
            inflater.inflate(R.layout.connect_null, inflatedLayout);

            itemDatas =  mDbOpenHelper.DbMainSelect();
            customAdapter.setItemDatas(itemDatas);
        }


    }


    @Override
    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        /**
         * Back키 두번 연속 클릭 시 앱 종료
         */

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
        }
        else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(),"뒤로 가기 키을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }


}
