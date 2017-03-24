package com.sprocomm.mobilebycle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wanghui
 * on 2017/3/10 0010.
 */
public class Settings extends Activity implements View.OnClickListener, TextWatcher {

    public static final String IP = "ip";
    public static final String DEFAULT_IP = "112.64.126.122";
    public static final String PORT = "port";
    public static final int DEFAULT_PORT = 7088;
    public static final String GPS_ECHO_GAP_RUN = "gps_echo_gap_run";
    public static final int DEFAULT_GPS_ECHO_GAP_RUN = 45;
    public static final String GPS_ECHO_GAP_LOCK = "gps_echo_gap_lock";
    public static final int DEFAULT_GPS_ECHO_GAP_LOCK = 70;
    public static final String GPS_PRE_TIME = "gps_pre_time";
    public static final int DEFAULT_GPS_PRE_TIME = 45;
    public static final String HEART_BEAT_GAP = "heart_beat_gap";
    public static final int DEFAULT_HEART_BEAT_GAP = 480;
    public static final String TYRE_PERIMETER = "tyre_perimeter";
    public static final int DEFAULT_TYRE_PERIMETER = 2100;
    public static final String IMEI = "bike_id";

    private Button mSaveButton;
    private EditText mIPEdit;
    private EditText mPortEdit;
    private EditText mGPSEchoTimeEditLock;
    private EditText mGPSEchoTimeEditRun;
    private EditText mHeartBeatGapEdit;
    private EditText mGPSPreTimeEdit;
    private EditText mTyrePerimeterEdit;

    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_layout);
        initView();

    }

    private void initView(){
        SharedPreferences spfs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        mSaveButton = (Button) findViewById(R.id.save);
        mSaveButton.setOnClickListener(this);

        mIPEdit = (EditText) findViewById(R.id.edit_ip);
        mIPEdit.addTextChangedListener(this);
        mIPEdit.setHint(spfs.getString(IP, DEFAULT_IP));

        mPortEdit = (EditText) findViewById(R.id.edit_port);
        mPortEdit.addTextChangedListener(this);
        mPortEdit.setHint(""+spfs.getInt(PORT, DEFAULT_PORT));

        mGPSEchoTimeEditLock = (EditText) findViewById(R.id.edit_gps_echo_gap_lock);
        mGPSEchoTimeEditLock.addTextChangedListener(this);
        mGPSEchoTimeEditLock.setHint("" + spfs.getInt(GPS_ECHO_GAP_LOCK, DEFAULT_GPS_ECHO_GAP_LOCK));

        mGPSEchoTimeEditRun = (EditText) findViewById(R.id.edit_gps_echo_gap_run);
        mGPSEchoTimeEditRun.addTextChangedListener(this);
        mGPSEchoTimeEditRun.setHint("" + spfs.getInt(GPS_ECHO_GAP_RUN, DEFAULT_GPS_ECHO_GAP_RUN));

        mHeartBeatGapEdit = (EditText) findViewById(R.id.edit_heart_beat_gap);
        mHeartBeatGapEdit.addTextChangedListener(this);
        mHeartBeatGapEdit.setHint("" + spfs.getInt(HEART_BEAT_GAP, DEFAULT_HEART_BEAT_GAP));

        mGPSPreTimeEdit = (EditText) findViewById(R.id.edit_gps_pre_time);
        mGPSPreTimeEdit.addTextChangedListener(this);
        mGPSPreTimeEdit.setHint("" + spfs.getInt(GPS_PRE_TIME, DEFAULT_GPS_PRE_TIME));

        mTyrePerimeterEdit = (EditText) findViewById(R.id.edit_tyre_perimeter);
        mTyrePerimeterEdit.addTextChangedListener(this);
        mTyrePerimeterEdit.setHint("" + spfs.getInt(TYRE_PERIMETER, DEFAULT_TYRE_PERIMETER));
    }

    private boolean isGoodIp(String ip){
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(ip);
        return mat.find();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.save) {
            if(mIPEdit.getText().length() > 6) {
                final String ip = mIPEdit.getText().toString();
                if(isGoodIp(ip)) {
                    Log.i("simon", "good ip addr");
                    mEditor.putString(IP, ip);
                }
            }

            if(mPortEdit.getText().length() > 0) {
                final String portstr = mPortEdit.getText().toString();
                final int port = Integer.parseInt(portstr);
                Log.i("simon", "port = " + port);
                mEditor.putInt(PORT, port);
            }

            if(mGPSEchoTimeEditLock.getText().length() > 0) {
                final String str = mGPSEchoTimeEditLock.getText().toString();
                final int gap = Integer.parseInt(str);
                Log.i("simon", "lock gps echo gap = " + gap);
                mEditor.putInt(GPS_ECHO_GAP_LOCK, gap);
            }

            if(mGPSEchoTimeEditRun.getText().length() > 0) {
                final String str = mGPSEchoTimeEditRun.getText().toString();
                final int gap = Integer.parseInt(str);
                Log.i("simon", "run gps echo gap = " + gap);
                mEditor.putInt(GPS_ECHO_GAP_RUN, gap);
            }

            if(mHeartBeatGapEdit.getText().length() > 0) {
                final String str = mHeartBeatGapEdit.getText().toString();
                final int gap = Integer.parseInt(str);
                Log.i("simon", "heart beat gap = " + gap);
                mEditor.putInt(HEART_BEAT_GAP, gap);
            }

            if(mGPSPreTimeEdit.getText().length() > 0) {
                final String str = mGPSPreTimeEdit.getText().toString();
                final int preTime = Integer.parseInt(str);
                Log.i("simon", "gps pre time = " + preTime);
                mEditor.putInt(GPS_PRE_TIME, preTime);
            }

            if(mTyrePerimeterEdit.getText().length() > 0) {
                final String str = mTyrePerimeterEdit.getText().toString();
                final int tyrePerimeter = Integer.parseInt(str);
                Log.i("simon", "Tyre Perimeter = " + tyrePerimeter);
                mEditor.putInt(GPS_PRE_TIME, tyrePerimeter);
            }

            mEditor.commit();
            finish();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        boolean enable = editable.length() > 0;
        mSaveButton.setEnabled(enable);
    }
}
