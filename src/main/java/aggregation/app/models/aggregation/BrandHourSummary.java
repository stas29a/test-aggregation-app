package aggregation.app.models.aggregation;

import java.util.Date;
import java.util.Objects;

public class BrandHourSummary
{
    private int brandId;

    private String brandName;

    private Date dateTime;

    private Metrics metrics;

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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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
        BrandHourSummary that = (BrandHourSummary) o;
        return brandId == that.brandId &&
                Objects.equals(brandName, that.brandName) &&
                Objects.equals(dateTime, that.dateTime) &&
                Objects.equals(metrics, that.metrics);
    }

    @Override
    public int hashCode() {

        return Objects.hash(brandId, brandName, dateTime, metrics);
    }
}
