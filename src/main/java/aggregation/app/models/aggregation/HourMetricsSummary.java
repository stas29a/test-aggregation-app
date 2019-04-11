package aggregation.app.models.aggregation;

import java.util.Date;
import java.util.Objects;

public class HourMetricsSummary
{
    private Date dateTime;

    private Metrics metrics;

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
        HourMetricsSummary that = (HourMetricsSummary) o;
        return Objects.equals(dateTime, that.dateTime) &&
                Objects.equals(metrics, that.metrics);
    }

    @Override
    public int hashCode() {

        return Objects.hash(dateTime, metrics);
    }
}
