package fr.max2.annotated.lib.network.serializer;

import java.util.Map;
import java.util.function.IntFunction;

import net.minecraft.network.FriendlyByteBuf;

public class MapSerializer<K, V, T extends Map<K, V>> implements NetworkSerializer<T> 
{
	private final IntFunction<T> implementationConstructor;
	private final NetworkSerializer<K> keySerializer;
	private final NetworkSerializer<V> valueSerializer;

	private MapSerializer(IntFunction<T> implementationConstructor, NetworkSerializer<K> keySerializer, NetworkSerializer<V> valueSerializer)
	{
		this.implementationConstructor = implementationConstructor;
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;
	}
	
	public static <K, V, T extends Map<K, V>> NetworkSerializer<T> of(IntFunction<T> implementationConstructor, NetworkSerializer<K> keySerializer, NetworkSerializer<V> valueSerializer)
	{
		return new MapSerializer<>(implementationConstructor, keySerializer, valueSerializer);
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		buf.writeMap(value, this.keySerializer::encode, this.valueSerializer::encode);
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		return buf.readMap(this.implementationConstructor, this.keySerializer::decode, this.valueSerializer::decode);
	}
}