package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public final class PrimitiveArraySerializer
{
	private PrimitiveArraySerializer()
	{ }
	
	public static final NetworkSerializer<byte[]> ByteArraySerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeByteArray, FriendlyByteBuf::readByteArray);
	
	public static final NetworkSerializer<short[]> ShortArraySerializer = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, short[] value)
		{
			buf.writeVarInt(value.length);
			
			for(short v : value)
			{
				PrimitiveSerializer.ShortSerializer.INSTANCE.encodePrimitive(v, buf);
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
	
	public static final NetworkSerializer<int[]> IntArraySerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray);
	
	public static final NetworkSerializer<long[]> LongArraySerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeLongArray, FriendlyByteBuf::readLongArray);
	
	public static final NetworkSerializer<float[]> FloatArraySerializer = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, float[] value)
		{
			buf.writeVarInt(value.length);
			
			for(float v : value)
			{
				PrimitiveSerializer.FloatSerializer.INSTANCE.encodePrimitive(v, buf);
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
	
	public static final NetworkSerializer<double[]> DoubleArraySerializer = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, double[] value)
		{
			buf.writeVarInt(value.length);
			
			for(double v : value)
			{
				PrimitiveSerializer.DoubleSerializer.INSTANCE.encodePrimitive(v, buf);
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
	
	public static final NetworkSerializer<boolean[]> BooleanArraySerializer = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, boolean[] value)
		{
			buf.writeVarInt(value.length);
			
			// TODO [v3.0] Group the booleans in bytes
			for(boolean v : value)
			{
				PrimitiveSerializer.BooleanSerializer.INSTANCE.encodePrimitive(v, buf);
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
	
	public static final NetworkSerializer<char[]> CharArraySerializer = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, char[] value)
		{
			buf.writeVarInt(value.length);
			
			for(char v : value)
			{
				PrimitiveSerializer.CharSerializer.INSTANCE.encodePrimitive(v, buf);
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
