package dev.oculifelis.managadgets.test;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;

/** Dev-only mod that holds the GameTests and the hidden-client check. Never shipped. */
@Mod(ManaGadgetsTestMod.ID)
public final class ManaGadgetsTestMod {
    public static final String ID = "managadgets_test";

    public ManaGadgetsTestMod() {
        if (FMLEnvironment.dist.isClient() && Files.exists(FMLPaths.GAMEDIR.get().resolve("hiddentest.properties"))) {
            ClientCheck.arm();
        }
    }
}
