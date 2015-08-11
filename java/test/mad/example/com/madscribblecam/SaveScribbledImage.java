package test.mad.example.com.madscribblecam;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by prateek on 7/7/2015.
 */
public class SaveScribbledImage {

    Context mContext;
    Bitmap image_to_save=null;
    SaveScribbledImage( Context context){
        mContext=context;
    }

    public void saveImage(Bitmap image){
        image_to_save=image;
        File saveFile = openFileForImage();
        if (saveFile != null) {
            saveImageToFile(saveFile);
        } else {
            Toast.makeText(mContext, "Unable to open file for saving image.",
                    Toast.LENGTH_LONG).show();
        }
    }
    private File openFileForImage() {
        File imageDirectory = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MADCamActivity");




            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
                        Locale.getDefault());

                return new File(imageDirectory.getPath() +
                        File.separator + "image_" +
                        dateFormat.format(new Date()) + ".jpeg");
            }
        }
        return null;
    }

    private void saveImageToFile(File file) {
        if (image_to_save != null) {
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
                if (!image_to_save.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                    Toast.makeText(mContext, "Unable to save image to file.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Saved image to: " + file.getPath(),
                            Toast.LENGTH_LONG).show();
                }
                outStream.close();
            } catch (Exception e) {
                Toast.makeText(mContext, "Unable to save image to file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
