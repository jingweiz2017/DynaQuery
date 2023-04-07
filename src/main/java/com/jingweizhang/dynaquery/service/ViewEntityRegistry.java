package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.exception.InvalidViewEntityException;
import com.jingweizhang.dynaquery.extension.ViewEntity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * Used to handle all view entity class to obtain its metadata for later process.
 *
 * @Author rocky.zhang on 2023/4/7
 */
public class ViewEntityRegistry {
    private final Map<Class<? extends ViewEntity>, Map<String, Class<?>>> viewEntityDictionary;
    public ViewEntityRegistry(String packageFullNamespace) {
        this.viewEntityDictionary = this.initializeViewEntityDictionary(packageFullNamespace);
    }

    private Map<Class<? extends ViewEntity>, Map<String, Class<?>>> initializeViewEntityDictionary(String packageFullNamespace) {
        Map<Class<? extends ViewEntity>, Map<String, Class<?>>> viewEntityDictionary = new HashMap<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(ViewEntity.class));

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageFullNamespace)) {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName(), false, classLoader);

                if (ViewEntity.class.isAssignableFrom(clazz)) {
                    viewEntityDictionary.put((Class<? extends ViewEntity>) clazz, this.extractEntityMetaData("", clazz, new HashMap<>()));
                }
            }
        } catch (Exception ex) {
            throw new InvalidViewEntityException(ex);
        }

        return viewEntityDictionary;
    }

    private Map<String, Class<?>> extractEntityMetaData(String root, Class<?> clazz, Map<String, Class<?>> map) {
        for (Field field : clazz.getDeclaredFields()) {
            String path = root == null || root.isEmpty() ? field.getName() : root + '.' + field.getName();
            if (this.isBuiltInType(field.getType())) {
                map.put(path, field.getType());
            } else {
                this.extractEntityMetaData(path, field.getType(), map);
            }
        }

        return map;
    }

    private boolean isBuiltInType(Class<?> type) {
        return type.isPrimitive() || type.equals(Boolean.class) || type.equals(Byte.class) ||
                type.equals(Character.class) || type.equals(Short.class) || type.equals(Integer.class) ||
                type.equals(Long.class) || type.equals(Float.class) || type.equals(Double.class) ||
                type.equals(String.class) || type.equals(Instant.class) || type.isEnum();
    }

    public Map<String, Class<?>> getEntityMetaData(Class<?> clazz) {
        return this.viewEntityDictionary.get(clazz);
    }

    // Check if the entity is registered
    public boolean isRegistered(Class<?> clazz) {
        return this.viewEntityDictionary.containsKey(clazz);
    }

    // Check the registry to see if the inputted view entity name is a supported view entity
    public boolean isSupported(String viewEntityName) {
        for (Class<?> clazz : this.viewEntityDictionary.keySet()) {
            if (clazz.getSimpleName().equals(viewEntityName)) {
                return true;
            }
        }

        return false;
    }

    public Class<? extends ViewEntity> getViewEntityClass(String viewEntityName) {
        for (Class<? extends ViewEntity> clazz : this.viewEntityDictionary.keySet()) {
            if (clazz.getSimpleName().equals(viewEntityName)) {
                return clazz;
            }
        }

        return null;
    }
}
