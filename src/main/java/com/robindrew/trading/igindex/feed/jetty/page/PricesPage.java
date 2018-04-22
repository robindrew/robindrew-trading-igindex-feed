package com.robindrew.trading.igindex.feed.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.trading.igindex.IIgInstrument;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.IStreamingService;

public class PricesPage extends AbstractServicePage {

	public PricesPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		ITradingPlatform<IIgInstrument> platform = getDependency(ITradingPlatform.class);
		IStreamingService<IIgInstrument> service = platform.getStreamingService();
		dataMap.put("prices", getPrices(service.getPriceStreams()));
	}

	private String getPrices(Set<IInstrumentPriceStream<IIgInstrument>> subscriptions) {
		List<FeedPrice> prices = new ArrayList<>();
		for (IInstrumentPriceStream<IIgInstrument> subscription : subscriptions) {
			prices.add(new FeedPrice(subscription));
		}

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(prices);
	}

}
