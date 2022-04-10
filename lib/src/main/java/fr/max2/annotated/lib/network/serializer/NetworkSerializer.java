package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public interface NetworkSerializer<T>
{
	void encode(FriendlyByteBuf buf, T value);
	T decode(FriendlyByteBuf buf);
}
