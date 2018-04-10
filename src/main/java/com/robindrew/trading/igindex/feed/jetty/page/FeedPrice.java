package com.robindrew.trading.igindex.feed.jetty.page;

import static java.lang.System.currentTimeMillis;

import com.robindrew.common.html.Bootstrap;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.price.candle.IPriceCandle;
import com.robindrew.trading.price.candle.streaming.IPriceCandleSnapshot;
import com.robindrew.trading.price.candle.streaming.IStreamingCandlePrice;
import com.robindrew.trading.price.decimal.Decimals;

public class FeedPrice implements Comparable<FeedPrice> {

	private static final int STALE_THRESHOLD = 10000;

	public static final String toId(String instrument) {
		StringBuilder id = new StringBuilder();
		for (char c : instrument.toCharArray()) {
			if (Character.isDigit(c) || Character.isLetter(c)) {
				id.append(c);
			}
		}
		return id.toString();
	}

	private final String id;
	private final String instrument;
	private final String close;
	private final String direction;
	private final String lastUpdated;
	private final String updateCount;
	private final String directionColor;

	public FeedPrice(IInstrumentPriceStream subscription) {
		IStreamingCandlePrice price = subscription.getPrice();
		IPriceCandleSnapshot snapshot = price.getSnapshot();

		this.instrument = subscription.getInstrument().getName();
		this.id = toId(this.instrument);

		if (snapshot == null) {
			this.close = "-";
			this.direction = "STALE";
			this.lastUpdated = "-";
			this.updateCount = "-";
			this.directionColor = Bootstrap.COLOR_WARNING;
		} else {
			IPriceCandle latest = snapshot.getLatest();

			// Normalise time to the nearest second to give impression of ticking
			long millis = currentTimeMillis() - snapshot.getTimestamp();
			millis = (millis / 1000) * 1000;

			this.close = Decimals.toBigDecimal(latest.getClosePrice(), latest.getDecimalPlaces()).toPlainString();
			this.direction = millis >= STALE_THRESHOLD ? "STALE" : snapshot.getDirection().name();
			this.lastUpdated = millis >= STALE_THRESHOLD ? Strings.duration(millis) : "-";
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
