package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxy extends LocaleProxy {
    private Map<Method, ConstantDescriptor> descriptors = new HashMap<Method, ConstantDescriptor>();

    protected ConstantsProxy(Class<? extends LocalizableResource> cls, Logger log) {
        super(cls, log);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ConstantDescriptor desc = getDescriptor(method);
        Properties properties = getProperties();

        Object returnValue;
        if (properties.containsKey(desc.key)) {
            returnValue = properties.get(desc.key);
            if (returnValue == null) {
                returnValue = properties.getProperty(desc.key); // redirect to defaults
            }
        } else {
            if (desc.defaultValue == null) {
                log.error(String.format("Unlocalized key '%s' for locale '%s'", desc.key, getLocale()));
            }
            returnValue = desc.defaultValue;
        }

        if (returnValue instanceof String && args != null) {
            return MessageFormat.format(returnValue.toString(), args);
        }

        if (args != null) {
            throw new IllegalArgumentException();
        }

        return returnValue;
    }

    @Override
    protected Properties createBundleProperties(final Class<? extends LocalizableResource> clazz) {
        return new ConstantsProperties(clazz);
    }

    private synchronized ConstantDescriptor getDescriptor(Method method) {
        ConstantDescriptor desc = descriptors.get(method);
        if (desc == null) {
            desc = new ConstantDescriptor();
            desc.key = getKey(method);
            {
                Constants.DefaultBooleanValue annotation = method.getAnnotation(Constants.DefaultBooleanValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultDoubleValue annotation = method.getAnnotation(Constants.DefaultDoubleValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultFloatValue annotation = method.getAnnotation(Constants.DefaultFloatValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultIntValue annotation = method.getAnnotation(Constants.DefaultIntValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultStringArrayValue annotation = method.getAnnotation(Constants.DefaultStringArrayValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultStringMapValue annotation = method.getAnnotation(Constants.DefaultStringMapValue.class);
                if (annotation != null) {
                    String[] values = annotation.value();
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; (i + 1) < values.length; i += 2) {
                        map.put(values[i], values[i + 1]);
                    }
                    desc.defaultValue = map;
                }
            }
            {
                Constants.DefaultStringValue annotation = method.getAnnotation(Constants.DefaultStringValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            descriptors.put(method, desc);
        }
        return desc;
    }

    private class ConstantDescriptor {
        String key;
        Object defaultValue;
    }
}
