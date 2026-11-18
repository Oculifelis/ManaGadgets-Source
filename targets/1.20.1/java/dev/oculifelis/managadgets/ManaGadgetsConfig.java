package dev.oculifelis.managadgets;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ManaGadgetsConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<Mode> MODE;
    public static final ForgeConfigSpec.DoubleValue MANA_PER_FE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        MODE = b.comment("Where Building Gadgets get their power.",
                        "MANA_ONLY: mana from mana tablets, rings and other mana items you carry. Gadgets can't be charged with FE.",
                        "MANA_FIRST: mana when you carry enough, the gadget's FE otherwise.",
                        "FE_ONLY: plain Building Gadgets.")
                .translation("managadgets.configuration.mode")
                .defineEnum("mode", Mode.MANA_ONLY);
        MANA_PER_FE = b.comment("Mana spent for each FE a gadget would use. Building Gadgets spends 50 FE per block by default.",
                        "0 makes gadgets free.")
                .translation("managadgets.configuration.manaPerFE")
                .defineInRange("manaPerFE", 1.0, 0.0, 1000.0);
        SPEC = b.build();
    }

    private ManaGadgetsConfig() {
    }

    private static volatile Mode forcedMode;

    /** Lets the GameTests switch modes without touching the config file. Null goes back to the config. */
    public static void forceMode(Mode mode) {
        forcedMode = mode;
    }

    public static Mode mode() {
        Mode forced = forcedMode;
        if (forced != null) return forced;
        return SPEC.isLoaded() ? MODE.get() : Mode.MANA_ONLY;
    }

    public static double manaPerFe() {
        return SPEC.isLoaded() ? MANA_PER_FE.get() : 1.0;
    }
}
