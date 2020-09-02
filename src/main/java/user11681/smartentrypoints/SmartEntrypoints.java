package user11681.smartentrypoints;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.metadata.EntrypointMetadata;

public class SmartEntrypoints {
    public static <T> void executeOptionalEntrypoint(final String name, final Class<T> entrypointType, final Consumer<T> onExecute) {
        final ReferenceArrayList<T> entrypoints = ReferenceArrayList.wrap((T[]) Array.newInstance(entrypointType, 5), 0);
        final ModContainer[] mods = FabricLoader.getInstance().getAllMods().toArray(new ModContainer[0]);
        final int modCount = mods.length;
        int i;
        int j;
        int entrypointCount;
        EntrypointMetadata[] modEntrypoints;

        for (i = 0; i < modCount; i++) {
            modEntrypoints = mods[i].getInfo().getEntrypoints(name).toArray(new EntrypointMetadata[0]);

            for (j = 0, entrypointCount = modEntrypoints.length; j < entrypointCount; j++) {
                try {
                    final Class<?> klass = Class.forName(modEntrypoints[j].getValue());

                    if (entrypointType.isAssignableFrom(klass)) {
                        entrypoints.add(entrypointType.cast(klass.getConstructor().newInstance()));
                    }
                } catch (final ClassNotFoundException exception) {
                    throw new IllegalArgumentException(String.format("class %s specified in the %s entrypoint of mod %s does not exist", modEntrypoints[j].getValue(), name, mods[i].getMetadata().getName()), exception);
                } catch (final IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
                    throw new IllegalStateException(String.format("class %s specified in the %s entrypoint of mod %s cannot be instantiated", modEntrypoints[j].getValue(), name, mods[i].getMetadata().getName()), exception);
                } catch (final InvocationTargetException exception) {
                    throw new RuntimeException(String.format("an error was encountered during the execution of the %s entrypoint of class %s", name, modEntrypoints[j].getValue()));
                }
            }
        }

        final T[] entrypointArray = entrypoints.elements();
        entrypointCount = entrypoints.size();

        for (i = 0; i < entrypointCount; i++) {
            onExecute.accept(entrypointArray[i]);
        }
    }
}
