package fr.max2.annotated.lib.network.serializer;

import org.junit.Test;

public class PrimitiveTest
{
	@Test
	public void testByte()
	{
		SerializerTester<Byte> tester = new SerializerTester<>(PrimitiveSerializer.ByteSerializer.INSTANCE);
		tester.test(1, (byte)0);
		tester.test(1, (byte)1);
		tester.test(1, (byte)-1);
		tester.test(1, (byte)2);
		tester.test(1, (byte)15);
		tester.test(1, (byte)16);
		tester.test(1, (byte)(Byte.MIN_VALUE + 1));
		tester.test(1, Byte.MIN_VALUE);
		tester.test(1, (byte)(Byte.MAX_VALUE - 1));
		tester.test(1, Byte.MAX_VALUE);
	}

	@Test
	public void testShort()
	{
		SerializerTester<Short> tester = new SerializerTester<>(PrimitiveSerializer.ShortSerializer.INSTANCE);
		tester.test(2, (short)0);
		tester.test(2, (short)1);
		tester.test(2, (short)-1);
		tester.test(2, (short)2);
		tester.test(2, (short)15);
		tester.test(2, (short)16);
		tester.test(2, (short)(Short.MIN_VALUE + 1));
		tester.test(2, Short.MIN_VALUE);
		tester.test(2, (short)(Short.MAX_VALUE - 1));
		tester.test(2, Short.MAX_VALUE);
	}

	@Test
	public void testInt()
	{
		SerializerTester<Integer> tester = new SerializerTester<>(PrimitiveSerializer.IntSerializer.INSTANCE);
		tester.test(1, 5, 0);
		tester.test(1, 5, 1);
		tester.test(1, 5, -1);
		tester.test(1, 5, 2);
		tester.test(1, 5, 15);
		tester.test(1, 5, 16);
		tester.test(1, 5, Integer.MIN_VALUE + 1);
		tester.test(1, 5, Integer.MIN_VALUE);
		tester.test(1, 5, Integer.MAX_VALUE - 1);
		tester.test(1, 5, Integer.MAX_VALUE);
	}

	@Test
	public void testLong()
	{
		SerializerTester<Long> tester = new SerializerTester<>(PrimitiveSerializer.LongSerializer.INSTANCE);
		tester.test(1, 9, 0l);
		tester.test(1, 9, 1l);
		tester.test(1, 9, -1l);
		tester.test(1, 9, 2l);
		tester.test(1, 9, 15l);
		tester.test(1, 9, 16l);
		tester.test(1, 9, Long.MIN_VALUE + 1l);
		tester.test(1, 9, Long.MIN_VALUE);
		tester.test(1, 9, Long.MAX_VALUE - 1l);
		tester.test(1, 9, Long.MAX_VALUE);
	}

	@Test
	public void testFloat()
	{
		SerializerTester<Float> tester = new SerializerTester<>(PrimitiveSerializer.FloatSerializer.INSTANCE);
		tester.test(4, 0.0f);
		tester.test(4, -0.0f);
		tester.test(4, 1.0f);
		tester.test(4, -1.0f);
		tester.test(4, 2.0f);
		tester.test(4, 0.5f);
		tester.test(4, 15.0f);
		tester.test(4, 16.0f);
		tester.test(4, Float.MIN_VALUE);
		tester.test(4, -Float.MIN_VALUE);
		tester.test(4, Float.MAX_VALUE);
		tester.test(4, -Float.MAX_VALUE);
		tester.test(4, Float.MIN_NORMAL);
		tester.test(4, -Float.MIN_NORMAL);
		tester.test(4, Float.NaN);
		tester.test(4, Float.POSITIVE_INFINITY);
		tester.test(4, Float.NEGATIVE_INFINITY);
	}

	@Test
	public void testDouble()
	{
		SerializerTester<Double> tester = new SerializerTester<>(PrimitiveSerializer.DoubleSerializer.INSTANCE);
		tester.test(8, 0.0d);
		tester.test(8, -0.0d);
		tester.test(8, 1.0d);
		tester.test(8, -1.0d);
		tester.test(8, 2.0d);
		tester.test(8, 0.5d);
		tester.test(8, 15.0d);
		tester.test(8, 16.0d);
		tester.test(8, Double.MIN_VALUE);
		tester.test(8, -Double.MIN_VALUE);
		tester.test(8, Double.MAX_VALUE);
		tester.test(8, -Double.MAX_VALUE);
		tester.test(8, Double.MIN_NORMAL);
		tester.test(8, -Double.MIN_NORMAL);
		tester.test(8, Double.NaN);
		tester.test(8, Double.POSITIVE_INFINITY);
		tester.test(8, Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testBoolean()
	{
		SerializerTester<Boolean> tester = new SerializerTester<>(PrimitiveSerializer.BooleanSerializer.INSTANCE);
		tester.test(1, true);
		tester.test(1, false);
	}

	@Test
	public void testChar()
	{
		SerializerTester<Character> tester = new SerializerTester<>(PrimitiveSerializer.CharSerializer.INSTANCE);
		tester.test(1, 2, 'a');
		tester.test(1, 2, 'Z');
		tester.test(1, 2, Character.MIN_VALUE);
		tester.test(1, 2, Character.MAX_VALUE);
		tester.test(1, 2, Character.MIN_SURROGATE);
		tester.test(1, 2, Character.MAX_SURROGATE);
	}
}
