package mcjty.incontrol.tools.typed;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A Type object represents a given type.
 */
public record Type<V>(@Nonnull Class<V> type) {

    // Root
    public static final Type<Object> OBJECT = new Type<Object>(Object.class);

    // Basic
    public static final Type<Integer> INTEGER = create(Integer.class);
    public static final Type<Double> DOUBLE = create(Double.class);
    public static final Type<Float> FLOAT = create(Float.class);
    public static final Type<Long> LONG = create(Long.class);
    public static final Type<String> STRING = create(String.class);
    public static final Type<Boolean> BOOLEAN = create(Boolean.class);
    public static final Type<String> JSON = create(String.class);
    public static final Type<ResourceKey<Level>> DIMENSION_TYPE = create(ResourceKey.class);

    // Map
    public static final Type<AttributeMap> MAP = create(AttributeMap.class);

    @Nonnull
    public static <V> Type<V> create(@Nonnull final Class<? super V> type) {
        return new Type<V>((Class<V>) type);
    }

    @Nonnull
    public List<V> convert(@Nonnull List list) {
        return (List<V>) list;
    }

    public V convert(Object o) {
        return (V) o;
    }

    @Override
    public String toString() {
        return "Type(" + type().getSimpleName() + ')';
    }
}
