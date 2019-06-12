/*package com.example.dostya.cpptojava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.res.AssetManager;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        int test=5;


         AssetManager mgr = getResources().getAssets();


        // java.io.InputStream input = mgr.open()
        float latitude = 45.444716f;
        float longitude = 4.392575f;
        int resangl = 30;
        float[] a=mainjni(mgr,latitude,longitude,resangl);

        float[] angle = new float[(360+60)*resangl];
        float[] alt = new float[(360+60)*resangl];
        float myZ = 0.f;

        for(int i=0;i<((360+60)*resangl)*2+1;i++)
       {
           if(i<(360+60)*resangl)
               angle[i] = a[i];
           else
                if(i<(360+60)*resangl*2)
                  alt[i-(360+60)*resangl] = a[i];
               else
                   myZ = a[i];

        }

        Log.d("try","altitude :");
        Log.d("try",Float.toString(myZ));
        Log.d("try","angles :");
        Log.d("try",Float.toString(angle[0]));
        Log.d("try",Float.toString(angle[1]));
        Log.d("try",Float.toString(angle[2]));
        Log.d("try",Float.toString(angle[3]));
        Log.d("try",Float.toString(angle[4]));
        Log.d("try",Float.toString(angle[12596]));
        Log.d("try",Float.toString(angle[12597]));
        Log.d("try",Float.toString(angle[12598]));
        Log.d("try",Float.toString(angle[12599]));
        Log.d("try","alt :");
        Log.d("try",Float.toString(alt[0]));
        Log.d("try",Float.toString(alt[1]));
        Log.d("try",Float.toString(alt[2]));
        Log.d("try",Float.toString(alt[3]));
        Log.d("try",Float.toString(alt[4]));

    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native float[] mainjni(AssetManager mgr, float latitude, float longitude, int ResolAng);
//}
