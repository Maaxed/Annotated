package fr.max2.annotated.lib.network.serializer;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class VectorClassSerializer
{
	private VectorClassSerializer()
	{ }
	
	public static final NetworkSerializer<AABB> AABB = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, AABB value)
		{
			buf.writeDouble(value.minX);
			buf.writeDouble(value.minY);
			buf.writeDouble(value.minZ);
			buf.writeDouble(value.maxX);
			buf.writeDouble(value.maxY);
			buf.writeDouble(value.maxZ);
		}
		
		@Override
		public AABB decode(FriendlyByteBuf buf)
		{
			return new AABB(
				buf.readDouble(), buf.readDouble(), buf.readDouble(),
				buf.readDouble(), buf.readDouble(), buf.readDouble()
			);
		}
	};
	
	public static final NetworkSerializer<BoundingBox> STRUCTURE_BB = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, BoundingBox value)
		{
			buf.writeInt(value.minX());
			buf.writeInt(value.minY());
			buf.writeInt(value.minZ());
			buf.writeInt(value.maxX());
			buf.writeInt(value.maxY());
			buf.writeInt(value.maxZ());
		}
		
		@Override
		public BoundingBox decode(FriendlyByteBuf buf)
		{
			return new BoundingBox(
				buf.readInt(), buf.readInt(), buf.readInt(),
				buf.readInt(), buf.readInt(), buf.readInt()
			);
		}
	};
	
	public static final NetworkSerializer<Vec3i> VEC3I = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vec3i value)
		{
			buf.writeInt(value.getX());
			buf.writeInt(value.getY());
			buf.writeInt(value.getZ());
		}
		
		@Override
		public Vec3i decode(FriendlyByteBuf buf)
		{
			return new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
		}
	};
	
	public static final NetworkSerializer<Vec3> VEC3 = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vec3 value)
		{
			buf.writeDouble(value.x);
			buf.writeDouble(value.y);
			buf.writeDouble(value.z);
		}
		
		@Override
		public Vec3 decode(FriendlyByteBuf buf)
		{
			return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		}
	};
	
	public static final NetworkSerializer<Vec2> VEC2 = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vec2 value)
		{
			buf.writeFloat(value.x);
			buf.writeFloat(value.y);
		}
		
		@Override
		public Vec2 decode(FriendlyByteBuf buf)
		{
			return new Vec2(buf.readFloat(), buf.readFloat());
		}
	};
	
	public static final NetworkSerializer<Vector3d> VECTOR3D = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vector3d value)
		{
			buf.writeDouble(value.x);
			buf.writeDouble(value.y);
			buf.writeDouble(value.z);
		}
		
		@Override
		public Vector3d decode(FriendlyByteBuf buf)
		{
			return new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		}
	};
	
	public static final NetworkSerializer<Vector3f> VECTOR3F = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vector3f value)
		{
			buf.writeFloat(value.x());
			buf.writeFloat(value.y());
			buf.writeFloat(value.z());
		}
		
		@Override
		public Vector3f decode(FriendlyByteBuf buf)
		{
			return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
		}
	};
	
	public static final NetworkSerializer<Vector4f> VECTOR4F = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Vector4f value)
		{
			buf.writeFloat(value.x());
			buf.writeFloat(value.y());
			buf.writeFloat(value.z());
			buf.writeFloat(value.w());
		}
		
		@Override
		public Vector4f decode(FriendlyByteBuf buf)
		{
			return new Vector4f(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
		}
	};
	
}
