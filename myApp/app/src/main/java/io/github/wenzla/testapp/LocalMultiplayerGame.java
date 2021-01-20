package io.github.wenzla.testapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import io.github.wenzla.testapp.CanvasView.CanvasViewListener;

/**
 * Created by Allen on 10/26/2017.
 */


public class LocalMultiplayerGame extends AppCompatActivity {

    private LocalCanvasView cv;
    TextView   statusView;

    private int mInterval = 3000;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_multiplayer_setup);

        cv = (LocalCanvasView) findViewById(R.id.signature_canvas);

        statusView  = (TextView) findViewById(R.id.status);

        cv.setCustomViewListener(new LocalCanvasView.LocalCanvasViewListener() {
            @Override
            public void onUpdateValue(String updatedValue) {
                statusView.setText(updatedValue);
            }
        });

        SessionHandler.createSession();

        mHandler = new Handler();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SessionHandler.endSession();
    }


    public void clearCanvas(View v){
        cv.clearCanvas();
    }

    public void backClick(View v) {
        startActivity(new Intent(LocalMultiplayerGame.this, MainActivity.class));
    }

}


