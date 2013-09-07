/**
 * Portfolio.java
 */

package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.util.Log;

/**
 * Class to hold and manage a set of companies that are being monitored for quotes. This class manages adding and removing companies
 * from the set as well as updating the current quote information for each monitored business
 * 
 * @author Ryan Harrison
 */
public class Portfolio {

  /** The list of companies that are currently being monitored in this portfolio */
  private List<Quote>         companies;

  /**
   * The URL used when updating the quotes for each company. The stock data is obtained from the Yahoo! Finance API which is free to
   * use for personal use
   */
  private static final String QUOTEURL = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quote%20where%20symbol%20%3D%20%22%s%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

  public static final String  TAG      = "Portfolio";

  /**
   * Construct a new Portfolio object
   */
  public Portfolio() {
    this.companies = new ArrayList<Quote>();
  }

  /**
   * Add a new company to the current list of monitored companies without updating the quotes for the set
   * 
   * @param ticker
   *          The ticker of the company to add
   */
  public void addCompanyNoUpdate(String ticker) {
    Log.i(TAG, "Adding " + ticker + " to the portfolio");

    // Add the company to the list, wrapped inside a default quote object
    Quote q = new Quote();
    q.ticker = ticker;
    this.companies.add(q);
  }

  /**
   * Download an updated quote for a specified company ticker
   * 
   * @param quote
   *          The existing quote to update
   * @return An updated quote for the specified ticker
   */
  private Quote downloadQuoteFor(Quote quote) throws IOException {
    Log.i(TAG, "Downloading new quote data for " + quote.ticker);
    String url;
    try {
      // Construct the custom url using the ticker
      url = QUOTEURL.replace("%s", URLEncoder.encode(quote.ticker, "utf-8"));
      // Download a quote json object from the internet and parse it to get the quote
      return this.loadJsonFromNetwork(url);
    }
    catch (JSONException e) {
      Log.e(TAG, e.getMessage());
      return quote;
    }
  }

  /**
   * Get a list of quotes for each company currently being monitored
   * 
   * @return A list of quotes for each company currently being monitored
   */
  public List<Quote> getQuotes() {
    return this.companies;
  }

  /**
   * Download and parse a json object and construct a quote object from its data
   * 
   * @param url
   *          The url of the json object to download and parse
   * @return A Quote object containing the data from the downloaded json object
   * @throws JSONException
   *           If there was an error parsing the json object
   * @throws IOException
   *           If there was an error downloading the json object
   */
  private Quote loadJsonFromNetwork(String url) throws JSONException, IOException {
    InputStream stream = null;
    // Create a parser to retrieve the data from the json object
    QuoteJsonParser quoteParser = new QuoteJsonParser();
    Quote quote;
    try {
      Log.i(TAG, "Retrieiving url stream");
      // Download the json object from the url
      stream = Utils.downloadUrl(url);

      Log.i(TAG, "Parsing json object for quote data");
      // Parse the json string into a Quote object
      quote = quoteParser.parse(stream);
    }
    finally {
      // Make sure that the InputStream is closed in all cases
      if (stream != null) {
        stream.close();
      }
    }
    return quote;
  }

  /**
   * Remove a company from the current list of monitored companies
   * 
   * @param ticker
   *          The ticker of the company to remove
   */
  public void removeCompany(String ticker) {
    Log.i(TAG, "Removing " + ticker + " from portfolio");
    // Loop through each company in the monitored list
    for (int i = 0; i < this.companies.size(); i++) {
      // If the two tickers match, remove it from the list
      if (this.companies.get(i).ticker.equalsIgnoreCase(ticker)) {
        this.companies.remove(i);
      }
    }
  }

  /**
   * Update the quote information for each currently monitored company by downloading the data from Yahoo! Finance
   * 
   * @throws IOException
   *           If there was an error updating the quotes
   */
  public void update() throws IOException {
    Log.i(TAG, "Updating all portfolio quote data");
    for (int i = 0; i < this.companies.size(); i++) {
      // Download an updated quote for each company, using its ticker symbol
      Quote q = this.downloadQuoteFor(this.companies.get(i));
      // Overwrite the old quote with the newly updated one
      this.companies.set(i, q);
    }
  }
}
