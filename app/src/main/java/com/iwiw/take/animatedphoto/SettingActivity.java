package com.iwiw.take.animatedphoto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    SeekBar m_seekBarH;
    SeekBar m_seekBarS;
    SeekBar m_seekBarV;
    SeekBar m_seekBarBlur;
    SeekBar m_seekBarEdge;
    RadioGroup m_radioGroupSize;
    TextView m_textViewColor;
    TextView m_textViewSettingHue;
    TextView m_textViewSettingSaturation;
    TextView m_textViewSettingValue;
    TextView m_textViewSettingBlur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViewComponents();
    }

    public void onClickConvertDo(View view) {
        int size;
        switch (m_radioGroupSize.getCheckedRadioButtonId()) {
            default:
            case R.id.radioButtonSettingSmall:
                size = Consts.SIZE_S;
                break;
            case R.id.radioButtonSettingMedium:
                size = Consts.SIZE_M;
                break;
            case R.id.radioButtonSettingLarge:
                size = Consts.SIZE_L;
                break;
        }

        saveSetting(size);

        Intent intent = new Intent();
        intent.putExtra(ViewActivity.EXTRA_SETTING_SIZE, size);
        intent.putExtra(ViewActivity.EXTRA_SETTING_H, m_seekBarH.getProgress() + 1);
        intent.putExtra(ViewActivity.EXTRA_SETTING_S, m_seekBarS.getProgress() + 1);
        intent.putExtra(ViewActivity.EXTRA_SETTING_V, m_seekBarV.getProgress() + 1);
        intent.putExtra(ViewActivity.EXTRA_SETTING_BLUR, m_seekBarBlur.getProgress() * 2 + 1);
        intent.putExtra(ViewActivity.EXTRA_SETTING_EDGE, m_seekBarEdge.getProgress()!=0);
        intent.putExtra(ViewActivity.EXTRA_SETTING_EDGE_STRENGTH, m_seekBarEdge.getProgress());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickConvertCancel(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    private void getViewComponents() {
        m_seekBarH = (SeekBar) findViewById(R.id.seekBarConvertHue);
        m_seekBarS = (SeekBar) findViewById(R.id.seekBarConvertSaturation);
        m_seekBarV = (SeekBar) findViewById(R.id.seekBarConvertValue);
        m_seekBarBlur = (SeekBar) findViewById(R.id.seekBarConvertBlur);
        m_seekBarEdge = (SeekBar) findViewById(R.id.seekBarConvertEdge);
        m_radioGroupSize = (RadioGroup) findViewById(R.id.RadioGroupSettingSize);
        m_textViewColor = (TextView) findViewById(R.id.textViewSettingColor);
        m_textViewSettingHue = (TextView) findViewById(R.id.textViewSettingHue);
        m_textViewSettingSaturation = (TextView) findViewById(R.id.textViewSettingSaturation);
        m_textViewSettingValue = (TextView) findViewById(R.id.textViewSettingValue);
        m_textViewSettingBlur = (TextView) findViewById(R.id.textViewSettingBlur);
    }

    private void initViewComponents() {
        getViewComponents();
        loadSetting();
        updateViewOutput();

        SeekBar.OnSeekBarChangeListener seekbarHandler = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateViewOutput();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        m_seekBarH.setOnSeekBarChangeListener(seekbarHandler);
        m_seekBarS.setOnSeekBarChangeListener(seekbarHandler);
        m_seekBarV.setOnSeekBarChangeListener(seekbarHandler);
        m_seekBarBlur.setOnSeekBarChangeListener(seekbarHandler);
    }

    private void updateViewOutput() {
        m_textViewColor.setText("Total Color = " +
                (m_seekBarH.getProgress() + 1) * (m_seekBarS.getProgress() + 1) * (m_seekBarV.getProgress() + 1)
                + " colors");
        if ((m_seekBarH.getProgress() + 1) * (m_seekBarS.getProgress() + 1) * (m_seekBarV.getProgress() + 1) > 512) {
            m_textViewColor.setText(m_textViewColor.getText() + "\n" + "It will take time with this setting...");
        }
        m_textViewSettingHue.setText("Hue (" + (m_seekBarH.getProgress() + 1) + "):");
        m_textViewSettingSaturation.setText("Saturation (" + (m_seekBarS.getProgress() + 1) + "):");
        m_textViewSettingValue.setText("Value (" + (m_seekBarV.getProgress() + 1) + "):");
        m_textViewSettingBlur.setText("Blur Filter (" + (m_seekBarBlur.getProgress() * 2 + 1) + "):");
    }

    private void saveSetting(int size) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("NUM_H", m_seekBarH.getProgress());
        editor.putInt("NUM_S", m_seekBarS.getProgress());
        editor.putInt("NUM_V", m_seekBarV.getProgress());
        editor.putInt("NUM_BLUR", m_seekBarBlur.getProgress());
        editor.putInt("EDGE_STRENGTH", m_seekBarEdge.getProgress());
        editor.putInt("NUM_SIZE", size);
        editor.commit();
    }

    private void loadSetting() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_seekBarH.setProgress(sharedPreferences.getInt("NUM_H", Consts.DEFAULT_FILTER_NUM_H-1));
        m_seekBarS.setProgress(sharedPreferences.getInt("NUM_S", Consts.DEFAULT_FILTER_NUM_S-1));
        m_seekBarV.setProgress(sharedPreferences.getInt("NUM_V", Consts.DEFAULT_FILTER_NUM_V-1));
        m_seekBarBlur.setProgress(sharedPreferences.getInt("NUM_BLUR", Consts.DEFAULT_FILTER_BLUR / 2));
        m_seekBarEdge.setProgress(sharedPreferences.getInt("EDGE_STRENGTH", Consts.DEFAULT_FILTER_EDGE_STRENGTH));
        int size = sharedPreferences.getInt("NUM_SIZE", Consts.SIZE_S);
        RadioButton rb;
        switch (size){
            default:
            case 640:
                rb = (RadioButton)findViewById(R.id.radioButtonSettingSmall);
                rb.setChecked(true);
                break;
            case 1280:
                rb = (RadioButton)findViewById(R.id.radioButtonSettingMedium);
                rb.setChecked(true);
                break;
            case 1920:
                rb = (RadioButton)findViewById(R.id.radioButtonSettingLarge);
                rb.setChecked(true);
                break;
        }
    }
}
