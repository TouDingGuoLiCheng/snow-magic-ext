package com.snow.magicext;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Snow! Real Magic! 附属：让雪也铺在台阶、楼梯、墙、栅栏和栅栏门上。
 *
 * <p>SRM 自带的五种雪覆盖变体在 1.20 里既不能被自然落雪触发（{@code canContainState} 名单不含它们），
 * 也不能在 {@code snow-cover.retainOriginalBlocks = true} 时使用（{@code Hooks.convert} 直接返回 false，
 * 而那只雪楼梯没有方块实体、化雪后拿不回原方块）。因此本模组自带一套带方块实体的覆雪变体：
 * 外观是雪造型，原方块存在方块实体里，化雪时原样复原。
 */
@Mod(SnowMagicExt.MODID)
public class SnowMagicExt {
    public static final String MODID = "snow_magic_ext";

    @SuppressWarnings("removal")
    public SnowMagicExt() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.BLOCKS.register(bus);
        ModRegistry.BLOCK_ENTITIES.register(bus);
    }
}
