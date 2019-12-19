package com.OpenNAC.openauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean langSpanish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button beginButton = findViewById(R.id.beginButton);

        beginButton.setOnClickListener(view -> {
            Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
        });

        final Button infoBtn = findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(v -> {
            String info = "https://www.opencloudfactory.com/en/";
            Uri webaddress = Uri.parse(info);

            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(getPackageManager()) != null) {
                startActivity(goToSite);
            }
        });

        ImageButton spanishBtn = findViewById(R.id.spanishlang);
        spanishBtn.setOnClickListener(v -> {
            TextView welcomeText = findViewById(R.id.welcomeText);
            welcomeText.setText("Bienvenido al servicio de autenticación de OpenNAC");
            infoBtn.setText("Visita nuestro sitio web");
            Button info = findViewById(R.id.beginButton);
            info.setText("Haga clic aquí para comenzar");

        });
        ImageButton englishBtn = findViewById(R.id.englang);
        englishBtn.setOnClickListener(v -> {
            TextView welcomeText = findViewById(R.id.welcomeText);
            welcomeText.setText("Welcome to OpenNAC Authentication Service");
            infoBtn.setText("Visit our website");
            Button info = findViewById(R.id.beginButton);
            info.setText("Click here to begin");

        });
    }
}
