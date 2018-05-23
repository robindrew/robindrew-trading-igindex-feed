package com.robindrew.trading.igindex.feed.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.trading.igindex.IIgIndexInstrument;
import com.robindrew.trading.igindex.platform.IIgIndexSession;
import com.robindrew.trading.igindex.platform.rest.IIgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getmarkets.response.Markets;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.IStreamingService;

public class FeedsPage extends AbstractServicePage {

	public FeedsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		IIgIndexSession session = getDependency(IIgIndexSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		ITradingPlatform<IIgIndexInstrument> platform = getDependency(ITradingPlatform.class);
		IStreamingService<IIgIndexInstrument> service = platform.getStreamingService();
		dataMap.put("feeds", getFeeds(service.getPriceStreams()));
	}

	private Set<Feed> getFeeds(Set<IInstrumentPriceStream<IIgIndexInstrument>> subscriptions) {
		IIgIndexRestService rest = getDependency(IIgIndexRestService.class);
		Set<Feed> feeds = new TreeSet<>();
		for (IInstrumentPriceStream<IIgIndexInstrument> subscription : subscriptions) {
			String epic = subscription.getInstrument().getName();
			Markets markets = rest.getMarkets(epic, false);
			feeds.add(new Feed(subscription, markets));
		}
		return feeds;
	}

	public static class Feed implements Comparable<Feed> {

		private final IInstrumentPriceStream<IIgIndexInstrument> subscription;
		private final Markets markets;
		private final FeedPrice price;

		public Feed(IInstrumentPriceStream<IIgIndexInstrument> subscription, Markets markets) {
			this.subscription = subscription;
			this.markets = markets;
			this.price = new FeedPrice(subscription);
		}

		public String getId() {
			return FeedPrice.toId(subscription.getInstrument().getName());
		}

		public IInstrumentPriceStream<IIgIndexInstrument> getSubscription() {
			return subscription;
		}

		public Markets getMarkets() {
			return markets;
		}

		public FeedPrice getPrice() {
			return price;
		}

		@Override
		public int compareTo(Feed that) {
			return this.getPrice().compareTo(that.getPrice());
		}
	}
}
