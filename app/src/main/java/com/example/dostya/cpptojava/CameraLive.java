package com.example.dostya.cpptojava;

import android.Manifest;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Images.Media.getBitmap;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class CameraLive extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener{

    private SensorManager manag;
    private Sensor magnetic;
    private Sensor accelerometer;
    private Camera mCamera;
    private SurfaceView mSurface;
    private SurfaceHolder mSurfaceHolder;
    float[] accelerometerVector=new float[4];
    float[] gravity= new float[3];
    float[] magneticVector=new float[3];
    float[] resultMatrix=new float[9];
    float[] values=new float[4];
    private final float alpha = (float) 0.8;
    float north, northh,topBottom,leftRight=0;
    int x,y=0;
    float distance,angleFinal=0;
    double hauteur;
    float northSend,topBottomSend,leftRightSend=0;
    float angle[];
    float alt[];
    int []posBDD=new int [1920];
    int lengthData=0;
    float [] dataToSend;
    int ind;
    int angleZElem;
    int ind_j;
    int ind_i;
    int verifX, pos_yy, pos_xx=0;
    int _j;
    int _i;
    float myZZ;
    double firstPoint=0.0;
    float resolI=0.f;
    float resolJ=0.f;
    Uri pictureURI = null;
    private String createImageName() {
        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Calendar cal = Calendar.getInstance();
        String strDate = timeStamp.format(cal.getTime());
        String imageFileName = "JPEG_" +strDate + "_";

        return imageFileName;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.d("tryy","taille data: "+Integer.toString(lengthData=data.length));
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            mCamera.startPreview();
            //Bitmap.Config conf = Bitmap.Config.ARGB_8888;
           //Bitmap bitmap2=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),conf);
            byte [] iBlue=new byte [bitmap.getHeight()*bitmap.getWidth()];
            for (int x = 0; x < bitmap.getWidth(); x++)
            {
                for (int y = 0; y < bitmap.getHeight(); y++)
                {
                   // bitmap2.setPixel(x, y), bitmap.getPixel(x, y) & 0xFF0000FF);
                    int rgb=bitmap.getPixel(x, y);
                    iBlue[(y+x*bitmap.getHeight())]= (byte)(rgb & 0xff);
                }
            }
            //int size = bitmap2.getRowBytes() * bitmap2.getHeight();
            //ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            byte[] data2;
            //bitmap.copyPixelsToBuffer(byteBuffer);
            //data2 = byteBuffer.array();
           // Log.d("tryy","taille data2: "+Integer.toString(lengthData=data2.length));
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            File pictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File picsDir = new File(pictureFile, "SkylinePics");
            picsDir.mkdirs();
            String pictureName = "";
            pictureName = createImageName();
            File imageFile = new File(picsDir, pictureName);
            if (imageFile == null){
                Log.d("test", "Error creating media file, check storage permissions");
                return;
            }
            /*try {
                FileOutputStream fos = new FileOutputStream(imageFile);
               fos.write(iBlue);
               fos.close();
            } catch (FileNotFoundException e) {
                Log.d("test", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("test", "Error accessing file: " + e.getMessage());
            }*/
            topBottomSend=topBottom;
            northSend=north;
            leftRightSend=leftRight;
            //angle en global donc peut être envoyé !
        }
    };
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                TextView tuto=findViewById(R.id.tuto);
               tuto.setVisibility(INVISIBLE);
            }
        }, 10000);

        angle=getIntent().getFloatArrayExtra("ANGLE");
        alt=getIntent().getFloatArrayExtra("ALT");
        myZZ=getIntent().getFloatExtra("Z",0);
        double latitude=getIntent().getDoubleExtra("lat",0);
        double longitude=getIntent().getDoubleExtra("long",0);
        TextView lat= findViewById(R.id.textView);
        lat.setText("Latitude: "+latitude);
        TextView longi= findViewById(R.id.textView2);
        longi.setText("Longitude: "+longitude);
        Button boutton = (Button) CameraLive.this.findViewById(R.id.button22);
        boutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        Log.d ("caca",Float.toString(myZZ));
        Log.d ("caca",Float.toString(angle[0]));
        Log.d ("caca",Float.toString(alt[3000]));
        manag = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetic = manag.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = manag.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manag.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manag.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);


        Camera cm;
        if (checkCameraHardware(CameraLive.this)==true)
            {

                mSurface= (SurfaceView) findViewById(R.id.surfaceView);
                mSurfaceHolder =mSurface.getHolder();
                mSurfaceHolder.addCallback((SurfaceHolder.Callback) this);
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }

}

     @Override
    public void onRequestPermissionsResult(int requestCode,
                                        String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //j'ai l'autorisation

                } else {
                    Toast.makeText(CameraLive.this, "Permission denied to access your camera", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }
     private boolean checkCameraHardware(Context context) {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
        // this device has a camera
        return true;
    } else {
        // no camera on this device
        return false;
    }
}
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera=Camera.open();
        Camera.Parameters parameters;
        parameters=mCamera.getParameters();
        mCamera.setDisplayOrientation(0);
        parameters.setPictureSize(1920, 1080);
        mCamera.setParameters(parameters);
        double thetaV = (parameters.getVerticalViewAngle());
        double thetaH = (parameters.getHorizontalViewAngle());
        Log.d("Field", Double.toString(thetaV));
        Log.d("Field", Double.toString(thetaH));

        mSurface.setOnTouchListener(new OnTouchListener ()
        {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            x=(int)event.getX();
            y=(int)event.getY();
            //Log.d("tryy", "x: "+Integer.toString(x));
            //Log.d("tryy", "y: "+Integer.toString(y));
            int posNorm=mSurface.getWidth()/2-x;
            float posNormDeg=(posNorm/(mSurface.getWidth()/(60)));
            if (north <0) {
                north = (360 + north);
            }
            Log.d("tryy","Nord"+ north);
            float AzymuthCursor=north-posNormDeg;//northh à changer
            angleZElem=(int)((AzymuthCursor+30)*30);
            Log.d("tryy","positiontab"+ angleZElem);
            if (angleZElem>=0 && angleZElem<angle.length) {
                angleFinal = angle[angleZElem];
                distance = alt[angleZElem];
                hauteur = myZZ + Math.tan(angleFinal * Math.PI / 180) * (double) (distance);
                Log.d("tryy","Distance"+ distance);
                Log.d("tryy","Hauteur"+ hauteur);
            }
            verifX=1;
            return false;

        }});
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        mCamera.startPreview();
        {

    }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    @Override

    public void onSensorChanged(SensorEvent event) {

        if (mSurface.getWidth() !=0 &&  mSurface.getHeight()!=0)
        {
            Bitmap bitmap = Bitmap.createBitmap(mSurface.getWidth(), mSurface.getHeight(), Bitmap.Config.ARGB_8888);
            //Math.round(mSurface.getWidth()/60)
            float xNorth= (north+90)*30+(mSurface.getWidth()/2); // faux a faire sur 180 °
            int xPixNorth = mSurface.getWidth() - Math.round(xNorth);
            // début new version
            float resolAng = 30;
            if (north <0) {
                north = (360 + north);
            }
            firstPoint=(north+30)*30;

            ImageView aiguille=findViewById(R.id.imageView3);
            //Log.d ("tryy","Nord"+ northh);
            //Log.d ("tryy","Nord2"+ (northh + 360)% 360);
            aiguille.setRotation(north+90);
            resolJ=mSurface.getWidth()/60;
            resolI=mSurface.getHeight()/30;
            int l=0;
            for (int k=0; k<80*30;k++)
            {
               ind = (int)(firstPoint + k-40*30);
               if (ind>0&&ind<420*30)
               {
                   float rho= angle[ind]+90+topBottom;
                   float pos_i =  rho*resolI;
                   float pos_j=((float)k/30.f-40.f)*resolJ;
                   pos_i=(float)(pos_i*Math.cos(Math.toRadians(leftRight))-(pos_j*Math.sin(Math.toRadians(leftRight))));
                   pos_j=(float)(pos_i*Math.sin(Math.toRadians(leftRight))+(pos_j*Math.cos(Math.toRadians(leftRight))));
                   pos_i=mSurface.getHeight()/2-pos_i;
                   pos_j=mSurface.getWidth()/2+pos_j;
                   ind_i=Math.round(pos_i);
                   ind_j=Math.round(pos_j);
                   if (ind==angleZElem)
                   {
                       pos_yy=ind_i;
                       pos_xx=ind_j;
                   }
                  // Log.d("test","rho: " +Float.toString(rho));
                  // Log.d("test","j: " +Integer.toString(ind_j));
                   //Log.d("test","i: " +Integer.toString(ind_i));
                   if (ind_j>0 && ind_i>0 && ind_i<mSurface.getHeight()-1 && ind_j<mSurface.getWidth()) {
                       //posBDD[l]=ind_i; a completer
                       bitmap.setPixel(ind_j,ind_i, Color.BLUE);
                       bitmap.setPixel(ind_j,ind_i+1, Color.BLUE);
                       bitmap.setPixel(ind_j,ind_i-1, Color.BLUE);
                   }
                   }
               l++;
               }

            if (verifX==1 && pos_yy!=0) {
                TextView hauteurTxt=findViewById(R.id.textView3);
                hauteurTxt.setText("Hauteur: "+hauteur);
                hauteurTxt.setVisibility(VISIBLE);
                TextView distanceTxt=findViewById(R.id.textView4);
                distanceTxt.setText("Distance: "+distance);
                distanceTxt.setVisibility(VISIBLE);
                for (int i=1;i<25;i++)
                {
                    for (int j=1;j<i;j++)
                    {
                        if (pos_xx>=0 && pos_xx<mSurface.getWidth() && pos_yy-i<mSurface.getHeight()&& pos_yy-i>=0)
                        {
                            bitmap.setPixel(pos_xx, pos_yy - i, Color.RED);
                        }
                        if (pos_xx-j<mSurface.getWidth()&& pos_xx-j>=0 && pos_yy-i<mSurface.getHeight()&& pos_yy-i>=0) {
                            bitmap.setPixel(pos_xx - j, pos_yy - i, Color.RED);
                        }
                        if (pos_xx+j<mSurface.getWidth()&& pos_xx+j>=0 && pos_yy-i<mSurface.getHeight()&& pos_yy-i>=0) {
                            bitmap.setPixel(pos_xx + j, pos_yy - i, Color.RED);
                        }
                     }
                }
            }
            ImageView view=findViewById(R.id.view);
            view.setImageBitmap(bitmap);

    }
        // Extraction des données de l'accelerometre et du champs mag
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            // accelerometerVector contient les données accelerometre
            accelerometerVector[0] = event.values[0];
            accelerometerVector[1] = event.values[1];
            accelerometerVector[2] = event.values[2];
            accelerometerVector[3] = 0;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
           // ici ce sont les données champs magnetique
            magneticVector = event.values;
        }
        // On extrait maintenant les angles correspondants
        float[] R = new float[9];
        float[] R2 = new float[9];
        float[] I2 = new float[9];
        float[] tmp = new float[3];
        // Estime la matrice de rotation du téléphone a partir des données accel + champs mag
        manag.getRotationMatrix(resultMatrix, null, gravity, magneticVector);
        // Méthode 1 : détermine azymut/pitch/roll
        manag.getOrientation(resultMatrix, tmp);
        //double azymuth;
        //azymuth=Math.atan((resultMatrix[1]-resultMatrix[3])/(resultMatrix[0]+resultMatrix[4]));
        // l'azimuth
         north =(float) Math.toDegrees(tmp[0]);
        // le pitch
       leftRight = (float) Math.toDegrees(tmp[1]);
        // le roll
        topBottom = (float) Math.toDegrees(tmp[2]);
        // Méthode 2 : détermine les variations suivant des axes fixes ("repere monde")
        manag.getRotationMatrix(resultMatrix, null, gravity, magneticVector);
        manag.remapCoordinateSystem(resultMatrix,manag.AXIS_Y,
                manag.AXIS_Z, R);
        float orientation[] = new float[3];
        manag.getOrientation(R, orientation);
        //angles
        float X = (float) Math.toDegrees(orientation[0]);
        float Y = (float) Math.toDegrees(orientation[1]);
        float Z= (float) Math.toDegrees(orientation[2]);

        Log.d ("Angle", "Nord:" +Float.toString(north));
       // Log.d ("Angle", "Azymuth:" +Math.toDegrees(azymuth));
        //Log.d ("Angle","leftright:" +Float.toString(leftRight));
        //Log.d ("Angle", "TopBottom:" +Float.toString(topBottom));
       // Log.d ("Angle", "X:" +Float.toString(X));
        //Log.d ("Angle","Y:" +Float.toString(Y));
        //Log.d ("Angle", "Z:" +Float.toString(Z));

    }

}

