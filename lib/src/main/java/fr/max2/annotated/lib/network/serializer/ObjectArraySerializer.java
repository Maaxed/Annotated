package fr.max2.annotated.lib.network.serializer;

import java.util.function.IntFunction;

import net.minecraft.network.FriendlyByteBuf;

public class ObjectArraySerializer<T> implements NetworkSerializer<T[]>
{
	private final IntFunction<T[]> arrayConstructor;
	private final NetworkSerializer<T> contentSerializer;

	private ObjectArraySerializer(IntFunction<T[]> arrayConstructor, NetworkSerializer<T> contentSerializer)
	{
		this.arrayConstructor = arrayConstructor;
		this.contentSerializer = contentSerializer;
	}
	
	public static <T> ObjectArraySerializer<T> of(IntFunction<T[]> arrayConstructor, NetworkSerializer<T> contentSerializer)
	{
		return new ObjectArraySerializer<>(arrayConstructor, contentSerializer);
	}

	@Override
	public void encode(FriendlyByteBuf buf, T[] value)
	{
		buf.writeVarInt(value.length);
		
		for (T content : value)
		{
			this.contentSerializer.encode(buf, content);
		}
	}

	@Override
	public T[] decode(FriendlyByteBuf buf)
	{
		int count = buf.readVarInt();
		T[] res = this.arrayConstructor.apply(count);
		
		for (int i = 0; i < count; i++)
		{
			res[i] = this.contentSerializer.decode(buf);
		}
		
		return res;
	}
}
