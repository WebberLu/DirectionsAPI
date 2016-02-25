package com.kymco.directionsapi;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
    /**
     * Receives a JSONObject and returns a list of lists containing latitude and
     * longitude
     */
    JSONObject mJSONData = null;

    public void setmJSONData(JSONObject mJSONData) {
        this.mJSONData = mJSONData;
    }

    public List<List<HashMap<String, String>>> parseLatLng() {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = mJSONData.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                .get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat",
                                    Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng",
                                    Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }

    public String getMainDistance(){
        String maindistance = "";
        try {
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONObject jDistance = null;
            jRoutes = mJSONData.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            jDistance = ((JSONObject) jLegs.get(0)).getJSONObject("distance");
            maindistance = jDistance.getString("text");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return maindistance;
    }

    public String getMainDuration(){
        String mainduration = "";
        try {
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONObject jDuration = null;
            jRoutes = mJSONData.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            jDuration= ((JSONObject) jLegs.get(0)).getJSONObject("duration");
            mainduration = jDuration.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mainduration;
    }

    /**
     *
     * return routes 底下 legs(0) 的 end_address & end_location
     * @return HashMap<String,String>
     */
    public HashMap<String,String> getMainEndAddressAndLatLng(){
        HashMap<String,String> map = new HashMap<String,String>();
        try {
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONObject jend_location = null;
            String end_address = null;
            jRoutes = mJSONData.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            jend_location= ((JSONObject) jLegs.get(0)).getJSONObject("end_location");
            end_address= ((JSONObject) jLegs.get(0)).getString("end_address");
            map.put("address",end_address);
            map.put("lat",jend_location.getString("lat"));
            map.put("lng",jend_location.getString("lng"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     *
     * return routes 底下 legs(0) 的 start_address & start_location
     * @return HashMap<String,String>
     */
    public HashMap<String,String> getMainStartAddressAndLatLng(){
        HashMap<String,String> map = new HashMap<String,String>();
        try {
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONObject jstart_location = null;
            String start_address = null;
            jRoutes = mJSONData.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            jstart_location= ((JSONObject) jLegs.get(0)).getJSONObject("start_location");
            start_address= ((JSONObject) jLegs.get(0)).getString("start_address");
            map.put("address",start_address);
            map.put("lat",jstart_location.getString("lat"));
            map.put("lng",jstart_location.getString("lng"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    //

    /**
     * Method to decode polyline points Courtesy :
     * jeffreysambells.com/2010/05/27
     * /decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
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

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
