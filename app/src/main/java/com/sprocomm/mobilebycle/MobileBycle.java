package com.sprocomm.mobilebycle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MobileBycle extends Activity implements OnClickListener, LocationSource, AMapLocationListener {

    private static final int BUTTON_STATE = 1;
    private static final int RECEIVE_FROM_SERVER = 3;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_DISCONNECTED = 3;
    private static final int RESULT_FROM_CAPTURE_ACTIVITY = 1;
    private static final String RETURN_BYCLE_ID = "return_bycle_id";
    private Button unLock;
    private Button findBike;
    private Button modifyServer;
    private Button modifyGpsTime;
    private Button modifyInterval;
    private Button modifyGpsPreTime;
    private Button rebootDevice;
    private Button mSettingsButton;
    private Button  btScan;
    private Button modifyTyrePerimeter;
    private TextView imei;
    private EditText inputImei;
    private Button connect;
    private double mLontitude;
    private double mLatitude;
    private Button disconnect;
    private Socket mSocket = null;
    private MapView mMapView;
    private AMap aMap;
    private Toast mToast;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocation mMapLocation;


    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case BUTTON_STATE:
                    buttonState(STATE_CONNECTED);
                    break;
                case RECEIVE_FROM_SERVER:
                    final String mess = (String) msg.obj;
                    serverText.setText(mess);
                    if(mess.startsWith("**,101")) {

                    }
                    break;
            }

        }
    };

    private TextView serverText;
    private String[] titles = new String[] { "one", "two", "three", "four" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mobile_bycle_layout);
        initView();
        mMapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mLocationClient.stopLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.onDestroy();
    }

    private void initView(){
        inputImei = (EditText)findViewById(R.id.put_imei);
        imei = (TextView)findViewById(R.id.imei);
        serverText = (TextView)findViewById(R.id.server_text);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button)findViewById(R.id.disconnect);
        unLock = (Button)findViewById(R.id.unlocking);
        findBike = (Button)findViewById(R.id.find_bike);
        modifyServer = (Button)findViewById(R.id.modify_server_data);
        modifyGpsTime = (Button)findViewById(R.id.modify_gps);
        modifyInterval = (Button)findViewById(R.id.modify_interval);
        modifyGpsPreTime =(Button)findViewById(R.id.modify_start_gps_time);
        rebootDevice = (Button) findViewById(R.id.reboot_device);
        mSettingsButton = (Button) findViewById(R.id.settings);
        modifyTyrePerimeter= (Button) findViewById(R.id.modify_tyre_perimeter);
        Button btGetLocation = (Button) findViewById(R.id.get_location);
        mMapView = (MapView) findViewById(R.id.map);
        btScan = (Button)findViewById(R.id.scan);
        imei.setText(R.string.default_imei);
        imei.setTextSize(20);
        connect.setOnClickListener(this);
        disconnect.setOnClickListener(this);
        imei.setOnClickListener(this);
        btGetLocation.setOnClickListener(this);
        unLock.setOnClickListener(this);
        findBike.setOnClickListener(this);
        modifyServer.setOnClickListener(this);
        modifyGpsTime.setOnClickListener(this);
        modifyInterval.setOnClickListener(this);
        modifyGpsPreTime.setOnClickListener(this);
        rebootDevice.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        modifyTyrePerimeter.setOnClickListener(this);
        btScan.setOnClickListener(this);
        //初始化地图控制器对象
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        mToast = Toast.makeText(this,"", Toast.LENGTH_SHORT);
    }

    public void buttonState(int state){
        switch(state) {
            case STATE_CONNECTED:
                disconnect.setEnabled(true);
                connect.setEnabled(false);
                unLock.setEnabled(true);
                findBike.setEnabled(true);
                modifyServer.setEnabled(true);
                modifyGpsTime.setEnabled(true);
                modifyInterval.setEnabled(true);
                modifyGpsPreTime.setEnabled(true);
                rebootDevice.setEnabled(true);
                modifyTyrePerimeter.setEnabled(true);
                send("##"+ imei.getText() + ",999&&");
                break;
            case STATE_CONNECTING:
                disconnect.setEnabled(false);
                connect.setEnabled(false);
                unLock.setEnabled(false);
                findBike.setEnabled(false);
                modifyServer.setEnabled(false);
                modifyGpsTime.setEnabled(false);
                modifyInterval.setEnabled(false);
                modifyGpsPreTime.setEnabled(false);
                rebootDevice.setEnabled(false);
                modifyTyrePerimeter.setEnabled(false);
                break;
            case STATE_DISCONNECTED:
                disconnect.setEnabled(false);
                connect.setEnabled(true);
                unLock.setEnabled(false);
                findBike.setEnabled(false);
                modifyServer.setEnabled(false);
                modifyGpsTime.setEnabled(false);
                modifyInterval.setEnabled(false);
                modifyGpsPreTime.setEnabled(false);
                rebootDevice.setEnabled(false);
                modifyTyrePerimeter.setEnabled(false);
                break;
        }
    }
    private void receiveMsg(){
        if(mSocket != null){
            try {
                InputStream in = mSocket.getInputStream();
                if(in != null){
                    byte[] buffer = new byte[1024];
                    int count = in.read(buffer);
                    if(count == 0){
                        return;
                    }
                    String bufferToString = new String(buffer);
                    String realMessage = bufferToString.substring(0, count);
                    Message message = mHandler.obtainMessage();
                    message.obj = realMessage;
                    message.what = RECEIVE_FROM_SERVER;
                    mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void send(String msg) {
        if(mSocket != null) {
            try {
                OutputStream out = mSocket.getOutputStream();
                if(out != null) {
                    out.write(msg.getBytes());
                    out.flush();
                    return;
                } else {
                    Log.i("simon", "output stream null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        showErrorConnectToast();
    }

    private void connect() {

        buttonState(STATE_CONNECTING);

        SharedPreferences pfs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        final  String ip = pfs.getString(Settings.IP, Settings.DEFAULT_IP);
        final int port = pfs.getInt(Settings.PORT, Settings.DEFAULT_PORT);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    mSocket = new Socket(InetAddress.getByName(ip), port);
                    if(mSocket.isConnected()) {
                        mHandler.sendEmptyMessage(BUTTON_STATE);
                        while(true){
                            receiveMsg();
                        }
                    }
                }catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void disconnect(){
        if(mSocket != null) {
            try {
                send("##CLOSE");
                mSocket.shutdownOutput();
                mSocket.close();
                mSocket = null;
                buttonState(STATE_DISCONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {

        SharedPreferences pfs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        switch(v.getId()){
            case R.id.imei:
                if(inputImei.isShown()){
                    if(inputImei.getText().length() == 15){
                        imei.setText(inputImei.getText());
                        inputImei.setText(null);
                        inputImei.setVisibility(View.INVISIBLE);
                    }else{
                        Toast.makeText(this, R.string.hint_input_imei, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    inputImei.setVisibility(View.VISIBLE);
                    inputImei.setHint(R.string.hint_input_imei);
                }
            case R.id.connect:
                connect();
                break;
            case R.id.disconnect:
                disconnect();
                break;
            case R.id.unlocking:
                send("##"+ imei.getText() + ",202&&");
                break;
            case R.id.find_bike:
                send("##"+ imei.getText() + ",203&&");
                break;
            case R.id.modify_server_data:
                final String ip = pfs.getString(Settings.IP, Settings.DEFAULT_IP);
                final int port = pfs.getInt(Settings.PORT, Settings.DEFAULT_PORT);
                send("##"+ imei.getText() + ",301," + ip + "," + port + "&&");
                break;
            case R.id.modify_gps:
                final int lockSecond = pfs.getInt(Settings.GPS_ECHO_GAP_LOCK, Settings.DEFAULT_GPS_ECHO_GAP_LOCK);
                final int runSecond = pfs.getInt(Settings.GPS_ECHO_GAP_RUN, Settings.DEFAULT_GPS_ECHO_GAP_RUN);
                send("##"+ imei.getText() + ",304,"+lockSecond + "," + runSecond + "&&");
                break;
            case R.id.modify_interval:
                final int interval = pfs.getInt(Settings.HEART_BEAT_GAP, Settings.DEFAULT_HEART_BEAT_GAP);
                send("##"+ imei.getText() + ",307," + interval + "&&");
                break;
            case R.id.modify_start_gps_time:
                final int gpsPreTime = pfs.getInt(Settings.GPS_PRE_TIME, Settings.DEFAULT_GPS_PRE_TIME);
                send("##"+ imei.getText() + ",308," + gpsPreTime + "&&");
                break;
            case R.id.modify_tyre_perimeter:
                final int tyrePerimeter = pfs.getInt(Settings.TYRE_PERIMETER, Settings.DEFAULT_TYRE_PERIMETER);
                send("##"+ imei.getText() + ",309," + tyrePerimeter + "&&");
                break;
            case R.id.reboot_device:
                send("##"+ imei.getText() + ",900&&");
                break;
            case R.id.get_location:
                if(mMapLocation != null && mListener !=null){
                    mListener.onLocationChanged(mMapLocation);
                    aMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                }else{
                    showTip("定位失败");
                }
                Log.d("wjb ---","-mLatitude:" + mLatitude +" mLontitude:" + mLontitude);
                break;
            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.scan:
                startActivityForResult(new Intent(this,CaptureActivity.class),RESULT_FROM_CAPTURE_ACTIVITY);
                break;
            default:
                break;
        }
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private void showErrorConnectToast(){
        Toast.makeText(this, R.string.no_connect_to_server,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
// TODO Auto-generated method stub
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);
                mMapLocation = amapLocation;
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                Log.d("wjb sprocomm","amapLocation:" + amapLocation);
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case RESULT_FROM_CAPTURE_ACTIVITY:
                if(data ==null){
                    return;
                }
                String returnBycleId = data.getStringExtra(RETURN_BYCLE_ID);
                if(returnBycleId !=null) {
                    imei.setText(returnBycleId);
                    if(returnBycleId.length() == 15){
                        send("##"+ imei.getText() + ",202&&");
                    }
                }
        }
    }
}
