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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


public class MobileBycle extends Activity implements OnClickListener, OnMarkerClickListener, OnMarkerDragListener{

    private static final int BUTTON_STATE = 1;
    private static final int RECEIVE_FROM_SERVER = 3;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_DISCONNECTED = 3;
    private Button unLock;
    private Button findBike;
    private Button modifyServer;
    private Button modifyGpsTime;
    private Button modifyInterval;
    private Button modifyGpsPreTime;
    private Button rebootDevice;
    private Button mSettingsButton;
    private Button modifyTyrePerimeter;
    private TextView imei;
    private EditText inputImei;
    private Button connect;
    private double mLontitude;
    private double mLatitude;
    private Button disconnect;
    private Socket mSocket = null;

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
                        addMarker(getLatLng(mess));
                    }
                    break;
            }

        }
    };

    private TextView serverText;
    private MapView mMapView;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private BaiduMap mBaiduMap;
    private String[] titles = new String[] { "one", "two", "three", "four" };
    private LatLng[] latlngs = new LatLng[] {new LatLng(mLatitude,mLontitude), new LatLng(31.213945,121.635361),
            new LatLng(31.213977,121.635361), new LatLng(31.213944,121.635366) };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mobile_bycle_layout);
        initView();
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );
        //注册监听函数
//		initMarker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.start();
        initLocation();
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
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    /*  "**,101,432423423,31.24254,160.43432&&" */
    private LatLng getLatLng(String msg) {
        int i = msg.lastIndexOf(',');
        int j = msg.lastIndexOf('&');
        double lat = Double.parseDouble(msg.substring(i+1,j-1));

        final String tmp = msg.substring(0, i-1);
        j = tmp.lastIndexOf(',');
        double lon = Double.parseDouble(msg.substring(j+1,i-1));

        return new LatLng(lat, lon);
    }

    private void addMarker(LatLng latLng) {
        // 设置地图类型 MAP_TYPE_NORMAL 普通图； MAP_TYPE_SATELLITE 卫星图
        mBaiduMap.setMapType(BaiduMap. MAP_TYPE_NORMAL);
        // 开启交通图
        mBaiduMap.setTrafficEnabled( true);
        // 设置地图当前级别
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(19);
        mBaiduMap.setMapStatus(statusUpdate);
        //覆盖物显示的图标
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_focuse_mark);
        OverlayOptions option = new MarkerOptions().position(latLng).icon(descriptor).draggable(true);
        // 清除地图上所有的覆盖物
        mBaiduMap.clear();
        // 将覆盖物添加到地图上
        mBaiduMap.addOverlay(option);
        // 将覆盖物设置为地图中心
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        // 以动画方式更新地图状态，动画耗时 300 ms
        mBaiduMap.animateMapStatus(u);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMarkerDragListener(this);
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
        mMapView = (MapView) findViewById(R.id.bmapView);
        Button btGetLocation = (Button) findViewById(R.id.get_location);
        mBaiduMap = mMapView.getMap();
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
                if(mLontitude == 0 && mLatitude == 0){
                    Toast.makeText(MobileBycle.this, "定位失败", Toast.LENGTH_SHORT).show();
                }else{
                    setMarker(new LatLng(mLatitude,mLontitude));
                }
                Log.d("wjb ---","-mLatitude:" + mLatitude +" mLontitude:" + mLontitude);
                break;
            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                break;
            default:
                break;
        }
    }

    private void showErrorConnectToast(){
        Toast.makeText(this, R.string.no_connect_to_server,Toast.LENGTH_SHORT).show();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setOpenGps(true);
        option.setAddrType("all");// 返回的定位结果包含地址信息
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02

        option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            //获取定位结果
            StringBuilder sb = new StringBuilder(256);

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息

            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息

            sb.append("\nradius : ");
            sb.append(location.getRadius());    //获取定位精准度
            mLontitude = location.getLongitude();
            mLatitude = location.getLatitude();
            setMarker(new LatLng(mLatitude,mLontitude));
            if (location.getLocType() == BDLocation.TypeGpsLocation){

                // GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());    // 单位：公里每小时

                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());    //获取卫星数

                sb.append("\nheight : ");
                sb.append(location.getAltitude());    //获取海拔高度信息，单位米

                sb.append("\ndirection : ");
                sb.append(location.getDirection());    //获取方向信息，单位度

                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\noperationers : ");
                sb.append(location.getOperators());    //获取运营商信息

                sb.append("\ndescribe : ");
                sb.append("网络定位成功");

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

            }

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());    //位置语义化信息

            List<Poi> list = location.getPoiList();    // POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            Log.i("BaiduLocationApiDem", sb.toString());
        }

        @Override
        public void onConnectHotSpotMessage(String arg0, int arg1) {

        }
    }
    private void setMarker(LatLng latLng){
        mBaiduMap.clear();
        OverlayOptions overlayOptions = null;
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setTrafficEnabled( true);
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(17);
        mBaiduMap.setMapStatus(statusUpdate);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
        overlayOptions = new MarkerOptions().position(latLng).icon(descriptor);
        Marker marker=(Marker) mBaiduMap.addOverlay(overlayOptions);
        Bundle bundle = new Bundle();
        bundle.putString( "info", titles[0]+ "个");
        marker.setExtraInfo(bundle);
        // 将覆盖物添加到地图上
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(u);
        mBaiduMap.setOnMarkerClickListener(this);
    }

    private void initMarker() {
        mBaiduMap.clear();
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
        // 设置地图类型 MAP_TYPE_NORMAL 普通图； MAP_TYPE_SATELLITE 卫星图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 开启交通图
        mBaiduMap.setTrafficEnabled( true);
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(17);
        mBaiduMap.setMapStatus(statusUpdate);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
        //循环添加四个覆盖物到地图上
        for ( int i = 0; i < titles. length; i++) {
            latLng= latlngs[i];
            overlayOptions = new MarkerOptions().position(latLng).icon(descriptor);
            // 将覆盖物添加到地图上
            Marker marker=(Marker) mBaiduMap.addOverlay(overlayOptions);
            Bundle bundle = new Bundle();
            bundle.putString( "info", titles[i]+ "个");
            marker.setExtraInfo(bundle);
        }
        // 将最后一个坐标设置为地图中心
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(u);
        //设置地图覆盖物的点击事件
        mBaiduMap.setOnMarkerClickListener(this);

    }

    /**
     * @Title: onMarkerClick
     * @Description: 覆盖物点击事件,每次点击一个覆盖物则会在相应的覆盖物上显示一个InfoWindow
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        final String msg = marker.getExtraInfo().getString( "info");
        InfoWindow mInfoWindow;
        // 生成一个TextView用户在地图中显示InfoWindow
        TextView location = new TextView(getApplicationContext());
        location.setBackgroundResource(R.drawable.icon_openmap_focuse_mark);
        location.setPadding(30, 20, 30, 20);
        location.setText(msg);
        //构建弹框所在的经纬度，
        final LatLng ll = marker.getPosition();
        Point p = mBaiduMap.getProjection().toScreenLocation(ll);
        p. y -= 47; //让弹框在Y轴偏移47
        LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
        //根据上面配置好的参数信息，构造一个InfoWindow。
        mInfoWindow = new InfoWindow(location, llInfo, -47);
        //构建好之后，然后调用show的方法，让弹框显示出来
        mBaiduMap.showInfoWindow(mInfoWindow);
        //弹框点击事件-
        location.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MobileBycle.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    @Override
    public void onMarkerDrag(Marker arg0) {

    }

    @Override
    public void onMarkerDragEnd(Marker arg0) {

    }

    @Override
    public void onMarkerDragStart(Marker arg0) {

    }
}
