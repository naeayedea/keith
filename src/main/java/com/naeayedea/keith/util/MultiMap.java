package com.naeayedea.keith.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class MultiMap<E, K> extends HashMap<E, K> {

    /*
     * This is a wrapper class for easily adding multiple keys corresponding to the same value in a map
     * it simply hides the process of doing map.put("test", object), map.put("test2", object") etc. and instead allows
     * the user to do multimap.putAll(collection, object) and all elements inside the collection would be added as keys
     * to the hashmap for the object
     */

    public MultiMap() {
        super();
    }

    public void putAll(Collection<E> collection, K object) {
        for (E item : collection) {
            this.put(item, object);
        }
    }

    /*
     * Removes all keys corresponding to the value entered.
     */
    public void removeWithValue(K value) {
        //remove(value) would only remove the first occurrence
        this.values().removeAll(Collections.singleton(value));
    }

    /*
     * Removes the key, value pair corresponding to the key and *all* other pairs with the same value returned by the
     * key passed as a parameter.
     */
    public void removeWithKey(E key) {
        K value = this.get(key);
        if (value != null) {
            removeWithValue(value);
        }
    }
}
