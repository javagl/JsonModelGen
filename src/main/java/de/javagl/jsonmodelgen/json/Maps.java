/*
 * JsonModelGen - Model Generation from JSON Schema 
 *
 * Copyright (c) 2015-2016 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jsonmodelgen.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods related to maps
 */
public class Maps
{
    /**
     * Returns an unmodifiable map that maps the keys of the given map to
     * unmodifiable views on the values of the given map.
     * 
     * @param map The map
     * @return The deeply unmodifiable result
     */
    static <K, V> Map<K, List<V>> deepUnmodifiable(Map<K, List<V>> map)
    {
        Map<K, List<V>> result = new LinkedHashMap<K, List<V>>();
        for (Entry<K, List<V>> entry : map.entrySet())
        {
            K key = entry.getKey();
            List<V> value = entry.getValue();
            result.put(key, Collections.unmodifiableList(value));
        }
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private Maps()
    {
        // Private constructor to prevent instantiation
    }

}
