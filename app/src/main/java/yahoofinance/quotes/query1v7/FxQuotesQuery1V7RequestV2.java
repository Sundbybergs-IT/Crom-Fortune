package yahoofinance.quotes.query1v7;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

import yahoofinance.Utils;
import yahoofinance.quotes.fx.FxQuote;

public class FxQuotesQuery1V7RequestV2 extends QuotesRequestV2<FxQuote> {

    public FxQuotesQuery1V7RequestV2(String symbols) {
        super(symbols);
    }

    @Override
    protected FxQuote parseJson(JsonNode node) {
        String symbol = node.get("symbol").asText();
        BigDecimal price = Utils.getBigDecimal(node.get("regularMarketPrice").asText());

        return new FxQuote(symbol, price);
    }

}
