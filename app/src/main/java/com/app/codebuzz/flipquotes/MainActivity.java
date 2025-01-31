package com.app.codebuzz.flipquotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private VerticalViewPager viewPager;

    private static final String PREFS_NAME = "prefs";
    private static final String KEY_CARD_COLOR = "card_color";
    private int defaultColor;
    private CardView cardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        cardView = findViewById(R.id.card_view);
        defaultColor = ContextCompat.getColor(this, R.color.default_card_color);

        // Retrieve the saved color from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedColor = preferences.getInt(KEY_CARD_COLOR, defaultColor);
        cardView.setCardBackgroundColor(savedColor);

        Button colorPickerButton = findViewById(R.id.color_picker_button);
        colorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPickerDialog();
            }
        });

    }

    private void openColorPickerDialog() {
        AmbilWarnaDialog colorPickerDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                cardView.setCardBackgroundColor(color);

                // Save the selected color in SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(KEY_CARD_COLOR, color);
                editor.apply();
                if (viewPager.getAdapter() != null) {
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Do nothing
            }
        });
        colorPickerDialog.show();

    }
    public VerticalViewPager getViewPager() {
        return viewPager;
    }
}

