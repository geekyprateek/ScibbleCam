package test.mad.example.com.madscribblecam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MADCamActivity extends Activity implements Camera.PictureCallback,SurfaceHolder.Callback ,View.OnTouchListener {

    private Camera mCamera;                     // camera object
    private ImageView mCameraImagePreview;      // captured Image preview for Scribbling
    private SurfaceView mCameraPreview;         // Surface view for camera
    private Button mCaptureImageButton;         // capture button for camera
    private byte[] mCameraData;                 // image data get on callback after picture taken from camera
    private boolean mIsCapturing;               // whether in preview mode or capturing mode
    Bitmap mCameraBitmap=null;                  // camera image on callback generated from raw camera data
    Button saveButton=null;
    TextView ImageMode=null;


    Bitmap alteredBitmap;                       //image after scribble initail:blank like white paper
    Canvas canvas;                              //like a painting holder object on which alteredBitmap pasted
    Paint paint;                                //like brush to draw
    Matrix matrix;                              //raw parameter for the constructor of canvas

    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    SaveScribbledImage saveImage=null;//object to save altered or scribbled image


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_madcam);

        /*****************set the preview ImageView visibility to false*****************/
        mCameraImagePreview = (ImageView) findViewById(R.id.camera_image_view);
        mCameraImagePreview.setVisibility(View.INVISIBLE);

        /*****************set the surface view for camera capture*****************/
        mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        mCaptureImageButton = (Button) findViewById(R.id.capture_image_button);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);

        saveButton = (Button) findViewById(R.id.Save_button);
        saveButton.setOnClickListener(mSaveImageButtonClickListener);

        ImageMode=(TextView)findViewById(R.id.ImageMode);



        /*****************reference class to save altered image******************/
        saveImage=new SaveScribbledImage(this);

        /******************* camera is in capturing mode on start of app***********************/
        mIsCapturing = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (Exception e) {
                Toast.makeText(MADCamActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /* Difference listeners are used for code modularity ,we can use single listener also but used for future extension*/


    /*   callback listener for capturing image button MAD it!! button*/
    private View.OnClickListener mCaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            captureImage();
        }
    };

    /*   callback listener for capturing image again REMAD it!! button*/
    private View.OnClickListener mRecaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupImageCapture();
        }
    };

    /*   callback listener for saving image */
    private View.OnClickListener mSaveImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveImage.saveImage(alteredBitmap);
        }
    };



    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        setupImageDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(MADCamActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    private void captureImage() {
        mCamera.takePicture(null, null, this);
    }



    private void setupImageCapture() {
        mCameraImagePreview.setVisibility(View.INVISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);

        saveButton.setEnabled(false);
        mCamera.startPreview();
        mCaptureImageButton.setText("Mad it!!");
        ImageMode.setText("Capture Image");
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);

        mIsCapturing=true;
    }

    private void setupImageDisplay() {
        mCamera.stopPreview();

        /*  decoding raw data from camera to a Bitmap */
        mCameraBitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
        mCameraImagePreview.setImageBitmap(mCameraBitmap);

        mCameraPreview.setVisibility(View.INVISIBLE);
        mCameraImagePreview.setVisibility(View.VISIBLE);

        saveButton.setEnabled(true);
        mCaptureImageButton.setText("REMad it!!");
        ImageMode.setText("Scribble Image");
        mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
        mIsCapturing=false;

        startScribble();
    }

/*

    Creating an alteredBitmap from original image for Scribbling action

  */
    public void startScribble(){
        try {
                alteredBitmap = Bitmap.createBitmap(mCameraBitmap.getWidth(), mCameraBitmap
                        .getHeight(), mCameraBitmap.getConfig());
                canvas = new Canvas(alteredBitmap);
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(5);
                matrix = new Matrix();
                canvas.drawBitmap(mCameraBitmap, matrix, paint);


                mCameraImagePreview.setImageBitmap(alteredBitmap);
                mCameraImagePreview.setOnTouchListener(this);
        } catch (Exception e) {
                Log.v("ERROR", e.toString());
        }
    }



    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = event.getX();
                downy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                upx = event.getX();
                upy = event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                mCameraImagePreview.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                upx = event.getX();
                upy = event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                mCameraImagePreview.invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }
}


