package com.example.dostya.cpptojava;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;




public class bddextract extends AppCompatActivity {
    int resangl = 30;
    final float[] angle = new float[(360 + 60) * resangl];
    final float[] alt = new float[(360 + 60) * resangl];
    float myZ = 0;
    float extract[];
    public double my_latitude = 0;
    public double my_longitude = 0;
    TextView boutonLoading;
    private volatile boolean stopThread = false;
    AssetManager mgr = null;
    int testt=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        my_latitude = getIntent().getDoubleExtra("lat", 0);
        my_longitude = getIntent().getDoubleExtra("long", 0);
        setContentView(R.layout.activity_opening2);
        boutonLoading = findViewById(R.id.loading);
        Log.d("latt",  Double.toString(my_latitude));
        Log.d("latt",  Double.toString(my_longitude));

        mgr = getResources().getAssets();
        ExtractBDDTask thread = new ExtractBDDTask();
        thread.execute();


        }

        class ExtractBDDTask extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                Log.d("lol", "inside async task");
                extract = mainjni(mgr, my_latitude, my_longitude, 30);
                testt = 1;
                for (int i = 0; i < ((360 + 60) * resangl) * 2; i++) {
                    if (i < (360 + 60) * resangl)
                        angle[i] = extract[i];
                    else if (i < (360 + 60) * resangl * 2)
                        alt[i - (360 + 60) * resangl] = extract[i];
                }
                myZ = extract[(360 + 60) * resangl * 2];
                final Intent p = new Intent(bddextract.this, CameraLive.class);
                p.putExtra("ANGLE", angle);
                p.putExtra("ALT", alt);
                p.putExtra("Z", myZ);
                p.putExtra("lat",my_latitude);
                p.putExtra("long",my_longitude);
                startActivity(p);
                return null;

            }
        }

    public native float[] mainjni(AssetManager mgr, double latitude, double longitude, int ResolAng );

    }

