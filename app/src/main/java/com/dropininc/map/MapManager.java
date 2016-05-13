package com.dropininc.map;

import android.content.Context;

import com.dropininc.R;
import com.dropininc.model.MapDirectionModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapManager {
    private Context mContext;
    private ArrayList<Polyline> mPolyLine;
    MapDirectionModel mapDirectionModel;
    private int distance;
    private String duration;

    public MapManager(Context context) {
        mContext = context;
    }

    public void removePolyLines() {
        if (mPolyLine == null) return;
        for (int i = 0; i < mPolyLine.size(); i++) {
            Polyline polyLine = mPolyLine.get(i);
            polyLine.remove();
        }
        mPolyLine = null;
    }

    public void drawDirections(GoogleMap googleMap, MapDirectionModel model) {
        if (googleMap == null || model == null) return;

        mapDirectionModel = model;

        PolylineOptions rectLine = new PolylineOptions().width(10).color(
                mContext.getResources().getColor(R.color.route_location));

        if (model.routeList.size() > 0) {
            MapDirectionModel.Route route = model.routeList.get(0);

            List<MapDirectionModel.Leg> legs = route.legs;

            if (legs.size() > 0) {
                distance = legs.get(0).distance.value;
                duration = legs.get(0).steps.get(0).duration.text;
                for (MapDirectionModel.Step step : legs.get(0).steps) {
                    rectLine.add(new LatLng(step.startLocation.lat, step.startLocation.lng));
                    rectLine.addAll(decodePoly(step.polyline.points));
                    rectLine.add(new LatLng(step.endLocation.lat, step.endLocation.lng));
                }
            }

            Polyline polyline = googleMap.addPolyline(rectLine);

            if (mPolyLine == null) {
                mPolyLine = new ArrayList<>();
            }

            mPolyLine.add(polyline);
        }
    }

    public int getDistance() {
        return distance;
    }

    public String getStringDistance() {
        return distance + "miles";
    }

    public String getStringDuration() {
        return duration;
    }


    public List<MapDirectionModel.Step> getListDirection() {
        if (mapDirectionModel == null || mapDirectionModel.routeList.size() == 0 ||
                mapDirectionModel.routeList.get(0).legs.size() == 0) return new ArrayList<>();

        return mapDirectionModel.routeList.get(0).legs.get(0).steps;
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}
