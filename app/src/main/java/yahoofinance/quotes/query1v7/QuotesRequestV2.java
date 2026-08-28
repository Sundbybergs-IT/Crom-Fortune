package yahoofinance.quotes.query1v7;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        String cookie = CrumbManagerV2.getCookie();
        String crumb = CrumbManagerV2.getCrumb();

        List<T> result = new ArrayList<>();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbols", this.symbols);
        params.put("crumb", crumb);

        String url = YahooFinance.QUOTES_QUERY1V7_BASE_URL + "?" + Utils.getURLParameters(params);

        // Get JSON from Yahoo
        log.info("Sending request: " + url);

        URL request = new URL(url);
        RedirectableRequest redirectableRequest = new RedirectableRequest(request, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Cookie", cookie);
        requestProperties.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        URLConnection connection = redirectableRequest.openConnection(requestProperties);

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

}
