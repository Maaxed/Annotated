package fr.max2.annotated.lib.network.serializer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;

public class DelegatedSerializer<T> implements NetworkSerializer<T>
{
	private final BiConsumer<FriendlyByteBuf, T> encoder;
	private final Function<FriendlyByteBuf, T> decoder;

	public DelegatedSerializer(BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder)
	{
		this.encoder = encoder;
		this.decoder = decoder;
	}

	@Override
	public void encode(FriendlyByteBuf buf, T value)
	{
		this.encoder.accept(buf, value);
	}

	@Override
	public T decode(FriendlyByteBuf buf)
	{
		return this.decoder.apply(buf);
	}
	
}
