package com.OpenNAC.openauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.remote.TimeBasedOneTimePasswordUtil;

import java.security.GeneralSecurityException;
import java.util.Date;

public class OTPScreen extends AppCompatActivity {
    TextView accNameTxt,otpTxt,countdownTxt;
    private Date date;
    static String code,base32Secret, nameBox;
    /**
     * This method will be run when the page is created
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpscreen);

        Button generateBtn = findViewById(R.id.generateBtn);
        accNameTxt = findViewById(R.id.accNameTxt);
        otpTxt = findViewById(R.id.otpTxt);
        countdownTxt = findViewById(R.id.countdownTxt);
        base32Secret = getIntent().getStringExtra("shareKeyBox"); //getting pre-shared key from previous page
        nameBox = getIntent().getStringExtra("name"); //getting account name from previous page
        code = null;

                try {
                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

///comment
        otpTxt.setText(code);
        accNameTxt.setText(nameBox);
        Thread t = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try {
                        Thread.sleep(1000);
//                        System.out.println(System.currentTimeMillis());
                        date = new Date();

                        runOnUiThread(() -> {

                            String datetxt;
                                try {
                                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                                    otpTxt.setText(code);
                                    if(date.getSeconds()>=30){
                                        date.setSeconds(date.getSeconds()-30);
                                    }
                                    datetxt = "" + date.getSeconds()+ " /30";
                                    countdownTxt.setText(datetxt);
                                }
                                catch(GeneralSecurityException e){
                                    e.printStackTrace();
                                }


                            });
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
         t.start();
        /**
         * This method will generate a QR Code whit the account name and pre-shared key
         */
        generateBtn.setOnClickListener(v -> {
            String info = TimeBasedOneTimePasswordUtil.qrImageUrl(nameBox,base32Secret);
            Uri webAddress = Uri.parse(info);
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webAddress);
            if (goToSite.resolveActivity(getPackageManager()) != null){
                startActivity(goToSite);
            }
        });


    }

}


