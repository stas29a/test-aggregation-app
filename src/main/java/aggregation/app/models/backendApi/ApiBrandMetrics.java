package aggregation.app.models.backendApi;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ApiBrandMetrics
{
    public static class Metrics {
        private String metric;
        private int count;

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metrics metrics = (Metrics) o;
            return count == metrics.count &&
                    Objects.equals(metric, metrics.metric);
        }

        @Override
        public int hashCode() {

            return Objects.hash(metric, count);
        }
    }

    public static class BrandMetrics {
        private int brandId;

        private List<Metrics> metrics;

        private Date dateTime;

        public int getBrandId() {
            return brandId;
        }

        public void setBrandId(int brandId) {
            this.brandId = brandId;
        }

        public List<Metrics> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<Metrics> metrics) {
            this.metrics = metrics;
        }

        public Date getDateTime() {
            return dateTime;
        }

        public void setDateTime(Date dateTime) {
            this.dateTime = dateTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BrandMetrics metrics1 = (BrandMetrics) o;
            return brandId == metrics1.brandId &&
                    Objects.equals(metrics, metrics1.metrics) &&
                    Objects.equals(dateTime, metrics1.dateTime);
        }

        @Override
        public int hashCode() {

            return Objects.hash(brandId, metrics, dateTime);
        }
    }

    private int size;

    private List<BrandMetrics> brandMetrics;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<BrandMetrics> getBrandMetrics() {
        return brandMetrics;
    }

    public void setBrandMetrics(List<BrandMetrics> brandMetrics) {
        this.brandMetrics = brandMetrics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiBrandMetrics that = (ApiBrandMetrics) o;
        return size == that.size &&
                Objects.equals(brandMetrics, that.brandMetrics);
    }

    @Override
    public int hashCode() {

        return Objects.hash(size, brandMetrics);
    }
}
