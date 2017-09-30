package go_solution.dechiffrierer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.Toast;

import com.inthecheesefactory.lab.intent_fileprovider.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_TAKE_PHOTO = 1;

    Button btnTakePhoto;
    ImageView ivPreview;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initInstances();
    }

    private void initInstances() {
        btnTakePhoto = (Button) findViewById(R.id.decipher);
        ivPreview = (ImageView) findViewById(R.id.imageview);

        btnTakePhoto.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view == btnTakePhoto) {
            MainActivityPermissionsDispatcher.startCameraWithCheck(this);
        }
    }


    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void startCamera() {
        try {
            dispatchTakePictureIntent();
        } catch (IOException e) {
        }
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("Access to External Storage is required")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                Bitmap temp = BitmapFactory.decodeStream(ims);
                temp = filterMethod(temp);

                //ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));
                ivPreview.setImageBitmap(temp);
            } catch (FileNotFoundException e) {
                return;
            }

            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }
            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private Bitmap filterMethod(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];

        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        int[] rgbteil = new int[3];

        int rot = Color.RED;
        /*
        int malrot = Color.red(rot);
        int malgruen = Color.green(rot);
        int malblau = Color.blue(rot);
        int malalpha = Color.alpha(rot);
        int alphateil = 0;
        int rotteil = 0;
        int blauteil = 0;
        int gruenteil = 0;*/


        //get alpha
        int rota = (rot>>24) & 0xff;

        //get red
        int rotr = (rot>>16) & 0xff;

        //get green
        int rotg = (rot>>8) & 0xff;

        //get blue
        int rotb = rot & 0xff;


        for (int i=0; i<data.length; i++){

           /*
            alphateil = Color.alpha(data[i]);
            rotteil = Color.red(data[i]);
            blauteil = Color.blue(data[i]);
            gruenteil = Color.green(data[i]);


            rotteil *= malrot;
            gruenteil *= malgruen;
            blauteil *= malblau;

            data[i] = Color.rgb(rotteil,gruenteil, blauteil);*/

            //get alpha
            int a = (data[i]>>24) & 0xff;

            //get red
            int r = (data[i]>>16) & 0xff;

            //get green
            int g = (data[i]>>8) & 0xff;

            //get blue
            int b = data[i] & 0xff;

            a *= rota;
            r *= rotr;
            g *= rotg;
            b *= rotb;

            data[i] = (a<<24) | (r<<16) | (g<<8) | b;



        }

        return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
    }

}
