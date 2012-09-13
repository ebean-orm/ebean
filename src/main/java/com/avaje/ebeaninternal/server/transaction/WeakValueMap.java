/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.transaction;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A Weak value map designed for use with DefaultPersistenceContext.
 * <p>
 * This provides the mechanism where entries in the persistence context will be
 * automatically removed when they are not referenced externally.
 * </p>
 * 
 * @author mario, rbygrave
 */
public class WeakValueMap<K, V> {

    protected final ReferenceQueue<V> refQueue = new ReferenceQueue<V>();

    /**
     * Backing map.
     */
    private final Map<K, WeakReferenceWithKey<K, V>> backing;

    /**
     * Hold the key with the value for expunge purposes.
     */
    private static class WeakReferenceWithKey<K, V> extends WeakReference<V> {

        private final K key;

        public WeakReferenceWithKey(K key, V referent, ReferenceQueue<? super V> q) {
            super(referent, q);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }

    public WeakValueMap() {
        this.backing = new HashMap<K, WeakReferenceWithKey<K, V>>();
    }

    private WeakReferenceWithKey<K, V> createReference(K key, V value) {
        return new WeakReferenceWithKey<K, V>(key, value, refQueue);
    }

    @SuppressWarnings({ "rawtypes" })
    private void expunge() {

        Reference ref;

        while ((ref = refQueue.poll()) != null) {
            backing.remove(((WeakReferenceWithKey) ref).getKey());
        }
    }

    /**
     * Put the key value pair if there is not already a matching entry. If there
     * is an existing entry then return that instead.
     */
    public Object putIfAbsent(K key, V value) {
        expunge();

        Reference<V> ref = backing.get(key);
        if (ref != null) {
            V existingValue = ref.get();
            if (existingValue != null) {
                // it is not absent
                return existingValue;
            }
        }
        // put the new value and return null
        // indicating the put was successful
        backing.put(key, createReference(key, value));
        return null;
    }

    public void put(K key, V value) {
        expunge();

        backing.put(key, createReference(key, value));
    }

    public V get(K key) {
        expunge();

        Reference<V> v = backing.get(key);
        return v == null ? null : v.get();
    }

    public int size() {
        expunge();

        return backing.size();
    }

    public boolean isEmpty() {
        expunge();

        return backing.isEmpty();
    }

    public boolean containsKey(Object key) {
        expunge();

        return backing.containsKey(key);
    }

    public V remove(K key) {
        expunge();

        Reference<V> v = backing.remove(key);
        return v == null ? null : v.get();
    }

    public void clear() {
        expunge();
        backing.clear();
        expunge();
    }

    public String toString() {
        expunge();

        return backing.toString();
    }

}
