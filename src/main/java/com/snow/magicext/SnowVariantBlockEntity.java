package com.snow.magicext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 记住被雪盖住之前是什么方块，化雪时原样还回去。 */
public class SnowVariantBlockEntity extends BlockEntity {

    private static final String KEY = "Contained";
    private BlockState contained = Blocks.AIR.defaultBlockState();

    public SnowVariantBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.SNOW_VARIANT.get(), pos, state);
    }

    public BlockState getContained() {
        return contained;
    }

    public void setContained(BlockState state) {
        contained = state;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(KEY, NbtUtils.writeBlockState(contained));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(KEY, 10)) {
            contained = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(KEY));
        }
    }
}
