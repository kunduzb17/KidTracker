package com.google.kaist.lavi.java.kidtracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

public class KidTrackerApplication extends Application {

//    private static final String msHttpsSiteURL = "https://www.365rcare.co.kr";	//365RCARE 사이트 주소-상용서비스 서버 주소

    private SharedPreferences mSharedPreferences;		//프리퍼런스 관련변수
    private PreferenceSettingInfo mPreferenceSettingInfo;	//프리퍼런스 데이타 저장 클래스

    private static KidTrackerApplication instance;
    private static Context appContext;

    public static KidTrackerApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        this.appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        this.setAppContext(getApplicationContext());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        InitPreferenceSetting();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }



    private void InitPreferenceSetting() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mPreferenceSettingInfo = new PreferenceSettingInfo();

        //프리퍼런스 값 셋팅-시작
        //사용자 이메일
        //Defautl value = null
        String sRet = mSharedPreferences.getString("pre_email", null);
        if (sRet != null)
            mPreferenceSettingInfo.UserEmail = sRet;

        //사용자 이름
        //Defautl value = null
        sRet = mSharedPreferences.getString("pre_name", null);
        if (sRet != null)
            mPreferenceSettingInfo.UserName = sRet;

        //사용자 전화번호
        //Defualt value = null
        sRet = mSharedPreferences.getString("pre_phonenumber",  null);
        if (sRet != null)
            mPreferenceSettingInfo.UserPhoneNumber = sRet;

        mPreferenceSettingInfo.DeviceListFileName = "device_list.txt";      //BLE 장치 정보를 저장한 파일 이름
        mPreferenceSettingInfo.ContactListFileName = "contact_list.txt";    //SMS 보낼 연락처 정보를 저장한 파일 이름

    }

    //프리퍼런스 셋팅 값
    public PreferenceSettingInfo getPreferenceSettingInfo() {
        return mPreferenceSettingInfo;
    }

    public void SavePreferenceInfo() {
        //프리퍼런스 갱신
        SharedPreferences.Editor pedit = mSharedPreferences.edit();

        pedit.putString("pre_email", mPreferenceSettingInfo.UserEmail); //사용자 이메일
        pedit.putString("pre_name", mPreferenceSettingInfo.UserName);   //사용자 이름
        pedit.putString("pre_phonenumber", mPreferenceSettingInfo.UserPhoneNumber); //사용자 전화번호

        pedit.commit();

    }
}