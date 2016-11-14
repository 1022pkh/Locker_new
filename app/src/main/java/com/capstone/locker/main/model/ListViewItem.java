package com.capstone.locker.main.model;

public class ListViewItem {
    public int id;
    public int img;
    public String qualification;
    public String nickName;

    public ListViewItem(int id, int img, String qualification, String nickName) {
        this.id = id;
        this.img = img;
        this.qualification = qualification;
        this.nickName = nickName;
    }
}
