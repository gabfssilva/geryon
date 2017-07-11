package org.geryon;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
class Maps {
    private Maps() {
    }

    static <K, V> MapBuilder<K, V> newMap() {
        return new MapBuilder(new HashMap<>());
    }

    static class MapBuilder<K, V> {
        private Map<K, V> map;

        MapBuilder(Map<K, V> map) {
            this.map = map;
        }

        MapBuilder<K, V> put(K key, V value) {
            this.map.put(key, value);
            return this;
        }

        Map<K, V> build() {
            return map;
        }
    }
}
