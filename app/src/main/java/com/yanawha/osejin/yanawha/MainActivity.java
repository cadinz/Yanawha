package com.yanawha.osejin.yanawha;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
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




        //Map view init
        mapView = new MapView(this);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter( ));
        mapViewContainer = findViewById(R.id.map_view);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        mapViewContainer.addView(mapView);

        //custom Progress
        cp = new CustomProgress(MainActivity.this);

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
                        alert2.showDialog();
                        break;

                    }
                    case "다시하기": {
                        markers.clear( );
                        mapView.removeAllPOIItems( );
                        mapView.refreshMapTiles( );
                        btnSearchCenter.setVisibility(View.GONE);
                    }

                }

            }
        });


        findViewById(R.id.map_scale_up).setOnClickListener(v -> mapView.zoomIn(true));
        findViewById(R.id.map_scale_down).setOnClickListener(v -> mapView.zoomOut(true));
        findViewById(R.id.map_cur_loc).setOnClickListener(v -> {
            //찾은뒤 onCurrentLocationUpdate 호출됨
            cp.show( );
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        });


        Log.d("tag", "onCreate: start go");


        //Search fab init
        findViewById(R.id.fab_search).setOnClickListener(v -> {

            try {
                AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder( )
                        .setTypeFilter(Place.TYPE_COUNTRY)
                        .setCountry("KOR")
                        .build( );
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .setFilter(autocompleteFilter)
                        .build(MainActivity.this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Toast.makeText(MainActivity.this, "검색을 사용할 수 없습니다", Toast.LENGTH_SHORT).show( );
            }
        });
    }

    void showWebViewDialogAsUrl(String url) {
        cp.show( );
        AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create( );


        WebView wv = new WebView(this);
        wv.getSettings( ).setJavaScriptEnabled(true);
        wv.loadUrl(url);
        alert.setView(wv);
        wv.setWebViewClient(new WebViewClient( ) {
            Boolean flag = false;
            Boolean showflag = true;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (showflag) {
                    alert.show( );
                    showflag = false;
                }
                cp.dismiss( );
                super.onPageFinished(view, url);
                flag = false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                flag = true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                int type = view.getHitTestResult( ).getType( );
                Log.d(TAG, "(WEBVIEW)shouldOverrideUrlLoading : " + url);
                Log.d(TAG, "     - getURLLoding Type:" + type);

                if (!flag) {
                    if (type > 0) {
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
                    } else {
                        Log.d(TAG, "     - startLoading...:loadUrl pass!!");
                        return false;
                    }
                } else {
                    Log.d(TAG, "     - under Loading... (SKIP...) ");
                    return false;
                }
                return false;
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "close", new DialogInterface.OnClickListener( ) {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss( );
            }
        });
        alert.hide( );


    }

    //마커를 추가한다 (장소이름, 위경도)
    void addMarker(MarkerInfo markerinfo) {
        marker = new MapPOIItem( );
        marker.setItemName(markerinfo.getPlace( ));
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
    }

    //중간위치의 정보들을 받아 마커를 추가한다
    void addCenterMarker(List<MarkerInfo> markerinfo) {


        for (MarkerInfo mk : markerinfo) {

            marker = new MapPOIItem( );
            marker.setItemName(mk.getPlace( ));
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
                final Place place = PlaceAutocomplete.getPlace(this, data);

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




class Holder<T> {
    private T value;

    Holder(T value) {
        setValue(value);
    }

    T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = value;
    }
}
