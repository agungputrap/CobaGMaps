package com.example.cobagmaps;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.LangUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements LocationListener, android.location.LocationListener {
	  private LocationManager locationManager;
	  private String provider;
	  EditText s ;
	  EditText d;
	  ListView listRute;
	  LatLng src;
	  LatLng dest;
	  Location loc;
	  GoogleMap map;
	 
	  boolean udahdiklik;
	  int jumlahrute=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

  	   
  	    MapFragment fm = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
  	 
      // Getting Map for the SupportMapFragment
  	    this.map = fm.getMap();
  	    this.map.setMyLocationEnabled(true);
  	    loc = getLocation();
        // Initialize the location fields
        if (loc != null) {
        	MainActivity.this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),loc.getLongitude()), 7.0f));
          onLocationChanged(loc);
       
        } else {
            Toast.makeText(this, "belum terpilih" + provider,
                    Toast.LENGTH_LONG).show();
          }
        listRute= (ListView) findViewById(R.id.Listrute);
        s = (EditText) findViewById(R.id.src);
        d = (EditText) findViewById(R.id.dest);
        Button search = (Button) findViewById(R.id.Search);
        
        
        search.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View view) {
            	MainActivity.this.src = getLatLongFromAddress (s.getText().toString());
            	MainActivity.this.dest = getLatLongFromAddress (d.getText().toString());
            	//buat dapet rute
            	String url = MainActivity.this.getDirectionsUrl(src, dest);
            	 
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
               
                
            }
       });
            
    
   }

    private String getDirectionsUrl(LatLng origin, LatLng dest)
    {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
 
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
 
        // Sensor enabled
        String sensor = "sensor=false&units=metric&alternatives=true";
 
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
 
        // Output format
        String output = "json";
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
 
        return url;
    }
 
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);
 
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb = new StringBuffer();
 
            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
 
            data = sb.toString();
 
            br.close();
 
        } catch (Exception e)
        {
            Log.d("Exception while downloading url", e.toString());
        } finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
 
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>
    {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url)
        {
 
            // For storing data from web service
            String data = "";
 
            try
            {
                // Fetching the data from web service
                data = MainActivity.this.downloadUrl(url[0]);
            } catch (Exception e)
            {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
 
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
 
            ParserTask parserTask = new ParserTask();
 
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
 
        }
    }
 
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
    {

        PolylineOptions [] line ;
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
 
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";
            String [] ru = new String [result.size()];
            PolylineOptions lineOptions=null;
            line = new PolylineOptions [result.size()];
            if (result.size() < 1)
            {
                Toast.makeText(MainActivity.this.getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
 
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++)
            {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
 
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
 
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++)
                {
                    HashMap<String, String> point = path.get(j);
 
                    if (j == 0)
                    { // Get distance from the list
                        distance= point.get("distance");
                        continue;
                    } else if (j == 1)
                    { // Get duration from the list
                        duration =point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                    
                }
 
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
                
                line[i]=lineOptions;
               // MainActivity.this.map.addPolyline(lineOptions);
                lineOptions = null;
                ru[i]="rute "+i+", duration "+ duration +", distance "+ distance;
               
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                  android.R.layout.simple_list_item_1, ru);
            MainActivity.this.listRute.setAdapter(adapter);
            MainActivity.this.listRute.setOnItemClickListener(new OnItemClickListener() {
   			 
   			 @Override
   				public void onItemClick(AdapterView<?> arg0, View arg1,
   						int arg2, long arg3) {
   				 MainActivity.this.map.clear();
   				 MainActivity.this.map.addPolyline(line[arg2]);
   				 MainActivity.this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.this.src, 9.0f));
   				}	
   	        });
        }
    }
 
    public Location getLocation() {
    	Location location = null;
    	LocationManager locationManager1 = null;
    	try {
            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
           boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                	
                	 locationManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                   
                    if (locationManager1 != null) {
                    	
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                       
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                	
                    if (location == null) {
                        locationManager1.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                100,
                               0, (android.location.LocationListener) this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                        	
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public LatLng getLatLongFromAddress(String strAddress)
    {
    Geocoder coder = new Geocoder(this);
    List<Address> address;
    Address location;
        try {
			address = coder.getFromLocationName(strAddress,1);
			if (address == null) {
	            return null;
	        }
	        location = address.get(0);
	        return new LatLng(location.getLatitude(), location.getLongitude());
	        
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 

    }
    

    
	@Override
	public void onLocationChanged(Location arg0) {
		Toast.makeText(this, "provider" + "\n"+arg0.getTime()+"\n"+arg0.getLatitude()+"\n"+arg0.getLongitude()+"\n"+arg0.getSpeed(),
		          Toast.LENGTH_SHORT).show();
		
        try {
            DefaultHttpClient client = new DefaultHttpClient();  
            String postURL ="http://10.0.2.2/simakui/tambahkomentar.php";
            HttpPost post = new HttpPost(postURL);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("komen", "sss"));
                params.add(new BasicNameValuePair("idwarung", "7"));
               // params.add(new BasicNameValuePair("id", id));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);  
                HttpEntity resEntity = responsePOST.getEntity();  
                if (resEntity != null) {    
                    Log.i("RESPONSE",EntityUtils.toString(resEntity));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        }

//      /* Request updates at startup */
      @Override
      protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 0, this);
      }
//
//      /* Remove the locationlistener updates when Activity is paused */
      @Override
      protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
      }
//
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
     

    }
