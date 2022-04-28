package fr.max2.annotated.lib.network.serializer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import net.minecraft.network.FriendlyByteBuf;

public class CollectionSerializer<C, T extends Collection<C>> implements NetworkSerializer<T>
{
	private final IntFunction<T> implementationConstructor;
	private final NetworkSerializer<C> contentSerializer;

	private CollectionSerializer(IntFunction<? extends T> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		this.implementationConstructor = implementationConstructor::apply;
		this.contentSerializer = contentSerializer;
	}
	
	public static <C, T extends Collection<C>> NetworkSerializer<T> of(IntFunction<? extends T> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		return new CollectionSerializer<>(implementationConstructor, contentSerializer);
	}

	@SuppressWarnings({ "unchecked" })
	public static <C> NetworkSerializer<List<? extends C>> listOf(IntFunction<? extends List<C>> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		return (NetworkSerializer<List<? extends C>>)(NetworkSerializer<?>)of(implementationConstructor, contentSerializer);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <C> NetworkSerializer<Set<? extends C>> setOf(IntFunction<? extends Set<C>> implementationConstructor, NetworkSerializer<C> contentSerializer)
	{
		return (NetworkSerializer<Set<? extends C>>)(NetworkSerializer<?>)of(implementationConstructor, contentSerializer);
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
