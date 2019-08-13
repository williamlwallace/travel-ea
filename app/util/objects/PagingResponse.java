package util.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

public class PagingResponse<K> {

    private Collection<K> data;
    private Integer draw;
    private Integer recordsTotal;
    private Integer recordsFiltered;
    private String error;

    @JsonCreator
    public PagingResponse(Collection<K> data, Integer draw, Integer recordsTotal,
        Integer recordsFiltered, String error) {
        this.data = data;
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.error = error;
    }

    public Collection<K> getData() {
        return data;
    }

    public Integer getDraw() {
        return draw;
    }

    public Integer getRecordsTotal() {
        return recordsTotal;
    }

    public Integer getRecordsFiltered() {
        return recordsFiltered;
    }

    public String getError() {
        return error;
    }
}

