package dev.oculifelis.managadgets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

/** Turns the player's carried mana into the FE numbers Building Gadgets works with. */
public final class ManaPower {
    private ManaPower() {
    }

    /** FE the gadget reports as stored, given what its own battery holds. */
    public static int stored(ItemStack gadget, Player player, int feStored) {
        Mode mode = Platform.mode();
        if (mode == Mode.FE_ONLY || player == null) return feStored;
        int fromMana = toFe(availableMana(gadget, player), Platform.manaPerFe());
        return mode == Mode.MANA_ONLY ? fromMana : saturatingAdd(feStored, fromMana);
    }

    /** Pays one use of the gadget in mana. Returns false when the FE battery should pay instead. */
    public static boolean pay(ItemStack gadget, Player player, int feCost) {
        Mode mode = Platform.mode();
        if (mode == Mode.FE_ONLY || player == null) return false;
        int mana = toMana(feCost, Platform.manaPerFe());
        if (mana <= 0) return true;
        boolean paid = ManaItemHandler.instance().requestManaExact(gadget, player, mana, true);
        return paid || mode == Mode.MANA_ONLY;
    }

    public static int availableMana(ItemStack gadget, Player player) {
        return ManaItemHandler.instance().requestMana(gadget, player, Integer.MAX_VALUE, false);
    }

    /** Mana capacity of everything the player carries that gadgets can draw on. */
    public static long carriedCapacity(Player player) {
        long max = 0;
        ManaItemHandler handler = ManaItemHandler.instance();
        for (ItemStack stack : handler.getManaItems(player)) max += capacity(stack);
        for (ItemStack stack : handler.getManaAccesories(player)) max += capacity(stack);
        return max;
    }

    private static int capacity(ItemStack stack) {
        ManaItem item = Platform.manaItem(stack);
        return item == null ? 0 : Math.max(0, item.getMaxMana());
    }

    static int toFe(long mana, double manaPerFe) {
        if (manaPerFe <= 0) return Integer.MAX_VALUE;
        double fe = Math.floor(mana / manaPerFe);
        return fe >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fe;
    }

    static int toMana(int fe, double manaPerFe) {
        if (manaPerFe <= 0 || fe <= 0) return 0;
        double mana = Math.ceil(fe * manaPerFe - 1e-9);
        return mana >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) mana;
    }

    private static int saturatingAdd(int a, int b) {
        long sum = (long) a + b;
        return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }
}
