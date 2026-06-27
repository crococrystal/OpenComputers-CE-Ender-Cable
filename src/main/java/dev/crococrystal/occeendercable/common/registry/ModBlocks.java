package dev.crococrystal.occeendercable.common.registry;

import dev.crococrystal.occeendercable.EnderCableMod;
import dev.crococrystal.occeendercable.common.block.EnderCableBlock;
import dev.crococrystal.occeendercable.common.item.NetworkVisualizerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EnderCableMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnderCableMod.MOD_ID);

    public static final RegistryObject<Block> ENDER_CABLE = BLOCKS.register("ender_cable", () -> new EnderCableBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(0.6F)
            .sound(SoundType.METAL)
            .noOcclusion()
    ));

    public static final RegistryObject<Item> ENDER_CABLE_ITEM = ITEMS.register("ender_cable", () ->
        new BlockItem(ENDER_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> NETWORK_VISUALIZER = ITEMS.register("network_visualizer", () ->
        new NetworkVisualizerItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private ModBlocks() {
    }
}
