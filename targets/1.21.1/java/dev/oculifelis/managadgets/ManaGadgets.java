package dev.oculifelis.managadgets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(ManaGadgets.ID)
public final class ManaGadgets {
    public static final String ID = "managadgets";

    public ManaGadgets(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ManaGadgetsConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, ManaGadgets::onTooltip);
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BaseGadget) {
            ManaDisplay.tooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
        }
    }
}
