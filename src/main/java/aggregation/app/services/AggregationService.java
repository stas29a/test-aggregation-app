package aggregation.app.services;

import aggregation.app.models.aggregation.*;
import aggregation.app.models.backendApi.ApiBrand;
import aggregation.app.models.backendApi.ApiBrandMetrics;
import aggregation.app.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.asynchttpclient.Dsl.*;

@Service
public class AggregationService
{
    private interface IAggregate<T> {
        void aggregate(T aggregationObj, ApiBrandMetrics.BrandMetrics brandMetrics);
    }

    private class ParallelMetricsAggregator<T> {
        CompletableFuture<T> run(CompletableFuture<T> aggregationObjFuture, IAggregate<T> aggregator)
        {
            for(int i=0; i < MAX_REQUEST_PER_WINDOW; i++) {
                aggregationObjFuture = aggregationObjFuture.thenCombine(getApiBrandMetricsResponse(), (localAggregationObj, metricsResponse) -> {
                    if (metricsResponse != null && metricsResponse.getSize() > 0) {
                        metricsResponse
                                .getBrandMetrics()
                                .forEach(brandMetrics -> {
                                    aggregator.aggregate(localAggregationObj, brandMetrics);
                                });
                    }

                    return localAggregationObj;
                });
            }

            return aggregationObjFuture;
        }
    }

    private static final int MAX_REQUEST_PER_WINDOW = 20;
    private static final int REQUEST_TIMEOUT = 300; //300 milliseconds
    private final String BRANDS_PATH;
    private final String METRICS_PATH;
    private AsyncHttpClient asyncHttpClient;

    private Comparator<? super BrandSummary> brandSummaryComparator = (b1, b2) -> {
        return b2.getMetrics().getImpression() - b1.getMetrics().getImpression();
    };

    private Comparator<? super BrandHourSummary> brandHourSummaryComparator = (b1, b2) -> {
        int tmp = b2.getDateTime().compareTo(b1.getDateTime());

        if(tmp != 0)
            return tmp;

        return b1.getBrandName().compareTo(b2.getBrandName());
    };

    private Comparator<? super HourMetricsSummary> hourMetricsSummaryComparator = (h1, h2) -> {
        return h1.getDateTime().compareTo(h2.getDateTime());
    };


    @Autowired
    public AggregationService(@Value("${backend_api.base_path}") String baseApiPath, HttpClientFactory httpClientFactory)
    {
        this.BRANDS_PATH = baseApiPath + "/v1/brands";
        this.METRICS_PATH = baseApiPath + "/v1/metrics";
        asyncHttpClient = httpClientFactory.create();
    }


    public CompletableFuture<List<BrandSummary>> getBrandsSummary()
    {
        CompletableFuture<Map<Integer, BrandSummary>> brandSummaryFuture = getApiBrands().thenCompose(brandList -> {
            try {
                Map<Integer, BrandSummary> brandSummaries = new HashMap<>();

                brandList.forEach(apiBrand -> {
                    BrandSummary brandSummary = new BrandSummary();
                    brandSummary.setBrandId(apiBrand.getId());
                    brandSummary.setBrandName(apiBrand.getName());

                    brandSummaries.put(apiBrand.getId(), brandSummary);
                });

                return CompletableFuture.completedFuture(brandSummaries);
            }
            catch (Exception e)
            {
                return CompletableFuture.completedFuture(new HashMap<>());
            }
        });

        ParallelMetricsAggregator<Map<Integer, BrandSummary>> parallelMetricsAggregator = new ParallelMetricsAggregator<>();

        CompletableFuture<Map<Integer, BrandSummary>> brandSummaryAggregatedFuture;
        brandSummaryAggregatedFuture = parallelMetricsAggregator.run(brandSummaryFuture, (brandMap, apiBrandMetrics) ->
        {
            BrandSummary brandSummary = brandMap.get(apiBrandMetrics.getBrandId());

            if (brandSummary == null)
                return;

            Metrics summaryMetrics = brandSummary.getMetrics();
            apiBrandMetrics.getMetrics().forEach(apiBrandMetric -> {
                sum(summaryMetrics, apiBrandMetric);
            });
        });

        return brandSummaryAggregatedFuture.thenCompose(brandSummary -> {
            List<BrandSummary> brandSummaryList = new ArrayList<>(brandSummary.values());
            brandSummaryList.sort(brandSummaryComparator);
            return CompletableFuture.completedFuture(brandSummaryList);
        });
    }


    public CompletableFuture<List<BrandHourSummary>> getBrandHourSummary()
    {
        CompletableFuture<Map<Integer, String>> brandNameFuture = getApiBrands().thenCompose(brandList -> {
            Map<Integer, String> brandName = new HashMap<>();
            brandList.forEach(apiBrand -> {
                brandName.put(apiBrand.getId(), apiBrand.getName());
            });

            return CompletableFuture.completedFuture(brandName);
        });

        CompletableFuture<Map<String, BrandHourSummary>> brandHourMetricsSummaryFuture = CompletableFuture.completedFuture(new HashMap<>());
        ParallelMetricsAggregator<Map<String, BrandHourSummary>> parallelMetricsAggregator = new ParallelMetricsAggregator<>();

        brandHourMetricsSummaryFuture = parallelMetricsAggregator.run(brandHourMetricsSummaryFuture, (brandMap, apiBrandMetrics) ->
        {
            Date roundedDate = DateUtils.getRoundedHourDate(apiBrandMetrics.getDateTime());
            String key = apiBrandMetrics.getBrandId() + roundedDate.toString();
            BrandHourSummary brandHourSummary = brandMap.get(key);

            if (brandHourSummary == null)
            {
                brandHourSummary = new BrandHourSummary();
                brandHourSummary.setBrandId(apiBrandMetrics.getBrandId());
                brandHourSummary.setDateTime(roundedDate);
                brandHourSummary.setMetrics(new Metrics());
                brandMap.put(key, brandHourSummary);
            }

            Metrics summaryHourMetrics = brandHourSummary.getMetrics();

            for(ApiBrandMetrics.Metrics apiBrandMetric: apiBrandMetrics.getMetrics())
                sum(summaryHourMetrics, apiBrandMetric);
        });

        return brandHourMetricsSummaryFuture
                .thenCombine(brandNameFuture, (sumMetrics, brandNames) -> {
                    List<BrandHourSummary> result = new ArrayList<>();

                    sumMetrics.forEach((key, value) -> {
                        value.setBrandName( brandNames.get(value.getBrandId()) );
                        result.add(value);
                    });

                    result.sort(brandHourSummaryComparator);

                    return result;
                });
    }


    public CompletableFuture<MetricsSummary> getMetricsSummary()
    {
        CompletableFuture<MetricsSummary> metricsSummaryFuture = CompletableFuture.completedFuture(new MetricsSummary());
        ParallelMetricsAggregator<MetricsSummary> parallelMetricsAggregator = new ParallelMetricsAggregator<>();

        metricsSummaryFuture = parallelMetricsAggregator.run(metricsSummaryFuture, (metricsSummary, apiBrandMetrics) -> {
            for(ApiBrandMetrics.Metrics apiBrandMetric: apiBrandMetrics.getMetrics())
                sum(metricsSummary, apiBrandMetric);

        });

        return  metricsSummaryFuture;
    }


    public CompletableFuture<List<HourMetricsSummary>> getHourMetricsSummary()
    {
        CompletableFuture<Map<String, HourMetricsSummary>> hourMetricsSummaryMapFuture = CompletableFuture.completedFuture(new HashMap<>());
        ParallelMetricsAggregator<Map<String, HourMetricsSummary>> parallelMetricsAggregator = new ParallelMetricsAggregator<>();

        hourMetricsSummaryMapFuture = parallelMetricsAggregator.run(hourMetricsSummaryMapFuture, (hourMetricsSummaryMap, apiBrandsMetrics) -> {
            Date roundedDate = DateUtils.getRoundedHourDate(apiBrandsMetrics.getDateTime());
            String key = roundedDate.toString();

            HourMetricsSummary hourMetricsSummary = hourMetricsSummaryMap.get(key);

            if(hourMetricsSummary == null)
            {
                hourMetricsSummary = new HourMetricsSummary();
                hourMetricsSummary.setDateTime(roundedDate);
                hourMetricsSummary.setMetrics(new Metrics());
                hourMetricsSummaryMap.put(key, hourMetricsSummary);
            }

            Metrics hourMetrics = hourMetricsSummary.getMetrics();

            for(ApiBrandMetrics.Metrics apiBrandMetric : apiBrandsMetrics.getMetrics())
                sum(hourMetrics, apiBrandMetric);
        });

        return hourMetricsSummaryMapFuture.thenCompose(hourMetricsSummaryMap -> {
            List<HourMetricsSummary> result = new ArrayList<>(hourMetricsSummaryMap.values());
            result.sort(hourMetricsSummaryComparator);
            return CompletableFuture.completedFuture(result);
        });
    }


    private CompletableFuture<ApiBrandMetrics> getApiBrandMetricsResponse()
    {
        Request request = get(METRICS_PATH)
                .setRequestTimeout(REQUEST_TIMEOUT)
                .build();

        return asyncHttpClient
                .prepareRequest(request)
                .execute()
                .toCompletableFuture()
                .exceptionally(response -> {
                    return null;
                })
                .thenCompose(response -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        ApiBrandMetrics brandMetricsResponse = objectMapper.readValue(response.getResponseBody(), ApiBrandMetrics.class);
                        return CompletableFuture.completedFuture(brandMetricsResponse);
                    }
                    catch (Exception e)
                    {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    /*
     * This possibly this can be cached
     */
    private CompletableFuture<List<ApiBrand>> getApiBrands()
    {
        return asyncHttpClient
                .prepareGet(BRANDS_PATH)
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> {
                    try {
                        List<ApiBrand> apiBrands;
                        ObjectMapper objectMapper = new ObjectMapper();
                        apiBrands = objectMapper.readValue(response.getResponseBody(), objectMapper.getTypeFactory().constructCollectionType(
                                List.class, ApiBrand.class));

                        return CompletableFuture.completedFuture(apiBrands);
                    }
                    catch (Exception e)
                    {
                        return CompletableFuture.completedFuture(new ArrayList<ApiBrand>());
                    }
                });
    }

    private void sum(Metrics metrics, ApiBrandMetrics.Metrics apiMetrics)
    {
        switch (apiMetrics.getMetric()) {
            case "impression":
                metrics.setImpression(metrics.getImpression() + apiMetrics.getCount());
                break;
            case "click":
                metrics.setClick(metrics.getClick() + apiMetrics.getCount());
                break;
            case "interaction":
                metrics.setInteraction(metrics.getInteraction() + apiMetrics.getCount());
        }
    }
}
