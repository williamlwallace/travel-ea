package util.objects;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A pair used for return objects in a paginated way
 * @param <K> is the data required
 * @param <V> totalPageCount of the data being returned
 */
public class PagePair<K, V> {

    private K data;
    private V totalPageCount;

    @JsonCreator
    public PagePair(K key, V value) {
        this.data = key;
        this.totalPageCount = value;
    }

    /**
     * Gets the data to put on the page
     *
     * @return The data in the pair
     */
    public K getData() {
        return data;
    }

    /**
     * Gets the total number of pages the data takes up
     *
     * @return total pages
     */
    public V getTotalPageCount() {
        return totalPageCount;
    }
}