package com.yanawha.osejin.yanawha;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.JsonObject;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapView.MapViewEventListener, MapView.CurrentLocationEventListener {

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

    private CustomProgress cp;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private MapPOIItem marker;
    private MapPoint mapPoint;
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private Button btnSearchCenter;

    private ArrayList<MarkerInfo> markers = new ArrayList<>( );
    final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retro = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(RetrofitService.base)
                .build();
        RetrofitService api = retro.create(RetrofitService.class);

        Call<JsonObject> call = api.getAddress("맛집",
                                        "FD6","127.066737","37.609453",
                                                3);
        call.enqueue(new Callback<JsonObject>( ) {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Toast.makeText(MainActivity.this, "good", Toast.LENGTH_SHORT).show( );
                Log.d(TAG, "onResponse: "+response.body());

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


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

        //init buttons
        btnSearchCenter = findViewById(R.id.btn_search_center);
//        btnSearchCenter.setVisibility(View.GONE);
//        btnSearchCenter.setOnClickListener(view -> );


        findViewById(R.id.map_scale_up).setOnClickListener(v -> mapView.zoomIn(true));
        findViewById(R.id.map_scale_down).setOnClickListener(v -> mapView.zoomOut(true));
        findViewById(R.id.map_cur_loc).setOnClickListener(v -> {
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                    cp.show();
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

    void addMarker(MarkerInfo markerinfo) {
        marker = new MapPOIItem( );
        marker.setItemName(markerinfo.getPlace());
        marker.setTag(0);
        mapPoint = MapPoint.mapPointWithGeoCoord(markerinfo.getLat(), markerinfo.getLng());
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


    }

    private MarkerInfo computeCentroid(List<MarkerInfo> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (MarkerInfo point : points) {
            latitude += point.getLat();
            longitude += point.getLng();
        }

        return new MarkerInfo(latitude/n, longitude/n);
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
                    alert.setTvSelectedPlace(selectedPlace);
                    alert.setDialogListener(new MyDialogListener( ) {

                        @Override
                        public void onPositiveClicked() {
                            //검색한 장소의 마커를 추가합니다.
                            addMarker(new MarkerInfo(selectedPlace, place.getLatLng( ).latitude, place.getLatLng( ).longitude));
                            Log.d(TAG, "onPositiveClicked: select item click");
//                            Toast.makeText(MainActivity.this, "확인", Toast.LENGTH_SHORT).show( );
                        }

                        @Override
                        public void onNegativeClicked() {
                            Log.d(TAG, "onPositiveClicked: unselect item click");
//                            Toast.makeText(MainActivity.this, "놉", Toast.LENGTH_SHORT).show( );

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
            alert.setTvSelectedPlace(address);

            alert.setDialogListener(new MyDialogListener( ) {

                @Override
                public void onPositiveClicked() {
                    //검색한 장소의 마커를 추가합니다.
                    Log.d(TAG, "onPositiveClicked: select item click");

                    addMarker(new MarkerInfo(address, mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude));
                }

                @Override
                public void onNegativeClicked() {
                    Log.d(TAG, "onPositiveClicked: unselect item click");

                }
            });
            cp.dismiss( );
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setShowCurrentLocationMarker(false);
            alert.showDialog( );

        }

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
}





