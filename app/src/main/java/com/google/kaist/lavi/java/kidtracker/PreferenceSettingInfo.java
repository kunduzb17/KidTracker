package com.google.kaist.lavi.java.kidtracker;

import java.util.ArrayList;
import java.util.List;

public class PreferenceSettingInfo {
    //프리퍼런스 저장 변수-사용자 정보-프리퍼런스 파일에서 읽어 옴.
    String UserEmail = null;				//사용자 이메일
    String UserName = null;					//사용자 이름
    String UserPhoneNumber = null;			//사용자 전화번호

    String DeviceListFileName = null;	//BLE 장치 정보를 저장한 파일 이름
    String ContactListFileName = null;	//SMS 보낼 연락처 정보를 저장한 파일 이름


    //센서장치 데이타 저장 변수
    boolean IsPressure = false;     //좌석 착지 여부(true : ON, false : OFF)
    int BatteryValue = -1;			//좌석 배터리 잔량(= -1 : 초기화 데이타, > 0 : 실제 데이타)
    int TemperatureValue = 0;	    //좌석 온도
    int RSSI = -80;					//BLE RSSI 값

    //프리퍼런스 저장 static 변수-센서장치 정보
    int Leave_RSSI_Value = -80;			//이탈로 인식하는 RSSI 값
    List<String> bleList = new ArrayList<String>();
}
