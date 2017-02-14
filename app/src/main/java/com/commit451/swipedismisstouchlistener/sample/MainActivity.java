package com.commit451.swipedismisstouchlistener.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.swipedismisstouchlistener.SwipeDismissTouchListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewGroup root = (ViewGroup) findViewById(R.id.root);

        View view = findViewById(R.id.card1);
        SwipeDismissTouchListener swipeDismissTouchListener = new SwipeDismissTouchListener(view);
        swipeDismissTouchListener.setOnDismissListener(new SwipeDismissTouchListener.OnDismissListener() {
            @Override
            public void onDismiss(@NonNull View view) {
                root.removeView(view);
            }
        });
        view.setOnTouchListener(swipeDismissTouchListener);

    }
}
