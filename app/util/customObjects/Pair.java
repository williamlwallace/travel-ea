package util.customObjects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Pair<K, V> {

    private K key;
    private V value;

    @JsonCreator
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
