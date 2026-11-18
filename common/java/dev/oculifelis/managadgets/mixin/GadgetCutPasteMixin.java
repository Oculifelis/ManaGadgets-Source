package dev.oculifelis.managadgets.mixin;

import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import dev.oculifelis.managadgets.GadgetUser;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GadgetCutPaste.class, remap = false)
public abstract class GadgetCutPasteMixin {
    @ModifyVariable(method = "cutAndStore", at = @At("HEAD"), argsOnly = true, remap = false)
    private Player managadgets$enter(Player player) {
        GadgetUser.push(player);
        return player;
    }

    @Inject(method = "cutAndStore", at = @At("RETURN"), remap = false)
    private void managadgets$leave(CallbackInfo ci) {
        GadgetUser.pop();
    }
}
