package com.capstone.locker.main.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.locker.R;
import com.capstone.locker.main.model.ListViewItem;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter{

    // 아무것도 없을 때 빨간 줄
    // 기본적으로 무조건 적어줘야 하는 Override 메소드가 있기 때문 !!

    private ArrayList<ListViewItem> itemDatas = null;
    private LayoutInflater layoutInflater = null;

    //생성자
    public CustomAdapter(ArrayList<ListViewItem> itemDatas, Context ctx){
        this.itemDatas = itemDatas;
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItemDatas(ArrayList<ListViewItem> itemDatas){
        this.itemDatas = itemDatas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemDatas != null ? itemDatas.size():0;
    }

    @Override
    public Object getItem(int position) {
        return (itemDatas != null && ( position>=0 && position < itemDatas.size()) ? itemDatas.get(position):null);
    }

    @Override
    public long getItemId(int position) {
        return (itemDatas != null && ( position>=0 && position < itemDatas.size()) ? position:0);
    }



    /**
     * ViewHolder Pattern을 이용할 경우
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.main_list_item,parent,false);

            viewHolder.TextView_name = (TextView)convertView.findViewById(R.id.moduleName);
            viewHolder.TextView_qualification = (TextView)convertView.findViewById(R.id.modulequalification);
            viewHolder.img_item = (ImageView)convertView.findViewById(R.id.profile_image);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        ListViewItem itemData_temp = itemDatas.get(position);

        viewHolder.TextView_name.setText(itemData_temp.nickName);
        viewHolder.TextView_qualification.setText(itemData_temp.qualification);

        if(itemData_temp.img == 1){ //cycle
            viewHolder.img_item.setImageResource(R.drawable.ic_owner);
        }
        else if(itemData_temp.img == 2){ //lock
            viewHolder.img_item.setImageResource(R.drawable.ic_lock);
        }
        else if(itemData_temp.img == 3){ //owner
            viewHolder.img_item.setImageResource(R.drawable.ic_cycle);
        }
        return convertView;
    }

    public class ViewHolder {
        TextView TextView_name;
        TextView TextView_qualification;
        ImageView img_item;
    }

}
