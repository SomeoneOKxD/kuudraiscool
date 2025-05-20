package someoneok.kic.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {
    public static MethodHandle getField(Class<?> clazz, String... names) {
        Field f = null;
        for (String name : names) {
            try {
                f = clazz.getDeclaredField(name);
                break;
            } catch (NoSuchFieldException ignored) {}
        }
        if (f == null)
            throw new RuntimeException("Could not find any of fields " + Arrays.toString(names) + " on class " + clazz);
        f.setAccessible(true);
        try {
            return MethodHandles.publicLookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle getMethod(Class<?> clazz, List<String> names, Class<?>... parameterTypes) {
        Method method = null;
        for (String name : names) {
            try {
                method = clazz.getDeclaredMethod(name, parameterTypes);
                break;
            } catch (NoSuchMethodException ignored) {}
        }
        if (method == null)
            throw new RuntimeException("Could not find any of methods " + names + " on class " + clazz);
        method.setAccessible(true);
        try {
            return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
