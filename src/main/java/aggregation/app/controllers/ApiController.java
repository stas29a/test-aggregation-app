package aggregation.app.controllers;

import aggregation.app.models.aggregation.BrandHourSummary;
import aggregation.app.models.aggregation.BrandSummary;
import aggregation.app.models.aggregation.HourMetricsSummary;
import aggregation.app.models.aggregation.MetricsSummary;
import aggregation.app.services.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
public class ApiController
{
    private AggregationService aggregationService;

    @Autowired
    public ApiController(AggregationService aggregationService)
    {
        this.aggregationService = aggregationService;
    }

    @RequestMapping("brands-summary")
    public CompletableFuture<List<BrandSummary>> getBrandsSummary()
    {
        return aggregationService
                .getBrandsSummary();
    }

    @RequestMapping("brands-hour-summary")
    public CompletableFuture<List<BrandHourSummary>> getBrandsHourSummary()
    {
        return aggregationService.getBrandHourSummary();
    }

    @RequestMapping("metrics-summary")
    public CompletableFuture<MetricsSummary> getMetricsSummary()
    {
        return aggregationService.getMetricsSummary();
    }

    @RequestMapping("metrics-hour-summary")
    public CompletableFuture<List<HourMetricsSummary>> getHourMetricsSummary()
    {
        return aggregationService.getHourMetricsSummary();
    }
}
