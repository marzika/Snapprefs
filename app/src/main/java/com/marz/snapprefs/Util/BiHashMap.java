package com.marz.snapprefs.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by stirante on 2015-04-10.
 */
public class BiHashMap<K, V> implements Map<K, V> {

    private HashMap<K, V> byKey = new HashMap<K, V>();
    private HashMap<V, K> byValue = new HashMap<V, K>();

    @Override
    public int size() {
        return byKey.size();
    }

    @Override
    public boolean isEmpty() {
        return byKey.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return byKey.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return byKey.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return byKey.get(key);
    }

    @Override
    public V put(K key, V value) {
        byValue.put(value, key);
        return byKey.put(key, value);
    }

    @Override
    public V remove(Object key) {
        byValue.remove(byKey.get(key));
        return byKey.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        byKey.putAll(m);
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            byValue.put(e.getValue(), e.getKey());
        }
    }

    @Override
    public void clear() {
        byKey.clear();
        byValue.clear();
    }

    @Override
    public Set<K> keySet() {
        return byKey.keySet();
    }

    @Override
    public Collection<V> values() {
        return byKey.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return byKey.entrySet();
    }

    public K getByValue(Object value) {
        return byValue.get(value);
    }

}