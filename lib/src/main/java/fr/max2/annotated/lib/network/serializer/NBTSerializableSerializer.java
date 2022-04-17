package fr.max2.annotated.lib.network.serializer;

import java.util.function.Supplier;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

public class NBTSerializableSerializer<D extends Tag, T extends INBTSerializable<D>> implements NetworkSerializer<T>
{
	private final Supplier<T> constructor;
	private final NetworkSerializer<D> tagSerializer;

	private NBTSerializableSerializer(Supplier<T> constructor, NetworkSerializer<D> tagSerializer)
	{
		this.constructor = constructor;
		this.tagSerializer = tagSerializer;
	}

	public static <D extends Tag, T extends INBTSerializable<D>> NBTSerializableSerializer<D, T> of(Supplier<T> constructor, NetworkSerializer<D> tagSerializer)
	{
		return new NBTSerializableSerializer<>(constructor, tagSerializer);
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		this.tagSerializer.encode(buf, value.serializeNBT());
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		T value = this.constructor.get();
		value.deserializeNBT(this.tagSerializer.decode(buf));
		return value;
	}
}
