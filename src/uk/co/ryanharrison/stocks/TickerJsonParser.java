/**
 * TickerJsonParser.java
 */

package uk.co.ryanharrison.stocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to parse a downloaded JSON object into a collection of ticker strings
 * 
 * @author Ryan Harrison
 */
public class TickerJsonParser {

  /**
   * Parse a JSON object into a a Map of UI friendly business strings to their corresponding tickers
   * 
   * @param in
   *          A stream of the downloaded JSON object to parse
   * @return A Map of UI friendly business strings to their corresponding tickers
   * @throws IOException
   *           If there was an error reading the InputStream
   * @throws JSONException
   *           If there was an error parsing the JSON object
   */
  public Map<String, String> parse(InputStream in) throws IOException, JSONException {
    try {
      // Create a reader to get the json object from the stream
      BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8);

      // Get the json string and format for parsing
      String json = reader.readLine();
      json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);

      // Get an array of tickers from the json string
      JSONArray arr = new JSONObject(json).getJSONObject("ResultSet").getJSONArray("Result");

      // Create the map of name + ticker to ticker.
      // A LinkedHashMap is used to maintain the order of insertion
      Map<String, String> tickers = new LinkedHashMap<String, String>();

      // For each entry in the array
      for (int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);
        // Get the company name and ticker from the object
        String name = obj.getString("name");
        String symbol = obj.getString("symbol");
        // Insert the data into the map
        tickers.put(name + " (" + symbol + ")", symbol);
      }

      return tickers;
    }
    finally {
      // Even if an exception is thrown we must close the stream
      if (in != null) {
        in.close();
      }
    }
  }
}
