package fr.max2.annotated.lib.network.serializer;

import static org.junit.Assert.*;

import java.util.function.BiPredicate;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class SerializerTester<T>
{
	private final NetworkSerializer<T> serializer;
	private final BiPredicate<T, T> equalityChecker;

	public SerializerTester(NetworkSerializer<T> serializer, BiPredicate<T, T> equalityChecker)
	{
		this.serializer = serializer;
		this.equalityChecker = equalityChecker;
	}

	public SerializerTester(NetworkSerializer<T> serializer)
	{
		this(serializer, null);
	}

	public void test(T value)
	{
		this.test(0, Integer.MAX_VALUE, value);
	}

	public void test(int expectedUsedBytes, T value)
	{
		this.test(expectedUsedBytes, expectedUsedBytes, value);
	}

	public void test(int minExpectedUsedBytes, int maxExpectedUsedBytes, T value)
	{
		FriendlyByteBuf writeBuffer = new FriendlyByteBuf(Unpooled.buffer());
		try
		{
			this.serializer.encode(writeBuffer, value);
			assertEquals(0, writeBuffer.readerIndex());

			FriendlyByteBuf readBuffer = new FriendlyByteBuf(writeBuffer.asReadOnly());
			try
			{
				assertTrue(readBuffer.readableBytes() >= minExpectedUsedBytes);
				assertTrue(readBuffer.readableBytes() <= maxExpectedUsedBytes);
				T res = this.serializer.decode(readBuffer);
				if (this.equalityChecker == null)
				{
					assertEquals(value, res);
				}
				else
				{
					assertTrue("expected:<" + value + "> but was:<" + res + ">", this.equalityChecker.test(value, res));
				}
			}
			finally
			{
				//readBuffer.release();
			}
		}
		finally
		{
			writeBuffer.release();
		}
	}
}
