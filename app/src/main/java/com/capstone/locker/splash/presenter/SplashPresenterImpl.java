package com.capstone.locker.splash.presenter;

import com.capstone.locker.splash.view.SplashView;

/**
 * Created by kh on 2016. 10. 4..
 */
public class SplashPresenterImpl implements SplashPresenter {

    SplashView view;

    public SplashPresenterImpl(SplashView view) {
        this.view = view;
    }

}
