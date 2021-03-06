package com.yanawha.osejin.yanawha;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapView.MapViewEventListener, MapView.CurrentLocationEventListener {


    private CustomProgress cp;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private MapPOIItem marker;
    private MapPoint mapPoint;
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private Button btnSearchCenter;
    private ArrayList<MarkerInfo> markers = new ArrayList<>( );
    private ArrayList<MarkerInfo> centermarkers = new ArrayList<>( );
    private TextView tvTotalParticipants;
    private final int REQUEST_PERMISSION = 1;
    final static String TAG = "MainActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    private void initDefaultFont() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder( )
                .setDefaultFontPath("Sunflower-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build( ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDefaultFont( );


        Places.initialize(getApplicationContext( ), BuildConfig.PLACES_API_KEY);

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        mapView = new MapView(this);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter( ));
        mapViewContainer = findViewById(R.id.map_view);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        mapView.setHDMapTileEnabled(true);
        mapViewContainer.addView(mapView);

        //custom Progress
        cp = new CustomProgress(MainActivity.this);
        tvTotalParticipants = findViewById(R.id.tv_total_participants);
        //init Search Center buttons
        btnSearchCenter = findViewById(R.id.btn_search_center);
        btnSearchCenter.setVisibility(View.GONE);

        final String[] keyword = {""};
        final CenterInfoManager.Code[] code = {null};


        btnSearchCenter.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                switch (btnSearchCenter.getText( ).toString( )) {
                    case "중간지역찾기": {

                        cp.show( );

                        CustomDialog alert2 = new CustomDialog(MainActivity.this);
                        alert2.setDialogTitle("어떤 장소를 찾을까요?");
                        alert2.setDialogContent("선택해주세요");
                        alert2.setDialogYes("맛집");
                        alert2.setDialogNo("카페");
                        alert2.setDialogListener(new MyDialogListener( ) {
                            @Override
                            public void onPositiveClicked() {
                                keyword[0] = "맛집";
                                code[0] = CenterInfoManager.Code.RESTAURANT;
                                MarkerInfo marker = computeCentroid(markers);
                                CenterInfoManager centerInfoManager = new CenterInfoManager( );
                                centerInfoManager.getLocationInfo(new DataCallback( ) {
                                    @Override
                                    public void DataCallback(JsonObject itemlist) {
                                        Gson gson = new Gson( );
                                        MarkerInfo[] array = gson.fromJson(itemlist.get("documents"), MarkerInfo[].class);
                                        List<MarkerInfo> list = Arrays.asList(array);
                                        addCenterMarker(list);
                                    }
                                }, new MarkerInfo(marker.getLat( ), marker.getLng( )), code[0], keyword[0], 8);
                            }

                            @Override
                            public void onNegativeClicked() {
                                keyword[0] = "카페";
                                code[0] = CenterInfoManager.Code.CAFE;
                                MarkerInfo marker = computeCentroid(markers);
                                CenterInfoManager centerInfoManager = new CenterInfoManager( );
                                centerInfoManager.getLocationInfo(new DataCallback( ) {
                                    @Override
                                    public void DataCallback(JsonObject itemlist) {
                                        Gson gson = new Gson( );
                                        MarkerInfo[] array = gson.fromJson(itemlist.get("documents"), MarkerInfo[].class);
                                        List<MarkerInfo> list = Arrays.asList(array);
                                        addCenterMarker(list);
                                    }
                                }, new MarkerInfo(marker.getLat( ), marker.getLng( )), code[0], keyword[0], 8);
                            }
                        });
                        alert2.showDialog( );
                        break;

                    }
                    case "다시하기": {
                        markers.clear( );
                        mapView.removeAllPOIItems( );
                        mapView.refreshMapTiles( );
                        btnSearchCenter.setVisibility(View.GONE);
                        tvTotalParticipants.setText("참가자:" + markers.size( ));
                    }

                }

            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        findViewById(R.id.map_scale_up).setOnClickListener(v -> mapView.zoomIn(true));
        findViewById(R.id.map_scale_down).setOnClickListener(v -> mapView.zoomOut(true));
        findViewById(R.id.map_cur_loc).setOnClickListener(v -> {


            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_PERMISSION);

                } else {
                    cp.show( );
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                }
            }else{
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    CustomDialog dialog = new CustomDialog(this);
                    dialog.setDialogTitle("GPS가 꺼져있습니다.");
                    dialog.setDialogContent("예를 누르면 설정창으로 이동합니다.");
                    dialog.setDialogNo("아니요");
                    dialog.setDialogYes("예");
                    dialog.setDialogListener(new MyDialogListener( ) {
                        @Override
                        public void onPositiveClicked() {
                            //GPS 설정화면으로 이동
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                            return;
                        }

                        @Override
                        public void onNegativeClicked() {
                            return;
                        }
                    });
                    dialog.showDialog( );
                } else {
                    cp.show( );
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                }
            }


        });


        //Search fab init
        findViewById(R.id.fab_search).setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("KOR")
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissions are granted");
                    cp.show( );
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                } else {
                    return;
                }
                return;
        }
    }


    void showWebViewDialogAsUrl(String url) {
        AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create( );
        WebView wv = new WebView(this);
        LinearLayout wrapper = new LinearLayout(this);
        EditText keyboardHack = new EditText(this);
        keyboardHack.setVisibility(View.GONE);
        wv.getSettings( ).setJavaScriptEnabled(true);
        wv.setFocusable(true);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(wv, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        wrapper.addView(keyboardHack, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        WebViewClient webViewClient = new WebViewClient( ) {
            Boolean flag = true;

            @Override
            public void onPageFinished(WebView view, String url) {


                if (view.getCertificate( ) != null && view.getOriginalUrl( ) != null) {
                    alert.show( );
                    cp.dismiss( );
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.d(TAG, "                ----onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d(TAG, "                ----shouldOverrideUrlLoading: " + url);

                flag = false;

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                    return true;
                }
                // tel일경우 아래와 같이 처리해준다.
                else if (url.startsWith("tel:")) {
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(tel);
                    return true;
                }
                return false;
            }
        };
        wv.setWebViewClient(webViewClient);
        wv.loadUrl(url);
        alert.setView(wrapper);
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "close", new DialogInterface.OnClickListener( ) {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss( );
            }
        });
        alert.show( );
        alert.hide( );


    }

    //마커를 추가한다 (장소이름, 위경도)
    void addMarker(MarkerInfo markerinfo) {
        marker = new MapPOIItem( );
        marker.setItemName(markerinfo.getPlaceName( ));
        marker.setTag(0);
        mapPoint = MapPoint.mapPointWithGeoCoord(markerinfo.getLat( ), markerinfo.getLng( ));
        marker.setMapPoint(mapPoint);
        // 기본으로 제공하는 BluePin 마커 모양.
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        //ArrayList marker 추가
        markers.add(markerinfo);
        mapView.addPOIItem(marker);
        mapView.selectPOIItem(marker, true);
        mapView.setMapCenterPoint(marker.getMapPoint( ), false);
        mapView.setZoomLevel(4, true);

        if (markers.size( ) >= 2) {
            btnSearchCenter.setText("중간지역찾기");
            btnSearchCenter.setVisibility(View.VISIBLE);
        } else {
            btnSearchCenter.setVisibility(View.GONE);
        }
        tvTotalParticipants.setText("참여자 : " + markers.size( ));
    }

    //중간위치의 정보들을 받아 마커를 추가한다
    void addCenterMarker(List<MarkerInfo> markerinfo) {


        for (MarkerInfo mk : markerinfo) {

            marker = new MapPOIItem( );
            marker.setItemName(mk.getPlaceName( ));
            marker.setTag(0);
            //data를 잘못받아넣음 lat lng 바뀜
            mapPoint = MapPoint.mapPointWithGeoCoord(mk.getLng( ), mk.getLat( ));
            marker.setMapPoint(mapPoint);
            // 기본으로 제공하는 BluePin 마커 모양.
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
            marker.setCustomImageResourceId(R.drawable.star_32);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomSelectedImageResourceId(R.drawable.star_32);


            //ArrayList marker 추가
            centermarkers.add(mk);
            mapView.addPOIItem(marker);
            mapView.selectPOIItem(marker, true);
            mapView.setMapCenterPoint(marker.getMapPoint( ), false);

        }
        btnSearchCenter.setText("다시하기");
        mapView.setZoomLevel(4, true);
        cp.dismiss( );

    }

    private MarkerInfo computeCentroid(List<MarkerInfo> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size( );

        for (MarkerInfo point : points) {
            latitude += point.getLat( );
            longitude += point.getLng( );
        }

        return new MarkerInfo(latitude / n, longitude / n);
    }

    //gps 검색이 완료되면 호출됩니다.
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresslist = null;
        try {
            addresslist = geocoder.getFromLocation(mapPoint.getMapPointGeoCoord( ).latitude, mapPoint.getMapPointGeoCoord( ).longitude, 1);
        } catch (IOException e) {
            Log.e("error", "입출력 오류 - 서버에서 주소변환시 에러발생");
            e.printStackTrace( );
        }
        if (!addresslist.isEmpty( )) {

            CustomDialog alert = new CustomDialog(MainActivity.this);
            final String address = addresslist.get(0).getAddressLine(0).toString( );
            alert.setDialogContent(address);

            alert.setDialogListener(new MyDialogListener( ) {

                @Override
                public void onPositiveClicked() {
                    //검색한 장소의 마커를 추가합니다.
                    Log.d(TAG, "onPositiveClicked: select item click");

                    addMarker(new MarkerInfo(address, mapPoint.getMapPointGeoCoord( ).latitude, mapPoint.getMapPointGeoCoord( ).longitude));
                }

                @Override
                public void onNegativeClicked() {
                    Log.d(TAG, "onPositiveClicked: unselect item click");

                }
            });
            cp.dismiss( );
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setShowCurrentLocationMarker(false);
            mapView.setZoomLevel(4, true);
            alert.showDialog( );

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                if (place != null) {

                    //dialog event
                    CustomDialog alert = new CustomDialog(MainActivity.this);
                    String selectedPlace = place.getName( ).toString( );
                    alert.setDialogContent(selectedPlace);
                    alert.setDialogListener(new MyDialogListener( ) {

                        @Override
                        public void onPositiveClicked() {
                            //검색한 장소의 마커를 추가합니다.
                            addMarker(new MarkerInfo(selectedPlace, place.getLatLng( ).latitude, place.getLatLng( ).longitude));
                            Log.d(TAG, "onPositiveClicked: select item click");
//                            Toast.makeText(MainActivity.this, "YEs", Toast.LENGTH_SHORT).show( );
                        }

                        @Override
                        public void onNegativeClicked() {
                            Log.d(TAG, "onPositiveClicked: unselect item click");
//                            Toast.makeText(MainActivity.this, "NO", Toast.LENGTH_SHORT).show( );

                        }
                    });
                    alert.showDialog( );

                }
            }
        }
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        cp.show( );

        for (MarkerInfo marker : centermarkers) {
            if (marker.getPlaceName( ).equals(mapPOIItem.getItemName( ))) {
                showWebViewDialogAsUrl(marker.getPlaceURL( ));
            }
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater( ).inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((TextView) mCalloutBalloon.findViewById(R.id.text)).setText(poiItem.getItemName( ));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }


}
