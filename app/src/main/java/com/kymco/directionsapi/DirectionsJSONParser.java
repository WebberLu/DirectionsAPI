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

    public List<List<HashMap<String, String>>> parserPolylinePoints() {

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

    /**
     * 回傳每個 legs 內的屬性
     * @return List<HashMap<String, String>>
     */
    public List<HashMap<String, String>> getLegs(){

        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        try {
            jRoutes = mJSONData.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    String distance_text =
                            ((JSONObject) jLegs.get(j)).getJSONObject("distance").getString("text");
                    String distance_value =
                            ((JSONObject) jLegs.get(j)).getJSONObject("distance").getString("value");
                    String duration_text =
                            ((JSONObject) jLegs.get(j)).getJSONObject("duration").getString("text");
                    String duration_value =
                            ((JSONObject) jLegs.get(j)).getJSONObject("duration").getString("value");
                    String start_address =
                            ((JSONObject) jLegs.get(j)).getString("start_address");
                    String end_address =
                            ((JSONObject) jLegs.get(j)).getString("end_address");
                    String end_location_lat =
                            ((JSONObject) jLegs.get(j)).getJSONObject("end_location").getString("lat");
                    String end_location_lng =
                            ((JSONObject) jLegs.get(j)).getJSONObject("end_location").getString("lng");
                    String start_location_lat =
                            ((JSONObject) jLegs.get(j)).getJSONObject("start_location").getString("lat");
                    String start_location_lng =
                            ((JSONObject) jLegs.get(j)).getJSONObject("start_location").getString("lng");
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("distance_text",distance_text);
                    hm.put("duration_text",duration_text);
                    hm.put("start_address",start_address);
                    hm.put("end_address",end_address);
                    hm.put("end_location_lat",end_location_lat);
                    hm.put("end_location_lng",end_location_lng);
                    hm.put("start_location_lat",start_location_lat);
                    hm.put("start_location_lng",start_location_lng);
                    hm.put("start_location_lng", start_location_lng);
                    result.add(hm);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 回傳每個 legs 底下 steps 內的屬性
     * @return List<List<HashMap<String, String>>>
     */
    public List<List<HashMap<String, String>>> getSteps(){
        List<List<HashMap<String, String>>> result = new ArrayList<List<HashMap<String, String>>>();
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
                        String html_instructions = ((JSONObject) jSteps.get(k)).getString("html_instructions");
                        String travel_mode = ((JSONObject) jSteps.get(k)).getString("travel_mode");
                        String maneuver = ((JSONObject) jSteps.get(k)).getString("maneuver");

                        String distance_text = ((JSONObject) jSteps.get(k)).getJSONObject("distance").getString("text");
                        String distance_value = ((JSONObject) jSteps.get(k)).getJSONObject("distance").getString("value");

                        String duration_text = ((JSONObject) jSteps.get(k)).getJSONObject("duration").getString("text");
                        String duration_value = ((JSONObject) jSteps.get(k)).getJSONObject("duration").getString("value");

                        String start_lat = ((JSONObject) jSteps.get(k)).getJSONObject("start_location").getString("lat");
                        String start_lon = ((JSONObject) jSteps.get(k)).getJSONObject("start_location").getString("lng");

                        String end_lat = ((JSONObject) jSteps.get(k)).getJSONObject("end_location").getString("lat");
                        String end_lon = ((JSONObject) jSteps.get(k)).getJSONObject("end_location").getString("lng");
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("distance_text",distance_text);
                        hm.put("duration_text",duration_text);
                        hm.put("html_instructions",html_instructions);
                        hm.put("maneuver",maneuver);
                        hm.put("travel_mode",travel_mode);
                        hm.put("start_lat",start_lat);
                        hm.put("start_lon",start_lon);
                        hm.put("end_lat",end_lat);
                        hm.put("end_lon",end_lon);
                        path.add(hm);
                    }
                    result.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
