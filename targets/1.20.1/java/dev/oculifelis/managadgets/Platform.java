package dev.oculifelis.managadgets;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;

/** The few things that differ between Forge 1.20.1 and NeoForge 1.21.1. */
public final class Platform {
    private Platform() {
    }

    public static Mode mode() {
        return ManaGadgetsConfig.mode();
    }

    public static double manaPerFe() {
        return ManaGadgetsConfig.manaPerFe();
    }

    public static ManaItem manaItem(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).orElse(null);
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }
}
