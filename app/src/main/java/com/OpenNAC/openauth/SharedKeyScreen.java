package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.remote.EncryptionUtils;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class SharedKeyScreen extends AppCompatActivity {
    private static final String SHARED_PREFS = "sharedPrefs", SHARED_TEXT = "shareKeyText", ACCOUNT_NAME_TEXT = "accountName";
    private static final int MIN_KEY_BYTES = 15;
    private EditText shareKeyBox, accNameBox;
    private String shareKeyText, accNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen2);


        accNameBox = findViewById(R.id.nameBox);
        shareKeyBox = findViewById(R.id.shareKeyBox);
        final TextView errorTxt = findViewById(R.id.errorTxt);
        Button sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(view -> {
            Intent myIntent = new Intent(view.getContext(), OTPScreen.class);
            myIntent.putExtra("shareKeyBox", shareKeyBox.getText().toString().trim().
                    replaceAll("[^a-zA-Z2-7]",""));
            myIntent.putExtra("name", accNameBox.getText().toString());
            if (shareKeyBox.getText().toString().length() <= MIN_KEY_BYTES) {
                errorTxt.setText("Shared Key too short");
            }
            else if(shareKeyBox.getText().toString().trim().contains(" ")){
                errorTxt.setText("Shared Key is not Valid contains spaces");
            }
            else if(shareKeyBox.getText().toString().trim().contains(" ")||
                    shareKeyBox.getText().toString().trim().contains("1")||
                    shareKeyBox.getText().toString().trim().contains("8")||
                    shareKeyBox.getText().toString().trim().contains("9"))
            {
                errorTxt.setText("Shared Key is invalid contains numbers less than 2 or more than 7");
            }
            else {
                saveData();
                errorTxt.setText("");
                startActivityForResult(myIntent, 0);
            }
        });
        getMacAddr();
        System.out.println(getMacAddr());
        loadData();
        updateViews();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_NAME_TEXT, EncryptionUtils.encrypt(SharedKeyScreen.this, accNameBox.getText().toString()));
        editor.putString(SHARED_TEXT, EncryptionUtils.encrypt(SharedKeyScreen.this, shareKeyBox.getText().toString().trim()));
        editor.apply();
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        accNameText = EncryptionUtils.decrypt(SharedKeyScreen.this, sharedPreferences.getString(ACCOUNT_NAME_TEXT,""));
        shareKeyText = EncryptionUtils.decrypt(SharedKeyScreen.this, sharedPreferences.getString(SHARED_TEXT, ""));
    }

    public void updateViews() {
        accNameBox.setText(accNameText);
        shareKeyBox.setText(shareKeyText);
    }
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
}
