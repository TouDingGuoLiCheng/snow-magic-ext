package com.snow.magicext;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import snownee.snow.util.CommonProxy;

/** 覆雪变体的公共行为：挂方块实体、化雪还原、被挖时掉原方块。 */
public interface SnowCoveredVariant extends EntityBlock {

    Logger LOG = LogUtils.getLogger();
    AtomicLong SEEN = new AtomicLong();
    AtomicLong MELTED = new AtomicLong();

    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SnowVariantBlockEntity(pos, state);
    }

    /**
     * 化雪不靠 randomTick：覆雪方块实际收不到随机刻（实测 randomTick 一次都没触发），
     * 改由方块实体 ticker 每 40 tick 查一次，按位置错开避免同一刻集中计算。
     */
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && (server.getGameTime() + pos.asLong()) % 80 == 0) {
                melt(st, server, pos);
            }
        };
    }

    static List<ItemStack> drops(LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof SnowVariantBlockEntity variant && !variant.getContained().isAir()) {
            return List.of(new ItemStack(variant.getContained().getBlock().asItem()));
        }
        return List.of();
    }

    @SuppressWarnings("removal")
    static void melt(BlockState state, ServerLevel level, BlockPos pos) {
        // 先做最便宜的判定：正在下雪又没被光源照亮时，连群系都不用查
        boolean light = CommonProxy.blockLightEnoughToMelt(level, pos);
        boolean raining = level.isRaining();
        if (raining && !light) {
            return;
        }
        boolean temperature = CommonProxy.shouldMelt(level, pos);
        boolean cold = CommonProxy.coldEnoughToSnow(level, pos, level.getBiome(pos));
        boolean melt = light || temperature || !cold;

        long seen = SEEN.incrementAndGet();
        if (seen <= 20 || seen % 200 == 0) {
            LOG.info("snow_magic_ext: randomTick {} light={} raining={} tempMelt={} coldEnough={} -> melt={}",
                    pos, light, raining, temperature, cold, melt);
        }
        if (!melt) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof SnowVariantBlockEntity variant)) {
            LOG.info("snow_magic_ext: melt at {} but no variant block entity", pos);
            return;
        }
        BlockState contained = variant.getContained();
        if (contained.isAir()) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, contained, 3);
        }
        long melted = MELTED.incrementAndGet();
        if (melted <= 20 || melted % 200 == 0) {
            LOG.info("snow_magic_ext: restored {} at {} (melted={})", contained.getBlock(), pos, melted);
        }
    }
}
