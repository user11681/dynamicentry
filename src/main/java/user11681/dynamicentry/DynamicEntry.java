package user11681.dynamicentry;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
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
     * @param name   the name of the entrypoint.
     * @param onLoad the callback for when a class is loaded.
     */
    public static void load(final String name, final Consumer<Class<?>> onLoad) {
        for (final EntrypointContainer entrypoint : getEntrypointTargets(name)) {
            try {
                final Class<?> klass = Class.forName(entrypoint.entrypoint);

                if (onLoad != null) {
                    onLoad.accept(klass);
                }
            } catch (final ClassNotFoundException exception) {
                throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.entrypoint, name, entrypoint.entrypoint), exception);
            }
        }
    }

    /**
     * Execute an entrypoint.
     *
     * @param name           the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute      entrypoint execution callback with an instance of the entrypoint type.
     * @param <T>            the entrypoint type.
     */
    public static <T> void execute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute) {
        final ReferenceArrayList<T> entrypointTargets = ReferenceArrayList.wrap((T[]) Array.newInstance(entrypointType, 3), 0);
        final ReferenceArrayList<EntrypointContainer> entrypoints = getEntrypointTargets(name);

        for (final EntrypointContainer entrypoint : entrypoints) {
            try {
                entrypointTargets.add(entrypointType.cast(Class.forName(entrypoint.entrypoint).getConstructor().newInstance()));
            } catch (final ClassNotFoundException exception) {
                throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.entrypoint, name, entrypoint.mod), exception);
            } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                throw new IllegalStateException(String.format("class %s specified in the %s entrypoint of mod %s cannot be instantiated", entrypoint.entrypoint, name, entrypoint.mod), exception);
            } catch (final InvocationTargetException exception) {
                throw new RuntimeException(String.format("an error was encountered during the instantiation of the %s entrypoint class %s", name, entrypoint.entrypoint), exception);
            }
        }

        final T[] entrypointArray = entrypointTargets.elements();
        final int entrypointCount = entrypointTargets.size();

        for (int i = 0; i < entrypointCount; i++) {
            onExecute.accept(entrypointArray[i]);
        }
    }

    /**
     * Load and execute an entrypoint if its type is implemented.
     *
     * @param name           the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute      entrypoint execution callback with an instance of the entrypoint type.
     * @param <T>            the entrypoint type.
     */
    public static <T> void tryExecute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute) {
        tryExecute(name, entrypointType, onExecute, null);
    }

    /**
     * Load and execute an entrypoint if its type is implemented.
     *
     * @param name           the name of the entrypoint to execute.
     * @param entrypointType the entrypoint type.
     * @param onExecute      entrypoint execution callback with an instance of the entrypoint type.
     * @param onLoad         entrypoint class load callback.
     * @param <T>            the entrypoint type.
     */
    public static <T> void tryExecute(final String name, final Class<T> entrypointType, final Consumer<T> onExecute, final Consumer<Class<?>> onLoad) {
        final ReferenceArrayList<T> entrypoints = ReferenceArrayList.wrap((T[]) Array.newInstance(entrypointType, 3), 0);

        for (final EntrypointContainer entrypoint : getEntrypointTargets(name)) {
            try {
                final Class<?> klass = Class.forName(entrypoint.entrypoint);

                if (onLoad != null) {
                    onLoad.accept(klass);
                }

                if (entrypointType.isAssignableFrom(klass)) {
                    entrypoints.add(entrypointType.cast(klass.getConstructor().newInstance()));
                }
            } catch (final ClassNotFoundException exception) {
                throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", entrypoint.entrypoint, name, entrypoint.mod), exception);
            } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                throw new IllegalStateException(String.format("class %s specified in the %s entrypoint of mod %s cannot be instantiated", entrypoint.entrypoint, name, entrypoint.mod), exception);
            } catch (final InvocationTargetException exception) {
                throw new RuntimeException(String.format("an error was encountered during the instantiation of the %s entrypoint class %s", name, entrypoint.entrypoint));
            }
        }

        final T[] entrypointArray = entrypoints.elements();
        final int entrypointCount = entrypoints.size();

        for (int i = 0; i < entrypointCount; i++) {
            onExecute.accept(entrypointArray[i]);
        }
    }

    private static ReferenceArrayList<EntrypointContainer> getEntrypointTargets(final String entrypoint) {
        final ReferenceArrayList<EntrypointContainer> entrypoints = ReferenceArrayList.wrap(new EntrypointContainer[1], 0);

        for (final ModContainer mod : (Collection<ModContainer>) (Object) FabricLoader.getInstance().getAllMods()) {
            final List<EntrypointMetadata> modEntrypoints = mod.getInfo().getEntrypoints(entrypoint);

            if (modEntrypoints != null) {
                for (final EntrypointMetadata metadata : modEntrypoints) {
                    entrypoints.add(new EntrypointContainer(metadata.getValue(), mod.getMetadata().getId()));
                }
            }
        }

        return entrypoints;
    }
}
