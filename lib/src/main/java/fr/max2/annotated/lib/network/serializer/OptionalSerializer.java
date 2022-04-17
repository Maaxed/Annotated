package fr.max2.annotated.lib.network.serializer;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;

public class OptionalSerializer<T> implements NetworkSerializer<Optional<T>>
{
	private final NetworkSerializer<T> contentSerializer;

	private OptionalSerializer(NetworkSerializer<T> contentSerializer)
	{
		this.contentSerializer = contentSerializer;
	}
	
	public static <T> NetworkSerializer<Optional<T>> of(NetworkSerializer<T> contentSerializer)
	{
		return new OptionalSerializer<>(contentSerializer);
	}

	@Override
	public void encode(FriendlyByteBuf buf, Optional<T> value)
	{
		buf.writeOptional(value, this.contentSerializer::encode);
	}

	@Override
	public Optional<T> decode(FriendlyByteBuf buf)
	{
		return buf.readOptional(this.contentSerializer::decode);
	}
}
