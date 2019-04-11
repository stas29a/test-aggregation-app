package aggregation.app.models.aggregation;

import java.util.Objects;

public class Metrics {
    private int impression;

    private int click;

    private int interaction;

    public int getImpression() {
        return impression;
    }

    public void setImpression(int impression) {
        this.impression = impression;
    }

    public int getClick() {
        return click;
    }

    public void setClick(int click) {
        this.click = click;
    }

    public int getInteraction() {
        return interaction;
    }

    public void setInteraction(int interaction) {
        this.interaction = interaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metrics metrics = (Metrics) o;
        return impression == metrics.impression &&
                click == metrics.click &&
                interaction == metrics.interaction;
    }

    @Override
    public int hashCode() {

        return Objects.hash(impression, click, interaction);
    }
}
