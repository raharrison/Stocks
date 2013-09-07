/**
 * QuoteViewActivity.java
 */

package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;

import uk.co.ryanharrison.stocks.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An activity that displays a Quote object in a graphical manner. This activity also downloads an RSS feed for the company that the
 * quote represents along with a button to view a price chart
 * 
 * @author Ryan Harrison
 */
public class QuoteActivity extends Activity {

  /**
   * Task to download an rss feed from the internet and display it in the listview
   * 
   * @author Ryan Harrison (rh00148)
   */
  private class DownloadRSSXmlTask extends AsyncTask<String, Void, Map<String, String>> {

    /**
     * Main work to do in the separate thread. Download and return the xml from the url passed in
     * 
     * @param urls
     *          The url of the xml file to download
     * @return The xml file at the specified url
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Map<String, String> doInBackground(String... urls) {
      try {
        return this.loadXmlFromNetwork(urls[0]);
      }
      catch (IOException e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
      catch (XmlPullParserException e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
    }

    /**
     * Download and parse the xml file at the given url
     * 
     * @param url
     *          The url of the xml file to download and parse
     * @return A Map of RSS item titles to their corresponding link urls
     * @throws XmlPullParserException
     *           If there was an error parsing the xml file
     * @throws IOException
     *           If there was an error downloading the xml file
     */
    private Map<String, String> loadXmlFromNetwork(String url) throws XmlPullParserException, IOException {
      InputStream stream = null;
      // Instantiate the parser
      RSSFeedXmlParser rssParser = new RSSFeedXmlParser();
      Map<String, String> entries;
      try {
        Log.i(TAG, "Retrieiving url stream");
        // Download the xml file
        stream = Utils.downloadUrl(url);

        Log.i(TAG, "Parsing stream xml file for rss feed items");
        // Parse the xml file
        entries = rssParser.parse(stream);
      }
      finally {
        // Close the stream in all cases
        if (stream != null) {
          stream.close();
        }
      }
      return entries;
    }

    /**
     * Called back on the UI thread after the main work has finished. Display the RSS feed items in the UI views
     * 
     * @param result
     *          A Map of RSS item titles to their corresponding link urls
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Map<String, String> result) {
      Log.i(TAG, "Background work completed");

      // If the result is null there was an error so tell the user about it
      if (result == null) {
        QuoteActivity.this.feedTextField.setText(QuoteActivity.this.getResources().getString(R.string.unable_download_feed));
        return;
      }

      QuoteActivity.this.links = result;
      QuoteActivity.this.feedTextField.setText(QuoteActivity.this.getResources().getString(R.string.news_feed));

      // Populate the listview with the RSS feed items
      for (Entry<String, String> entry : QuoteActivity.this.links.entrySet()) {
        QuoteActivity.this.adapter.add(entry.getKey());
      }

      // Notify the adapter that the data set has changed so the listview should be redrawn
      QuoteActivity.this.adapter.notifyDataSetChanged();
    }

    /**
     * Called before the main work is done in the separate thread. Set a prompt for the user telling them that the RSS feed is being
     * downloaded
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
      QuoteActivity.this.feedTextField.setText(QuoteActivity.this.getResources().getString(R.string.searching_news_feed));
    }
  }

  /** The quote object that is being displayed in this activity */
  private Quote                quote;

  /** The adapter used to display RSS items in the listview */
  private ArrayAdapter<String> adapter;

  /** A Map of RSS item titles to their corresponding link urls */
  private Map<String, String>  links;

  /** The listview that will be populated by RSS items for the company */
  private ListView             rssFeed;

  /** A textview to provide information about the rss feed */
  private TextView             feedTextField;

  /**
   * The URL to use when downloading the RSS feed for a company. The RSS feeds are obtained from the Yahoo! Finance API which is
   * free to use for personal use
   */
  private static final String  FEEDURL = "http://feeds.finance.yahoo.com/rss/2.0/headline?s=%s&region=US&lang=en-US";

  /** The tag for this activity */
  public static final String   TAG     = "Quote";

  /**
   * Display the current Quote object in the views of the layout
   */
  private void displayQuote() {
    TextView changeView = (TextView) this.findViewById(R.id.changeTextView);
    TextView percentChangeView = (TextView) this.findViewById(R.id.percentchangeTextView);
    double change = this.quote.change;
    String percentChange = Utils.roundTwoPlaces(this.quote.percentChange);

    // Set the price change value, adding a '+' if necessary
    String changeText = "  " + (change < 0 ? Double.toString(change) : "+" + change);

    String percentChangeText = "  "
        + (this.quote.percentChange < 0 ? this.getResources().getString(R.string.down_arrow) + percentChange : this.getResources()
            .getString(R.string.up_arrow) + percentChange);

    changeView.setText(changeText);
    percentChangeView.setText(percentChangeText);

    // Change the colour of the textview depending on the value of the change
    if (change < 0) {

      changeView.setTextColor(this.getResources().getColor(R.color.red));
      percentChangeView.setTextColor(this.getResources().getColor(R.color.red));
    }
    else {
      changeView.setTextColor(this.getResources().getColor(R.color.green));
      percentChangeView.setTextColor(this.getResources().getColor(R.color.green));
    }

    // Set the text for the textviews in the layout from the Quote object
    ((TextView) this.findViewById(R.id.companyNameTextView)).setText(this.quote.name != null ? this.quote.name + " ("
        + this.quote.ticker + ")" : this.getResources().getString(R.string.not_available));
    ((TextView) this.findViewById(R.id.priceTextView)).setText("" + this.quote.lastTradePrice);
    ((TextView) this.findViewById(R.id.dayLowTextView)).setText("" + this.quote.daysLow);
    ((TextView) this.findViewById(R.id.dayHighTextView)).setText("" + this.quote.daysHigh);
    ((TextView) this.findViewById(R.id.yearLowTextView)).setText("" + this.quote.yearLow);
    ((TextView) this.findViewById(R.id.yearHighTextView)).setText("" + this.quote.yearHigh);
    ((TextView) this.findViewById(R.id.volumeTextView)).setText("" + this.quote.volume);
    ((TextView) this.findViewById(R.id.averageVolumeTextView)).setText("" + this.quote.averageDailyVolume);
    ((TextView) this.findViewById(R.id.marketCapTextView)).setText(this.quote.marketCapitalization == null
        || this.quote.marketCapitalization.equals("null") ? this.getResources().getString(R.string.not_available)
        : this.quote.marketCapitalization);
  }

  /**
   * Download and fill the RSS feed for a particular ticker
   * 
   * @param ticker
   *          The ticker to download and display the rss feed of
   */
  private void fillFeed(String ticker) {
    Log.i(TAG, "Filling rss feed with items for " + ticker);

    // If no internet network is available, display an error message
    if (!Utils.isNetworkAvailable(this)) {
      this.feedTextField.setText(this.getResources().getString(R.string.no_internet_connection));
      return;
    }

    String url;
    try {
      // Create the URL of the rss feed using the ticker
      url = String.format(FEEDURL, URLEncoder.encode(ticker.toLowerCase(Locale.getDefault()), "utf-8"));

      // Start the download in another thread so the UI does not lock up
      new DownloadRSSXmlTask().execute(url);
    }
    catch (UnsupportedEncodingException e) {
      Log.e(TAG, e.getMessage());
    }
  }

  /**
   * Called when the display chart button is clicked. Start the ChartActivity and pass in the ticker that it will display the charts
   * of
   * 
   * @param v
   *          The view that raised the event
   */
  public void onChartButtonClick(View v) {
    // Construct the Intent making sure to add the ticker that the activity will use
    Intent i = new Intent(this, ChartActivity.class);
    i.putExtra("ticker", this.quote.ticker);

    // Start the ChartActivity
    this.startActivity(i);
  }

  /**
   * Called when this activity is created. Initialise fields from the layout and add adapters and listeners to views
   * 
   * @param savedInstanceState
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the layout of this activity
    this.setContentView(R.layout.activity_quote);

    // Get the Quote object that is passed into the activity through the intent object
    Bundle extras = this.getIntent().getExtras();
    this.quote = (Quote) extras.get("quote");

    // Get views from the layout
    this.rssFeed = (ListView) this.findViewById(R.id.feedList);
    this.feedTextField = ((TextView) this.findViewById(R.id.feedTextView));

    // Set the adapter for the RSS feed listview
    this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    this.rssFeed.setAdapter(this.adapter);
    this.rssFeed.setOnItemClickListener(new OnItemClickListener() {

      /**
       * Called when an item in the listview is clicked on. Get the url of the corresponding rss item and start up the default
       * browser at the url
       */
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.i(TAG, "Feed item clicked at position " + position);

        // Get the text of the item that was clicked on
        String item = ((TextView) v).getText().toString();
        if (QuoteActivity.this.links != null) {
          // Get the link url of the corresponding rss item
          String url = QuoteActivity.this.links.get(item);
          if (url != null) {
            // Start the internet browser at the rss item url
            QuoteActivity.this.startUrl(url);
          }
        }
      }
    });

    // If we have some data to display
    if (this.quote != null) {
      /** Set the textviews text to the data held inside the quote object */
      this.displayQuote();

      /** Download and fill the rss feed with items related to the company that this activity displays */
      this.fillFeed(this.quote.ticker);
    }
    else {
      ((TextView) this.findViewById(R.id.companyNameTextView)).setText(this.getResources().getString(R.string.not_available));
      ((Button) this.findViewById(R.id.chartButton)).setEnabled(false);
    }
  }

  /**
   * Open up the default internet browser at the specified url
   * 
   * @param url
   *          The URL that the browser will load
   */
  private void startUrl(String url) {
    Log.i(TAG, "Starting browser with url of " + url);
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    this.startActivity(browserIntent);
  }
}
