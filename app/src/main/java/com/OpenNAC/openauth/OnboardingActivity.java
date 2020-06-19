package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class OnboardingActivity extends AppCompatActivity {

    public ViewPager slideViewPager;
    public LinearLayout linearLayout;
    public slide_layout slide_layout;
    public TextView[] dots;
    public Button next_button;
    public Button back_button;
    public int currentPage;
    public boolean finish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        slideViewPager = findViewById(R.id.slideViewPager);
        linearLayout = findViewById(R.id.linearLayout);
        next_button = findViewById(R.id.next_button);
        back_button = findViewById(R.id.back_button);
        slide_layout = new slide_layout(this,
                getString(R.string.help_screen_one_header),
                getString(R.string.help_screen_two_header),
                getString(R.string.help_screen_three_header),
                getString(R.string.help_screen_one_body),
                getString(R.string.help_screen_two_body),
                getString(R.string.help_screen_three_body));
        slideViewPager.setAdapter(slide_layout);
        addDotsIndicator(0);
        slideViewPager.addOnPageChangeListener(viewListener);
        next_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                slideViewPager.setCurrentItem(currentPage + 1);
            }
        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideViewPager.setCurrentItem(currentPage-1);
            }
        });

    }
    //    private boolean isFirstTimeStartApp(){
//        SharedPreferences ref = getApplicationContext().getSharedPreferences("",context.MODEPRIVATE);
//    }
    public void addDotsIndicator(int position){
        dots = new TextView[3];
        linearLayout.removeAllViews();
        for(int i = 0; i < dots.length ; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));
            linearLayout.addView(dots[i]);
        }
        if (dots.length > 0){
            dots[position].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        private static final String SHARED_PREFS = "sharedPrefs", FINISH = "finish";
        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;
            if (position == 0){
                next_button.setEnabled(true);
                back_button.setEnabled(false);
                back_button.setVisibility(View.INVISIBLE);
                next_button.setText("Next");
                next_button.setOnClickListener(v -> slideViewPager.setCurrentItem(currentPage + 1));
                back_button.setText("");
            }
            else if (position == dots.length-1){
                next_button.setEnabled(true);
                back_button.setEnabled(true);
                back_button.setVisibility(View.VISIBLE);
                next_button.setText(getString(R.string.finish));
                next_button.setOnClickListener(view -> {
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                    boolean finished = sharedPreferences.getBoolean(FINISH, false);
                    if(finished){
                        OnboardingActivity.super.onBackPressed();
                    }
                    else{
                        Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                        startActivityForResult(myIntent, 0);
                        finish = true;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(FINISH,finish);
                        editor.apply();
                        Toast.makeText(getBaseContext(),getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                });

                back_button.setText(getString(R.string.back));
            }
            else{
                next_button.setEnabled(true);
                back_button.setEnabled(true);
                back_button.setVisibility(View.VISIBLE);
                next_button.setText("Next");
                next_button.setOnClickListener(v -> slideViewPager.setCurrentItem(currentPage + 1));
                back_button.setText("Back");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    private long backPressedTime;
    private Toast backToast;
    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        else{
            backToast = Toast.makeText(getBaseContext(), getString(R.string.press_back_again), Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
