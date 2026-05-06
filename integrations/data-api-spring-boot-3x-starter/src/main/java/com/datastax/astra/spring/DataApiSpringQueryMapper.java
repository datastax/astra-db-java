package com.datastax.astra.spring;

import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.internal.reflection.CollectionBeanDefinition;
import com.datastax.astra.internal.reflection.EntityFieldDefinition;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Maps Spring Data query abstractions to Data API query objects.
 */
public final class DataApiSpringQueryMapper {

    private DataApiSpringQueryMapper() {
    }

    /**
     * Maps a Spring {@link Example} to a Data API {@link Filter}.
     *
     * @param example
     *      example to map
     * @param beanDefinition
     *      entity metadata
     * @param <T>
     *      entity type
     * @return
     *      mapped filter, or {@code null} when the example produces no predicate
     */
    public static <T> Filter mapExample(Example<T> example, CollectionBeanDefinition<T> beanDefinition) {
        if (example == null) {
            return null;
        }
        if (beanDefinition == null) {
            throw new IllegalArgumentException("Bean definition must not be null");
        }

        T probe = example.getProbe();
        if (probe == null) {
            return null;
        }

        ExampleMatcher matcher = example.getMatcher();
        List<Filter> filters = new ArrayList<>();

        for (EntityFieldDefinition field : beanDefinition.getFields().values()) {
            String fieldName = field.getName();
            if (matcher.isIgnoredPath(fieldName)) {
                continue;
            }
            Object value = readValue(probe, field);
            if (value == null) {
                continue;
            }

            if (value instanceof String stringValue) {
                String normalized = stringValue;
                if (matcher.isIgnoreCaseEnabled()) {
                    normalized = stringValue.toLowerCase();
                }
                ExampleMatcher.StringMatcher stringMatcher = resolveStringMatcher(matcher, fieldName);
                switch (stringMatcher) {
                    case DEFAULT:
                    case EXACT:
                        filters.add(Filters.eq(fieldName, normalized));
                        break;
                    case STARTING:
                        filters.add(Filters.match(fieldName, normalized + "*"));
                        break;
                    case ENDING:
                        filters.add(Filters.match(fieldName, "*" + normalized));
                        break;
                    case CONTAINING:
                        filters.add(Filters.match(fieldName, "*" + normalized + "*"));
                        break;
                    case REGEX:
                        filters.add(Filters.match(fieldName, normalized));
                        break;
                    default:
                        filters.add(Filters.eq(fieldName, normalized));
                        break;
                }
            } else {
                filters.add(Filters.eq(fieldName, value));
            }
        }

        if (filters.isEmpty()) {
            return null;
        }
        return filters.size() == 1 ? filters.get(0) : Filters.and(filters);
    }

    /**
     * Maps a Spring {@link org.springframework.data.domain.Sort} to Data API sorts.
     *
     * @param springSort
     *      spring sort
     * @return
     *      mapped sort array, empty if no sort
     */
    public static Sort[] mapSort(org.springframework.data.domain.Sort springSort) {
        if (springSort == null || springSort.isUnsorted()) {
            return new Sort[0];
        }
        List<Sort> sorts = new ArrayList<>();
        for (org.springframework.data.domain.Sort.Order order : springSort) {
            sorts.add(order.isAscending()
                    ? Sort.ascending(order.getProperty())
                    : Sort.descending(order.getProperty()));
        }
        return sorts.toArray(new Sort[0]);
    }

    /**
     * Maps a Spring {@link Pageable} to collection find options.
     *
     * @param pageable
     *      spring pageable
     * @return
     *      mapped find options
     */
    public static CollectionFindOptions mapPageable(Pageable pageable) {
        CollectionFindOptions options = new CollectionFindOptions();
        if (pageable == null || pageable.isUnpaged()) {
            return options;
        }
        options.skip((int) pageable.getOffset());
        options.limit(pageable.getPageSize());
        Sort[] sorts = mapSort(pageable.getSort());
        if (sorts.length > 0) {
            options.sort(sorts);
        }
        return options;
    }

    /**
     * Creates an id-in filter from an iterable of ids.
     *
     * @param ids
     *      ids to map
     * @return
     *      filter or null when no ids
     */
    public static Filter mapIdIn(Iterable<?> ids) {
        if (ids == null) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        ids.forEach(values::add);
        if (values.isEmpty()) {
            return null;
        }
        return Filters.in("_id", values.toArray());
    }

    private static <T> Object readValue(T probe, EntityFieldDefinition field) {
        Method getter = field.getGetter();
        if (getter == null) {
            return null;
        }
        try {
            return getter.invoke(probe);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read field '" + field.getName() + "' from example probe", e);
        }
    }

    private static ExampleMatcher.StringMatcher resolveStringMatcher(ExampleMatcher matcher, String fieldName) {
        Optional<ExampleMatcher.PropertySpecifier> propertySpecifier =
                matcher.getPropertySpecifiers().hasSpecifierForPath(fieldName)
                        ? Optional.of(matcher.getPropertySpecifiers().getForPath(fieldName))
                        : Optional.empty();

        return propertySpecifier
                .map(ExampleMatcher.PropertySpecifier::getStringMatcher)
                .orElseGet(matcher::getDefaultStringMatcher);
    }
}
