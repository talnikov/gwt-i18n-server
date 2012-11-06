package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Properties aware of GWT map entries. During the load, property entries are transformed
 * and converted according to the definition in the resource interface. Created objects
 * can be later accessed via {@link Hashtable} interface, i.e. {@link #get(Object)} method.
 *
 * @author Andrey Talnikov <a.v.talnikov@gmail.com>
 */
public class ConstantsProperties extends Properties {

    private static class MapEntryDescription {
        public final String entryName;
        public final List<String> mapKeys;

        private MapEntryDescription(final String entryName, final List<String> mapKeys) {
            this.entryName = entryName;
            this.mapKeys = mapKeys;
        }
    }

    private final Class<? extends LocalizableResource> cls;

    private boolean loading;

    private MapEntryDescription processedMapEntry;

    public ConstantsProperties(final Class<? extends LocalizableResource> cls) {
        this.cls = cls;
    }

    @Override
    public synchronized void load(final Reader reader) throws IOException {
        this.loading = true;
        try {
            super.load(reader);
        } finally {
            loading = false;
        }
    }

    @Override
    public synchronized void load(final InputStream inStream) throws IOException {
        this.loading = true;
        try {
            super.load(inStream);
        } finally {
            loading = false;
        }
    }

    @Override
    public synchronized Object put(final Object key, final Object value) {
        if (!loading || !(key instanceof String) || !(value instanceof String)) {
            return super.put(key, value);
        }

        return putDuringLoad((String) key, (String) value);
    }

    private Object putDuringLoad(final String key, final String value) {
        if (processedMapEntry != null) {
            if (processedMapEntry.mapKeys.contains(key)) {
                HashMap<String, String> processedMap = (HashMap<String, String>) get(processedMapEntry.entryName);
                processedMap.put(key, value);

                return null;
            } else {
                processedMapEntry = null;
            }
        }

        if (isMapTypedKey(key)) {
            processedMapEntry = new MapEntryDescription(key, Arrays.asList(value.split("\\s*(?<!\\\\),\\s*")));
            return super.put(key, new HashMap<String, String>());
        }

        return super.put(key, cast(key, value));
    }

    protected Object cast(String key, String value) {
        try {
            Class target = cls.getMethod(key).getReturnType();

            if (target.isAssignableFrom(Boolean.TYPE) || target.isAssignableFrom(Boolean.class)) {
                return Boolean.valueOf(value);
            } else if (target.isAssignableFrom(Double.TYPE) || target.isAssignableFrom(Double.class)) {
                return Double.valueOf(value);
            } else if (target.isAssignableFrom(Float.TYPE) || target.isAssignableFrom(Float.class)) {
                return Float.valueOf(value);
            } else if (target.isAssignableFrom(Integer.TYPE) || target.isAssignableFrom(Integer.class)) {
                return Integer.valueOf(value);
            } else if (target.isArray() && target.getComponentType().isAssignableFrom(String.class)) {
                return value.split("\\s*(?<!\\\\),\\s*");
            }
        } catch (NoSuchMethodException e) {
        }

        return value;
    }

    private boolean isMapTypedKey(final String key) {
        try {
            return cls.getMethod(key).getReturnType().isAssignableFrom(Map.class);
        } catch (NoSuchMethodException e) {
        }
        return false;
    }
}
