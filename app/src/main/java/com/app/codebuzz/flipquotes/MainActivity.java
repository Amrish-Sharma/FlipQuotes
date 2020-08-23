package com.app.codebuzz.flipquotes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VerticalViewPager viewPager = findViewById(R.id.card_view);
        viewPager.setAdapter(new SlidePageAdapter(this));

    }


}

