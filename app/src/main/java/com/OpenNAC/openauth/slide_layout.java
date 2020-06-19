package com.OpenNAC.openauth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

public class slide_layout extends PagerAdapter {

    Context context;
    String header1, header2, header3;
    String body1, body2, body3;
    LayoutInflater layoutInflater;
    public slide_layout(Context context, String header1, String header2, String header3, String body1, String body2, String body3){
        this.context = context;
        this.header1 = header1;
        this.header2 = header2;
        this.header3 = header3;
        this.body1 = body1;
        this.body2 = body2;
        this.body3 = body3;

    }

    public String getBody1() {
        return body1;
    }

    public int[] slide_images = {
            R.drawable.opencloudfactorylogo,
            R.drawable.usernamelogo,
            R.drawable.help_screen_three
    };

    public String[] slide_headings={
            getBody1(),
            getBody1(),
            header3
    };
    public String[] slide_description = {
            body1,
            body2,
            body3
    };
    @Override
    public int getCount() {
        return slide_headings.length;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position){
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_slide_layout,container,false);
        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDesc = view.findViewById(R.id.slide_desc);
        slideImageView.setImageResource(slide_images[position]);
        String[] slide_headings={
                header1,
                header2,
                header3
        };
        slideHeading.setText(slide_headings[position]);
        String[] slide_description = {
                body1,
                body2,
                body3
        };
        slideDesc.setText(slide_description[position]);
        container.addView(view);
        return view;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((ConstraintLayout)object);
    }


}