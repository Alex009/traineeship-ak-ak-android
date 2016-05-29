package ru.Artem.meganotes.app.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.Artem.meganotes.app.dataBaseHelper.DataBaseHelper;
import ru.Artem.meganotes.app.dialogs.AddImageDialog;
import ru.Artem.meganotes.app.R;
import ru.Artem.meganotes.app.models.Note;
import ru.Artem.meganotes.app.utils.CustomImageMaker;
import ru.Artem.meganotes.app.utils.DateUtils;
import ru.Artem.meganotes.app.utils.ImgUtils;

/**
 * Created by Артем on 22.04.2016.
 */
public class CreateNoteActivity extends AppCompatActivity implements AddImageDialog.OnItemListClickListener {

    private EditText mTitleNote;
    private EditText mContentNote;
    private ImageView mImageView;
    private LinearLayout mView;
    private RelativeLayout mLayoutForImages;
    private List<String> imagePaths;

    private Uri mOutFilePath = null;
    private ComponentName mCallingActivity;

    private final int GALLERY_REQUEST = 10;
    private final int CAMERA_REQUEST = 11;
    private final String LOG_TAG = CreateNoteActivity.class.getName();
    private String sSavePath;
    public final static String CREATE_NOTE_KEY = "noteCreate";
    public static final int CREATE_NOTE_REQUEST = 1001;

    private static final boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create);

        mTitleNote = (EditText) findViewById(R.id.editTitleNote);
        mContentNote = (EditText) findViewById(R.id.editContentNote);
        mImageView = (ImageView) findViewById(R.id.imageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mView = (LinearLayout) findViewById(R.id.layoutCreate);
        mLayoutForImages = (RelativeLayout) findViewById(R.id.LayoutForImages);

        mCallingActivity = getCallingActivity();

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorTitleText));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (mCallingActivity.getClassName().equals(MainActivity.class.getName())) {
                getSupportActionBar().setTitle(R.string.new_note);
            } else {
                getSupportActionBar().setTitle(R.string.edit_note);//поменять на заголовок заметки
            }
        }
        sSavePath = this.getFilesDir().toString();
        imagePaths = new ArrayList<>();

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (DEBUG) {
            Log.d(LOG_TAG, "our screen width is: " + display.getWidth());
            Log.d(LOG_TAG, "our screen height is: " + display.getHeight());
            int placeForImages = display.getWidth() - 32; //32 = dimen x2
            Log.d(LOG_TAG, "we have placeForImages value: " + placeForImages);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mCallingActivity.getClassName().equals(MainActivity.class.getName())) {
            getMenuInflater().inflate(R.menu.menu_create, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.upload_img) {

            final AddImageDialog addImageDialog = new AddImageDialog();

            addImageDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AddImageDialog);
            addImageDialog.show(getSupportFragmentManager(), AddImageDialog.DIALOG_KEY);

        } else if (id == android.R.id.home) {
            finish();

        } else if (id == R.id.close_with_out_save) {
            mContentNote.setText("");
            finish();
        }
        return true;
    }

    @Override
    public void finish() {
        if (!mContentNote.getText().toString().isEmpty()) {

            String date = DateUtils.getDate();

            if (mImageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                try {
                    imagePaths.add(ImgUtils.savePicture(bitmap, sSavePath));
                } catch (IOException e) {
                    Snackbar.make(mView, R.string.str_problems_save, Snackbar.LENGTH_LONG).show();
                }
            }
            //TODO переписвать то что выше так, что бы в imagePaths попадали все пути, из множественного добавления
            DataBaseHelper helper = DataBaseHelper.getInstance(getApplicationContext());
            Note newNote;
            try{
                newNote = helper.addNote(mTitleNote.getText().toString(), mContentNote.getText().toString(), date, imagePaths);
            }catch (SQLiteException e)
            {
                newNote=null;
                Snackbar.make(mView, R.string.cant_add_note_message,Snackbar.LENGTH_LONG).show();
            }

            if (DEBUG) {
                Log.d(LOG_TAG, "what we have in newNote?");
                Log.d(LOG_TAG, "newNote name: " + newNote.getNameNote());
                Log.d(LOG_TAG, "newNote content: " + newNote.getContent());
                List<String> tmpList = newNote.getPathImg();
                Log.d(LOG_TAG, "In image newNote we have count: " + tmpList.size());
                Log.d(LOG_TAG, "content image newNote is:" + tmpList.get(0));
            }
            Intent intent = new Intent();
            intent.putExtra(CREATE_NOTE_KEY, newNote);
            setResult(CREATE_NOTE_REQUEST, intent);
        } //иначе dataBaseHelper.edit(...); ??????
        super.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap img = null;
        if ((resultCode == Activity.RESULT_OK) && (requestCode == GALLERY_REQUEST)) {
            Uri selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch (IOException e) {
                Snackbar.make(mView, getString(R.string.str_problems_message), Snackbar.LENGTH_LONG).show();
            }
            if (DEBUG) {
                Log.d(LOG_TAG, "we have selectedImage is: " + selectedImage);
            }
            CustomImageMaker image = new CustomImageMaker(getBaseContext(),"имя файла",selectedImage.toString(),true);
            mLayoutForImages.addView(image);
            //mImageView.setImageBitmap(img);
        }
        if ((resultCode == Activity.RESULT_OK) && (requestCode == CAMERA_REQUEST)) {
            Uri selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch (IOException e) {
                Snackbar.make(mView, getString(R.string.str_problems_message), Snackbar.LENGTH_LONG).show();
            }
            if (DEBUG) {
                Log.d(LOG_TAG, "we have selectedImage is: " + selectedImage);
            }
            mImageView.setImageBitmap(img);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(DialogFragment dialogFragment, int position) {
        switch (position) {
            case 0:
                if (ActivityCompat.checkSelfPermission(CreateNoteActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 0);
                } else {
                    try {
                        mOutFilePath = ImgUtils.cameraRequest(CreateNoteActivity.this, CAMERA_REQUEST, sSavePath);
                    } catch (IOException e) {
                        mOutFilePath = null;
                        Snackbar.make(mView, getString(R.string.str_problems_save), Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
            case 1:
                if (ActivityCompat.checkSelfPermission(CreateNoteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    ImgUtils.galleryRequest(CreateNoteActivity.this, GALLERY_REQUEST);
                }
                break;
        }
        dialogFragment.dismiss();
    }
}
