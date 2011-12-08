package rarto360;


import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Localize from resource.
 * <p/>
 * Localizes a text from a given class. Searches for the "strings_'locale'.properties" in the class folder.
 *
 * @author Jesenko
 * @version $Id: LocalizeFromResource.java,v 1.12 2010-11-23 01:08:02 snake Exp $
 * @since Mar 23, 2010, 12:10:19 AM
 */
public class LocalizeFromResource {
    private static Logger logger = Logger.getLogger(LocalizeFromResource.class);
    private static Map<Class, Map<String, String>> map = new HashMap<Class, Map<String, String>>();


    public static String getString(Object objectWithClass, String key) {
        return getString(objectWithClass.getClass(), key);
    }

    /**
     * Localizes a string.
     *
     * @param classToLocalize the class to localize from
     * @param key             the key of the text
     * @param locale          the locale
     * @return the localized text if one is found, otherwise the key itself is returned
     */
    public static String getString(Class classToLocalize, String key, Locale locale) {
        try {
            String cached = get(classToLocalize, key);
            if (cached != null) {
                return cached;
            }
            ResourceBundle bundle =
                    ResourceBundle.getBundle("rarto360/strings", locale, ClassLoader.getSystemClassLoader());
            if (!bundle.getLocale().equals(locale)) {
                throw new Exception();
            }
            String key2 = classToLocalize.getSimpleName() + "." + key;
            String value = bundle.getString(key2);
            put(classToLocalize, key, value);
            return value;
        } catch (Exception ignored) {
            logger.error("key not found: " + key + " in classpath: " + classToLocalize);
            return key;
        }
    }

    private static void put(Class clazz, String key, String value) {
        if (map.get(clazz) == null) {
            map.put(clazz, new HashMap<String, String>());
        }
        map.get(clazz).put(key, value);
    }

    private static String get(Class clazz, String key) {
        if (map.get(clazz) != null) {
            return map.get(clazz).get(key);
        }
        return null;
    }

    /**
     * Localizes a string with the locale being used in the application.
     *
     * @param classToLocalize the class to localize from
     * @param key             the key
     * @return the localized text if one is found, otherwise the key itself is returned
     */
    public static String getString(Class classToLocalize, String key) {
        return getString(classToLocalize, key, Application.getLocale());
    }

}
