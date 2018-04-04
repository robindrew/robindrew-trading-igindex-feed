package com.robindrew.trading.igindex.feed.jetty.page;

import static java.lang.System.currentTimeMillis;

import com.robindrew.common.html.Bootstrap;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.latest.IPriceSnapshot;
import com.robindrew.trading.platform.streaming.latest.IStreamingPrice;
import com.robindrew.trading.price.candle.IPriceCandle;
import com.robindrew.trading.price.decimal.Decimals;

public class FeedPrice implements Comparable<FeedPrice> {

	private final String id;
	private final String instrument;
	private final String close;
	private final String direction;
	private final String lastUpdated;
	private final String updateCount;
	private final String directionColor;

	public FeedPrice(IInstrumentPriceStream subscription) {
		IStreamingPrice price = subscription.getPrice();
		IPriceSnapshot snapshot = price.getSnapshot();

		this.instrument = subscription.getInstrument().getName();
		this.id = this.instrument.replace('.', '_');

		if (snapshot == null) {
			this.close = "-";
			this.direction = "STALE";
			this.lastUpdated = "-";
			this.updateCount = "-";
			this.directionColor = Bootstrap.COLOR_WARNING;
		} else {
			IPriceCandle latest = snapshot.getLatest();
			
			this.close = Decimals.toBigDecimal(latest.getClosePrice(), latest.getDecimalPlaces()).toPlainString();
			this.direction = snapshot.getDirection().name();
			long millis = currentTimeMillis() - snapshot.getTimestamp();
			this.lastUpdated = millis >= 5000 ? Strings.duration(millis) : "-";
			this.updateCount = String.valueOf(price.getUpdateCount());
			this.directionColor = snapshot.getDirection().isBuy() ? Bootstrap.COLOR_INFO : Bootstrap.COLOR_DANGER;
		}
	}

	public String getInstrument() {
		return instrument;
	}

	public String getClose() {
		return close;
	}

	public String getDirection() {
		return direction;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public String getId() {
		return id;
	}

	public String getUpdateCount() {
		return updateCount;
	}

	public String getDirectionColor() {
		return directionColor;
	}

	@Override
	public int compareTo(FeedPrice that) {
		return this.getInstrument().compareTo(that.getInstrument());
	}
}
