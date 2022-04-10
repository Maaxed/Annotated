package fr.max2.annotated.lib.network.serializer;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class RegistryEntrySerializer<T extends ForgeRegistryEntry<T>> implements NetworkSerializer<T>
{
	private final IForgeRegistry<T> registry;

	private RegistryEntrySerializer(IForgeRegistry<T> registry)
	{
		this.registry = registry;
	}
	
	public static <T extends ForgeRegistryEntry<T>> RegistryEntrySerializer<T> of(IForgeRegistry<T> registry)
	{
		return new RegistryEntrySerializer<>(registry);
	}
	
	public static <T extends ForgeRegistryEntry<T>> RegistryEntrySerializer<T> of(ResourceKey<Registry<T>> registryKey)
	{
		return new RegistryEntrySerializer<>(RegistryManager.ACTIVE.getRegistry(registryKey));
	}

    /**
     * @deprecated The uniqueness of registry super types will not be guaranteed starting in 1.19.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
	public static <T extends ForgeRegistryEntry<T>> RegistryEntrySerializer<T> of(Class<T> registryClass)
	{
		return new RegistryEntrySerializer<>(RegistryManager.ACTIVE.getRegistry(registryClass));
	}
	
	public static <T extends ForgeRegistryEntry<T>> RegistryEntrySerializer<T> of(ResourceLocation registryName)
	{
		return new RegistryEntrySerializer<T>(RegistryManager.ACTIVE.getRegistry(registryName));
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		buf.writeRegistryIdUnsafe(this.registry, value);
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		return buf.readRegistryIdUnsafe(this.registry);
	}
}
