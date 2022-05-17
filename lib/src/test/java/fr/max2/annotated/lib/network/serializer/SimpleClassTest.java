package fr.max2.annotated.lib.network.serializer;

import java.util.BitSet;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.junit.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SimpleClassTest
{
	@Test
	public void testString()
	{
		SerializerTester<String> tester = new SerializerTester<>(SimpleClassSerializer.STRING);
		tester.test("");
		tester.test("TEST");
		tester.test("This is a longer test");
	}

	@Test
	public void testBoundingBox()
	{
		SerializerTester<UUID> tester = new SerializerTester<>(SimpleClassSerializer.UUID);
		tester.test(1 * 2, 9 * 2, new UUID(0l, 0l));
		tester.test(1 * 2, 9 * 2, new UUID(1l, 1l));
		tester.test(1 * 2, 9 * 2, new UUID(Long.MAX_VALUE, Long.MAX_VALUE));
	}

	@Test
	public void testDate()
	{
		SerializerTester<Date> tester = new SerializerTester<>(SimpleClassSerializer.DATE);
		tester.test(1, 9, new Date(0l));
		tester.test(1, 9, new Date(1l));
		tester.test(1, 9, new Date(Long.MAX_VALUE));
	}

	@Test
	public void testBlockPos()
	{
		SerializerTester<BlockPos> tester = new SerializerTester<>(SimpleClassSerializer.BLOCK_POS);
		tester.test(1 * 3, 5 * 3, BlockPos.ZERO);
		tester.test(1 * 3, 5 * 3, new BlockPos(1, 1, 1));
		tester.test(1 * 3, 5 * 3, new BlockPos(-1, 1, -1));
		tester.test(1 * 3, 5 * 3, new BlockPos(30_000_000, 255, 30_000_000));
	}

	@Test
	public void testChunkPos()
	{
		SerializerTester<ChunkPos> tester = new SerializerTester<>(SimpleClassSerializer.CHUNK_POS);
		tester.test(1, 9, ChunkPos.ZERO);
		tester.test(1, 9, new ChunkPos(-1, -1));
		tester.test(1, 9, new ChunkPos(Integer.MIN_VALUE, Integer.MIN_VALUE));
		tester.test(1, 9, new ChunkPos(1_000_000, 1_000_000));
	}

	@Test
	public void testSectionPos()
	{
		SerializerTester<SectionPos> tester = new SerializerTester<>(SimpleClassSerializer.SECTION_POS);
		tester.test(1, 9, SectionPos.of(0, 0, 0));
		tester.test(1, 9, SectionPos.of(1, 1, 1));
		tester.test(1, 9, SectionPos.of(-1, 1, -1));
		tester.test(1, 9, SectionPos.of(1_000_000, 15, 1_000_000));
	}

	@Test
	public void testResourceLocation()
	{
		SerializerTester<ResourceLocation> tester = new SerializerTester<>(SimpleClassSerializer.RESOURCE_LOCATION);
		tester.test(new ResourceLocation("test"));
		tester.test(new ResourceLocation("testmod", "test_elem"));
	}

	/*@Test
	public void testItemStack()
	{
		SerializerTester<ItemStack> tester = new SerializerTester<>(SimpleClassSerializer.ITEM_STACK);
		tester.test(ItemStack.EMPTY);
		tester.test(new ItemStack(Items.APPLE, 2));
	}

	@Test
	public void testFluidStack()
	{
		SerializerTester<FluidStack> tester = new SerializerTester<>(SimpleClassSerializer.FLUID_STACK);
		tester.test(FluidStack.EMPTY);
		tester.test(new FluidStack(Fluids.WATER, 2));
	}*/

	@Test
	public void testTextComponent()
	{
		SerializerTester<Component> tester = new SerializerTester<>(SimpleClassSerializer.TEXT_COMPONENT);
		tester.test(new TextComponent("This is a test !"));
		tester.test(new TranslatableComponent("test.text"));
	}

	@Test
	public void testBlockHitResult()
	{
		SerializerTester<BlockHitResult> tester = new SerializerTester<>(SimpleClassSerializer.BLOCK_HIT_RESULT,
			(a, b) -> Objects.equals(a.getLocation(), b.getLocation())
				   && a.getDirection() == b.getDirection()
				   && Objects.equals(a.getBlockPos(), b.getBlockPos())
				   && a.getType() == b.getType()
				   && a.isInside() == b.isInside());
		//tester.test(BlockHitResult.miss(new Vec3(0.0d, 1.0d, -1.0d), Direction.DOWN, new BlockPos(0, 1, -1))); // Miss is not supported yet
		tester.test(new BlockHitResult(new Vec3(0.0d, 1.0d, -1.0d), Direction.DOWN, new BlockPos(0, 1, -1), true));
		tester.test(new BlockHitResult(new Vec3(100.0d, 0.0d, -1.0d), Direction.EAST, new BlockPos(5, 1, -1), false));
	}

	@Test
	public void testBitSet()
	{
		SerializerTester<BitSet> tester = new SerializerTester<>(SimpleClassSerializer.BITSET);
		tester.test(new BitSet(0));
		BitSet bs = new BitSet(128);
		bs.set(4, 105);
		tester.test(bs);
	}
}
