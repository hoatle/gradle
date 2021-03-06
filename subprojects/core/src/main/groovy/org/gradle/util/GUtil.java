/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.util;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.UncheckedIOException;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * @author Hans Dockter
 */
public class GUtil {
    public static <T extends Collection> T flatten(Object[] elements, T addTo, boolean flattenMaps) {
        return flatten(asList(elements), addTo, flattenMaps);
    }

    public static <T extends Collection> T flatten(Object[] elements, T addTo) {
        return flatten(asList(elements), addTo);
    }

    public static <T extends Collection> T flatten(Collection elements, T addTo) {
        return flatten(elements, addTo, true);
    }

    public static <T extends Collection> T flattenElements(Object... elements) {
        Collection<T> out = new LinkedList<T>();
        flatten(elements, out, true);
        return (T) out;
    }

    public static <T extends Collection> T flatten(Collection elements, T addTo, boolean flattenMapsAndArrays) {
        return flatten(elements, addTo, flattenMapsAndArrays, flattenMapsAndArrays);
    }

    public static <T extends Collection> T flatten(Collection elements, T addTo, boolean flattenMaps, boolean flattenArrays) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof Collection) {
                flatten((Collection) element, addTo, flattenMaps, flattenArrays);
            } else if ((element instanceof Map) && flattenMaps) {
                flatten(((Map) element).values(), addTo, flattenMaps, flattenArrays);
            } else if ((element.getClass().isArray()) && flattenArrays) {
                flatten(asList((Object[]) element), addTo, flattenMaps, flattenArrays);
            } else {
                addTo.add(element);
            }
        }
        return addTo;
    }

    /**
     * Flattens input collections (including arrays *but* not maps).
     * If input is not a collection wraps it in a collection and returns it.
     * @param input any object
     * @return collection of flattened input or single input wrapped in a collection.
     */
    public static Collection collectionize(Object input) {
        if (input == null) {
            return emptyList();
        } else if (input instanceof Collection) {
            Collection out = new LinkedList();
            flatten((Collection) input, out, false, true);
            return out;
        } else if (input.getClass().isArray()) {
            Collection out = new LinkedList();
            flatten(asList((Object[]) input), out, false, true);
            return out;
        } else {
            return asList(input);
        }
    }

    public static List flatten(Collection elements, boolean flattenMapsAndArrays) {
        return flatten(elements, new ArrayList(), flattenMapsAndArrays);
    }

    public static List flatten(Collection elements) {
        return flatten(elements, new ArrayList());
    }

    public static String join(Collection self, String separator) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;

        if (separator == null) {
            separator = "";
        }

        for (Object value : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(value.toString());
        }
        return buffer.toString();
    }

    public static String join(Object[] self, String separator) {
        return join(asList(self), separator);
    }

    public static List<String> prefix(String prefix, Collection<String> strings) {
        List<String> prefixed = new ArrayList<String>();
        for (String string : strings) {
            prefixed.add(prefix + string);
        }
        return prefixed;
    }

    public static boolean isTrue(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Collection) {
            return ((Collection) object).size() > 0;
        } else if (object instanceof String) {
            return ((String) object).length() > 0;
        }
        return true;
    }

    public static <T> T elvis(T object, T defaultValue) {
        return isTrue(object) ? object : defaultValue;
    }

    public static <T> Set<T> addSets(Iterable<? extends T>... sets) {
        return addToCollection(new HashSet<T>(), sets);
    }
    
    public static <T> Set<T> toSet(Iterable<? extends T> elements) {
        return addToCollection(new HashSet<T>(), elements);
    }

    public static <T> List<T> addLists(Iterable<? extends T>... lists) {
        return addToCollection(new ArrayList<T>(), lists);
    }

    public static <T> List<T> toList(Iterable<? extends T> list) {
        return addToCollection(new ArrayList<T>(), list);
    }

    public static <V, T extends Collection<? super V>> T addToCollection(T dest, Iterable<? extends V>... srcs) {
        for (Iterable<? extends V> src : srcs) {
            for (V v : src) {
                dest.add(v);
            }
        }
        return dest;
    }

    public static Comparator<String> caseInsensitive() {
        return new Comparator<String>() {
            public int compare(String o1, String o2) {
                int diff = o1.compareToIgnoreCase(o2);
                if (diff != 0) {
                    return diff;
                }
                return o1.compareTo(o2);
            }
        };
    }

    public static Map addMaps(Map map1, Map map2) {
        HashMap map = new HashMap();
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    public static void addToMap(Map<String, String> dest, Map<?, ?> src) {
        for (Map.Entry<?, ?> entry : src.entrySet()) {
            dest.put(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    public static Properties loadProperties(File propertyFile) {
        try {
            FileInputStream inputStream = new FileInputStream(propertyFile);
            try {
                return loadProperties(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Properties loadProperties(URL url) {
        try {
            return loadProperties(url.openStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Properties loadProperties(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    public static void saveProperties(Properties properties, File propertyFile) {
        try {
            FileOutputStream propertiesFileOutputStream = new FileOutputStream(propertyFile);
            try {
                properties.store(propertiesFileOutputStream, null);
            } finally {
                propertiesFileOutputStream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map map(Object... objects) {
        Map map = new HashMap();
        assert objects.length % 2 == 0;
        for (int i = 0; i < objects.length; i += 2) {
            map.put(objects[i], objects[i + 1]);
        }
        return map;
    }

    public static String toString(Iterable<?> names) {
        Formatter formatter = new Formatter();
        boolean first = true;
        for (Object name : names) {
            if (first) {
                formatter.format("'%s'", name);
                first = false;
            } else {
                formatter.format(", '%s'", name);
            }
        }
        return formatter.toString();
    }

    /**
     * Converts an arbitrary string to a camel-case string which can be used in a Java identifier. Eg, with_underscores -> withUnderscores
     */
    public static String toCamelCase(CharSequence string) {
        if (string == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("[^\\w]+").matcher(string);
        int pos = 0;
        while (matcher.find()) {
            builder.append(StringUtils.capitalize(string.subSequence(pos, matcher.start()).toString()));
            pos = matcher.end();
        }
        builder.append(StringUtils.capitalize(string.subSequence(pos, string.length()).toString()));
        return builder.toString();
    }

    /**
     * Converts an arbitrary string to upper case identifier with words separated by _. Eg, camelCase -> CAMEL_CASE
     */
    public static String toConstant(CharSequence string) {
        if (string == null) {
            return null;
        }
        return toWords(string, '_').toUpperCase();
    }

    /**
     * Converts an arbitrary string to space-separated words. Eg, camelCase -> camel case, with_underscores -> with underscores
     */
    public static String toWords(CharSequence string) {
        return toWords(string, ' ');
    }

    public static String toWords(CharSequence string, char separator) {
        if (string == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        Matcher matcher = Pattern.compile("(\\p{Upper}*)(\\p{Lower}*)").matcher(string);
        while (pos < string.length()) {
            matcher.find(pos);
            if (matcher.end() == pos) {
                // Not looking at a match
                pos++;
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            String group1 = matcher.group(1).toLowerCase();
            String group2 = matcher.group(2);
            if (group2.length() == 0) {
                builder.append(group1);
            } else {
                if (group1.length() > 1) {
                    builder.append(group1.substring(0, group1.length() - 1));
                    builder.append(separator);
                    builder.append(group1.substring(group1.length() - 1));
                } else {
                    builder.append(group1);
                }
                builder.append(group2);
            }
            pos = matcher.end();
        }

        return builder.toString();
    }

    public static byte[] serialize(Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return outputStream.toByteArray();
    }

    public static <T> Comparator<T> last(final Comparator<? super T> comparator, final T lastValue) {
        return new Comparator<T>() {
            public int compare(T o1, T o2) {
                boolean o1Last = comparator.compare(o1, lastValue) == 0;
                boolean o2Last = comparator.compare(o2, lastValue) == 0;
                if (o1Last && o2Last) {
                    return 0;
                }
                if (o1Last && !o2Last) {
                    return 1;
                }
                if (!o1Last && o2Last) {
                    return -1;
                }
                return comparator.compare(o1, o2);
            }
        };
    }
}
