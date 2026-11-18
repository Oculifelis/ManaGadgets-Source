package dev.oculifelis.managadgets.test;

import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.logging.LogUtils;
import dev.oculifelis.managadgets.ManaDisplay;
import dev.oculifelis.managadgets.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import vazkii.botania.api.mana.ManaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Dev-only check in a real client, armed only when the hidden test kit's hiddentest.properties is in the game folder.
 * In the kit's superflat world it makes the player survival, gives a gadget, a full mana tablet and stone, builds one
 * block with mana, checks the gadget's bar and tooltip on the client, then takes the tablet away and checks that a
 * second build does nothing. Logs HIDDENTEST lines; the kit stops the client.
 */
public final class ClientCheck {
    private static final Logger LOG = LogUtils.getLogger();
    private static int ticks;
    private static BlockPos first;
    private static BlockPos second;
    private static int manaBefore;
    private static int cost;

    private ClientCheck() {
    }

    public static void arm() {
        LOG.info("HIDDENTEST managadgets client check armed");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    private static Item item(String ns, String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ns, path));
    }

    private static void pass(String what) {
        LOG.info("HIDDENTEST PASS managadgets {}", what);
    }

    private static void fail(String what) {
        LOG.info("HIDDENTEST FAIL managadgets {}", what);
    }

    private static void check(boolean ok, String what) {
        if (ok) pass(what);
        else fail(what);
    }

    private static void onServer(Minecraft mc, java.util.function.BiConsumer<ServerLevel, ServerPlayer> job) {
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null) {
            fail("no integrated server");
            return;
        }
        server.execute(() -> {
            try {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                job.accept(p.serverLevel(), p);
            } catch (Throwable t) {
                LOG.error("HIDDENTEST managadgets server step threw", t);
                fail("server step threw " + t);
            }
        });
    }

    private static ItemStack tablet(ServerPlayer p) {
        for (ItemStack s : p.getInventory().items) {
            if (s.is(item("botania", "mana_tablet"))) return s;
        }
        return ItemStack.EMPTY;
    }

    private static void build(ServerLevel level, ServerPlayer p, BlockPos at) {
        ArrayList<StatePos> list = new ArrayList<>(List.of(new StatePos(Blocks.STONE.defaultBlockState(), BlockPos.ZERO)));
        BuildingUtils.build(level, p, list, at, p.getMainHandItem(), true);
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        ticks++;
        try {
            switch (ticks) {
                case 100 -> onServer(mc, (level, p) -> {
                    p.setGameMode(GameType.SURVIVAL);
                    p.getInventory().clearContent();
                    ItemStack gadget = new ItemStack(item("buildinggadgets2", "gadget_building"));
                    p.getInventory().setItem(0, gadget);
                    p.getInventory().selected = 0;
                    ItemStack tablet = new ItemStack(item("botania", "mana_tablet"));
                    ManaItem mana = Platform.manaItem(tablet);
                    mana.addMana(mana.getMaxMana());
                    p.getInventory().setItem(1, tablet);
                    p.getInventory().setItem(2, new ItemStack(Items.STONE, 16));
                    first = p.blockPosition().offset(3, 0, 0);
                    second = p.blockPosition().offset(3, 0, 2);
                    level.setBlockAndUpdate(first, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(second, Blocks.AIR.defaultBlockState());
                    manaBefore = Platform.manaItem(tablet).getMana();
                    cost = BuildingUtils.getEnergyCost(gadget);
                    check(!p.isCreative(), "player is survival");
                    build(level, p, first);
                });
                case 120 -> {
                    ItemStack held = mc.player.getMainHandItem();
                    check(held.is(item("buildinggadgets2", "gadget_building")), "client sees the gadget in hand");
                    check(held.isBarVisible() && held.getBarColor() == ManaDisplay.BAR_COLOR,
                            "gadget bar shows mana (visible=" + held.isBarVisible() + " color=" + Integer.toHexString(held.getBarColor()) + ")");
                    List<Component> lines = held.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL);
                    String text = lines.stream().map(Component::getString).reduce("", (a, b) -> a + " | " + b);
                    check(text.contains("Mana:"), "tooltip shows mana:" + text);
                }
                case 140 -> onServer(mc, (level, p) -> {
                    check(level.getBlockState(first).is(Blocks.STONE), "gadget built a block with mana at " + first);
                    int spent = manaBefore - Platform.manaItem(tablet(p)).getMana();
                    check(spent == cost, "tablet paid " + spent + " mana for a " + cost + " FE build");
                    p.getInventory().removeItem(tablet(p));
                    check(tablet(p).isEmpty(), "tablet removed");
                    build(level, p, second);
                });
                case 190 -> onServer(mc, (level, p) -> {
                    check(level.getBlockState(second).isAir(), "gadget refused to build without mana at " + second);
                    LOG.info("HIDDENTEST managadgets client check finished");
                });
                default -> {
                }
            }
        } catch (Throwable t) {
            LOG.error("HIDDENTEST managadgets client step threw", t);
            fail("client step threw " + t);
        }
    }
}
