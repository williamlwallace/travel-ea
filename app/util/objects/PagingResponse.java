package util.objects;

import java.util.List;

public class PagingResponse<T> {

    public List<T> data;
    public Integer requestOrder;
    public Integer totalNumberPages;

    /**
     * Constructor to initialize a paging response object, this is the only chance to set values
     *
     * @param data The collection of data returned for the current page / pagesize
     * @param requestOrder The order this request was sent in, keeps track of which request is most recent
     * @param totalNumberPages Total number of pages to show available (how many more data sets there are)
     */
    public PagingResponse(List<T> data, Integer requestOrder, Integer totalNumberPages) {
        this.data = data;
        this.requestOrder = requestOrder;
        this.totalNumberPages = totalNumberPages;
    }

    /**
     * Empty constructor for getting class when deserializing
     */
    public PagingResponse() { }
}

