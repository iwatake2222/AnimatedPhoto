package com.iwiw.take.animatedphoto;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bitmap> {
    public static final String EXTRA_IMAGE_URI_ORG = "com.iwiw.take.animatedphoto.EXTRA_IMAGE_URI_ORG";
    public static final String EXTRA_SETTING_H = "com.iwiw.take.animatedphoto.EXTRA_SETTING_H";
    public static final String EXTRA_SETTING_S = "com.iwiw.take.animatedphoto.EXTRA_SETTING_S";
    public static final String EXTRA_SETTING_V = "com.iwiw.take.animatedphoto.EXTRA_SETTING_V";
    public static final String EXTRA_SETTING_BLUR = "com.iwiw.take.animatedphoto.EXTRA_SETTING_BLUR";
    public static final String EXTRA_SETTING_EDGE = "com.iwiw.take.animatedphoto.EXTRA_SETTING_EDGE";
    public static final String EXTRA_SETTING_SIZE = "com.iwiw.take.animatedphoto.EXTRA_SETTING_SIZE";
    static final int REQUEST_SETTING = 200;
    Switch m_switchOrg;
    ImageView m_imageViewOrg;
    ImageView m_imageViewConv;
    Bitmap m_bmpOrg;
    Bitmap m_bmpConv;
    Uri m_orgUri;
    ConvertTask m_convertTaskLoader;
    public Handler m_bitmapProgressHandler;
    public Handler m_progressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        m_switchOrg = (Switch) findViewById(R.id.switchViewImage);
        m_imageViewOrg = (ImageView) findViewById(R.id.imageViewViewOrg);
        m_imageViewConv = (ImageView) findViewById(R.id.imageViewViewConv);
        String orgUriStr = getIntent().getStringExtra(EXTRA_IMAGE_URI_ORG);
        if (orgUriStr != null) {
            m_bmpConv = null;
            m_orgUri = Uri.parse(orgUriStr);
            m_bmpOrg = BitmapFactory.decodeFile(m_orgUri.getPath());
            m_bmpOrg = Bitmap.createScaledBitmap(m_bmpOrg, Consts.SIZE_L, Consts.SIZE_L * m_bmpOrg.getHeight() / m_bmpOrg.getWidth(), false);
            m_imageViewOrg.setImageBitmap(m_bmpOrg);
            showOriginalImage();
        }
        setProgressHandler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarViewProgress);
                if (progressBar.getVisibility() != View.VISIBLE) {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickConvert(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, REQUEST_SETTING);
    }

    public void onClickSave(View view) {
        if (m_bmpConv != null) {
            Utility.saveBitmap(this, m_bmpConv, getConvUri().getPath());
            view.setEnabled(false);
        }
    }


    public void onClickOriginal(View view) {
        Switch switchOrg = (Switch) view;
        if (switchOrg.isChecked()) {
            showOriginalImage();
        } else {
            showConvertedImage();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTING && resultCode == RESULT_OK) {
            int size = data.getIntExtra(EXTRA_SETTING_SIZE, Consts.SIZE_S);
            int numH = data.getIntExtra(EXTRA_SETTING_H, Consts.DEFAULT_FILTER_NUM_H);
            int numS = data.getIntExtra(EXTRA_SETTING_S, Consts.DEFAULT_FILTER_NUM_S);
            int numV = data.getIntExtra(EXTRA_SETTING_V, Consts.DEFAULT_FILTER_NUM_V);
            int blur = data.getIntExtra(EXTRA_SETTING_BLUR, Consts.DEFAULT_FILTER_BLUR);
            boolean isShowEdge = data.getBooleanExtra(EXTRA_SETTING_EDGE, true);
            startConvert(size, numH, numS, numV, blur, isShowEdge);
        }
    }

    private Uri getConvUri() {
        String convUriStr = Utility.getSaveDirectory() + "/" + m_orgUri.getLastPathSegment();
        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat dataFormat = new SimpleDateFormat("mmss");
        convUriStr += "_" + dataFormat.format(date) + ".jpg";
        return Uri.parse(convUriStr);
    }

    private void startConvert(int size, int numH, int numS, int numV, int blur, boolean isShowEdge) {
//        Utility.logDebug(Integer.toString(numH));
//        Utility.logDebug(Integer.toString(numS));
//        Utility.logDebug(Integer.toString(numV));
//        Utility.logDebug(Integer.toString(blur));
//        Utility.logDebug(Boolean.toString(isShowEdge));
//        Utility.logDebug(Integer.toString(size));
        Bundle bundle = new Bundle();
        bundle.putString("orgUrl", m_orgUri.toString());
        bundle.putInt("size", size);
        bundle.putInt("numH", numH);
        bundle.putInt("numS", numS);
        bundle.putInt("numV", numV);
        bundle.putInt("blur", blur);
        bundle.putBoolean("isShowEdge", isShowEdge);
//        getLoaderManager().initLoader(1, bundle, this);
        getLoaderManager().restartLoader(1, bundle, this);

        showConvertedImage();
        setTitle("Converting");
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarViewProgress);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutViewControllers);
        linearLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        if (id == 1) {
            m_convertTaskLoader = new ConvertTask(this,
                    Uri.parse(args.getString("orgUrl")), args.getInt("size"),
                    args.getInt("numH"), args.getInt("numS"), args.getInt("numV"),
                    args.getInt("blur"),
                    args.getBoolean("isShowEdge")
                    );
            m_convertTaskLoader.forceLoad();
            return m_convertTaskLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        if (data == null) {
            Utility.logError("Convert error");
            Utility.showToast(this, "Failed to convert");
            return;
        }

        m_bmpConv = data;
        m_imageViewConv.setImageBitmap(m_bmpConv);
        showConvertedImage();
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        Utility.logDebug("onLoaderReset");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_convertTaskLoader!=null) {
            m_convertTaskLoader.stopLoading();
            getLoaderManager().destroyLoader(1);
        }
        showOriginalImage();
    }



    private void showOriginalImage() {
        m_switchOrg.setChecked(true);
        setTitle("Original image");
        Button saveButton = (Button) findViewById(R.id.buttonViewSave);
        Button convButton = (Button) findViewById(R.id.buttonViewConvert);
        saveButton.setVisibility(View.INVISIBLE);
        convButton.setVisibility(View.VISIBLE);
        m_imageViewConv.setVisibility(View.INVISIBLE);
        m_imageViewOrg.setVisibility(View.VISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarViewProgress);
        progressBar.setVisibility(View.INVISIBLE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutViewControllers);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void showConvertedImage() {
        m_switchOrg.setChecked(false);
        setTitle("Converted image");
        Button saveButton = (Button) findViewById(R.id.buttonViewSave);
        Button convButton = (Button) findViewById(R.id.buttonViewConvert);
        saveButton.setVisibility(View.VISIBLE);
        convButton.setVisibility(View.INVISIBLE);
        if (m_bmpConv != null) saveButton.setEnabled(true);
        m_imageViewConv.setVisibility(View.VISIBLE);
        m_imageViewOrg.setVisibility(View.INVISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarViewProgress);
        progressBar.setVisibility(View.INVISIBLE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutViewControllers);
        linearLayout.setVisibility(View.VISIBLE);
    }


    private void setProgressHandler() {
        m_bitmapProgressHandler = new Handler() {
            public void handleMessage(Message msg) {
                m_bmpConv = (Bitmap) msg.obj;
                m_imageViewConv.setImageBitmap(m_bmpConv);
            }
        };
        m_progressHandler = new Handler() {
            public void handleMessage(Message msg) {
                int progress = (Integer) msg.obj;
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarViewProgress);
                progressBar.setProgress(progress);
            }
        };
    }
}
