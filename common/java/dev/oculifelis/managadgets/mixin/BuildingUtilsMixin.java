package dev.oculifelis.managadgets.mixin;

import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import dev.oculifelis.managadgets.GadgetUser;
import dev.oculifelis.managadgets.ManaPower;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BuildingUtils.class, remap = false)
public abstract class BuildingUtilsMixin {
    @ModifyVariable(method = {"build", "exchange", "removeTickHandler"}, at = @At("HEAD"), argsOnly = true, remap = false)
    private static Player managadgets$enter(Player player) {
        GadgetUser.push(player);
        return player;
    }

    @Inject(method = {"build", "exchange", "removeTickHandler"}, at = @At("RETURN"), remap = false)
    private static void managadgets$leave(CallbackInfoReturnable<?> ci) {
        GadgetUser.pop();
    }

    @Inject(method = "getEnergyStored", at = @At("RETURN"), cancellable = true, remap = false)
    private static void managadgets$stored(ItemStack gadget, CallbackInfoReturnable<Integer> ci) {
        Player player = GadgetUser.current();
        if (player != null) ci.setReturnValue(ManaPower.stored(gadget, player, ci.getReturnValue()));
    }

    @Inject(method = "useEnergy", at = @At("HEAD"), cancellable = true, remap = false)
    private static void managadgets$use(ItemStack gadget, CallbackInfo ci) {
        Player player = GadgetUser.current();
        if (player != null && ManaPower.pay(gadget, player, BuildingUtils.getEnergyCost(gadget))) ci.cancel();
    }
}
