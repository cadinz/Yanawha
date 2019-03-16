package com.yanawha.osejin.yanawha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private MapPOIItem marker;
    private MapPoint mapPoint;
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private String selectedPlace;
    private ArrayList<MapPOIItem> markers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Map view init
        mapView = new MapView(this);
        mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);






        //Search fab init
        findViewById(R.id.fab_search).setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(Place.TYPE_COUNTRY)
                            .setCountry("KOR")
                            .build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(autocompleteFilter)
                            .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(MainActivity.this, "검색을 사용할 수 없습니다", Toast.LENGTH_SHORT).show( );
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                if (place != null) {

                    final LatLng latLng = place.getLatLng();
                    String SN = "Lat=" + String.valueOf(latLng.latitude) +
                            "Lng=" + String.valueOf(latLng.longitude);
                    Log.d("SN.toString()", SN);

                    //dialog event
                    CustomDialog alert = new CustomDialog(MainActivity.this);
                    selectedPlace = place.getName( ).toString( );
                    alert.setTvSelectedPlace(selectedPlace);
                    alert.setDialogListener(new MyDialogListener( ) {
                        @Override
                        public void onPositiveClicked() {
                            marker = new MapPOIItem();
                            marker.setItemName(selectedPlace);
                            marker.setTag(0);
                            mapPoint = MapPoint.mapPointWithGeoCoord(latLng.latitude, latLng.longitude);
                            marker.setMapPoint(mapPoint);
                            // 기본으로 제공하는 BluePin 마커 모양.
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                            // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mapView.addPOIItem(marker);
                            //ArrayList marker 추가
                            markers.add(marker);


                            Toast.makeText(MainActivity.this, "확인", Toast.LENGTH_SHORT).show( );
                        }

                        @Override
                        public void onNegativeClicked() {
                            Toast.makeText(MainActivity.this, "놉", Toast.LENGTH_SHORT).show( );

                        }
                    });
                    alert.showDialog();


                }
            }
        }
    }

}





