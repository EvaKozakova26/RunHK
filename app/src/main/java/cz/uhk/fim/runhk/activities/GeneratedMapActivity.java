package cz.uhk.fim.runhk.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cz.uhk.fim.runhk.R;
import cz.uhk.fim.runhk.fragments.ChallengeLocationFragment;
import cz.uhk.fim.runhk.model.PolyLineData;
import cz.uhk.fim.runhk.service.AsyncResponse;
import cz.uhk.fim.runhk.service.ElevationService;

public class GeneratedMapActivity extends FragmentActivity implements OnMapReadyCallback, ChallengeLocationFragment.onLocationUpdateInterface, AsyncResponse {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    double lat = 49.627378;
    double lon = 16.52206;
    double prevLat;
    double prevLng;
    private double distance;
    private double avgDistance;
    private long avgTime;
    private double avgElevation;
    private int avgCalories;
    private List<Double> elevations;
    private List<LatLng> distancePoints;
    private PolyLineData currentPolylineData;
    private List<Double> listLaTLon;


    private double elevationGain;
    private int polyLineIndex;


    private String address;
    private String address3;
    private String address4;
    private String address5;

    private LatLng myLocation;

    private ChallengeLocationFragment challengeLocationFragment;
    private ElevationService elevationService;

    private DirectionsResult directionsResult1;
    private DirectionsResult directionsResult2;
    private DirectionsResult directionsResult3;

    private PolyLineData currentPolyLineData1;
    private PolyLineData currentPolyLineData2;
    private PolyLineData currentPolyLineData3;

    private FusedLocationProviderClient fusedLocationProviderClient;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ChallengeLocationFragment challengeLocationFragment1 = (ChallengeLocationFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentQuest);
        challengeLocationFragment1.setOnLocationUpdateInterface(this);

        Intent intent = getIntent();
        avgDistance = intent.getDoubleExtra("distance", 0);
        avgTime = intent.getLongExtra("time", 0);
        avgElevation = intent.getDoubleExtra("elevation", 0);
        avgCalories = intent.getIntExtra("calories", 0);

        listLaTLon = new ArrayList<>();

        elevations = new ArrayList<>();
        elevationService = new ElevationService();
        distancePoints = new ArrayList<>();
        elevationService.delegate = this;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastKnownLocation();

        if (findViewById(R.id.fragmentQuest) != null) {
            challengeLocationFragment = new ChallengeLocationFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentQuest, challengeLocationFragment) // kam to chci a co
                    .commit();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * <p>
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //TODO - current location při spusteni aktivity
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker to your position and move the camera
        //     mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12.5f));
        //     mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Snackbar snackbar = Snackbar.make(mapFragment.getView(), "Run this route?", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("RUN", onRunClickListener);
                snackbar.show();
                currentPolylineData = (PolyLineData) polyline.getTag();
                polyLineIndex = currentPolylineData.getIndex();
                System.out.println("current index " + polyLineIndex);
                elevations.clear();

                if (currentPolyLineData1 == null && currentPolylineData.getIndex() == 1) {
                    currentPolyLineData1 = currentPolylineData;
                    getElevationFromRoute(currentPolylineData.getPolyLinePoints(), 1);
                } else if (currentPolyLineData2 == null && currentPolylineData.getIndex() == 2) {
                    currentPolyLineData2 = currentPolylineData;
                    getElevationFromRoute(currentPolyLineData2.getPolyLinePoints(), 2);
                } else if (currentPolyLineData3 == null && currentPolylineData.getIndex() == 3) {
                    currentPolyLineData3 = currentPolylineData;
                    getElevationFromRoute(currentPolyLineData3.getPolyLinePoints(), 3);
                } else {
                    switch (polyLineIndex) {
                        case 1:
                            onButtonShowPopupWindowClick(currentPolyLineData1);
                            break;
                        case 2:
                            onButtonShowPopupWindowClick(currentPolyLineData2);
                            break;
                        case 3:
                            onButtonShowPopupWindowClick(currentPolyLineData3);
                            break;
                    }
                }
            }
        });



    }

    View.OnClickListener onRunClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do something here
        }
    };

    private void onButtonShowPopupWindowClick(PolyLineData polyLineData) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);
        popupView.setAlpha(0.6f);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView popupText = popupView.findViewById(R.id.popupText);
        popupText.setText(polyLineData.getDistance() + " metres" + "\n"
                + polyLineData.getElevationGain() + "elevation gain" + "\n"
                + polyLineData.getTime() + " minutes" + "\n"
                + polyLineData.getCalories() + " calories");

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(mapFragment.getView(), Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }


    @Override
    public void onLocationUpdate(Location currentLocation) {
        if (currentLocation == null) {
            return;
        } else {
            prevLat = lat;
            prevLng = lon;

            lat = currentLocation.getLatitude();
            lon = currentLocation.getLongitude();
            myLocation = new LatLng(lat, lon);

            //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

            if (prevLat != 0) {
                mMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .jointType(JointType.ROUND)
                        .color(Color.BLUE)
                        .startCap(new RoundCap())
                        .endCap(new RoundCap())
                        .add(
                                new LatLng(prevLat, prevLng),
                                new LatLng(lat, lon)
                        ));


                float[] distance2 = new float[2];
                Location.distanceBetween(prevLat, prevLng, lat, lon, distance2);
                double currentDistance = distance2[0];

                distance = distance + currentDistance;
                challengeLocationFragment.updateDistance(distance);

            }
        }
    }

    private void getAddressFromLocation() throws IOException {
        // ziskani adresy
        Geocoder geocoder;
        List<Address> addresses;
        List<Address> addresses3;
        List<Address> addresses4;
        List<Address> addresses5;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(myLocation.latitude, myLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        Random r = new Random();
        int randomHeading = r.nextInt(360);
        int randomHeading2 = r.nextInt(360);
        int randomHeading3 = r.nextInt(360);
        LatLng waypoint = SphericalUtil.computeOffset(myLocation, avgDistance / 2, randomHeading);
        LatLng waypoint2 = SphericalUtil.computeOffset(myLocation, avgDistance / 2, randomHeading2);
        LatLng waypoint3 = SphericalUtil.computeOffset(myLocation, avgDistance / 2, randomHeading3);

        addresses3 = geocoder.getFromLocation(waypoint.latitude, waypoint.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        addresses4 = geocoder.getFromLocation(waypoint2.latitude, waypoint2.longitude, 1);
        addresses5 = geocoder.getFromLocation(waypoint3.latitude, waypoint3.longitude, 1);

        address3 = addresses3.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        address4 = addresses4.get(0).getAddressLine(0);
        address5 = addresses5.get(0).getAddressLine(0);
    }

    private void getRoute() {
        directionsResult1 = createDirectionResult(address, address3);
        directionsResult2 = createDirectionResult(address, address4);
        directionsResult3 = createDirectionResult(address, address5);

        createRoute(directionsResult1, Color.BLUE, 1);
        createRoute(directionsResult2, Color.GREEN, 2);
        createRoute(directionsResult3, Color.YELLOW, 3);

    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey("AIzaSyDS6vXNVTJFOUkJTcVhHfsEhuFOwmtkNxk")
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);

    }

    private void addMarkers(DirectionsResult directionsResult, GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(directionsResult.routes[0].legs[0].startLocation.lat, directionsResult.routes[0].legs[0].startLocation.lng))
                .title(directionsResult.routes[0].legs[0].startAddress));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap, int color, int index) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        PolyLineData polyLineData = new PolyLineData();
        polyLineData.setDistance(results.routes[0].legs[0].distance.inMeters);
        polyLineData.setTime(0);
        polyLineData.setCalories(0);
        polyLineData.setElevationGain(0);
        polyLineData.setPolyLinePoints(decodedPath);
        polyLineData.setIndex(index);
        //TODO projet decoded Path a zjistit body a z nich prevyseni :)
        mMap.addPolyline(new PolylineOptions().color(color).clickable(true).addAll(decodedPath)).setTag(polyLineData);

    }

    private DirectionsResult createDirectionResult(String startAddress, String waypoint) {
        DirectionsResult directionsResult = new DirectionsResult();
        try {
            directionsResult = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.WALKING)
                    .origin(startAddress)
                    .destination(startAddress)
                    .alternatives(true)
                    .waypoints(waypoint)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return directionsResult;
    }

    private void createRoute(DirectionsResult directionsResult, int color, int index) {
        if (directionsResult != null) {
            addMarkers(directionsResult, mMap);
            addPolyline(directionsResult, mMap, color, index);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(directionsResult.routes[0].legs[0].startLocation.lat,
                    directionsResult.routes[0].legs[0].startLocation.lng
            ), 12.5f));
        }
    }

    private void updateLocation() {
        myLocation = new LatLng(listLaTLon.get(0), listLaTLon.get(1));

    }

    private void getElevationFromRoute(List<LatLng> distancePoints, int index) {
        distancePoints = getSortedDistancePoints(distancePoints);
        this.distancePoints = distancePoints;
        for (LatLng point : distancePoints) {
            //spusti async task
            elevationService.getElevation(point.latitude, point.longitude);
        }
    }

    private List<LatLng> getSortedDistancePoints(List<LatLng> distancePoints) {
        List<LatLng> sortedDistancePoints = new ArrayList<>();
        for (int i = 0; i < distancePoints.size() - 1; i += 2) {
            sortedDistancePoints.add(distancePoints.get(i));
        }
        return sortedDistancePoints;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit? You will have to start your challenge again")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        GeneratedMapActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    public void processFinish(Double output) {
        elevations.add(output);
        // count elevationGain
        if (elevations.size() == distancePoints.size()) {
            System.out.println("pocitam elevation Gain");
            for (int i = 0; i < elevations.size() - 1; i++) {
                if (elevations.get(i + 1) > elevations.get(i)) {
                    elevationGain = elevationGain + (elevations.get(i + 1) - elevations.get(i));
                }
            }
            switch (polyLineIndex) {
                case 1:
                    currentPolyLineData1.setElevationGain((int) elevationGain);
                    onButtonShowPopupWindowClick(currentPolyLineData1);
                    break;
                case 2:
                    currentPolyLineData2.setElevationGain((int) elevationGain);
                    onButtonShowPopupWindowClick(currentPolyLineData2);
                    break;
                case 3:
                    currentPolyLineData3.setElevationGain((int) elevationGain);
                    onButtonShowPopupWindowClick(currentPolyLineData3);
                    break;
                default:
                    System.out.println("nic");
            }

        }
    }

    @SuppressLint({"StaticFieldLeak", "MissingPermission"})
    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(GeneratedMapActivity.this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            myLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                            try {
                                getAddressFromLocation();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            getRoute();
                        } else {
                            System.out.println("ouh");
                        }
                    }

                });
    }
}