package fr.max2.annotated.lib.network.serializer;

import java.util.Collection;
import java.util.function.IntFunction;

import net.minecraft.network.FriendlyByteBuf;

public class CollectionSerializer<C, T extends Collection<C>> implements NetworkSerializer<T>
{
	private final IntFunction<T> implementationConstructor;
	private final NetworkSerializer<C> contentSerializer;

	private CollectionSerializer(IntFunction<T> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		this.implementationConstructor = implementationConstructor;
		this.contentSerializer = contentSerializer;
	}
	
	public static <C, T extends Collection<C>> NetworkSerializer<T> of(IntFunction<T> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		return new CollectionSerializer<>(implementationConstructor, contentSerializer);
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		buf.writeCollection(value, this.contentSerializer::encode);
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		return buf.readCollection(this.implementationConstructor, this.contentSerializer::decode);
	}
}
