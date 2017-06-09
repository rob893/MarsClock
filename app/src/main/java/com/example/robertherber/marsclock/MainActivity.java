package com.example.robertherber.marsclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;


public class MainActivity extends AppCompatActivity {

    private String time = "";
    private double MSD;
    SimpleDateFormat GMT;
    SimpleDateFormat choiceDate;
    Date date;
    String[] timeZones;
    TextView textViewMTC;
    TextView textViewMSD;
    TextView textViewGMT;
    TextView textViewChoice;
    Spinner spinner;
    Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewMTC = (TextView) findViewById(R.id.marsTime);
        textViewMSD = (TextView) findViewById(R.id.MSD);
        textViewGMT = (TextView) findViewById(R.id.GMT);
        textViewChoice = (TextView) findViewById(R.id.choiceTime);

        GMT = new SimpleDateFormat("HH:mm:ss");
        choiceDate = new SimpleDateFormat("HH:mm:ss");


        spinner = (Spinner) findViewById(R.id.spinner);
        timeZones = TimeZone.getAvailableIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, timeZones);
        spinner.setAdapter(adapter);

        Timer t = new Timer(); //timer for Mars clock
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                myHandler.post(runnable);
            }
        }, 0, 1027); //1027 ms is about 1 "martian" second (1000ms * 1.02749)

        Timer earthT = new Timer(); //timer for Earth clock
        earthT.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                myHandler.post(earthR);
            }
        }, 0, 1000);

    }

    final Runnable runnable = new Runnable(){
        public void run(){
            calculateCoordinatedMarsTime();
            String MTC = "MTC: " + time;
            String MSDS = "MSD: " + MSD;

            textViewMTC.setText(MTC);
            textViewMSD.setText(MSDS);
        }
    };

    final Runnable earthR = new Runnable(){
        public void run(){
            date = new Date();
            String userChoice = spinner.getSelectedItem().toString();
            GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
            choiceDate.setTimeZone(TimeZone.getTimeZone(userChoice));
            String earthTime = "GMT: " + GMT.format(date);
            String choiceTime = userChoice + ": " + choiceDate.format(date);

            textViewChoice.setText(choiceTime);
            textViewGMT.setText(earthTime);
        }
    };

    public double getDaysSinceJ2000(){
        double unixEpoc = System.currentTimeMillis();
        double julianDate = 2440587.5 + (unixEpoc / 86400000);
        double julianTT = julianDate + (37 +32.184) /86400;
        double j2000Epoch = 2451545.0;
        return julianTT - j2000Epoch;
    }

    public double calculateMarsSolDate(){
        return (((getDaysSinceJ2000() - 4.5) /1.027491252) + 44796.0 - 0.00096);
    }

    public void calculateCoordinatedMarsTime(){
        MSD = calculateMarsSolDate();
        double MTC = (24 * MSD) % 24;
        int hour = (int)Math.floor(MTC);
        double minD = (MTC - hour) * 60;
        int min = (int)minD;
        int secs = (int)((minD - min) * 60);
        MSD = Math.round(MSD * 100.0) / 100.0; //round to 2 decimals
        String minF = String.format("%02d", min);
        String secF = String.format("%02d", secs);
        String hourF = String.format("%02d", hour);
        time = hourF + ":" + minF + ":" + secF;
    }
}

