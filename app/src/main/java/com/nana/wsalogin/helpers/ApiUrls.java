package com.nana.wsalogin.helpers;

/**
 * Created by NanaYaw on 5/12/2017.
 */

public class ApiUrls {
    public static final String BASE_URL = "http://192.168.43.38:1234/autoirrigation/public/api";
    public static final String LOGIN_URL = BASE_URL + "/user/login"; //Url for the login
    public static final String LOGOUT_URL = BASE_URL + "/user/logout";
    public static final String POST_USER_SYSTEMS_URL = BASE_URL + "/user/system/all";
    public static final String POST_USER_SINGLE_SYSTEM_URL = BASE_URL + "/user/system/";
    public static final String POST_USER_RECORDED_SYSTEMS_URL = BASE_URL + "/user/system/recorded/";
    public static final String POST_PUMP_SWITCH_URL = BASE_URL + "/user/system/pump/";
    public static final String POST_DEVICE_TOKEN_URL = BASE_URL+ "/user/device/token/";

}
