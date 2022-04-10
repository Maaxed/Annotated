package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public class EnumSerializer<T extends Enum<T>> implements NetworkSerializer<T>
{
	private final Class<T> enumClass;

	private EnumSerializer(Class<T> enumClass)
	{
		this.enumClass = enumClass;
	}
	
	public static <T extends Enum<T>> EnumSerializer<T> of(Class<T> enumClass)
	{
		return new EnumSerializer<>(enumClass);
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		buf.writeEnum(value);
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		return buf.readEnum(this.enumClass);
	}
}
