package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public final class PrimitiveArraySerializer
{
	private PrimitiveArraySerializer()
	{ }
	
	public static final NetworkSerializer<byte[]> BYTE_ARRAY = new DelegatedSerializer<>(FriendlyByteBuf::writeByteArray, FriendlyByteBuf::readByteArray);
	
	public static final NetworkSerializer<short[]> SHORT_ARRAY = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, short[] value)
		{
			buf.writeVarInt(value.length);
			
			for(short v : value)
			{
				PrimitiveSerializer.ShortSerializer.INSTANCE.encodePrimitive(buf, v);
			}
		}

		@Override
		public short[] decode(FriendlyByteBuf buf)
		{
			int count = buf.readVarInt();
			short[] res = new short[count];
			
			for (int i = 0; i < count; i++)
			{
				res[i] = PrimitiveSerializer.ShortSerializer.INSTANCE.decodePrimitive(buf);
			}
			
			return res;
		}
	};
	
	public static final NetworkSerializer<int[]> INT_ARRAY = new DelegatedSerializer<>(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray);
	
	public static final NetworkSerializer<long[]> LONG_ARRAY = new DelegatedSerializer<>(FriendlyByteBuf::writeLongArray, FriendlyByteBuf::readLongArray);
	
	public static final NetworkSerializer<float[]> FLOAT_ARRAY = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, float[] value)
		{
			buf.writeVarInt(value.length);
			
			for(float v : value)
			{
				PrimitiveSerializer.FloatSerializer.INSTANCE.encodePrimitive(buf, v);
			}
		}

		@Override
		public float[] decode(FriendlyByteBuf buf)
		{
			int count = buf.readVarInt();
			float[] res = new float[count];
			
			for (int i = 0; i < count; i++)
			{
				res[i] = PrimitiveSerializer.FloatSerializer.INSTANCE.decodePrimitive(buf);
			}
			
			return res;
		}
	};
	
	public static final NetworkSerializer<double[]> DOUBLE_ARRAY = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, double[] value)
		{
			buf.writeVarInt(value.length);
			
			for(double v : value)
			{
				PrimitiveSerializer.DoubleSerializer.INSTANCE.encodePrimitive(buf, v);
			}
		}

		@Override
		public double[] decode(FriendlyByteBuf buf)
		{
			int count = buf.readVarInt();
			double[] res = new double[count];
			
			for (int i = 0; i < count; i++)
			{
				res[i] = PrimitiveSerializer.DoubleSerializer.INSTANCE.decodePrimitive(buf);
			}
			
			return res;
		}
	};
	
	public static final NetworkSerializer<boolean[]> BOOLEAN_ARRAY = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, boolean[] value)
		{
			buf.writeVarInt(value.length);
			
			// TODO [v3.0] Group the booleans in bytes
			for(boolean v : value)
			{
				PrimitiveSerializer.BooleanSerializer.INSTANCE.encodePrimitive(buf, v);
			}
		}

		@Override
		public boolean[] decode(FriendlyByteBuf buf)
		{
			int count = buf.readVarInt();
			boolean[] res = new boolean[count];
			
			for (int i = 0; i < count; i++)
			{
				res[i] = PrimitiveSerializer.BooleanSerializer.INSTANCE.decodePrimitive(buf);
			}
			
			return res;
		}
	};
	
	public static final NetworkSerializer<char[]> CHAR_ARRAY = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, char[] value)
		{
			buf.writeVarInt(value.length);
			
			for(char v : value)
			{
				PrimitiveSerializer.CharSerializer.INSTANCE.encodePrimitive(buf, v);
			}
		}

		@Override
		public char[] decode(FriendlyByteBuf buf)
		{
			int count = buf.readVarInt();
			char[] res = new char[count];
			
			for (int i = 0; i < count; i++)
			{
				res[i] = PrimitiveSerializer.CharSerializer.INSTANCE.decodePrimitive(buf);
			}
			
			return res;
		}
	};
}
