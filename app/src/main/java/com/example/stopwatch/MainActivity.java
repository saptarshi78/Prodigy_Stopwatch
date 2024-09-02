package com.example.stopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button startButton, pauseButton, resetButton, lapButton, saveButton, shareButton;
    private ListView lapListView;
    private Handler handler = new Handler();
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private boolean running = false;
    private ArrayList<String> lapTimes = new ArrayList<>();
    private ArrayAdapter<String> lapAdapter;

    private Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = System.currentTimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);

            timerTextView.setText("" + mins + ":" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timerTextView);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);
        lapButton = findViewById(R.id.lapButton);
        saveButton = findViewById(R.id.saveButton);
        shareButton = findViewById(R.id.shareButton);
        lapListView = findViewById(R.id.lapListView);

        lapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lapTimes);
        lapListView.setAdapter(lapAdapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(updateTimer, 0);
                    running = true;
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    timeSwapBuff += timeInMilliseconds;
                    handler.removeCallbacks(updateTimer);
                    running = false;
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = 0L;
                timeInMilliseconds = 0L;
                timeSwapBuff = 0L;
                updatedTime = 0L;
                running = false;
                handler.removeCallbacks(updateTimer);
                timerTextView.setText("0:00:000");
                lapTimes.clear();
                lapAdapter.notifyDataSetChanged();
            }
        });

        lapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    String lapTime = timerTextView.getText().toString();
                    lapTimes.add(lapTime);
                    lapAdapter.notifyDataSetChanged();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLapTimesToFile();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLapTimes();
            }
        });
    }

    private void saveLapTimesToFile() {
        String fileName = "LapTimes_" + System.currentTimeMillis() + ".txt";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, fileName);

        try {
            path.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            for (String lap : lapTimes) {
                fos.write((lap + "\n").getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shareLapTimes() {
        StringBuilder lapTimeString = new StringBuilder();
        for (String lap : lapTimes) {
            lapTimeString.append(lap).append("\n");
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, lapTimeString.toString());
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}
