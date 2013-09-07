/**
 * QuoteJsonParser.java
 */

package uk.co.ryanharrison.stocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to parse a downloaded JSON object into a stock quote
 * 
 * @author Ryan Harrison
 */
public class QuoteJsonParser {

  /**
   * Parse a downloaded JSON object into a Quote object for a stock
   * 
   * @param in
   *          A stream of the downloaded JSON object to parse
   * @return A Quote for a particular stock
   * @throws IOException
   *           If there was an error downloading the JSON object
   * @throws JSONException
   *           If there was an error parsing the JSON object
   */
  public Quote parse(InputStream in) throws IOException, JSONException {
    try {
      // Read the JSON object as a String through the stream
      BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8);
      String json = reader.readLine();
      json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);

      // Get the object from the JSON string that holds the quote information for the stock
      JSONObject obj = new JSONObject(json).getJSONObject("query").getJSONObject("results").getJSONObject("quote");

      Quote quote = new Quote();

      // Assign values to each field of the Quote corresponding to values in the JSON object
      quote.averageDailyVolume = Long.parseLong(obj.getString("AverageDailyVolume"));
      quote.change = Double.parseDouble(obj.getString("Change"));
      quote.yearLow = Double.parseDouble(obj.getString("YearLow"));
      quote.yearHigh = Double.parseDouble(obj.getString("YearHigh"));
      quote.marketCapitalization = obj.getString("MarketCapitalization");
      quote.lastTradePrice = Double.parseDouble(obj.getString("LastTradePriceOnly"));
      quote.name = obj.getString("Name");
      quote.ticker = obj.getString("Symbol");
      quote.volume = Long.parseLong(obj.getString("Volume"));
      quote.stockExchange = obj.getString("StockExchange");
      quote.percentChange = quote.change / quote.lastTradePrice * 100.0;

      // If DaysLow is null it means that the stock market has not yet opened for trading so we don't want to parse it
      if (!obj.getString("DaysLow").equals("null")) {
        quote.daysLow = Double.parseDouble(obj.getString("DaysLow"));
        quote.daysHigh = Double.parseDouble(obj.getString("DaysHigh"));
      }
      return quote;
    }
    finally {
      // Close the stream in all cases
      if (in != null) {
        in.close();
      }
    }
  }
}
