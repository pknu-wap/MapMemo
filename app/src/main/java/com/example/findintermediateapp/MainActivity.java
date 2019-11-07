package com.example.findintermediateapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gigamole.library.ShadowLayout;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.naver.maps.map.widget.ZoomControlView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ChangeStateBar implements OnMapReadyCallback {
    private MapView mapView;
    private Cursor mCursor;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    View et_inputLocation;
    LinearLayout ll_input;
    LocationButtonView myLocation_btn;
    String user_location = "false";
    String location_mapx;
    String location_mapy;
    SQLiteDatabase db;
    Intent intent;
    double user_mapX;
    double user_mapY;
    int locationNum;
    int markerCount;
    SharedPreferences sf;

    RecyclerView markerRecyclerView = null;
    MarkerListAdapter markerListAdapter = null;
    ArrayList<MarkerListItem> markerList = new ArrayList<MarkerListItem>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("kafivuks0k"));
        myLocation_btn = findViewById(R.id.myLocation_btn);
        et_inputLocation = findViewById(R.id.input_location);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();

        int colorValue = Color.parseColor("#a3a3a3");
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_aa);

        markerRecyclerView = findViewById(R.id.marker_list);
        markerListAdapter = new MarkerListAdapter(markerList);
        markerRecyclerView.setAdapter(markerListAdapter);
        markerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final ShadowLayout shadowLayout = (ShadowLayout)findViewById(R.id.toolbar_shadow);
        shadowLayout.setIsShadowed(true);
        shadowLayout.setShadowAngle(5);
        shadowLayout.setShadowRadius(25);
        shadowLayout.setShadowDistance(0);
        shadowLayout.setShadowColor(colorValue);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        mapView.getMapAsync(naverMap -> {
            myLocation_btn.setMap(naverMap);
            myLocation_btn.setOnClickListener(overlay -> {
                Toast.makeText(this, "마커 1 클릭", Toast.LENGTH_SHORT).show();
            });

        });

        et_inputLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                    if (!hasFocus) {

                    } else {
                        if(markerCount < 5) {
                            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                            startActivity(searchIntent);
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "위치 추가는 최대 5번까지만 가능합니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.input_location:
                        if(markerCount < 5) {
                            Intent searchIntent2 = new Intent(MainActivity.this, SearchActivity.class);
                            startActivity(searchIntent2);
                            break;
                        }
                        else{
                            Toast.makeText(MainActivity.this, "위치 추가는 최대 5번 가능합니다.", Toast.LENGTH_SHORT).show();
                        }
                }
            }
        };

        et_inputLocation.setOnClickListener(clickListener);

        intent = getIntent();


        if(intent.getStringExtra("location_mapx") != null)
        {
            Log.d("location_mapx111", intent.getStringExtra("location_mapx"));
        }
    }

    public void myLocationOnClick(View view) {
        Toast.makeText(this, "마커 1 클릭", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_search:
                return true;
            case android.R.id.home:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setLocationButtonEnabled(false);

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);

        NaverMapOptions options = new NaverMapOptions()
                .locationButtonEnabled(false);

        myLocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "마커 1 클릭", Toast.LENGTH_SHORT).show();
            }
        });

        if(intent.getStringExtra("user_location") == null)
        {
            sf = getSharedPreferences("sFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sf.edit();
            editor.putInt("count", 0);
            markerCount = sf.getInt("count", 0);
            editor.commit();
            Log.d("위치 선택전 markerCount", String.valueOf(markerCount));
        }
        else{
            sf = getSharedPreferences("sFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sf.edit();
            editor.putInt("count", (sf.getInt("count",0)+1));
            editor.commit();
            markerCount = sf.getInt("count", 0);

            Log.d("위치 선택후 markerCount", String.valueOf(markerCount));

            // 카텍좌표계 -> 경위도 좌표계 변환
            GeoTransPoint oKA = new GeoTransPoint(Double.valueOf(intent.getStringExtra("location_mapx")), Double.valueOf(intent.getStringExtra("location_mapy")));
            GeoTransPoint oGeo = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, oKA);
            double lat = oGeo.getY();
            double lng = oGeo.getX();

            user_mapX = lat;
            user_mapY = lng;

            if(markerCount == 1) {
                CameraUpdate cameraUpdate1 = CameraUpdate.scrollTo(new LatLng(lat, lng));
                naverMap.moveCamera(cameraUpdate1);
                // 첫번째 마커 찍기
                Marker firstMarker = new Marker();
                addMarkerList(markerCount, intent.getStringExtra("location_name"));
                markerListAdapter.notifyDataSetChanged();

                firstMarker.setPosition(new LatLng(user_mapX, user_mapY));
                editor.putString("markerName1", intent.getStringExtra("location_name"));
                editor.putInt("markerNum1", markerCount);
                editor.putString("firstMapX", String.valueOf(user_mapX));
                editor.putString("firstMapY", String.valueOf(user_mapY));
                editor.commit();

                firstMarker.setMap(naverMap);
              }
              else if(markerCount == 2) {
                  Log.d("markerCount2", String.valueOf(markerCount));
                double firstMapX = Double.valueOf(sf.getString("firstMapX", "0"));
                double firstMapY = Double.valueOf(sf.getString("firstMapY", "0"));
                addMarkerList(sf.getInt("marKerNum1", 1), sf.getString("markerName1", "0"));
                addMarkerList(markerCount, intent.getStringExtra("location_name"));
                Log.d("sf.getInt(\"marKerNum1\", 0)", String.valueOf(sf.getInt("marKerNum1", 0)));
                markerListAdapter.notifyDataSetChanged();
                editor.putString("markerName2", intent.getStringExtra("location_name"));
                editor.putInt("markerNum2", markerCount);
                editor.putString("secondMapX", String.valueOf(user_mapX));
                editor.putString("secondMapY", String.valueOf(user_mapY));
                editor.commit();
                Log.d("firstMapX", sf.getString("firstMapX", "0"));
                Marker firstMarker = new Marker();
                firstMarker.setPosition(new LatLng(firstMapX, firstMapY));
                firstMarker.setMap(naverMap);

                CameraUpdate cameraUpdate2 = CameraUpdate.scrollTo(new LatLng(lat, lng));
                naverMap.moveCamera(cameraUpdate2);
                // 두번째 마커 찍기
                Marker secondMarker = new Marker();
                secondMarker.setPosition(new LatLng(user_mapX, user_mapY));
                secondMarker.setMap(naverMap);
            }
             else if(markerCount == 3) {
                double firstMapX = Double.valueOf(sf.getString("firstMapX", "0"));
                double firstMapY = Double.valueOf(sf.getString("firstMapY", "0"));
                double secondMapX = Double.valueOf(sf.getString("secondMapX", "0"));
                double secondMapY = Double.valueOf(sf.getString("secondMapY", "0"));
                addMarkerList(sf.getInt("markerNum1",1), sf.getString("markerName1", "0"));
                addMarkerList(sf.getInt("markerNum2", 2), sf.getString("markerName2", "0"));
                addMarkerList(markerCount, intent.getStringExtra("location_name"));
                markerListAdapter.notifyDataSetChanged();

                editor.putString("thirdMapX", String.valueOf(user_mapX));
                editor.putString("thirdMapY", String.valueOf(user_mapY));
                editor.putInt("markerNum3", markerCount);
                editor.putString("markerName3", intent.getStringExtra("location_name"));
                editor.commit();

                Log.d("firstMapX", sf.getString("firstMapX", "0"));
                Marker firstMarker = new Marker();
                firstMarker.setPosition(new LatLng(firstMapX, firstMapY));
                firstMarker.setMap(naverMap);
                Marker secondMarker = new Marker();
                secondMarker.setPosition(new LatLng(secondMapX, secondMapY));
                secondMarker.setMap(naverMap);

                CameraUpdate cameraUpdate3 = CameraUpdate.scrollTo(new LatLng(lat, lng));
                naverMap.moveCamera(cameraUpdate3);
                // 세번째 마커 찍기
                Marker thirdMarker = new Marker();
                thirdMarker.setPosition(new LatLng(user_mapX, user_mapY));
                thirdMarker.setMap(naverMap);
            }
             else if(markerCount == 4) {
                double firstMapX = Double.valueOf(sf.getString("firstMapX", "0"));
                double firstMapY = Double.valueOf(sf.getString("firstMapY", "0"));
                double secondMapX = Double.valueOf(sf.getString("secondMapX", "0"));
                double secondMapY = Double.valueOf(sf.getString("secondMapY", "0"));
                double thirdMapX = Double.valueOf(sf.getString("thirdMapX", "0"));
                double thirdMapY = Double.valueOf(sf.getString("thirdMapY", "0"));
                addMarkerList(sf.getInt("markerNum1",1), sf.getString("markerName1", "0"));
                addMarkerList(sf.getInt("markerNum2", 2), sf.getString("markerName2", "0"));
                addMarkerList(sf.getInt("markerNum3", 3), sf.getString("markerName3", "0"));
                addMarkerList(markerCount, intent.getStringExtra("location_name"));
                markerListAdapter.notifyDataSetChanged();
                editor.putString("fourthMapX", String.valueOf(user_mapX));
                editor.putString("fourthMapY", String.valueOf(user_mapY));
                editor.putInt("markerNum4", markerCount);
                editor.putString("markerName4", intent.getStringExtra("location_name"));
                editor.commit();
                Log.d("firstMapX", sf.getString("firstMapX", "0"));
                Marker firstMarker = new Marker();
                firstMarker.setPosition(new LatLng(firstMapX, firstMapY));
                firstMarker.setMap(naverMap);
                Marker secondMarker = new Marker();
                secondMarker.setPosition(new LatLng(secondMapX, secondMapY));
                secondMarker.setMap(naverMap);
                Marker thirdMarker = new Marker();
                thirdMarker.setPosition(new LatLng(thirdMapX, thirdMapY));
                thirdMarker.setMap(naverMap);

                CameraUpdate cameraUpdate4 = CameraUpdate.scrollTo(new LatLng(lat, lng));
                naverMap.moveCamera(cameraUpdate4);
                // 네번째 마커 찍기
                Marker fourthMarker = new Marker();
                fourthMarker.setPosition(new LatLng(user_mapX, user_mapY));
                fourthMarker.setMap(naverMap);
            }
             else if(markerCount == 5)
             {
                 double firstMapX = Double.valueOf(sf.getString("firstMapX", "0"));
                 double firstMapY = Double.valueOf(sf.getString("firstMapY", "0"));
                 double secondMapX = Double.valueOf(sf.getString("secondMapX", "0"));
                 double secondMapY = Double.valueOf(sf.getString("secondMapY", "0"));
                 double thirdMapX = Double.valueOf(sf.getString("thirdMapX", "0"));
                 double thirdMapY = Double.valueOf(sf.getString("thirdMapY", "0"));
                 double fourthMapX = Double.valueOf(sf.getString("fourthMapX", "0"));
                 double fourthMapY = Double.valueOf(sf.getString("fourthMapY", "0"));
                 addMarkerList(sf.getInt("markerNum1",1), sf.getString("markerName1", "0"));
                 addMarkerList(sf.getInt("markerNum2", 2), sf.getString("markerName2", "0"));
                 addMarkerList(sf.getInt("markerNum3", 3), sf.getString("markerName3", "0"));
                 addMarkerList(sf.getInt("markerNum4", 4), sf.getString("markerName4", "0"));
                 addMarkerList(markerCount, intent.getStringExtra("location_name"));
                 markerListAdapter.notifyDataSetChanged();
                 editor.putString("fifthMapX", String.valueOf(user_mapX));
                 editor.putString("fifthMapY", String.valueOf(user_mapY));
                 editor.putInt("markerNum5", markerCount);
                 editor.putString("markerName5", intent.getStringExtra("location_name"));
                 editor.commit();
                 Log.d("firstMapX", sf.getString("firstMapX", "0"));
                 Marker firstMarker = new Marker();
                 firstMarker.setPosition(new LatLng(firstMapX, firstMapY));
                 firstMarker.setMap(naverMap);
                 Marker secondMarker = new Marker();
                 secondMarker.setPosition(new LatLng(secondMapX, secondMapY));
                 secondMarker.setMap(naverMap);
                 Marker thirdMarker = new Marker();
                 thirdMarker.setPosition(new LatLng(thirdMapX, thirdMapY));
                 thirdMarker.setMap(naverMap);
                 Marker fourthMarker = new Marker();
                 fourthMarker.setPosition(new LatLng(fourthMapX, fourthMapY));
                 fourthMarker.setMap(naverMap);

                 CameraUpdate cameraUpdate5 = CameraUpdate.scrollTo(new LatLng(lat, lng));
                 naverMap.moveCamera(cameraUpdate5);
                 // 다섯번째 마커 찍기
                 Marker fifthMarker = new Marker();
                 fifthMarker.setPosition(new LatLng(user_mapX, user_mapY));
                 fifthMarker.setMap(naverMap);
             }
        }
    }

    public void addMarkerList(int num, String location)
    {
        MarkerListItem item = new MarkerListItem();
        item.setMarkerNum(num);
        item.setMarkerLocation(location);

        markerList.add(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            markerCount = 0;
            SharedPreferences.Editor editor = sf.edit();
            editor.clear();
            editor.commit();
           ActivityCompat.finishAffinity(this);
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
