package com.devf5r.msg.Utils;


public class Constant {

    public static final String AD_STATUS_ON = "on";
    public static final String ADMOB = "admob";
    public static final String FAN = "fan";
    public static final String STARTAPP = "startapp";
    public static final String UNITY = "unity";
    public static final String APPLOVIN = "applovin";


    public static final String LOAD_API = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2FsbWdybzdhbDNuenkvY2hlY2tkZXYvbWFpbi9kZXYudHh0";
    public static final String URL_DATA = Tools.decodeString(LOAD_API);

    public static final String LOAD_TEMP = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2FsbWdybzdhbDNuenkvY2hlY2tkZXYvbWFpbi9wcS50eHQ=";
    public static final String URL_TEMP = Tools.decodeString(LOAD_TEMP);

    public static final String PQ = "REVWRjVSLVBR";
    public static final String APP_NAME = Tools.decodeString(PQ);

    //startapp native ad image parameters
    public static final int STARTAPP_IMAGE_XSMALL = 1; //for image size 100px X 100px
    public static final int STARTAPP_IMAGE_SMALL = 2; //for image size 150px X 150px
    public static final int STARTAPP_IMAGE_MEDIUM = 3; //for image size 340px X 340px
    public static final int STARTAPP_IMAGE_LARGE = 4; //for image size 1200px X 628px

    //unity banner ad size
    public static final int UNITY_ADS_BANNER_WIDTH = 320;
    public static final int UNITY_ADS_BANNER_HEIGHT = 50;
    // This App is Created by Abbas Developers
    public static final int MAX_NUMBER_OF_NATIVE_AD_DISPLAYED = 25;
    public static final int BANNER_HOME = 1;
    public static final int BANNER_POST_DETAIL = 1;
    public static final int BANNER_CATEGORY_DETAIL = 1;
    public static final int BANNER_SEARCH = 1;
    public static final int INTERSTITIAL_POST_LIST = 1;
    public static final int NATIVE_AD_POST_DETAIL = 1;

}