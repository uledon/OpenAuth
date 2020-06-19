package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class ResponseActivity extends AppCompatActivity {

    TextView messageTitle;
    TextView account_name_extra, body_text, account_name_text;
    Button yesButton, noButton;
    String challengeId, accountName, hostname, macAddress, netDeviceIp,policy,time,city,
            location, titles, bodys;
    private static final String SHARED_PREFS = "sharedPrefs",URL_TEXT = "urlBox",useragent = "Mozilla/5.0";
    private static final String
            CHALLENGE_ID_EXTRA = "challengeId", ACCOUNT_NAME_EXTRA = "accountName",
            HOSTNAME_EXTRA = "hostname", MAC_ADDRESS_EXTRA = "macaddress",
            NET_DEVICE_IP_EXTRA = "netDeviceIp", POLICY_EXTRA = "policy",
            TIME_EXTRA = "time", LOCATION_EXTRA = "location", TITLE_EXTRA = "titles",
            BODY_EXTRA = "bodys";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        getLocation();
        messageTitle = findViewById(R.id.messageTitle);
        account_name_extra = findViewById(R.id.account_name_extra);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        body_text = findViewById(R.id.body_text);
        account_name_text = findViewById(R.id.account_name_text);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //getting custom data payload for both background and foreground
        if(getIntent().getExtras()!=null){
            for (String key : getIntent().getExtras().keySet()){
                if(key.equals(CHALLENGE_ID_EXTRA))
                    challengeId = getIntent().getExtras().getString(key);
                else if(key.equals(ACCOUNT_NAME_EXTRA))
                    accountName = " " + getIntent().getExtras().getString(key) + "\n";
                else if(key.equals(HOSTNAME_EXTRA))
                    hostname =  "hostname"+": "+ getIntent().getExtras().getString(key) + "\n";
                else if(key.equals(MAC_ADDRESS_EXTRA))
                    macAddress =  "MAC Address" + ": " + getIntent().getExtras().getString(key) + "\n";
                else if(key.equals(NET_DEVICE_IP_EXTRA))
                    netDeviceIp = getIntent().getExtras().getString(key);
                else if(key.equals(POLICY_EXTRA))
                    policy = getIntent().getExtras().getString(key);
                else if(key.equals(TIME_EXTRA))
                    time = getString(R.string.time) + ": " + getIntent().getExtras().getString(key) + "\n";
                else if(key.equals(LOCATION_EXTRA))
                    location =  getString(R.string.location) + ": " + getIntent().getExtras().getString(key) + "\n";
                else if(key.equals(TITLE_EXTRA))
                    titles = getIntent().getExtras().getString(key);
                else if(key.equals(BODY_EXTRA))
                    bodys =  getIntent().getExtras().getString(key) + "\n";
            }
            messageTitle.setText(titles);
            body_text.setText(bodys);
            account_name_text.setText(accountName);
            String total_message = hostname + location + time +
                    macAddress + "\n";
            account_name_extra.setText(total_message);
        }
        yesButton.setOnClickListener((View view) -> {
            makeCall(challengeId,"true", view);
            if(sharedPreferences.getBoolean("logged", false)){
                Intent myIntent = new Intent(view.getContext(), OTPActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
                Toast.makeText(this,"Response Sent",Toast.LENGTH_SHORT).show();
            }
            else{
                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
                Toast.makeText(this,"Response sent",Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(this,"You have been logged out",Toast.LENGTH_SHORT).show();
//            Intent myIntent = new Intent(view.getContext(), MainActivity.class);
//            startActivityForResult(myIntent, 0);
//            finish();
        });
        noButton.setOnClickListener((View view) -> {
            makeCall(challengeId,"false", view);
            if(sharedPreferences.getBoolean("logged", false)){
                Intent myIntent = new Intent(view.getContext(), OTPActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
                Toast.makeText(this,"Response Sent",Toast.LENGTH_SHORT).show();
            }
            else{
                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
                Toast.makeText(this,"Response sent",Toast.LENGTH_SHORT).show();

            }

        });
    }
    public void makeCall(String challengeId, String response, View view){
        try{
            // HttpLoggingInterceptor used to log the connection and debug the connection
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String urlText = EncryptionUtils.decrypt(ResponseActivity.this, sharedPreferences.getString(URL_TEXT, ""));
            String cookie = EncryptionUtils.decrypt(ResponseActivity.this, sharedPreferences.getString("cookie", ""));
            String username = EncryptionUtils.decrypt(ResponseActivity.this, sharedPreferences.getString("account", ""));
            String token = EncryptionUtils.decrypt(ResponseActivity.this, sharedPreferences.getString("token", ""));
            String action = "user_response";
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlText)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            UserService api = retrofit.create(UserService.class);
            RequestBody actionPart = RequestBody.create(MultipartBody.FORM, action);
            RequestBody challengeIdPart = RequestBody.create(MultipartBody.FORM, challengeId);
            RequestBody responsePart = RequestBody.create(MultipartBody.FORM, response);
            RequestBody locationPart = RequestBody.create(MultipartBody.FORM, city);
            System.out.println("before call token is: "+token);
            System.out.println("before call username is: "+username);
            System.out.println("before call cookie is: "+cookie);
            System.out.println("before call useragent is: "+useragent);
            System.out.println("before call action is: "+ action);
            System.out.println("before call challengeId is: "+ challengeId);
            System.out.println("before call response is: "+ response);
            System.out.println("before call location is: "+ location);
            Call<User> call = api.mobile_notification(token, username, cookie,useragent,actionPart,challengeIdPart, responsePart,locationPart);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {

                    if (!response.isSuccessful()) {
                       System.out.println("Operation not successfull because " + response.code() +
                                " " + response.message() + " " + response.body());
                        Toast.makeText(ResponseActivity.super.getBaseContext(), "An error occured when responding" + response.code() +
                                " " + response.message() + " " + response.body(), Toast.LENGTH_SHORT).show();
                        return;

                    }
                        System.out.println("the thing went great");
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    System.out.println("the problem is failure: " + t.getMessage());
                   // Toast.makeText(ResponseActivity.super.getBaseContext(), "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch(IllegalArgumentException ex){
            System.out.println("the problem is illegal argument exception: " + ex.getMessage() + ex.getCause()+ ex.getLocalizedMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
            System.out.println("response activity city is " + city);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
        }
    }
}
