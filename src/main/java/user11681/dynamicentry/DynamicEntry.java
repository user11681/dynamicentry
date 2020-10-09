package user11681.dynamicentry;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.metadata.EntrypointMetadata;

public class DynamicEntry {
    /**
     * Load classes specified in the entrypoint <b>{@code name}</b>.
     *
     * @param name the name of the entrypoint.
     */
    public static void load(final String name) {
        load(name, null);
    }

    /**
     * Load classes specified in the entrypoint <b>{@code name}</b> and execute <b>{@code onLoad}</b> when a class is loaded.
     *
     * @param name the name of the entrypoint.
     * @param onLoad the callback for when a class is loaded.
     */
    public static void load(final String name, final Consumer<Class<?>> onLoad) {
        for (final ModContainer mod : FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0])) {
            for (final EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints(name)) {
                try {
                    final Class<?> klass = Class.forName(entrypoint.getValue());

                    if (onLoad != null) {
                        onLoad.accept(klass);
                    }
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.getValue(), name, mod.getMetadata().getName()), exception);
                }
            }
        }
    }

    /**
     * Execute an entrypoint.
     *
     * @param name the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute entrypoint execution callback with an instance of the entrypoint type.
     * @param <T> the entrypoint type.
     */
    public static <T> void execute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute) {
        final ReferenceArrayList<T> entrypoints = ReferenceArrayList.wrap((T[]) Array.newInstance(entrypointType, 3), 0);

        for (final ModContainer mod : FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0])) {
            for (final EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints(name)) {
                try {
                    entrypoints.add(entrypointType.cast(Class.forName(entrypoint.getValue()).getConstructor().newInstance()));
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.getValue(), name, mod.getMetadata().getName()), exception);
                } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                    throw new IllegalStateException(String.format("class %s specified in the %s entrypoint of mod %s cannot be instantiated", entrypoint.getValue(), name, mod.getMetadata().getName()), exception);
                } catch (final InvocationTargetException exception) {
                    throw new RuntimeException(String.format("an error was encountered during the instantiation of the %s entrypoint class %s", name, entrypoint.getValue()), exception);
                }
            }
        }

        final T[] entrypointArray = entrypoints.elements();
        final int entrypointCount = entrypoints.size();

        for (int i = 0; i < entrypointCount; i++) {
            onExecute.accept(entrypointArray[i]);
        }
    }

    /**
     * Load and execute an entrypoint if its type is implemented.
     *
     * @param name the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute entrypoint execution callback with an instance of the entrypoint type.
     * @param <T> the entrypoint type.
     */
    public static <T> void maybeExecute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute) {
        maybeExecute(name, entrypointType, onExecute, null);
    }

    /**
     * Load and execute an entrypoint if its type is implemented.
     *
     * @param name the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute entrypoint execution callback with an instance of the entrypoint type.
     * @param onLoad entrypoint class load callback.
     * @param <T> the entrypoint type.
     */
    public static <T> void maybeExecute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute, final Consumer<Class<?>> onLoad) {
        final ReferenceArrayList<T> entrypoints = ReferenceArrayList.wrap((T[]) Array.newInstance(entrypointType, 3), 0);

        for (final ModContainer mod : FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0])) {
            for (final EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints(name)) {
                try {
                    final Class<?> klass = Class.forName(entrypoint.getValue());

                    if (onLoad != null) {
                        onLoad.accept(klass);
                    }

                    if (entrypointType.isAssignableFrom(klass)) {
                        entrypoints.add(entrypointType.cast(klass.getConstructor().newInstance()));
                    }
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.getValue(), name, mod.getMetadata().getName()), exception);
                } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                    throw new IllegalStateException(String.format("class %s specified in the %s entrypoint of mod %s cannot be instantiated", entrypoint.getValue(), name, mod.getMetadata().getName()), exception);
                } catch (final InvocationTargetException exception) {
                    throw new RuntimeException(String.format("an error was encountered during the instantiation of the %s entrypoint class %s", name, entrypoint.getValue()));
                }
            }
        }

        final T[] entrypointArray = entrypoints.elements();
        final int entrypointCount = entrypoints.size();

        for (int i = 0; i < entrypointCount; i++) {
            onExecute.accept(entrypointArray[i]);
        }
    }
}
