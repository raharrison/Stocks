package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import uk.co.ryanharrison.stocks.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An activity to allow the user to search for businesses that can later be added to a Portfolio.
 * 
 * The activity allows the user to enter a search term and then downloads a list of possible company names and their corresponding
 * tickers. The user can then select the one they are looking for. The ticker for the company is then sent back to the caller
 * activity.
 * 
 * @author Ryan Harrison
 */
public class SearchActivity extends Activity {

  /**
   * Task to download a set of business names and tickers that are related to a search query string
   * 
   * @author Ryan Harrison (rh00148)
   */
  private class DownloadTickerJsonTask extends AsyncTask<String, Void, Map<String, String>> {

    /**
     * Main work to do in a separate thread
     * 
     * @param urls
     *          The URL to download a the businesses from
     * @return A Map of UI friendly business strings to their corresponding tickers
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Map<String, String> doInBackground(String... urls) {
      try {
        // Download the json object and parse it for the business data
        return this.loadJsonFromNetwork(urls[0]);
      }
      catch (IOException e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
      catch (JSONException e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
    }

    /**
     * Download a JSON object from the url and parse it for the business names and tickers
     * 
     * @param url
     *          The URL to download a JSON object from
     * @return A Map of UI friendly business strings to their corresponding tickers
     * @throws JSONException
     *           If there was an error when parsing the json object
     * @throws IOException
     *           If there was an error downloading the json object
     */
    private Map<String, String> loadJsonFromNetwork(String url) throws JSONException, IOException {
      InputStream stream = null;
      // Instantiate the parser
      TickerJsonParser tickerParser = new TickerJsonParser();
      Map<String, String> entries;
      try {
        Log.i(TAG, "Retrieiving url stream");
        // Get an input stream from the url
        stream = Utils.downloadUrl(url);

        Log.i(TAG, "Parsing json object for tickers");
        // Download and parse the json object and get the business names and tickers from it
        entries = tickerParser.parse(stream);
      }
      finally {
        // Makes sure that the InputStream is closed after the app is finished using it.
        if (stream != null) {
          stream.close();
        }
      }
      return entries;
    }

    /**
     * Called back on the UI thread after the main work has finished. Change the user prompt and populate the listview with the
     * downloaded company names
     * 
     * @param result
     *          A Map of UI friendly business strings to their corresponding tickers
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Map<String, String> result) {
      Log.i(TAG, "Background work finished");

      // Re-enable the search button
      SearchActivity.this.searchButton.setEnabled(true);

      // Display an error message if no results were found
      if (result == null || result.size() == 0) {
        SearchActivity.this.searchTextView.setText(SearchActivity.this.getResources().getString(R.string.unable_find_results)
            + " '" + SearchActivity.this.query + "'");
        return;
      }
      else {
        // Otherwise populate the listview adapter with the UI friendly strings from the result Map
        SearchActivity.this.namesToTickers = result;
        SearchActivity.this.searchTextView.setText(SearchActivity.this.getResources().getString(R.string.search_results) + " '"
            + SearchActivity.this.query + "'");
        SearchActivity.this.adapter.clear();
        for (String e : result.keySet()) {
          SearchActivity.this.adapter.add(e);
        }

        // Notify the adapter that the data set is changed so the view can be redrawn
        SearchActivity.this.adapter.notifyDataSetChanged();
      }
    }

    /**
     * Called before the main work is done in the separate thread. Set a prompt for the user telling them that tickers are being
     * downloaded. Also clear any existing tickers that may have been already downloaded
     * 
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
      // If there are existing tickers, then clear them
      if (SearchActivity.this.namesToTickers != null) {
        SearchActivity.this.namesToTickers.clear();
      }

      // Give a prompt to the user and disable the search button
      SearchActivity.this.searchTextView.setText(SearchActivity.this.getResources().getString(R.string.searching_for) + " '"
          + SearchActivity.this.query + "'");
      SearchActivity.this.searchButton.setEnabled(false);
    }
  }

  /** An adapter used to populate the listview of company names and tickers */
  private ArrayAdapter<String> adapter;

  /** A textview to display download progress and other information to the user */
  private TextView             searchTextView;

  /** The button used to start a search of possible company names */
  private Button               searchButton;

  /** Edit text box used by the user to enter a search term */
  private EditText             tickerEditText;

  /** The query string that the user has entered into the search field */
  private String               query;

  /** A Map of UI friendly business strings to their corresponding tickers */
  private Map<String, String>  namesToTickers;

  /**
   * The URL used to download a set of possible business names and ticker from a search term from the user. The ticker suggestions
   * are obtained from the Yahoo! Finance API which is free to use for personal use
   */
  private static final String  TICKERURL = "http://autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback";

  /** The tag for this activity */
  public static final String   TAG       = "SearchActivity";

  /**
   * Download and display a set of business names and tickers related to a search query
   * 
   * @param query
   *          The query to use when downloading related business names and tickers
   */
  private void fillTickers(String query) {
    Log.i(TAG, "Filling possible tickers with query of " + query);
    // If no network connection is available, display an error message to the user
    if (!Utils.isNetworkAvailable(this)) {
      this.searchTextView.setText(this.getResources().getString(R.string.no_internet_connection));
      return;
    }

    String url;
    try {
      // Construct the url from the user query string
      url = String.format(TICKERURL, URLEncoder.encode(query.toLowerCase(Locale.getDefault()), "utf-8"));

      // Start the download in another thread so the UI does not lock up
      new DownloadTickerJsonTask().execute(url);
    }
    catch (UnsupportedEncodingException e) {
      Log.e(TAG, e.getMessage());
    }
  }

  /**
   * Called when the activity is created. Initialise fields and set up the click listener for the listview
   * 
   * @param savedInstanceState
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the layout of this activity
    this.setContentView(R.layout.activity_addticker);

    // Get views from the layout
    ListView list = (ListView) this.findViewById(R.id.searchList);
    this.searchTextView = (TextView) this.findViewById(R.id.searchProgressTextView);
    this.searchButton = (Button) this.findViewById(R.id.searchButton);
    this.tickerEditText = (EditText) this.findViewById(R.id.tickerEditText);

    // Set up the adapter for the listview
    this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    list.setAdapter(this.adapter);

    // Add a click listener to the listview. When an item is clicked, the ticker of the item is found and returned as a result of
    // this activity. The click also finishes the activity.
    list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.i(TAG, "List item clicked at position " + position);

        // Get the ticker corresponding to the text of the listview item that was clicked on
        String ticker = SearchActivity.this.namesToTickers.get(((TextView) v).getText().toString());

        // Add the ticker to the result intent
        Intent result = new Intent();
        result.putExtra("ticker", ticker);

        // Set the result of this activity which can then be picked up by the caller activity
        SearchActivity.this.setResult(Activity.RESULT_OK, result);

        // Finish this activity
        SearchActivity.this.finish();
      }
    });
  }

  /**
   * Called when the search button is pressed. Get the query string and start the process of downloading and displaying a set of
   * possible businesses
   * 
   * @param v
   *          The view that raised the event
   */
  public void searchButtonOnClick(View v) {
    // Get the user query
    this.query = this.tickerEditText.getText().toString();

    // Fill the list of tickers using the query
    this.fillTickers(this.query);
  }
}
