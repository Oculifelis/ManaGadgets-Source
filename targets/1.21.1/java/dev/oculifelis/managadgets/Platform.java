package dev.oculifelis.managadgets;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.ItemCapability;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;

/** The few things that differ between Forge 1.20.1 and NeoForge 1.21.1. */
public final class Platform {
    private static ItemCapability<ManaItem, Void> manaItemCap;

    private Platform() {
    }

    public static Mode mode() {
        return ManaGadgetsConfig.mode();
    }

    public static double manaPerFe() {
        return ManaGadgetsConfig.manaPerFe();
    }

    public static ManaItem manaItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (manaItemCap == null) manaItemCap = BotaniaNeoForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP);
        return stack.getCapability(manaItemCap);
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }
}
