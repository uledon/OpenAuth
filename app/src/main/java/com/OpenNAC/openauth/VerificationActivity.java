package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    ssid, timestamp, location;
    private static final String SHARED_PREFS = "sharedPrefs", URL_TEXT = "urlBox", useragent = "Mozilla/5.0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_verification);
        codeBox = findViewById(R.id.codeBox);
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
    public void verify( String mac, String factor, String fcmt, String vendor, String os, String version, String ip_addr, String model, String security_patch, String hostname, String ssid, String timestamp, View view){
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
//        RequestBody lcoationPart = RequestBody.create(MultipartBody.FORM, location);
        Call<User> call = api.mobilelogin(cookie,useragent,macPart,factorPart, fcmtPart,vendorPart,osPart,
                versionPart, ip_addrPart, modelPart, security_patchPart, hostnamePart, ssidPart, timestampPart);
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
                User postResponse = response.body();

                editor.putString("account", EncryptionUtils.encrypt(VerificationActivity.this,postResponse.getAccount()));
                editor.putString("secret", EncryptionUtils.encrypt(VerificationActivity.this,postResponse.getSecret()));
                if(getIntent().getBooleanExtra("logged", false)) {
                    editor.putBoolean("logged", true);
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
                Toast.makeText(VerificationActivity.super.getBaseContext(), "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
