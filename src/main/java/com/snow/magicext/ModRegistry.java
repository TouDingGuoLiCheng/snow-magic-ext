package com.snow.magicext;

import com.snow.magicext.block.SnowFenceBlock;
import com.snow.magicext.block.SnowFenceGateBlock;
import com.snow.magicext.block.SnowSlabBlock;
import com.snow.magicext.block.SnowStairBlock;
import com.snow.magicext.block.SnowWallBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SnowMagicExt.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SnowMagicExt.MODID);

    public static final RegistryObject<Block> SNOW_STAIRS = BLOCKS.register("snow_stairs",
            () -> new SnowStairBlock(Blocks.SNOW_BLOCK.defaultBlockState(), props()));
    public static final RegistryObject<Block> SNOW_SLAB = BLOCKS.register("snow_slab",
            () -> new SnowSlabBlock(props()));
    public static final RegistryObject<Block> SNOW_WALL = BLOCKS.register("snow_wall",
            () -> new SnowWallBlock(props()));
    public static final RegistryObject<Block> SNOW_FENCE = BLOCKS.register("snow_fence",
            () -> new SnowFenceBlock(props()));
    public static final RegistryObject<Block> SNOW_FENCE_GATE = BLOCKS.register("snow_fence_gate",
            () -> new SnowFenceGateBlock(props()));

    public static final RegistryObject<BlockEntityType<SnowVariantBlockEntity>> SNOW_VARIANT = BLOCK_ENTITIES.register("snow_variant",
            () -> BlockEntityType.Builder.of(SnowVariantBlockEntity::new,
                    SNOW_STAIRS.get(), SNOW_SLAB.get(), SNOW_WALL.get(), SNOW_FENCE.get(), SNOW_FENCE_GATE.get()).build(null));

    private ModRegistry() {
    }

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).randomTicks().strength(0.2F).sound(SoundType.SNOW);
    }
}
