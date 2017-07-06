package org.geryon;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
class Maps {
    private Maps() {
    }

    public static <K, V> MapBuilder<K, V> newMap() {
        return new MapBuilder(new HashMap<>());
    }

    public static class MapBuilder<K, V> {
        private Map<K, V> map;

        public MapBuilder(Map<K, V> map) {
            this.map = map;
        }

        public MapBuilder<K, V> put(K key, V value) {
            this.map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}
