package dev.oculifelis.managadgets.mixin;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import dev.oculifelis.managadgets.ClientPlayer;
import dev.oculifelis.managadgets.ManaDisplay;
import dev.oculifelis.managadgets.Platform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** In mana-only mode the gadget's bar shows the mana the player carries. Names cover dev and Forge 1.20.1 SRG. */
@Mixin(value = BaseGadget.class, remap = false)
public abstract class BaseGadgetBarMixin {
    private static Player managadgets$viewer() {
        return Platform.isClient() && ManaDisplay.replacesFe() ? ClientPlayer.get() : null;
    }

    @Inject(method = {"isBarVisible", "m_142522_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void managadgets$visible(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if (managadgets$viewer() != null) ci.setReturnValue(true);
    }

    @Inject(method = {"getBarWidth", "m_142158_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void managadgets$width(ItemStack stack, CallbackInfoReturnable<Integer> ci) {
        Player player = managadgets$viewer();
        if (player != null) ci.setReturnValue(ManaDisplay.barWidth(stack, player));
    }

    @Inject(method = {"getBarColor", "m_142159_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void managadgets$color(ItemStack stack, CallbackInfoReturnable<Integer> ci) {
        if (managadgets$viewer() != null) ci.setReturnValue(ManaDisplay.BAR_COLOR);
    }
}
