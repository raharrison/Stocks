package uk.co.ryanharrison.stocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import uk.co.ryanharrison.stocks.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Main Activity class for the app. Displays a grid of mini quote views for each stock that is being monitored as well as allows
 * users to click on the views to view more information about them. This activity is also enabled with the main menu which allows
 * the stock data to be updated and for new tickers to be added to the list of monitored stocks
 * 
 * @author Ryan Harrison
 */
public class MainActivity extends Activity {

  /**
   * Task to update the stock data for each business being monitored by a portfolio. The updated stock data is downloaded from the
   * internet and parsed as a JSON object
   * 
   * @author Ryan Harrison (rh00148)
   */
  private class UpdateQuotesTask extends AsyncTask<Portfolio, Void, Portfolio> {

    /**
     * Main work to do in a separate thread
     * 
     * @param urls
     *          The portfolio to update
     * @return The portfolio with updated stock data
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Portfolio doInBackground(Portfolio... params) {
      try {
        Log.i(TAG, "Updating portfolio quote data");
        // Update the portfolio. This downloads a JSON object for each monitored company which holds the data about the stock
        params[0].update();

        return params[0];
      }
      catch (Exception e) {
        Log.e(TAG, e.toString());
        return null;
      }

    }

    /**
     * Called back on the UI thread after the main work has finished. Change the user prompt and populate the gridview with the
     * updated stock data
     * 
     * @param result
     *          The portfolio object with updated stock data
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Portfolio result) {
      Log.i(TAG, "Background work completed");

      // If the result is null then display an error message to the user
      if (result == null) {
        MainActivity.this.progressText.setText(MainActivity.this.getResources().getString(R.string.unable_download_stocks));
        return;
      }

      // Recreate the adapter for the gridview and populate with the updated quotes from the portfolio
      MainActivity.this.adapter = new QuoteAdapter(MainActivity.this, MainActivity.this.portfolio.getQuotes());
      MainActivity.this.gridView.setAdapter(MainActivity.this.adapter);
      MainActivity.this.adapter.notifyDataSetChanged();

      // Set the information text view to the current time (the time the stock data was last updated)
      MainActivity.this.progressText.setText(MainActivity.this.getResources().getString(R.string.last_updated)
          + MainActivity.this.dateFormat.format(Calendar.getInstance().getTime()));
    }

    /**
     * Called before the main work is done in the separate thread. Set a prompt for the user telling them that updated stock data is
     * being downloaded.
     * 
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
      MainActivity.this.progressText.setText(MainActivity.this.getResources().getString(R.string.updating));
    }
  }

  /** Object which holds and manages the set of stocks that are being monitored by the app */
  private Portfolio           portfolio;

  /** Custom adapter for the gridview creates custom MiniQuoteViews for each stock in the portfolio */
  private QuoteAdapter        adapter;

  /** Progress textview displays information about the state of the app e.g updating/last updated etc */
  private TextView            progressText;

  /** The main gridview which is populated with MiniQuoteViews for each stock in the portfolio through the QuoteAdapter */
  private GridView            gridView;

  /** Allows the current date and time to be formatted in a particular way */
  private DateFormat          dateFormat;

  /** ID name for the preferences that stores the set of stocks being monitored for persistence */
  private static final String PREFS_NAME          = "StocksPrefs";

  /** Request code for the SearchActivity. Allows differentiation between activities that give results */
  private static final int    SEARCH_REQUEST_CODE = 1;

  /** Tag for this activity */
  public static final String  TAG                 = "MainActivity";

  /**
   * Called when an activity finishes with a result
   * 
   * @param requestCode
   *          The request code from the caller
   * @param resultCode
   *          The result code from the callee activity
   * @param data
   *          The result data
   * 
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case (SEARCH_REQUEST_CODE): {
        // If the activity completed successfully
        if (resultCode == Activity.RESULT_OK) {
          // Get the ticker from the data
          String ticker = data.getStringExtra("ticker");

          // Add the ticker to the portfolio of stocks and update the stock information
          this.portfolio.addCompanyNoUpdate(ticker);
          this.update();
        }
        break;
      }
    }
  }

  /**
   * @param newConfig
   * 
   * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    this.saveStocksToPreferences();
  }

  /**
   * Called when the activity is created. Set up fields and views with adapters and listeners. Populate the portfolio with the saved
   * stocks in the preferences if there are any. Also update the stock data for each stock currently being monitored when the
   * activity is created.
   * 
   * @param savedInstanceState
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the layout of this activity
    this.setContentView(R.layout.activity_main);

    // Initialise fields
    this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    this.portfolio = new Portfolio();

    SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
    // If there are a set of stocks held in the preferences, use them to fill the portfolio
    if (settings.contains("tickers")) {
      String tickers = settings.getString("tickers", "");
      for (String ticker : tickers.split(",")) {
        this.portfolio.addCompanyNoUpdate(ticker);
      }
    }
    else {
      Log.i(TAG, "No existing saved tickers found, filling defaults");
      // Otherwise fill the portfolio with some default stocks
      this.portfolio.addCompanyNoUpdate("YHOO");
      this.portfolio.addCompanyNoUpdate("ARM.L");
      this.portfolio.addCompanyNoUpdate("^FTSE");
      this.portfolio.addCompanyNoUpdate("MSFT");
      this.portfolio.addCompanyNoUpdate("AAPL");
      this.portfolio.addCompanyNoUpdate("FB");
    }

    // Set up the views and the adapter for the gridview
    this.progressText = (TextView) this.findViewById(R.id.progressTextView);
    this.gridView = (GridView) this.findViewById(R.id.gridview);

    this.adapter = new QuoteAdapter(this, this.portfolio.getQuotes());
    this.gridView.setAdapter(this.adapter);

    // If the device is currently in landscape mode, then we want to display 3 columns in the gridview instead of 2
    if (Utils.isInLandScape(this)) {
      this.gridView.setNumColumns(3);
    }

    // Listener for when the user clicks on a view inside the gridview.
    // Start the QuoteActivity, passing in the quote from the view that the user clicked on. The QuoteActivity then displays further
    // information about the stock using the quote
    this.gridView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.i(TAG, "Gridview item clicked at position " + position);

        Intent i = new Intent(MainActivity.this, QuoteActivity.class);
        // Add the corresponding quote object to the intent so it can be used in the other activity
        i.putExtra("quote", (Quote) MainActivity.this.adapter.getItem(position));

        // Start the new activity
        MainActivity.this.startActivity(i);
      }
    });

    // Add a long click listener to the gridview which is raised when the user clicks on an item for an extended period of time
    // This will be the mechanism for removing stocks from the current set of monitored businesses
    // Show the user a dialog asking them if they want to remove the stock from the current set. If they do, remove it from the
    // adapter, otherwise do nothing
    this.gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
        Log.i(TAG, "Gridview item long clicked at position " + position);

        // Get a builder for the dialog box
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        // Set the title of the dialog box
        alertDialogBuilder.setTitle(MainActivity.this.getResources().getString(R.string.remove_ticker_title));

        // Add a positive button to the dialog. This will be clicked on if the user wants to remove the stock
        alertDialogBuilder.setMessage(MainActivity.this.getResources().getString(R.string.remove_ticker)).setCancelable(false)
            .setPositiveButton(MainActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

              /**
               * Called when the user presses the 'yes' button. Remove the item that the user clicked on from the adapter. This will
               * also update the portfolio
               * 
               * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
               */
              @Override
              public void onClick(DialogInterface dialog, int id) {
                Log.i(TAG, "Removing item at position " + position);

                MainActivity.this.adapter.removeItem(position);
                MainActivity.this.adapter.notifyDataSetChanged();
              }
              // Also add a negative button. This will simply close the dialog
            }).setNegativeButton(MainActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

              /**
               * Called when the user presses the 'no' button. Simply close the dialog box
               * 
               * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
               */
              @Override
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        return true;
      }
    });

    // When the activity is created, we want to update the stock data in the portfolio as initially we have no data to display
    this.update();
  }

  /**
   * Initialise the contents of this activities menu
   * 
   * @param menu
   *          The menu to inflate
   * @return True if the menu was successfully inflated
   * 
   * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    this.getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  /**
   * Called when and item in the Menu is clicked on. Determine which item was clicked on and take the appropriate action
   * 
   * @param item
   *          The item that was clicked on
   * @return True if the event was handled successfully, otherwise false
   * 
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    // If the user clicked on the update item, update each stock in the portfolio concurrently
      case R.id.action_update:
        this.update();
        return true;
        // If the user clicked on the add ticker item, start a new activity that lets the user search for a business.
        // As the activity is expected to bring a result of the ticker that the user selected, startActivityForResult is used
        // instead.
        // onActivityResult will be called when the activity gives a result, the ticker can then be retrieved through the data
        // intent
      case R.id.action_addticker:
        Intent i = new Intent(this, SearchActivity.class);
        this.startActivityForResult(i, SEARCH_REQUEST_CODE);
        return true;
        // If the user clicked on the about item, start the about activity which displays text about the app
      case R.id.action_about:
        Intent in = new Intent(this, AboutActivity.class);
        this.startActivity(in);
        return true;
    }
    return false;
  }

  /**
   * Called when the activity is stopped. Saves each currently monitored stock into the preferences for persistence. These are then
   * loaded again when the activity is created
   * 
   * 
   * @see android.app.Activity#onStop()
   */
  @Override
  protected void onStop() {
    super.onStop();

    Log.i(TAG, "Saving tickers to preferences");

    this.saveStocksToPreferences();
  }

  /**
   * Saves the current set of monitored stocks to the preferences so that they can be reloaded again and changes to the list are not
   * lost
   */
  private void saveStocksToPreferences() {
    // Get an editor for the preferences
    SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();

    String tickers = "";
    // The stocks are held in one string in a comma separated manner
    for (Quote q : this.portfolio.getQuotes()) {
      if (q != null && q.ticker != null) {
        tickers += q.ticker + ",";
      }
    }
    if (tickers.length() != 0) {
      tickers = tickers.substring(0, tickers.length() - 1);
      // Put the string of stocks into the preferences and save them
      editor.putString("tickers", tickers);
      editor.commit();
    }
  }

  /**
   * Update the information about each stock held in the porfolio in a separate thread
   */
  private void update() {
    Log.i(TAG, "Updating quote data");
    // If a network connection is available, update the stock information
    if (Utils.isNetworkAvailable(this)) {

      // Start a custom task that downloads the data on another thread so the UI does not lock up
      new UpdateQuotesTask().execute(this.portfolio);
    }
    else {
      // Otherwise display an error message to the userr
      this.progressText.setText(this.getResources().getString(R.string.no_internet_connection));
    }
  }
}
