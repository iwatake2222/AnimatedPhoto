package com.iwiw.take.animatedphoto;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    static final int REQUEST_CAPTURE_IMAGE = 100;
    static final int REQUEST_GALLERY_IMAGE_BEFORE19 = 101;
    static final int REQUEST_GALLERY_IMAGE_AFTER19 = 102;
    Uri m_cameraSaveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Utility.getPermissionStorage(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onClickCamera(View view) {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        m_cameraSaveUri = Utility.getSaveFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, m_cameraSaveUri);
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    public void onClickGallery(View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY_IMAGE_BEFORE19);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            startActivityForResult(intent, REQUEST_GALLERY_IMAGE_AFTER19);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            Utility.registerContent(this, m_cameraSaveUri.getPath());
            // need wait?
            Intent intent = new Intent(this, ViewActivity.class);
            intent.putExtra(ViewActivity.EXTRA_IMAGE_URI_ORG, m_cameraSaveUri.toString());
            startActivity(intent);
        } else if (requestCode == REQUEST_GALLERY_IMAGE_BEFORE19) {
            Uri uri = data.getData();
            String[] columns = {MediaStore.Images.Media.DATA };
            Cursor c = getContentResolver().query(uri, columns, null, null, null);
            c.moveToFirst();
            int index = c.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = c.getString(index);
            Intent intent = new Intent(this, ViewActivity.class);
            intent.putExtra(ViewActivity.EXTRA_IMAGE_URI_ORG, path);
            startActivity(intent);
        } else if (requestCode == REQUEST_GALLERY_IMAGE_AFTER19) {
            Uri uri = data.getData();
            String path = "";
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = DocumentsContract.getDocumentId(uri);
                String selection = "_id=?";
                String[] selectionArgs = new String[]{id.split(":")[1]};
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns.DATA}, selection, selectionArgs, null);
                cursor.moveToFirst();
                path = cursor.getString(0);
                cursor.close();
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                String id = DocumentsContract.getDocumentId(uri);
                Uri docUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                Cursor cursor = getContentResolver().query(docUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                path = cursor.getString(0);
                cursor.close();
            }
            Intent intent = new Intent(this, ViewActivity.class);
            intent.putExtra(ViewActivity.EXTRA_IMAGE_URI_ORG, path);
            startActivity(intent);
        }
    }

}
