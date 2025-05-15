package com.app.codebuzz.flipquotes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SlidePageAdapter extends PagerAdapter {

    private Context context;
    private List<Quote> quotes;
    private static final String QUOTES_URL = "https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/main/Quotes.json"; // Replace with your URL

    SlidePageAdapter(Context context) {
        this.context = context;
        fetchQuotesFromUrl();
    }

    private void fetchQuotesFromUrl() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(QUOTES_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SlidePageAdapter", "Error fetching quotes", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Quote>>() {}.getType();
                    quotes = gson.fromJson(json, listType);

                    // Shuffle the quotes list
                    Collections.shuffle(quotes);

                    // Notify the adapter on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
                }
            }
        });
    }

    @Override
    public int getCount() {
        return quotes != null ? quotes.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = Objects.requireNonNull(layoutInflater).inflate(R.layout.card_layout, container, false);

        final TextView heading = view.findViewById(R.id.moreAt);
        final TextView content = view.findViewById(R.id.content);
        final ImageButton refreshButton = view.findViewById(R.id.r_button);
        final ImageButton shareButton = view.findViewById(R.id.share_button);

        if (quotes != null && !quotes.isEmpty()) {
            Quote quote = quotes.get(position);
            content.setText(quote.getQuote());
            heading.setText(String.format("~ %s", quote.getAuthor()));
        }

        view.setOnClickListener(v -> shareButton.setVisibility(View.VISIBLE));

        refreshButton.setOnClickListener(v -> {
            refreshButton.setVisibility(View.GONE);
            ((VerticalViewPager) container).setCurrentItem(0, true);
        });

        shareButton.setOnClickListener(v -> {
            Bitmap bitmap = getBitmapFromView(view);
            shareImage(bitmap);
        });

        container.addView(view);
        return view;
    }

    private Bitmap getBitmapFromView(View view) {
        ImageButton refreshButton = view.findViewById(R.id.r_button);
        ImageButton shareButton = view.findViewById(R.id.share_button);
        int originalVisibility = refreshButton.getVisibility();
        int originalVisibilityShare = shareButton.getVisibility();
        refreshButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        refreshButton.setVisibility(originalVisibility);
        shareButton.setVisibility(originalVisibilityShare);
        return bitmap;
    }

    private void shareImage(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Quote", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT,"Check out more amazing quotes at FlipQuotes https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes");
        context.startActivity(Intent.createChooser(intent, "Share Quote"));
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}