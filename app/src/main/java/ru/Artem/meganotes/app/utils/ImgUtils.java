package ru.Artem.meganotes.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Created by Артем on 03.05.2016.
 */
public class ImgUtils {

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("d.MM.yyyy k:m", Locale.ROOT);

    private final static String LOG_TAG = ImgUtils.class.getName();

    private static final boolean DEBUG = true;

    public static File createImageFile(String folderToSave) throws IOException {
        String timeStamp = sDateFormat.toString();
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        return new File(folderToSave, imageFileName);
    }

    public static Uri cameraRequest(Context context, int requestCode, String folderToSave) throws IOException {

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile;
            photoFile = createImageFile(folderToSave);
            if (DEBUG) Log.d(LOG_TAG, "we have in photoFile path: " + photoFile.getPath());

            Uri mOutFilePath = Uri.fromFile(photoFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutFilePath);
            ((Activity) context).startActivityForResult(captureIntent, requestCode);

            return mOutFilePath;

        }
        return null;
    }

    public static void galleryRequest(Context context, int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        ((Activity) context).startActivityForResult(photoPickerIntent, requestCode);
    }

    public static String savePicture(Bitmap bitmap, String folderToSave) throws IOException {
        String timeStamp = sDateFormat.toString();
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);

        File file = new File(folderToSave, imageFileName);
        FileOutputStream fOut = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
        fOut.flush();
        fOut.close();
        return "file://" + file.getAbsolutePath();
    }
}
