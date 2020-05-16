package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.remote.EncryptionUtils;

public class SharedKeyScreen extends AppCompatActivity {
    private static final String SHARED_PREFS = "sharedPrefs", SHARED_TEXT = "shareKeyText",
            ACCOUNT_NAME_TEXT = "accountName";
    private static final int MIN_KEY_BYTES = 15;
    private EditText shareKeyBox, accNameBox;
    public TextView test_view;
    private String shareKeyText, accNameText;
    private static final String CHANNEL_ID = "OpenAuth";
    private static final String CHANNEL_NAME = "OCF";
    private static final String CHANNEL_DESC = "OpenCloud Factory";
    private static boolean spanish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen2);
        accNameBox = findViewById(R.id.nameBox);
        shareKeyBox = findViewById(R.id.shareKeyBox);
        TextView nameTxt = findViewById(R.id.nameTxt);
        TextView keyTxt = findViewById(R.id.keyTxt);
        final TextView errorTxt = findViewById(R.id.errorTxt);
        spanish = getIntent().getBooleanExtra("spanishset",false);

        Button sendBtn = findViewById(R.id.sendBtn);
        if (spanish){
            sendBtn.setText("Enviar");
            nameTxt.setText("Tu Nombre");
            accNameBox.setHint("ingrese un nombre");
            shareKeyBox.setHint("ingrese su clave");
            keyTxt.setText("Tu llave");
        }
        sendBtn.setOnClickListener(view -> {
            Intent myIntent = new Intent(view.getContext(), OTPScreen.class);
            myIntent.putExtra("shareKeyBox", shareKeyBox.getText().toString().trim().
                    replaceAll("[^a-zA-Z2-7]",""));
            myIntent.putExtra("name", accNameBox.getText().toString());
            myIntent.putExtra("spanishset",spanish);
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
        loadData();
        updateViews();
        /** */
        ////////////// Trying out Get Response
        //getResponse();
        /////////////
        /** */
    }
    /////// Making sure when entered this page you can only go back!
    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        Intent myIntent = new Intent(this,MainActivity.class);
        startActivityForResult(myIntent, 0);
        this.finish();
        return;
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


}
