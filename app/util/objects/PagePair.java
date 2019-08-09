package util.objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PagePair<K, V> {

    private K data;
    private V totalPageCount;

    @JsonCreator
    public PagePair(K key, V value) {
        this.data = key;
        this.totalPageCount = value;
    }

    public K getData() {
        return data;
    }

    public V getTotalPageCount() {
        return totalPageCount;
    }
}