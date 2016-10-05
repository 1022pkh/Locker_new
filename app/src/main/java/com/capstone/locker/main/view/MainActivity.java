package com.capstone.locker.main.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.capstone.locker.R;
import com.capstone.locker.application.ApplicationController;
import com.capstone.locker.database.DbOpenHelper;
import com.capstone.locker.detail.DetailActivity;
import com.capstone.locker.main.model.ListViewItem;
import com.capstone.locker.main.presenter.CustomAdapter;
import com.capstone.locker.register.RegisterActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainView{

    private ArrayList<ListViewItem> itemDatas = null;

    @BindView(R.id.listview)
    SwipeMenuListView listView;
    @BindView(R.id.registerBtn)
    ImageView registerModuleBtn;

    CustomAdapter customAdapter;


    DbOpenHelper mDbOpenHelper;

    //Back 키 두번 클릭 여부 확인
    private final long FINSH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;

        itemDatas = new ArrayList<ListViewItem>();
        // itemDatas 들어갈 자료를 추가

        itemDatas =  mDbOpenHelper.DbMainSelect();

        Log.i("myTag",itemDatas.get(0).nickName);
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
//                Log.i("myTag", String.valueOf(itemDatas.get(position).id));
                Toast.makeText(getApplicationContext(), String.valueOf(itemDatas.get(position).id)+"번 리스트가 클릭 되었습니다.",Toast.LENGTH_SHORT).show();
            }
        });



        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(226, 202, 174)));
                // set item width
                openItem.setWidth(dp2px(70));
                // set item title
                openItem.setTitle("상세\n보기");
                // set item title fontsize
                openItem.setTitleSize(15);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);


                // create "modify" item
                SwipeMenuItem modifyItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                modifyItem.setBackground(new ColorDrawable(Color.rgb(114,109,58)));
                // set item width
                modifyItem.setWidth(dp2px(70));
                // set item title
                modifyItem.setTitle("수정");
                // set item title fontsize
                modifyItem.setTitleSize(15);
                // set item title font color
                modifyItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(modifyItem);


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(70));
                // set item title
                deleteItem.setTitle("삭제");
                // set item title fontsize
                deleteItem.setTitleSize(15);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // set a icon
//                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);

        // step 2. listener item click event
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //Toast.makeText(getApplicationContext(), "상세 보기",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra("id",String.valueOf(itemDatas.get(position).id));
                        startActivity(intent);

                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "수정",Toast.LENGTH_SHORT).show();
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


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        itemDatas =  mDbOpenHelper.DbMainSelect();
        customAdapter.setItemDatas(itemDatas);
    }


    @OnClick(R.id.registerBtn)
    public void registerModule(){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
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
