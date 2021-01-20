package io.github.wenzla.testapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SessionActivity extends AppCompatActivity {

    private int mInterval = 3000;
    private Handler mHandler;
    private int status = 0; //0: do nothing
                            //1: waiting for connect
                            //2: waiting for turn

    Button findbtn;
    Button turnbtn;
    Button endbtn;
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        findbtn = (Button) findViewById(R.id.findSession);
        findbtn.setOnClickListener(sessionHandler);
        turnbtn = (Button) findViewById(R.id.takeTurn);
        turnbtn.setOnClickListener(sessionHandler);
        endbtn = (Button) findViewById(R.id.endSession);
        endbtn.setOnClickListener(sessionHandler);

        info = (TextView) findViewById(R.id.currentInfo);

        endbtn.setEnabled(false);

        mHandler = new Handler();
        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


        private View.OnClickListener sessionHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.findSession:
                    info.setText("Waiting to connect...");
                    endbtn.setEnabled(true);
                    findbtn.setEnabled(false);
                    SessionHandler.createSession();
                    status = 1;
                    break;
                case R.id.takeTurn:
                    if (SessionHandler.isMyTurn()) {
                        //do my turn
                        setTurn(false);
                        SessionHandler.endTurn();
                    }
                    break;
                case R.id.endSession:
                    status = 0;
                    info.setText("Disconnected");
                    endbtn.setEnabled(false);
                    findbtn.setEnabled(true);
                    SessionHandler.endSession();
                    break;
            }
        }
    };

    private void setTurn(boolean turn) {
        if (turn) {
            info.setText("Your turn");
            turnbtn.setEnabled(true);
            status = 0;
        } else {
            info.setText("Opponent's turn");
            turnbtn.setEnabled(false);
            status = 1;
        }
    }

    protected void updateStatus() {
        if (status==1) {
            if (SessionHandler.sessionConnect()) {
                findbtn.setEnabled(false);
                endbtn.setEnabled(true);
                info.setText("Connected.");
                setTurn(SessionHandler.isMyTurn());
            }
        }
        if (status==2) {
            if (SessionHandler.isMyTurn()) {
                info.setText("Your turn");
                turnbtn.setEnabled(true);
                status = 0;
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
}
