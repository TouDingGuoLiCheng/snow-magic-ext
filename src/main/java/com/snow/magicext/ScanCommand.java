package com.snow.magicext;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /snowmagic scan —— 立地扫一圈，把每个候选方块的判定和 convert 结果打出来，用来定位为什么没覆雪。
 */
@Mod.EventBusSubscriber(modid = SnowMagicExt.MODID)
public final class ScanCommand {

    private static final int RADIUS = 8;

    private ScanCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("snowmagic")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("scan").executes(ctx -> scan(ctx.getSource())));
        event.getDispatcher().register(node);
    }

    private static int scan(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos center;
        try {
            ServerPlayer player = source.getPlayerOrException();
            center = player.blockPosition();
        } catch (Exception e) {
            center = BlockPos.containing(source.getPosition());
        }
        int span = RADIUS * 2 + 1;
        int candidates = 0;
        int converted = 0;
        StringBuilder detail = new StringBuilder();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                BlockPos above = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, center.offset(dx, 0, dz));
                BlockPos target = above.below();
                BlockState state = level.getBlockState(target);
                if (!SnowCover.isTarget(state)) {
                    continue;
                }
                candidates++;
                boolean ok = SnowCover.convert(level, target, state);
                if (ok) {
                    converted++;
                }
                if (candidates <= 12) {
                    detail.append("\n ").append(target.getX()).append(',').append(target.getY()).append(',').append(target.getZ())
                            .append(" ").append(state.getBlock())
                            .append(" [").append(SnowCover.gateInfo(level, target)).append("]")
                            .append(" -> ").append(ok ? "OK" : "FAIL");
                }
            }
        }
        String summary = "[snow_magic_ext] candidates=" + candidates + " converted=" + converted
                + " raining=" + level.isRaining() + detail;
        com.mojang.logging.LogUtils.getLogger().info(summary);
        source.sendSuccess(() -> Component.literal(summary), false);
        return converted;
    }
}
