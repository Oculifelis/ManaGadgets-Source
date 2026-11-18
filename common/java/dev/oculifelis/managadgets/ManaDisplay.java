package dev.oculifelis.managadgets;

import java.text.NumberFormat;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Client side: the gadget's bar and tooltip show carried mana instead of the FE battery. */
public final class ManaDisplay {
    private static final String ENERGY_LINE = "buildinggadgets2.tooltips.energy";
    public static final int BAR_COLOR = 0x3FA9F5;

    private ManaDisplay() {
    }

    public static boolean replacesFe() {
        return Platform.mode() == Mode.MANA_ONLY;
    }

    public static int barWidth(ItemStack gadget, Player player) {
        long max = ManaPower.carriedCapacity(player);
        if (max <= 0) return 0;
        long mana = ManaPower.availableMana(gadget, player);
        return (int) Math.min(13, Math.round(13.0 * mana / max));
    }

    /** Swaps the gadget's "Energy" line for a mana line (mana only), or adds one under it (mana first). */
    public static void tooltip(ItemStack gadget, Player player, List<Component> lines) {
        Mode mode = Platform.mode();
        if (mode == Mode.FE_ONLY || player == null) return;
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.getContents() instanceof TranslatableContents tc && ENERGY_LINE.equals(tc.getKey())) {
                NumberFormat fmt = NumberFormat.getIntegerInstance();
                MutableComponent mana = Component.translatable("managadgets.tooltip.mana",
                        fmt.format(ManaPower.availableMana(gadget, player)),
                        fmt.format(ManaPower.carriedCapacity(player))).withStyle(line.getStyle());
                if (mode == Mode.MANA_ONLY) lines.set(i, mana);
                else lines.add(i + 1, mana);
                return;
            }
        }
    }
}
