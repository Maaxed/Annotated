package fr.max2.annotated.lib.network.packet;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import fr.max2.annotated.lib.network.adapter.NetworkAdapter;
import fr.max2.annotated.lib.network.serializer.NetworkSerializer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketSettings<P>
{
	private final Class<P> packetType;
	private final NetworkSerializer<P> serializer;
	private BiConsumer<P, Supplier<NetworkEvent.Context>> messageConsumer = null;
	private Optional<NetworkDirection> direction = Optional.empty();
	private Boolean scheduled = null;

	private PacketSettings(Class<P> packetType, NetworkSerializer<P> serializer)
	{
		this.packetType = packetType;
		this.serializer = serializer;
	}

	public static <P> PacketSettings<P> forType(Class<P> packetType, NetworkSerializer<P> serializer)
	{
		return new PacketSettings<>(packetType, serializer);
	}

	public PacketSettings<P> setDirection(NetworkDirection direction)
	{
		Preconditions.checkNotNull(direction, "Direction must not be null");
		Preconditions.checkState(this.direction.isEmpty(), "Cannot set direction for packet '%s' more than once", this.packetType.getCanonicalName());
		this.direction = Optional.of(direction);
		return this;
	}

	public PacketSettings<P> setDestination(LogicalSide destination)
	{
		Preconditions.checkNotNull(destination, "Destination must not be null");
		return this.setDirection(destination.isServer() ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT);
	}

	public PacketSettings<P> setScheduled(boolean scheduled)
	{
		Preconditions.checkState(this.scheduled == null, "Cannot set scheduled for packet '%s' more than once", this.packetType.getCanonicalName());
		this.scheduled = scheduled;
		return this;
	}

	public PacketSettings<P> setConsumer(BiConsumer<P, Supplier<NetworkEvent.Context>> messageConsumer)
	{
		Preconditions.checkState(this.messageConsumer == null, "Cannot set consumer for packet '%s' more than once", this.packetType.getCanonicalName());
		this.messageConsumer = messageConsumer;
		return this;
	}

	public <T> PacketSettings<P> setConsumer(NetworkAdapter<T, P> adapter, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer)
	{
		return this.setConsumer((value, ctx) ->
		{
			messageConsumer.accept(adapter.fromNetwork(value, ctx.get()), ctx);
		});
	}

	public void register(SimpleChannel channel, int index)
	{
		Preconditions.checkState(this.direction.isPresent(), "A direction must be set");
		Preconditions.checkState(this.messageConsumer != null, "A consumer must be set");
		NetworkSerializer<P> serializer = this.serializer;
		channel.registerMessage(index, this.packetType, (p, buf) -> serializer.encode(buf, p), serializer::decode, this.consumer(), this.direction);
	}

	private BiConsumer<P, Supplier<NetworkEvent.Context>> consumer()
	{
		BiConsumer<P, Supplier<NetworkEvent.Context>> baseConsumer = this.messageConsumer;
		if (this.scheduled == null || this.scheduled)
		{
			return (msg, ctx) ->
			{
				ctx.get().enqueueWork(() -> baseConsumer.accept(msg, ctx));
				ctx.get().setPacketHandled(true);
			};
		}
		else
		{
			return (msg, ctx) ->
			{
				baseConsumer.accept(msg, ctx);
				ctx.get().setPacketHandled(true);
			};
		}
	}
}
