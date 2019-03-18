package com.yanawha.osejin.yanawha;

import java.util.ArrayList;
import java.util.List;

public class CenterInfoManager {

    MarkerInfo centerLatLng;

    private CenterInfoManager(MarkerInfo centerLatLng) {
        this.centerLatLng = centerLatLng;
    }

    private List<MarkerInfo> toMarkerInfos(){

        return new ArrayList<MarkerInfo>();
    }

    private List<MarkerInfo> toMarkerInfos(int Quantity){

        return new ArrayList<MarkerInfo>();
    }

}
