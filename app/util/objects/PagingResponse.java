package util.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;
import java.util.Collections;

public class PagingResponse<T> {

    private Collection<T> data;
    private Integer requestOrder;
    private Integer totalNumberPages;

    /**
     * Constructor to initialize a paging response object, this is the only chance to set values
     *
     * @param data The collection of data returned for the current page / pagesize
     * @param requestOrder The order this request was sent in, keeps track of which request is most recent
     * @param totalNumberPages Total number of pages to show available (how many more data sets there are)
     */
    @JsonCreator
    public PagingResponse(Collection<T> data, Integer requestOrder, Integer totalNumberPages) {
        this.data = data;
        this.requestOrder = requestOrder;
        this.totalNumberPages = totalNumberPages;
    }

    public Collection<T> getData() {
        return Collections.unmodifiableCollection(data);
    }

    public Integer getTotalNumberPages() {
        return totalNumberPages;
    }
}

