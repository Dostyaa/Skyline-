package com.example.dostya.cpptojava;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class page1 extends AppCompatActivity implements SensorEventListener {

    int k=0;
    private String createImageName() {
        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Calendar cal = Calendar.getInstance();
        String strDate = timeStamp.format(cal.getTime());
        Log.d ("fuck", strDate);
        String imageFileName = "JPEG_" +strDate + "_";

        return imageFileName;
    }
        float[] accelerometerVector=new float[4];
        float[] gravity= new float[3];
        float[] magneticVector=new float[3];
        float[] resultMatrix=new float[9];
        float[] values=new float[4];

    float x,y,z,north,topBottom,leftRight=0;

    private SensorManager manag;
    private Sensor magnetic;
    private Sensor accelerometer;


    private LocationListener locationListener;
    private LocationManager locationManager;
    private double my_latitude = 0;
    private double my_longitude = 0;
    int test = 0;
    int test2=0;
    Uri pictureURI = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    private final float alpha = (float) 0.8;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_page1);
                manag = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                magnetic = manag.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                accelerometer = manag.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                 LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                locationListener = (LocationListener) new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        my_latitude = location.getLatitude();
                        my_longitude = location.getLongitude();

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }

                };


                manag.registerListener(this, accelerometer, 100000);
                manag.registerListener(this, magnetic, 100000);


                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(page1.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
                return;
            }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, locationListener);



                Toolbar toolbar = findViewById(R.id.app_bar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle("Skyline");
                getSupportActionBar().setIcon(getDrawable(R.drawable.app_bar_icon2));
                ImageButton bouton = (ImageButton) findViewById(R.id.btnCamera);
                TextView latt = findViewById(R.id.lattitude);
                TextView longi = findViewById(R.id.longitude);
                TextView t1 = findViewById(R.id.textView4);
                TextView t2 = findViewById(R.id.textView5);
                TextView t3 = findViewById(R.id.textView3);
                t1.setVisibility(View.INVISIBLE);
                t2.setVisibility(View.INVISIBLE);
                t3.setVisibility(View.INVISIBLE);
                longi.setVisibility(View.INVISIBLE);
                latt.setVisibility(View.INVISIBLE);
                ImageView mImageClick = (ImageView) findViewById(R.id.photo);
                Toolbar toolbarTop = findViewById(R.id.app_bar);

                toolbarTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("TEST", "Clicked");


                    }
                });

        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File pictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File picsDir = new File(pictureFile, "SkylinePics");
                picsDir.mkdirs();
                String pictureName = "";
                pictureName = createImageName();
                File imageFile = new File(picsDir, pictureName);
                pictureURI = Uri.fromFile(imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureURI);
                startActivityForResult(pictureIntent, 0);
            }
        });
    }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                accelerometerVector[0] = event.values[0];
                accelerometerVector[1] = event.values[1];
                accelerometerVector[2] = event.values[2];
                accelerometerVector[3] = 0;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticVector = event.values;
            }
            float[] R = new float[16];
            float[] I = new float[16];
            float[] R2 = new float[9];
            float[] I2 = new float[9];
            /*SensorManager.getRotationMatrix(R2, I2, gravity, magneticVector);
            float [] A_D = event.values.clone();
            float [] A_W = new float[3];
            A_W[0] = R2[0] * A_D[0] + R2[1] * A_D[1] + R2[2] * A_D[2];
            A_W[1] = R2[3] * A_D[0] + R2[4] * A_D[1] + R2[5] * A_D[2];
            A_W[2] = R2[6] * A_D[0] + R2[7] * A_D[1] + R2[8] * A_D[2];
            Log.d("Field","\nX :"+A_W[0]+"\nY :"+A_W[1]+"\nZ :"+A_W[2]);

            SensorManager.getRotationMatrix(R,I, accelerometerVector,magneticVector);
            float[] inv = new float[16];
            android.opengl.Matrix.invertM(inv, 0, R, 0);

           android.opengl.Matrix.multiplyMV(values, 0, inv, 0, accelerometerVector, 0);
            Log.d("Acceleration", "Values: (" + values[0] + ", " + values[1] + ", " + values[2] + ")");

            x = (float) Math.toDegrees(values[0]);
            // acceleration en y
            y = (float)Math.toDegrees(values[1]);
            // angle avec le nord
            z = (float)Math.toDegrees(values[2]);
            Log.d("TESTtttttEast", Float.toString(x));
            Log.d("TESTtttttNorth", Float.toString(y));
            Log.d("TESTtttttSky", Float.toString(z));*/
            manag.getRotationMatrix(R2, I2, gravity, magneticVector);
            float orientation[] = new float[3];
            manag.getOrientation(R2, orientation);
            //angle par rapport au Nord
             north = (float) Math.toDegrees(orientation[0]);
             topBottom = (float) Math.toDegrees(orientation[1]);
             leftRight= (float) Math.toDegrees(orientation[2]);

            Log.d ("Angle", Float.toString(north));
            Log.d ("Angle", Float.toString(topBottom));
            Log.d ("Angle", Float.toString(leftRight));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        manag.unregisterListener(this);
        ImageView mImageView = (ImageView) findViewById(R.id.photo);
        //Log.d("TEST", pictureURI.toString());

        String path = pictureURI.toString();
        if (test == 1) {
            //txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        }

            // Bitmap picture = BitmapFactory.decodeFile(path);
        mImageView.setImageURI(pictureURI);
        ImageButton imBut = findViewById(R.id.btnCamera);
        //imBut.setVisibility(View.INVISIBLE);
        TextView latt= findViewById(R.id.lattitude);
        TextView longi= findViewById(R.id.longitude);
        longi.setVisibility(View.VISIBLE);
        latt.setVisibility(View.VISIBLE);
        longi.setText("longitude: " +String.valueOf(my_longitude));
        latt.setText("lattitude: " +String.valueOf(my_latitude));

        String datatoTxt= String.valueOf(my_latitude)+"\n"+String.valueOf(my_longitude)+"\n"+ Float.toString(north)+"\n"+ Float.toString(topBottom)+"\n"+ Float.toString(leftRight);
        TextView t1= findViewById(R.id.textView4);
        TextView t2= findViewById(R.id.textView5);
        TextView t3= findViewById(R.id.textView3);
        t1.setVisibility(View.VISIBLE);
        t2.setVisibility(View.VISIBLE);
        t3.setVisibility(View.VISIBLE);
        t1.setText("North: "+ String.valueOf(north));
        t2.setText("Left right: "+ String.valueOf(leftRight));
        t3.setText("Top Bottom: "+ String.valueOf(topBottom));

        File pathTxt=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File picsDir = new File(pathTxt, "SkylinePics");
        File file=new File (picsDir,createImageName()+".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.append(datatoTxt);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            OutputStreamWriter outputStream =new OutputStreamWriter(openFileOutput(file.getName(),MODE_PRIVATE));
            outputStream.write(datatoTxt);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        }

}


