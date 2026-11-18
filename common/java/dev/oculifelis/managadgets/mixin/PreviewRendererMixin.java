package dev.oculifelis.managadgets.mixin;

import dev.oculifelis.managadgets.GadgetUser;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The build previews check for enough energy to pick their colour, so they get the player too. */
@Mixin(targets = {"com.direwolf20.buildinggadgets2.client.renderer.VBORenderer",
        "com.direwolf20.buildinggadgets2.client.renderer.DestructionRenderer"}, remap = false)
public abstract class PreviewRendererMixin {
    @ModifyVariable(method = {"drawRender", "render"}, at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
    private static Player managadgets$enter(Player player) {
        GadgetUser.push(player);
        return player;
    }

    @Inject(method = {"drawRender", "render"}, at = @At("RETURN"), remap = false, require = 0)
    private static void managadgets$leave(CallbackInfo ci) {
        GadgetUser.pop();
    }
}
