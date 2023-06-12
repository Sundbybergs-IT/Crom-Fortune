package yahoofinance.quotes.query1v7;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import yahoofinance.Utils;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes2.CrumbManagerV2;
import yahoofinance.util.RedirectableRequest;

public abstract class QuotesRequestV2<T> {

    private static final Logger log = LoggerFactory.getLogger(QuotesRequestV2.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected final String symbols;

    public QuotesRequestV2(String symbols) {
        this.symbols = symbols;
    }

    public String getSymbols() {
        return symbols;
    }

    protected abstract T parseJson(JsonNode node);

    public T getSingleResult() throws IOException {
        List<T> results = this.getResult();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    /**
     * Sends the request to Yahoo Finance and parses the result
     *
     * @return List of parsed objects resulting from the Yahoo Finance request
     * @throws IOException when there's a connection problem or the request is incorrect
     */
    public List<T> getResult() throws IOException {
        String cookie = System.getProperty("yahoofinance.cookie");
        String crumb = System.getProperty("yahoofinance.crumb");

        if (cookie == null || crumb.isEmpty()) {
            cookie = getCookie();
            crumb = getCrumb(cookie);
            System.setProperty("yahoofinance.crumb", crumb);
            System.setProperty("yahoofinance.cookie", cookie);
            CrumbManagerV2.refresh();
        }

        List<T> result = new ArrayList<T>();

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbols", this.symbols);
        params.put("crumb", crumb);

        String url = YahooFinance.QUOTES_QUERY1V7_BASE_URL + "?" + Utils.getURLParameters(params);

        // Get JSON from Yahoo
        log.info("Sending request: " + url);

        URL request = new URL(url);
        RedirectableRequest redirectableRequest = new RedirectableRequest(request, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
        URLConnection connection = redirectableRequest.openConnection();

        InputStreamReader is = new InputStreamReader(connection.getInputStream());
        JsonNode node = objectMapper.readTree(is);
        if (node.has("quoteResponse") && node.get("quoteResponse").has("result")) {
            node = node.get("quoteResponse").get("result");
            for (int i = 0; i < node.size(); i++) {
                result.add(this.parseJson(node.get(i)));
            }
        } else {
            throw new IOException("Invalid response");
        }

        return result;
    }

    private String getCrumb(String cookie) throws IOException {
        URL preRequest = new URL("https://query1.finance.yahoo.com/v1/test/getcrumb");
        RedirectableRequest redirectableRequest = new RedirectableRequest(preRequest, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("set-cookie", cookie);
        URLConnection preConnection = redirectableRequest.openConnection(requestProperties);

        // Cast to HttpURLConnection to access additional methods
        if (preConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) preConnection;

            // Get the input stream to read the response body
            InputStream inputStream = httpConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read the response body line by line
            String line;
            StringBuilder responseBody = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }

            // Close the resources
            reader.close();
            inputStream.close();

            // Print the response body
            return responseBody.toString();
        } else {
            throw new IllegalStateException("Cannot retrieve crumb!");
        }
    }

    private static String getCookie() throws IOException {
        URL preRequest = new URL("https://fc.yahoo.com");
        RedirectableRequest redirectableRequest = new RedirectableRequest(preRequest, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
        URLConnection preConnection = redirectableRequest.openConnection();
        Map<String, List<String>> headerFields = preConnection.getHeaderFields();
        return Objects.requireNonNull(headerFields.get("Set-Cookie")).get(0);
    }

}
