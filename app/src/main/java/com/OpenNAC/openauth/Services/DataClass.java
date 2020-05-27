package com.OpenNAC.openauth.Services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.format.Formatter;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DataClass {

    static Build.VERSION version;

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "no mac address found";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public static String getFirebaseToken(){
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful()){
//                        tkn = task.getResult().getToken();
//                        System.out.println("token in task is: " + tkn);
//                        tokenview.setText(tkn);
//                    }
//                    else{
//                        System.out.println("Token not generated!" + task.getException().getMessage());
//                    }
//                });
        String tkn = FirebaseInstanceId.getInstance().getToken();
        return tkn;
    }
    public static String getVendor(){
        return Build.MANUFACTURER;
    }

    public static String getOS(){
        return "ANDROID";
    }

    public static String getVersion(){
        return version.RELEASE;
    }

    public static String getModel(){
        return Build.MODEL;
    }

    public static String getBrand () { return Build.BRAND; }

    public static String getSecurityPatch(){return  version.SECURITY_PATCH; }
    //hostname 2
    public static String getIp(WifiInfo wifiInfo){
        int ip = wifiInfo.getIpAddress(); //3
        String ipAddress = Formatter.formatIpAddress(ip);
        return ipAddress;
    }
    public static String getHostName(Context context){
        String hostName;
        if(Settings.Secure.getString(context.getContentResolver(), "bluetooth_name") != null){
            hostName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        }
        else{
            hostName = "default hostname";
        }
        Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        return hostName;
    }
    public static String getSsid(WifiInfo wifiInfo){
//        wifiInfo.getSSID();
//        wifiInfo.getSSID();
        String ssid = wifiInfo.getSSID();
        return ssid;
    }

    public static String getLocation (Context context, double lat, double lon){
        String cityName = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(lat,lon, 10);
            if (addresses.size()>0){
                for (Address adr : addresses){
                    if (adr.getLocality() != null && adr.getLocality().length() > 0){
                        cityName = adr.getLocality() +" country is " + adr.getCountryName();
                        break;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    // ssid // |-
    // hostname
    // model // |-
    // security patch // |-
    // ip address // |-
    // timestamp // |-
    // <<location>> // |-
        public static String getTimeStamp (){
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss z");
            String currentDate = simpleDateFormat.format(calendar.getTime());
        return currentDate;
        }


//        public void getdetails(){
//        String versionStr;
//        System.out.println("hardware is equal to " + Build.PRODUCT);
//        System.out.println("device is equal to " + Build.DEVICE);
//        System.out.println("model is equal to " + Build.MODEL); //
//        System.out.println("manufacturer is equal to " + Build.MANUFACTURER); //
//        System.out.println("base os is equal to " + version.BASE_OS);
//        System.out.println("sdk int is equal to " + version.SDK_INT);
//        System.out.println("codename is equal to " + version.CODENAME);
//        System.out.println("release is equal to " + version.RELEASE); //
//        System.out.println("incremental is equal to " + version.INCREMENTAL);
//        System.out.println("preview sdk is equal to " + version.PREVIEW_SDK_INT);
//    }

}
