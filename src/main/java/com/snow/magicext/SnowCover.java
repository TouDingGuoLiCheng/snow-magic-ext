package com.snow.magicext;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.util.CommonProxy;

/**
 * 单个方块的判定与转换，供 tick 扫描和命令共用，方便出问题时对着日志查。
 */
public final class SnowCover {

    private static final Logger LOG = LogUtils.getLogger();

    private static long okCount;
    private static long failCount;

    private SnowCover() {
    }

    public static boolean isTarget(BlockState state) {
        if (state.isAir() || state.hasBlockEntity()) {
            return false;
        }
        if (state.getBlock() instanceof SnowCoveredVariant) {
            return false;
        }
        if (variantOf(state) == null) {
            return false;
        }
        return state.getBlock() instanceof SlabBlock ? state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM : true;
    }

    private static RegistryObject<Block> variantOf(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FenceGateBlock) {
            return ModRegistry.SNOW_FENCE_GATE;
        }
        if (block instanceof StairBlock) {
            return ModRegistry.SNOW_STAIRS;
        }
        if (block instanceof SlabBlock) {
            return ModRegistry.SNOW_SLAB;
        }
        if (block instanceof WallBlock) {
            return ModRegistry.SNOW_WALL;
        }
        if (block instanceof FenceBlock) {
            return ModRegistry.SNOW_FENCE;
        }
        return null;
    }

    public static String gateInfo(ServerLevel level, BlockPos pos) {
        StringBuilder sb = new StringBuilder();
        sb.append("lightMelt=").append(CommonProxy.blockLightEnoughToMelt(level, pos));
        sb.append(" winterOnly=").append(SnowCommonConfig.accumulationWinterOnly);
        sb.append(" winter=").append(CommonProxy.isWinter(level, pos, level.getBiome(pos)));
        sb.append(" coldEnough=").append(CommonProxy.coldEnoughToSnow(level, pos, level.getBiome(pos)));
        sb.append(" raining=").append(level.isRaining());
        return sb.toString();
    }

    public static boolean weatherOk(ServerLevel level, BlockPos pos) {
        if (!level.isRaining()) {
            return false;
        }
        if (CommonProxy.blockLightEnoughToMelt(level, pos)) {
            return false;
        }
        if (SnowCommonConfig.accumulationWinterOnly && !CommonProxy.isWinter(level, pos, level.getBiome(pos))) {
            return false;
        }
        return CommonProxy.coldEnoughToSnow(level, pos, level.getBiome(pos));
    }

    public static boolean convert(ServerLevel level, BlockPos pos, BlockState state) {
        RegistryObject<Block> variant = variantOf(state);
        if (variant == null) {
            return false;
        }
        BlockState covered = Hooks.copyProperties(state, variant.get().defaultBlockState());
        boolean ok;
        try {
            ok = level.setBlock(pos, covered, 3) && level.getBlockEntity(pos) instanceof SnowVariantBlockEntity be
                    && finish(be, state);
        } catch (Throwable t) {
            LOG.warn("snow_magic_ext: convert threw at {} {}", pos, t.toString());
            ok = false;
        }
        if (ok) {
            okCount++;
            if (okCount <= 5 || okCount % 50 == 0) {
                LOG.info("snow_magic_ext: covered {} at {} with {} (ok={} fail={})", state.getBlock(), pos, covered.getBlock(), okCount, failCount);
            }
        } else {
            failCount++;
            if (failCount <= 5 || failCount % 50 == 0) {
                LOG.info("snow_magic_ext: cover FAILED for {} at {} (ok={} fail={}) stateNow={}",
                        state.getBlock(), pos, okCount, failCount, level.getBlockState(pos).getBlock());
            }
        }
        return ok;
    }

    private static boolean finish(SnowVariantBlockEntity be, BlockState contained) {
        be.setContained(contained);
        return true;
    }
}
