package dev.oculifelis.managadgets;

/** Where gadgets get their power. */
public enum Mode {
    /** Mana only. Gadgets can't be charged with FE. */
    MANA_ONLY,
    /** Mana when the player carries enough, FE otherwise. */
    MANA_FIRST,
    /** Plain Building Gadgets: FE only. */
    FE_ONLY
}
