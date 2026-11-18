package dev.oculifelis.managadgets;

import java.util.ArrayDeque;
import net.minecraft.world.entity.player.Player;

/**
 * The player whose gadget is working right now. Building Gadgets' energy helpers only get the gadget stack, so the
 * methods that do have the player (build, exchange, remove, cut, undo, the previews) push it here while they run.
 */
public final class GadgetUser {
    private static final ThreadLocal<ArrayDeque<Player>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private GadgetUser() {
    }

    public static void push(Player player) {
        ArrayDeque<Player> stack = STACK.get();
        if (stack.size() > 16) stack.clear(); // something threw past a pop; start clean
        stack.push(player);
    }

    public static void pop() {
        ArrayDeque<Player> stack = STACK.get();
        if (!stack.isEmpty()) stack.pop();
    }

    public static Player current() {
        return STACK.get().peek();
    }
}
