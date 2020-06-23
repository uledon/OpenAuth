package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.OpenNAC.openauth.Services.DataClass;
import com.OpenNAC.openauth.Services.User;
import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerificationActivity extends AppCompatActivity {
    EditText codeBox;
    Button verifyButton;
    String mac, factor, fcmt, vendor, os, version, ip_addr, model, security_patch, hostname,
    ssid, timestamp, city;
    private static final String SHARED_PREFS = "sharedPrefs", URL_TEXT = "urlBox", useragent = "Mozilla/5.0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_verification);
        getLocation();
        codeBox = findViewById(R.id.codeBox);
        codeBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        verifyButton = findViewById(R.id.verifyBtn);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        mac = DataClass.getMacAddr();
        fcmt = DataClass.getFirebaseToken();
        vendor = DataClass.getVendor();
        os = DataClass.getOS();
        version = DataClass.getVersion();
        ip_addr = DataClass.getIp(wifiInfo);
        model = DataClass.getModel();
        security_patch = DataClass.getSecurityPatch();
        hostname = DataClass.getHostName(this);
        ssid = DataClass.getSsid(wifiInfo);
        timestamp = DataClass.getTimeStamp();
        verifyButton.setOnClickListener(v -> {
            if (isCodeValid(codeBox.getText().toString().trim())){
                factor = codeBox.getText().toString().trim() ;
                try {
                    verify(mac,factor, fcmt, vendor, os,version, ip_addr, model, security_patch, hostname, ssid,timestamp, v);
                }
                catch (IllegalArgumentException ex){
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isCodeValid(String code){
        if(code.length() == 0 || code == null){
            Toast.makeText(this, "Please enter valid code", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            return true;
        }
    }

    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            findLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findLocation();
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public void findLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try {
            city = DataClass.getLocation(this, location.getLatitude(), location.getLongitude());
            System.out.println("verification activity city is " + city);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**This method will build the API request with Android library Retrofit.
     * @param mac mac address of the device
     * @param factor the code sent to the email of the user
     * @param fcmt firebase messaging token
     * @param vendor vendor of the mobile device
     * @param os Base OS of the device. In this case it is always ANDROID
     * @param version Version of the OS
     * @param ip_addr IP-Address of the device
     * @param model Model of the device
     * @param security_patch latest security patch installed
     * @param hostname Name of the device
     * @param ssid Name of the internet connection used for the device
     * @param timestamp Time of connection
     * @param view needed to get and set various values
     */
    public void verify( String mac, String factor, String fcmt, String vendor, String os,
                        String version, String ip_addr, String model, String security_patch,
                        String hostname, String ssid, String timestamp, View view){
        // HttpLoggingInterceptor used to log the connection and debug the connection
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String urlText = EncryptionUtils.decrypt(VerificationActivity.this, sharedPreferences.getString(URL_TEXT, ""));
        String cookie = EncryptionUtils.decrypt(VerificationActivity.this, sharedPreferences.getString("cookie", ""));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlText)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        UserService api = retrofit.create(UserService.class);
        // Changing the Strings to retrofit RequestBody format to use for API call
        RequestBody macPart = RequestBody.create(MultipartBody.FORM, mac);
        RequestBody factorPart = RequestBody.create(MultipartBody.FORM, factor);
        RequestBody fcmtPart = RequestBody.create(MultipartBody.FORM, fcmt);
        RequestBody vendorPart = RequestBody.create(MultipartBody.FORM, vendor);
        RequestBody osPart = RequestBody.create(MultipartBody.FORM, os);
        RequestBody versionPart = RequestBody.create(MultipartBody.FORM, version);
        RequestBody ip_addrPart = RequestBody.create(MultipartBody.FORM, ip_addr);
        RequestBody modelPart = RequestBody.create(MultipartBody.FORM, model);
        RequestBody security_patchPart = RequestBody.create(MultipartBody.FORM, security_patch);
        RequestBody hostnamePart = RequestBody.create(MultipartBody.FORM, hostname);
        RequestBody ssidPart = RequestBody.create(MultipartBody.FORM, ssid);
        RequestBody timestampPart = RequestBody.create(MultipartBody.FORM, timestamp);
        RequestBody locationPart = RequestBody.create(MultipartBody.FORM, city);
        Call<User> call = api.mobilelogin(cookie,useragent,macPart,factorPart, fcmtPart,vendorPart,osPart,
                versionPart, ip_addrPart, modelPart, security_patchPart, hostnamePart, ssidPart, timestampPart,locationPart);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (!response.isSuccessful()) {
                   Toast.makeText(VerificationActivity.super.getBaseContext(), "" + response.code() +
                           " " + response.message() + " " + response.body(), Toast.LENGTH_SHORT).show();
                    editor.putBoolean("logged", false);
                    editor.apply();
                    return;

                }
                Toast.makeText(VerificationActivity.this,getString(R.string.success), Toast.LENGTH_SHORT).show();
                User postResponse = response.body();

                editor.putString("account", EncryptionUtils.encrypt(VerificationActivity.this,postResponse.getAccount()));
                editor.putString("secret", EncryptionUtils.encrypt(VerificationActivity.this,postResponse.getSecret()));
                editor.putString("deviceId", EncryptionUtils.encrypt(VerificationActivity.this,postResponse.getDeviceId()));
                editor.putString("timestamp",timestamp);
                System.out.println(postResponse.getDeviceId());
                if(getIntent().getBooleanExtra("logged", false)) {
                    editor.putBoolean("logged", true);
                }
                else{
                    editor.putBoolean("logged", false);
                }
                editor.apply();
                if (postResponse.getSecret()== null){
                    Toast.makeText(VerificationActivity.this, "Response from server is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent myIntent = new Intent(view.getContext(), OTPActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                //result.setText(t.getMessage());
                editor.putBoolean("logged", false);
                editor.apply();
                System.out.println("on verification failure: " + t.getMessage());
                Toast.makeText(VerificationActivity.super.getBaseContext(), "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
