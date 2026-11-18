package dev.oculifelis.managadgets.test;

import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.authlib.GameProfile;
import dev.oculifelis.managadgets.ManaGadgetsConfig;
import dev.oculifelis.managadgets.Mode;
import dev.oculifelis.managadgets.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.ManaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-side checks of the energy hooks. Building Gadgets places queued blocks only for players in the server's
 * player list, which a fake player never is, so these tests check that the build was accepted into Building Gadgets'
 * queue (energy check passed) and what it cost. Real block placement is checked in a client (ClientCheck).
 */
@GameTestHolder(ManaGadgetsTestMod.ID)
@PrefixGameTestTemplate(false)
public final class GadgetTests {
    static final BlockPos SPOT = new BlockPos(3, 1, 3);

    static Item item(String ns, String path) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(ns, path));
    }

    static ServerPlayer survivalPlayer(GameTestHelper helper) {
        // The GameTest server hands fake players its default game mode, which is creative; creative skips the energy check.
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "mg_test"));
        player.setGameMode(GameType.SURVIVAL);
        helper.assertFalse(player.isCreative(), "test player is still creative");
        player.getInventory().setItem(2, new ItemStack(Items.STONE, 4));
        return player;
    }

    static ItemStack gadget() {
        return new ItemStack(item("buildinggadgets2", "gadget_building"));
    }

    static IEnergyStorage fe(ItemStack gadget) {
        return gadget.getCapability(ForgeCapabilities.ENERGY).orElse(null);
    }

    static ItemStack fullTablet() {
        ItemStack tablet = new ItemStack(item("botania", "mana_tablet"));
        ManaItem mana = Platform.manaItem(tablet);
        mana.addMana(mana.getMaxMana());
        return tablet;
    }

    static int mana(ItemStack tablet) {
        return Platform.manaItem(tablet).getMana();
    }

    /** Asks Building Gadgets to build one stone; true when it passed the energy check and queued the block. */
    static boolean build(GameTestHelper helper, ServerPlayer player, ItemStack gadget) {
        // BG2 never replaces a solid block, so make sure the spot is empty first.
        helper.setBlock(SPOT, Blocks.AIR);
        ArrayList<StatePos> list = new ArrayList<>(List.of(new StatePos(Blocks.STONE.defaultBlockState(), BlockPos.ZERO)));
        UUID id = BuildingUtils.build(helper.getLevel(), player, list, helper.absolutePos(SPOT), gadget, true);
        boolean queued = ServerTickHandler.buildMap.containsKey(id);
        ServerTickHandler.buildMap.remove(id);
        return queued;
    }

    /** Mana-only: an empty gadget builds from the tablet, and the tablet pays the gadget's FE cost in mana. */
    @GameTest(template = "arena", batch = "buildsWithMana")
    public static void buildsWithMana(GameTestHelper helper) {
        try {
            ManaGadgetsConfig.forceMode(Mode.MANA_ONLY);
            ServerPlayer player = survivalPlayer(helper);
            ItemStack tablet = fullTablet();
            int before = mana(tablet);
            player.getInventory().setItem(1, tablet); // setItem keeps this stack; add() would empty it
            ItemStack gadget = gadget();
            helper.assertTrue(fe(gadget).getEnergyStored() == 0, "gadget should start with no FE");
            int cost = BuildingUtils.getEnergyCost(gadget);
            helper.assertTrue(build(helper, player, gadget), "gadget refused to build with a full tablet");
            int spent = before - mana(tablet);
            helper.assertTrue(spent == cost, "tablet spent " + spent + " mana, expected " + cost);
            helper.assertTrue(fe(gadget).getEnergyStored() == 0, "gadget FE changed");
        } finally {
            ManaGadgetsConfig.forceMode(null);
        }
        helper.succeed();
    }

    /** Mana-only: no mana carried, nothing gets built. */
    @GameTest(template = "arena", batch = "refusesWithoutMana")
    public static void refusesWithoutMana(GameTestHelper helper) {
        try {
            ManaGadgetsConfig.forceMode(Mode.MANA_ONLY);
            ServerPlayer player = survivalPlayer(helper);
            helper.assertFalse(build(helper, player, gadget()), "gadget built with no mana");
        } finally {
            ManaGadgetsConfig.forceMode(null);
        }
        helper.succeed();
    }

    /** FE-only: the tablet is ignored and an empty gadget does nothing. */
    @GameTest(template = "arena", batch = "feOnlyIgnoresMana")
    public static void feOnlyIgnoresMana(GameTestHelper helper) {
        try {
            ManaGadgetsConfig.forceMode(Mode.FE_ONLY);
            ServerPlayer player = survivalPlayer(helper);
            ItemStack tablet = fullTablet();
            int before = mana(tablet);
            player.getInventory().setItem(1, tablet);
            helper.assertFalse(build(helper, player, gadget()), "FE-only gadget built with no FE");
            helper.assertTrue(mana(tablet) == before, "FE-only mode took mana");
        } finally {
            ManaGadgetsConfig.forceMode(null);
        }
        helper.succeed();
    }

    /** Mana-first: mana pays while there is some, the gadget's FE stays put; with no mana the FE pays. */
    @GameTest(template = "arena", batch = "manaFirstUsesManaThenFe")
    public static void manaFirstUsesManaThenFe(GameTestHelper helper) {
        try {
            ManaGadgetsConfig.forceMode(Mode.MANA_FIRST);
            ServerPlayer player = survivalPlayer(helper);
            ItemStack tablet = fullTablet();
            int before = mana(tablet);
            player.getInventory().setItem(1, tablet);
            ItemStack gadget = gadget();
            int charged = fe(gadget).receiveEnergy(10_000, false);
            helper.assertTrue(charged > 0, "mana-first mode refused FE charging");
            int feBefore = fe(gadget).getEnergyStored();
            int cost = BuildingUtils.getEnergyCost(gadget);
            helper.assertTrue(build(helper, player, gadget), "mana-first gadget refused to build");
            helper.assertTrue(before - mana(tablet) == cost, "mana-first did not pay with mana");
            helper.assertTrue(fe(gadget).getEnergyStored() == feBefore, "mana-first spent FE while mana was there");

            player.getInventory().setItem(1, ItemStack.EMPTY);
            helper.assertTrue(build(helper, player, gadget), "mana-first gadget refused to build on FE");
            helper.assertTrue(feBefore - fe(gadget).getEnergyStored() == cost, "mana-first did not fall back to FE");
        } finally {
            ManaGadgetsConfig.forceMode(null);
        }
        helper.succeed();
    }

    /** Mana-only gadgets refuse FE from chargers; FE-only gadgets take it. */
    @GameTest(template = "arena", batch = "chargingFollowsMode")
    public static void chargingFollowsMode(GameTestHelper helper) {
        try {
            ManaGadgetsConfig.forceMode(Mode.MANA_ONLY);
            ItemStack gadget = gadget();
            helper.assertTrue(fe(gadget).receiveEnergy(1000, false) == 0, "mana-only gadget took FE");
            helper.assertTrue(fe(gadget).getEnergyStored() == 0, "mana-only gadget stored FE");
            ManaGadgetsConfig.forceMode(Mode.FE_ONLY);
            helper.assertTrue(fe(gadget).receiveEnergy(1000, false) == 1000, "FE-only gadget refused FE");
        } finally {
            ManaGadgetsConfig.forceMode(null);
        }
        helper.succeed();
    }

    static final String[] GADGETS = {"gadget_building", "gadget_exchanging", "gadget_copy_paste", "gadget_cut_paste", "gadget_destruction"};

    /** Every gadget is made on the runic altar, and no crafting-table recipe for one is left. */
    @GameTest(template = "arena", batch = "gadgetsUseRunicAltar")
    public static void gadgetsUseRunicAltar(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String g : GADGETS) {
            var recipe = recipes.byKey(new ResourceLocation("buildinggadgets2", g));
            helper.assertTrue(recipe.isPresent(), "no recipe " + g);
            String type = String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.get().getType()));
            helper.assertTrue(type.equals("botania:runic_altar"), g + " recipe is " + type);
        }
        for (var r : recipes.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            var out = r.getResultItem(helper.getLevel().registryAccess());
            String id = String.valueOf(BuiltInRegistries.ITEM.getKey(out.getItem()));
            helper.assertFalse(id.startsWith("buildinggadgets2:gadget_"), "crafting recipe still makes " + id);
        }
        helper.succeed();
    }
}
