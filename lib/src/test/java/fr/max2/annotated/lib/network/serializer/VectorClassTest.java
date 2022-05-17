package fr.max2.annotated.lib.network.serializer;

import org.junit.Test;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class VectorClassTest
{
	@Test
	public void testAABB()
	{
		SerializerTester<AABB> tester = new SerializerTester<>(VectorClassSerializer.AABB);
		tester.test(8 * 6, new AABB(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d));
		tester.test(8 * 6, new AABB(-1.0d, -1.0d, -1.0d, 1.0d, 1.0d, 1.0d));
		tester.test(8 * 6, new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
	}

	@Test
	public void testBoundingBox()
	{
		SerializerTester<BoundingBox> tester = new SerializerTester<>(VectorClassSerializer.STRUCTURE_BB);
		tester.test(1 * 6, 5 * 6, new BoundingBox(0, 0, 0, 0, 0, 0));
		tester.test(1 * 6, 5 * 6, new BoundingBox(-1, -1, -1, 1, 1, 1));
		tester.test(1 * 6, 5 * 6, new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Test
	public void testVec3i()
	{
		SerializerTester<Vec3i> tester = new SerializerTester<>(VectorClassSerializer.VEC3I);
		tester.test(1 * 3, 5 * 3, new Vec3i(0, 0, 0));
		tester.test(1 * 3, 5 * 3, new Vec3i(-1, -1, -1));
		tester.test(1 * 3, 5 * 3, new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
		tester.test(1 * 3, 5 * 3, new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Test
	public void testVec3()
	{
		SerializerTester<Vec3> tester = new SerializerTester<>(VectorClassSerializer.VEC3);
		tester.test(8 * 3, new Vec3(0.0d, 0.0d, 0.0d));
		tester.test(8 * 3, new Vec3(-1.0d, -1.0d, -1.0d));
		tester.test(8 * 3, new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		tester.test(8 * 3, new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
	}

	@Test
	public void testVec2()
	{
		SerializerTester<Vec2> tester = new SerializerTester<>(VectorClassSerializer.VEC2, (a, b) -> a.x == b.x && a.y == b.y);
		tester.test(4 * 2, new Vec2(0.0f, 0.0f));
		tester.test(4 * 2, new Vec2(-1.0f, -1.0f));
		tester.test(4 * 2, new Vec2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		tester.test(4 * 2, new Vec2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
	}

	@Test
	public void testVector3d()
	{
		SerializerTester<Vector3d> tester = new SerializerTester<>(VectorClassSerializer.VECTOR3D, (a, b) -> a.x == b.x && a.y == b.y && a.z == b.z);
		tester.test(8 * 3, new Vector3d(0.0d, 0.0d, 0.0d));
		tester.test(8 * 3, new Vector3d(-1.0d, -1.0d, -1.0d));
		tester.test(8 * 3, new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		tester.test(8 * 3, new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
	}

	@Test
	public void testVector3f()
	{
		SerializerTester<Vector3f> tester = new SerializerTester<>(VectorClassSerializer.VECTOR3F);
		tester.test(4 * 3, new Vector3f(0.0f, 0.0f, 0.0f));
		tester.test(4 * 3, new Vector3f(-1.0f, -1.0f, -1.0f));
		tester.test(4 * 3, new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		tester.test(4 * 3, new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
	}

	@Test
	public void testVector4f()
	{
		SerializerTester<Vector4f> tester = new SerializerTester<>(VectorClassSerializer.VECTOR4F);
		tester.test(4 * 4, new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
		tester.test(4 * 4, new Vector4f(-1.0f, -1.0f, -1.0f, -1.0f));
		tester.test(4 * 4, new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		tester.test(4 * 4, new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
	}
}
