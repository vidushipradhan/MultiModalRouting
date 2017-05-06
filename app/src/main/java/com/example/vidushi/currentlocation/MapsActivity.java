package com.example.vidushi.currentlocation;

        import android.Manifest;
        import android.content.pm.PackageManager;
        import android.graphics.Color;
        import android.location.Location;
        import android.os.Build;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentActivity;
        import android.os.Bundle;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.widget.Toast;
        import android.view.View.OnClickListener;
        import android.widget.Spinner;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemSelectedListener;
        import android.os.AsyncTask;
        import com.google.android.gms.maps.model.PolylineOptions;

        import java.io.BufferedReader;
        import java.io.StringReader;
        import java.util.*;
        import com.android.volley.Request;
        import com.android.volley.RequestQueue;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.StringRequest;
        import com.android.volley.toolbox.Volley;
        import android.widget.LinearLayout;
        import java.lang.String;
        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import android.widget.Button;
        import android.widget.TextView;
        import android.view.View;
        import android.widget.AutoCompleteTextView;
        import android.widget.ArrayAdapter;

        import org.json.JSONObject;

        import static com.example.vidushi.currentlocation.R.id.map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,OnClickListener,OnItemSelectedListener {

    LatLng latLng;
    private LinearLayout mLayout;
    String line;
    LatLng destpos;
    LatLng sourcepos;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private double latitude;
    private double longitude;
    Button button;
    AutoCompleteTextView destinationview;
    String destination;
    AutoCompleteTextView sourceview;
    String source;
    String latitudeString;
    String longitudeString;
    String dropdown_option;
    ArrayList<LatLng> points = new ArrayList<LatLng>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        TextView textView2 = (TextView)findViewById(R.id.textView2);
        textView2.setText("Result:");
        destinationview = (AutoCompleteTextView)findViewById(R.id.destination_text);
        sourceview = (AutoCompleteTextView)findViewById(R.id.source_text);
        String[] countries = getResources().getStringArray(R.array.bus_stop_array);
// Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        destinationview.setAdapter(adapter);

        destination = destinationview.getText().toString();

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        sourceview.setAdapter(adapter2);

        source = sourceview.getText().toString();

        mLayout = (LinearLayout) findViewById(R.id.linearLayout);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

            Spinner spinner = (Spinner) findViewById(R.id.spinner);

            // Spinner click listener
            spinner.setOnItemSelectedListener(this);

            // Spinner Drop down elements
            List<String> categories = new ArrayList<String>();
            categories.add("BusOnlyNetwork");
            categories.add("HybridNetwork");



            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);


    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        dropdown_option = parent.getItemAtPosition(position).toString();
        System.out.println(dropdown_option);

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + dropdown_option, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public void onClick(View v){
        try{
        // TODO Auto-generated method stub
            if(destinationview.getText().toString().length()<1){
                // out of range
                Toast.makeText(this, "please enter something", Toast.LENGTH_LONG).show();
            }else{
                routing();
            }
        } catch(Exception e) {System.out.println(e);}
    }


    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }



    @Override
    public void onLocationChanged(Location location) {


        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        latitudeString = Double.toString(latitude);
        longitudeString = Double.toString(longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        markerOptions.title(latitude + " , " + longitude);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));



        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    public void routing()throws Exception
    {
    String url= "";

    mMap.clear();
        System.out.println("func start");
        destination = destinationview.getText().toString();
        source = sourceview.getText().toString();
        if(sourceview.getText().toString().length()<1) {
            url = "http://139.59.33.166:8080/Servlet" + "?latitude=" + latitudeString + "&longitude=" + longitudeString + "&destination=" + destination + "&source=" + "000"+ "&dropdown_option=" + dropdown_option;
        }
        else{

            url = "http://139.59.33.166:8080/Servlet" + "?latitude=" + "000" + "&longitude=" + "000" + "&destination=" + destination + "&source=" + source + "&dropdown_option=" + dropdown_option;
        }

        url = url.replaceAll(" ", "%20");

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try{
                            System.out.println("response is : " +response);
                            System.out.println();

                            BufferedReader br = new BufferedReader(new StringReader(response));
                            line = "" ;
                            String result[] ;

                            int temp = 0;

                            while((line=br.readLine())!=null)
                            {

                                result = line.split(" ") ;
                                if(result.length==1)
                                    break;
                                temp ++;
                                Double lat = Double.parseDouble(result[0]);
                                Double lon = Double.parseDouble(result[1]);
                                points.add(new LatLng(lat, lon));

                            }
                            //line = br.readLine() ;
                            StringBuffer stringBuffer = new StringBuffer();
                                String ans = "";
                            while ((ans = br.readLine()) != null) {
                                stringBuffer.append(ans);
                                stringBuffer.append("\n");
                            }



                            TextView textView = (TextView)findViewById(R.id.textView);
                            textView.setText(stringBuffer.toString());
                            System.out.println("line is" + stringBuffer.toString());

                          /*  mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude,longitude)));*/
                            MarkerOptions markerOptions = new MarkerOptions();

                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                            for (int i = 0; i < points.size(); i++)
                            {
                                LatLng position = points.get(i);
                                double lat = position.latitude;
                                double longi = position.longitude;
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat,longi)));
                                markerOptions.title(lat + " , " + longi);

                            }

                            if(points.size()>8){

                                if(sourceview.getText().toString().length()<1)
                                {
                                            mMap.addPolyline((new PolylineOptions())
                                            .add(latLng, points.get(0)).width(5).color(Color.BLUE)
                                            .geodesic(true));

                                    for (int i = 0; i < points.size()-1; i++) {
                                       LatLng src = points.get(i);
                                        LatLng dest = points.get(i+1);
                                        mMap.addPolyline((new PolylineOptions())
                                                .add(src, dest).width(5).color(Color.BLUE)
                                                .geodesic(true));

                                     }
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                                }
                                else
                                {
                                    for (int i = 0; i < points.size()-1; i++) {
                                        LatLng src = points.get(i);
                                        LatLng dest = points.get(i+1);
                                        mMap.addPolyline((new PolylineOptions())
                                                .add(src, dest).width(5).color(Color.BLUE)
                                                .geodesic(true));

                                    }
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                                }

                            }
                            else{
                            destpos = points.get(temp-1);
                            sourcepos = points.get(0);
                            System.out.println("destpos is" + destpos);
                            String sensor = "sensor=false";
                            String waypoints = "";
                            for(int i=0;i<points.size();i++){
                                LatLng t  = (LatLng) points.get(i);
                                if(i==0)
                                    waypoints = "waypoints=";
                                waypoints += t.latitude + "," + t.longitude + "|";
                            }

                                String new_url = "";
                            Double destposlat = destpos.latitude;
                            Double destposlong = destpos.longitude;

                            Double sourceposlat = sourcepos.latitude;
                            Double sourceposlong = sourcepos.longitude;


                                if(sourceview.getText().toString().length()<1) {
                                    new_url = ("http://maps.googleapis.com/maps/api/directions/json?" + "origin=" + latitude + "," + longitude + "&destination=" + destposlat + "," + destposlong + "&" + sensor + "&" + waypoints);
                                }
                                else{
                                    new_url = ("http://maps.googleapis.com/maps/api/directions/json?" + "origin=" + sourceposlat + "," + sourceposlong + "&destination=" + destposlat + "," + destposlong + "&" + sensor + "&" + waypoints);
                                }

                                new_url = new_url.replaceAll(" ", "%20");



                            StringRequest stringRequest = new StringRequest(Request.Method.GET, new_url,
                                    new Response.Listener<String>() {

                                        @Override
                                        public void onResponse(String response) {
                                            System.out.println(response);
                                            new ParserTask().execute(response);
                                        }
                                    }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                            Volley.newRequestQueue(getApplicationContext()).add(stringRequest);}

                            points.removeAll(points) ;

                        }
                        catch(Exception error){
                            System.out.println("error       .........."+error);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
        queue.start();

    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


}
