package aggregation.app.models.backendApi;

import java.util.Objects;

public class ApiBrand
{
    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiBrand apiBrand = (ApiBrand) o;
        return id == apiBrand.id &&
                Objects.equals(name, apiBrand.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }
}
