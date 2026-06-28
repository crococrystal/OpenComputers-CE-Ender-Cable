package dev.crococrystal.occeendercable.common.registry;

import dev.crococrystal.occeendercable.EnderCableMod;
import dev.crococrystal.occeendercable.common.block.ChatBoxBlock;
import dev.crococrystal.occeendercable.common.block.ComponentDeviceBlock;
import dev.crococrystal.occeendercable.common.block.EnderCableBlock;
import dev.crococrystal.occeendercable.common.blockentity.RadarBlockEntity;
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

    public static final RegistryObject<Block> RADAR = BLOCKS.register("radar", () -> new ComponentDeviceBlock(
        deviceProperties(MapColor.COLOR_CYAN),
        RadarBlockEntity::new
    ));

    public static final RegistryObject<Item> RADAR_ITEM = ITEMS.register("radar", () ->
        new BlockItem(RADAR.get(), new Item.Properties()));

    public static final RegistryObject<Block> CHAT_BOX = BLOCKS.register("chat_box", () ->
        new ChatBoxBlock(deviceProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Item> CHAT_BOX_ITEM = ITEMS.register("chat_box", () ->
        new BlockItem(CHAT_BOX.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private static BlockBehaviour.Properties deviceProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .strength(1.0F)
            .sound(SoundType.METAL);
    }

    private ModBlocks() {
    }
}
