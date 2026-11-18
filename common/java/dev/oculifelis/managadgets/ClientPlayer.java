package dev.oculifelis.managadgets;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/** Only loaded on the client. */
public final class ClientPlayer {
    private ClientPlayer() {
    }

    public static Player get() {
        return Minecraft.getInstance().player;
    }
}
