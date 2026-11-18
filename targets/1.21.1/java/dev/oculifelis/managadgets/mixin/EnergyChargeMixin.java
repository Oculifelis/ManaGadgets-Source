package dev.oculifelis.managadgets.mixin;

import com.direwolf20.buildinggadgets2.common.capabilities.EnergyStorageItemstack;
import dev.oculifelis.managadgets.Mode;
import dev.oculifelis.managadgets.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** In mana-only mode gadgets take no FE from chargers. */
@Mixin(value = EnergyStorageItemstack.class, remap = false)
public abstract class EnergyChargeMixin {
    @Inject(method = "receiveEnergy", at = @At("HEAD"), cancellable = true, remap = false)
    private void managadgets$noCharge(int amount, boolean simulate, CallbackInfoReturnable<Integer> ci) {
        if (Platform.mode() == Mode.MANA_ONLY) ci.setReturnValue(0);
    }

    @Inject(method = "canReceive", at = @At("HEAD"), cancellable = true, remap = false)
    private void managadgets$noChargeAccepted(CallbackInfoReturnable<Boolean> ci) {
        if (Platform.mode() == Mode.MANA_ONLY) ci.setReturnValue(false);
    }
}
