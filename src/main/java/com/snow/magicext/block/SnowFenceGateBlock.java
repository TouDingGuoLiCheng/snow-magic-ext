package com.snow.magicext.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootParams;
import com.snow.magicext.SnowCoveredVariant;

public class SnowFenceGateBlock extends FenceGateBlock implements SnowCoveredVariant {

    public SnowFenceGateBlock(Properties properties) {
        super(properties, WoodType.OAK);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return SnowCoveredVariant.drops(builder);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SnowCoveredVariant.melt(state, level, pos);
    }
}
