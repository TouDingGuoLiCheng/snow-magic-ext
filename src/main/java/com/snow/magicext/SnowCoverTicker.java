package com.snow.magicext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import snownee.snow.SnowCommonConfig;

/**
 * 每隔几 tick 扫一次玩家周围的露天方块，把台阶 / 楼梯 / 墙 / 栅栏 / 栅栏门
 * 换成本模组自带的覆雪变体（原方块存进方块实体，化雪复原）。
 */
@Mod.EventBusSubscriber(modid = SnowMagicExt.MODID)
public final class SnowCoverTicker {

    private static final int SCAN_INTERVAL = 10;
    private static final int RADIUS = 10;
    private static final int CONVERT_BUDGET = 24;

    private static int cursor;

    private SnowCoverTicker() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % SCAN_INTERVAL != 0) {
            return;
        }
        if (level.players().isEmpty() || !level.isRaining()) {
            return;
        }
        if (SnowCommonConfig.snowAccumulationMaxLayers <= 0) {
            return;
        }

        int budget = CONVERT_BUDGET;
        for (ServerPlayer player : level.players()) {
            if (budget <= 0) {
                break;
            }
            budget -= scan(level, player.blockPosition(), budget);
        }
    }

    private static int scan(ServerLevel level, BlockPos center, int budget) {
        int span = RADIUS * 2 + 1;
        int total = span * span;
        int done = 0;
        for (int attempt = 0; attempt < total && done < budget; attempt++) {
            int idx = (cursor + attempt) % total;
            int dx = idx % span - RADIUS;
            int dz = idx / span - RADIUS;
            BlockPos above = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, center.offset(dx, 0, dz));
            if (!level.getBlockState(above).isAir()) {
                continue;
            }
            BlockPos target = above.below();
            BlockState state = level.getBlockState(target);
            if (!SnowCover.isTarget(state) || !SnowCover.weatherOk(level, target)) {
                continue;
            }
            if (SnowCover.convert(level, target, state)) {
                done++;
            }
        }
        cursor = (cursor + Math.max(1, total / 3)) % total;
        return done;
    }
}
