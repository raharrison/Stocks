/**
 * ChartActivity.java
 */

package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import uk.co.ryanharrison.stocks.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Activity to display stock charts for a specific ticker. The timespan of the chart can be varied by the user through a series of
 * radiobuttons
 * 
 * @author Ryan Harrison
 */
public class ChartActivity extends Activity {

  /**
   * Task to download a stock chart from the internet and display it in an ImageView
   * 
   * @author Ryan Harrison (rh00148)
   */
  private class DownloadChartTask extends AsyncTask<String, Void, Bitmap> {

    /**
     * Main work to do in the separate thread. Download and return a Bitmap from the URL passed in
     * 
     * @param params
     *          The url of the image to download
     * @return A bitmap image of the stock chart
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Bitmap doInBackground(String... urls) {
      try {
        Log.i(TAG, "Loading chart bitmap from url");
        // Download the bitmap from the url
        return this.loadBitmapFromNetwork(urls[0]);
      }
      catch (IOException e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
    }

    /**
     * Download a chart from the url and return as a Bitmap
     * 
     * @param url
     *          The URL to download the chart from
     * @return A Bitmap downloaded from the specified urls
     * @throws IOException
     *           If there was an error downloading the chart
     */
    private Bitmap loadBitmapFromNetwork(String url) throws IOException {
      InputStream stream = null;
      Bitmap result;
      try {
        Log.i(TAG, "Retrieiving url stream");
        stream = Utils.downloadUrl(url);

        Log.i(TAG, "Decoding bitmap from stream");
        result = BitmapFactory.decodeStream(stream);
      }
      catch (Exception e) {
        Log.e(TAG, e.getMessage());
        return null;
      }
      finally {
        // Close the stream in all cases
        if (stream != null) {
          stream.close();
        }
      }
      return result;
    }

    /**
     * Called back on the UI thread after the main work has finished. Display the newly downloaded image in the UI imageview
     * 
     * @param result
     *          The stock chart bitmap that has just been downloaded
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Bitmap result) {
      Log.i(TAG, "Background work completed");

      // If the result is null there was an error so tell the user about it
      if (result == null) {
        ChartActivity.this.textView.setText(ChartActivity.this.getResources().getString(R.string.unable_download_chart)
            + ChartActivity.this.ticker);
        return;
      }

      // Otherwise display the image in the UI
      ChartActivity.this.textView.setText(ChartActivity.this.getResources().getString(R.string.chart_for)
          + ChartActivity.this.ticker);
      ChartActivity.this.chartView.setImageBitmap(result);
    }

    /**
     * Called before the main work is done in the separate thread. Set a prompt for the user telling them that a chart is being
     * downloaded
     * 
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
      ChartActivity.this.textView.setText(ChartActivity.this.getResources().getString(R.string.downloading_chart)
          + ChartActivity.this.ticker);
    }
  }

  /** The ticker of which charts will be displayed for */
  private String              ticker;

  /** The view that will be used to display the stock chart */
  private ImageView           chartView;

  /** A view used to display download progress to the user */
  private TextView            textView;

  /** The group of radiobuttons that specify the timespan of the chart */
  private RadioGroup          radioGroup;

  /**
   * The url that will be used to download the stock chart image. The charts are obtained from the Yahoo! Finance API which is free
   * to use for personal use
   */
  private static final String CHARTURL = "http://chart.finance.yahoo.com/z?s=%s&z=m&t=%s";

  /** Tag for this activity */
  public static final String  TAG      = "ChartActivity";

  /**
   * Download a stock chart for the specified ticker with the specified timespan
   * 
   * @param ticker
   *          The ticker to display the stock chart for
   * @param timeSpan
   *          The timespan of the stock chart that will be displayed
   */
  private void downloadChart(String ticker, String timeSpan) {
    Log.i(TAG, "Downloading chart for " + ticker + " with timespan of " + timeSpan);

    // If no internet network is available, display an error message
    if (!Utils.isNetworkAvailable(this)) {
      this.textView.setText(this.getResources().getString(R.string.no_internet_connection));
      return;
    }

    // Remove any existing chart that may already be being displayed
    this.chartView.setImageBitmap(null);
    String url;
    try {
      // Create the url for the chart download by inserting the ticker and timespan
      url = String.format(CHARTURL, URLEncoder.encode(ticker.toLowerCase(Locale.getDefault()), "utf-8"), timeSpan);

      // Start the download in another thread so the UI does not lock up
      new DownloadChartTask().execute(url);
    }
    catch (UnsupportedEncodingException e) {
      Log.e(TAG, e.getMessage());
    }
  }

  /**
   * Called when the activity is created. Get the ticker to use from the intent and initialise other fields.
   * 
   * @param savedInstanceState
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the layout of this activity
    this.setContentView(R.layout.activity_chart);

    // Get the ticker passed into the activity through the intent
    Bundle extras = this.getIntent().getExtras();
    this.ticker = extras.getString("ticker");

    // Initialise view fields
    this.chartView = (ImageView) this.findViewById(R.id.chartImageView);
    this.textView = (TextView) this.findViewById(R.id.chartProgressTextView);
    this.radioGroup = (RadioGroup) this.findViewById(R.id.chartRadioGroup);

    // When the activity is created, by default download and display the 3 month chart for the ticker
    this.downloadChart(this.ticker, "3m");
  }

  /**
   * Called when any radiobutton in the layout is clicked on. When a radiobutton is clicked, download the chart with the
   * corresponding timespan to the radiobutton that was pressed
   * 
   * @param v
   *          The view that raised the event
   */
  public void onRadioButtonClick(View v) {
    Log.i(TAG, "Timespan changed");

    // Get the id of the radiobutton that was clicked on
    int id = this.radioGroup.getCheckedRadioButtonId();

    // Depending on which button was pressed, download a chart with the corresponding timespan
    switch (id) {
      case R.id.oneDayRadioButton:
        this.downloadChart(this.ticker, "1d");
        break;
      case R.id.oneWeekRadioButton:
        this.downloadChart(this.ticker, "1w");
        break;
      case R.id.oneMonthRadioButton:
        this.downloadChart(this.ticker, "1m");
        break;
      case R.id.threeMonthRadioButton:
        this.downloadChart(this.ticker, "3m");
        break;
      case R.id.sixmonthRadioButton:
        this.downloadChart(this.ticker, "6m");
        break;
    }
  }
}
