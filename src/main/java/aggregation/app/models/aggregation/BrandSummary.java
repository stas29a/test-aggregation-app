package aggregation.app.models.aggregation;

import java.util.Objects;

public class BrandSummary
{
    private int brandId;

    private String brandName;

    private Metrics metrics = new Metrics();

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrandSummary that = (BrandSummary) o;
        return brandId == that.brandId &&
                Objects.equals(brandName, that.brandName) &&
                Objects.equals(metrics, that.metrics);
    }

    @Override
    public int hashCode() {

        return Objects.hash(brandId, brandName, metrics);
    }
}
