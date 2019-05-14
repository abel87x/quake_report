/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Quake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final int EARTHQUAKE_LOADER_ID = 0;
    /**
     * URL for earthquake data from the USGS dataset
     */
    private static final String USGS_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    ArrayList<Quake> earthquakes = new ArrayList<>();
    TextView emptyView;
    ProgressBar loadingBar;
    private QuakeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        Log.v(LOG_TAG, "We are in onCreate method");

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Find a reference to the ProgressBar
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create a new {@link custom ArrayAdapter} of earthquakes
        adapter = new QuakeAdapter(getBaseContext(), earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);

        // Have the ListView show the EmptyView, in case of no data in the CustomAdapter
        // Text is set in onLoadingFinished method
        emptyView = (TextView) findViewById(R.id.emty_view);
        earthquakeListView.setEmptyView(emptyView);

        if (isNetworkAvailable()) {
            // Having the LoaderManager to initialize the loader
            getLoaderManager().initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            loadingBar.setVisibility(View.GONE);
            emptyView.setText(R.string.no_network_text);
        }

        // OnItemClickListener is an interface, here we declare an anonymous class to implement it
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Quake currentQuakeItem = adapter.getItem(position);
                String currentQuakeUrl = currentQuakeItem.getQuakeWebSite();
                Uri webPage = Uri.parse(currentQuakeUrl);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, webPage);
                if (websiteIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(websiteIntent);
                }
            }
        });

    }

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<Quake>> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "We are in onCreateLoader method");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String minMagnitude = sharedPrefs.getString(getString(R.string.settings_min_magnitude_key), getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(USGS_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=geojson`
        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        // Return the completed uri `http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=10&minmag=minMagnitude&orderby=time
        return new EarthquakeLoader(this, uriBuilder.toString());

    }

    /**
     * This method runs on the main UI thread after the background work has been
     * completed. This method receives as input, the return value from the doInBackground()
     * method. First we clear out the adapter, to get rid of earthquake data from a previous
     * query to USGS. Then we update the adapter with the new list of earthquakes,
     * which will trigger the ListView to re-populate its list items.
     */
    @Override
    public void onLoadFinished(Loader<List<Quake>> loader, List<Quake> o) {

        Log.v(LOG_TAG, "We are in onLoadFinished method");

        loadingBar.setVisibility(View.GONE);

        if (o == null) {
            return;
        }

        earthquakes = (ArrayList<Quake>) o;

        // Clear the adapter of previous earthquake data
        adapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            adapter.addAll(earthquakes);
        }

        if (isNetworkAvailable()) {
            emptyView.setText(R.string.empty_view_text);
        } else {
            emptyView.setText(R.string.no_network_text);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

        Log.v(LOG_TAG, "We are in onLoaderReset method");

        adapter.clear();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@link AsyncTaskLoader} to perform the network request on a background thread, and then
     * update the UI with the list of earthquakes in the response.
     * <p>
     * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
     * an output type. Our task will take a String URL, and return an Earthquake. We won't do
     * progress updates, so the second generic is just Void.
     * <p>
     * We'll only override one of the methods of AsyncTaskLoader: loadInBackground().
     * The doInBackground() method runs on a background thread, so it can run long-running code
     * (like network activity), without interfering with the responsiveness of the app.
     * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
     * UI thread, so it can use the produced data to update the UI.
     * <p>
     * Note that it is better to declare this class in a separate file
     */
    private static class EarthquakeLoader extends AsyncTaskLoader<List<Quake>> {

        private String mData;

        public EarthquakeLoader(Context context, String string) {
            super(context);
            mData = string;
        }

        /**
         * This method runs on a background thread and performs the network request.
         * We should not update the UI from a background thread, so we return a list of
         * {@link Quake}s as the result.
         */
        @Override
        public ArrayList<Quake> loadInBackground() {

            Log.v(LOG_TAG, "We are in loadInBackground method");

            if (mData == null) {
                return null;
            }

            // Return the {@link Event}
            return QueryUtils.fetchQuakeData(mData);
        }

        @Override
        protected void onStartLoading() {
            Log.v(LOG_TAG, "We are in onStartLoading method");

            forceLoad();
        }
    }
}
