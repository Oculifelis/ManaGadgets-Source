package dev.oculifelis.managadgets;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ManaGadgets.ID)
public final class ManaGadgets {
    public static final String ID = "managadgets";

    public ManaGadgets() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ManaGadgetsConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.addListener(ManaGadgets::onTooltip);
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof com.direwolf20.buildinggadgets2.common.items.BaseGadget) {
            ManaDisplay.tooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
        }
    }
}
