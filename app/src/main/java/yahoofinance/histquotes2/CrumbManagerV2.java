package yahoofinance.histquotes2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yahoofinance.YahooFinance;
import yahoofinance.util.RedirectableRequest;

public class CrumbManagerV2 {

    private static final Logger log = LoggerFactory.getLogger(CrumbManagerV2.class);

    private static String crumb = "";
    private static String cookie = "";

    private static void setCookie() throws IOException {
        if(System.getProperty("yahoofinance.cookie") != null && !System.getProperty("yahoofinance.cookie").isEmpty()) {
            cookie =System.getProperty("yahoofinance.cookie");
            log.debug("Set cookie from system property: {}", cookie);
            return;
        }

        URL request = new URL(YahooFinance.HISTQUOTES2_SCRAPE_URL);
        RedirectableRequest redirectableRequest = new RedirectableRequest(request, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);

        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        URLConnection connection = redirectableRequest.openConnection(requestProperties);

        // Trigger connection
        connection.getHeaderFields();

        CookieStore cookieJar =  ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        for(HttpCookie hcookie : cookieJar.getCookies()) {
            if("B".equalsIgnoreCase(hcookie.getName())) {
                updateCookieFromStore(cookieJar);
                return;
            }
        }

        //  If cookie is not set, we should consent to activate cookie
        InputStreamReader isReader;
        try {
            isReader = new InputStreamReader(connection.getInputStream());
        } catch (IOException e) {
            if (connection instanceof HttpURLConnection) {
                InputStream errorStream = ((HttpURLConnection) connection).getErrorStream();
                if (errorStream != null) {
                    isReader = new InputStreamReader(errorStream);
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
        BufferedReader br = new BufferedReader(isReader);
        String line;
        Pattern patternPostForm = Pattern.compile("action=\"/consent\"");
        Pattern patternInput = Pattern.compile("(<input type=\"hidden\" name=\")(.*?)(\" value=\")(.*?)(\">)");
        Matcher matcher;
        Map<String,String> datas = new HashMap<String,String>();
        boolean postFind = false;
        // Read source to get params data for post request
        while( (line =br.readLine())!=null ) {
            matcher = patternPostForm.matcher(line);
            if(matcher.find()){
                postFind = true;
            }

            if(postFind){
                matcher = patternInput.matcher(line);
                if(matcher.find()){
                    String name = matcher.group(2);
                    String value = matcher.group(4);
                    datas.put(name, value);
                }
            }

        }
        // If params are not empty, send the post request
        if(datas.size()>0){

            datas.put("namespace",YahooFinance.HISTQUOTES2_COOKIE_NAMESPACE);
            datas.put("agree",YahooFinance.HISTQUOTES2_COOKIE_AGREE);
            datas.put("originalDoneUrl",YahooFinance.HISTQUOTES2_SCRAPE_URL);
            datas.put("doneUrl",YahooFinance.HISTQUOTES2_COOKIE_OATH_DONEURL+datas.get("sessionId")+"&inline="+datas.get("inline")+"&lang="+datas.get("locale"));

            URL requestOath = new URL(YahooFinance.HISTQUOTES2_COOKIE_OATH_URL);
            HttpURLConnection connectionOath = null;
            connectionOath = (HttpURLConnection) requestOath.openConnection();
            connectionOath.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
            connectionOath.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
            connectionOath.setRequestMethod( "POST" );
            connectionOath.setDoOutput( true );
            connectionOath.setRequestProperty("Referer", connection.getURL().toString());
            connectionOath.setRequestProperty("Host",YahooFinance.HISTQUOTES2_COOKIE_OATH_HOST);
            connectionOath.setRequestProperty("Origin",YahooFinance.HISTQUOTES2_COOKIE_OATH_ORIGIN);
            connectionOath.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            StringBuilder params=new StringBuilder("");

            for ( String key : datas.keySet() ) {
                if(params.length() == 0 ){
                    params.append(key);
                    params.append("=");
                    params.append(URLEncoder.encode(datas.get(key),"UTF-8"));
                }else{
                    params.append("&");
                    params.append(key);
                    params.append("=");
                    params.append(URLEncoder.encode(datas.get(key),"UTF-8"));

                }
            }


            log.debug("Params = "+ params.toString());
            connectionOath.setRequestProperty("Content-Length",Integer.toString(params.toString().length()));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connectionOath.getOutputStream());
            outputStreamWriter.write(params.toString());
            outputStreamWriter.flush();
            connectionOath.setInstanceFollowRedirects(true);
            connectionOath.getResponseCode();
        }

        updateCookieFromStore(cookieJar);
        if (cookie == null || cookie.isEmpty()) {
            log.debug("Failed to set cookie from http request. Historical quote requests will most likely fail.");
        }
    }

    private static void updateCookieFromStore(CookieStore cookieJar) {
        List<HttpCookie> cookies = cookieJar.getCookies();
        StringBuilder sb = new StringBuilder();
        for (HttpCookie hcookie: cookies) {
            if (hcookie.getDomain() != null && hcookie.getDomain().contains("yahoo.com")) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(hcookie.getName()).append("=").append(hcookie.getValue());
            }
        }
        if (sb.length() > 0) {
            cookie = sb.toString();
            System.setProperty("yahoofinance.cookie", cookie);
            log.debug("Set cookie from cookie store: {}", cookie);
        }
    }

    private static void setCrumb() throws IOException {
        // ... (rest of setCrumb unchanged)
        if(System.getProperty("yahoofinance.crumb") != null && !System.getProperty("yahoofinance.crumb") .isEmpty()) {
            crumb = System.getProperty("yahoofinance.crumb") ;
            log.debug("Set crumb from system property: {}", crumb);
            return;
        }

        URL crumbRequest = new URL(YahooFinance.HISTQUOTES2_CRUMB_URL);
        RedirectableRequest redirectableCrumbRequest = new RedirectableRequest(crumbRequest, 5);
        redirectableCrumbRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableCrumbRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("Cookie", cookie);
        requestProperties.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");

        URLConnection crumbConnection = redirectableCrumbRequest.openConnection(requestProperties);
        InputStreamReader isReader;
        try {
            isReader = new InputStreamReader(crumbConnection.getInputStream());
        } catch (IOException e) {
            if (crumbConnection instanceof HttpURLConnection) {
                InputStream errorStream = ((HttpURLConnection) crumbConnection).getErrorStream();
                if (errorStream != null) {
                    isReader = new InputStreamReader(errorStream);
                } else {
                    log.warn("Failed to get crumb, trying fallback scrape...");
                    scrapeCrumbFallback();
                    return;
                }
            } else {
                throw e;
            }
        }
        BufferedReader br = new BufferedReader(isReader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String crumbResult = sb.toString();

        if(crumbResult != null && !crumbResult.isEmpty() && !crumbResult.contains("<html") && !crumbResult.contains("{\"finance\"")) {
            crumb = crumbResult.trim();
            System.setProperty("yahoofinance.crumb", crumb);
            log.debug("Set crumb from http request: {}", crumb);
        } else if (crumbResult != null && (crumbResult.contains("<html") || crumbResult.contains("{\"finance\""))) {
            if (crumbResult.contains("{\"finance\"")) {
                log.warn("Crumb request returned JSON error: {}. Trying fallback scrape...", crumbResult);
            }
            extractCrumbFromHtml(crumbResult);
        } else {
            log.debug("Failed to set crumb from http request. Trying fallback scrape...");
            scrapeCrumbFallback();
        }

    }

    private static void scrapeCrumbFallback() throws IOException {
        URL scrapeUrl = new URL("https://finance.yahoo.com/quote/AAPL");
        RedirectableRequest redirectableRequest = new RedirectableRequest(scrapeUrl, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);

        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Cookie", cookie);
        requestProperties.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        URLConnection connection = redirectableRequest.openConnection(requestProperties);

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        extractCrumbFromHtml(sb.toString());
    }

    private static void extractCrumbFromHtml(String html) {
        if (html == null || html.isEmpty()) {
            return;
        }
        Pattern pattern = Pattern.compile("\"crumb\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String foundCrumb = matcher.group(1);
            if (foundCrumb != null) {
                crumb = foundCrumb.replace("\\u002F", "/");
                System.setProperty("yahoofinance.crumb", crumb);
                log.debug("Set crumb from HTML scrape: {}", crumb);
            }
        } else {
            log.debug("Failed to extract crumb from HTML.");
        }
    }

    public static void refresh() throws IOException {
        cookie = "";
        crumb = "";
        System.clearProperty("yahoofinance.cookie");
        System.clearProperty("yahoofinance.crumb");
        setCookie();
        setCrumb();
    }

    public static synchronized String getCrumb() throws IOException {
        if(crumb == null || crumb.isEmpty()) {
            refresh();
        }
        return crumb;
    }

    public static synchronized String getCookie() throws IOException {
        CookieStore cookieJar =  ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();
        if (cookies.isEmpty() && (cookie == null || cookie.isEmpty())) {
            refresh();
        }
        updateCookieFromStore(cookieJar);
        if(cookie == null || cookie.isEmpty()) {
            refresh();
        }
        return cookie;
    }

}
