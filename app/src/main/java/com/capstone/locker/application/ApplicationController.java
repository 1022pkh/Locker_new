package com.capstone.locker.application;

import android.app.Application;

import com.capstone.locker.database.DbOpenHelper;

import java.sql.SQLException;

public class ApplicationController extends Application {

    private static ApplicationController instance;

    public static ApplicationController getInstance() {
        return instance;
    }

    public DbOpenHelper mDbOpenHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationController.instance = this;
        this.buildDB();
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
