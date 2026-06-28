package dev.crococrystal.occeendercable.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModCreativeTabContents {
    private static final ResourceKey<CreativeModeTab> OPENCOMPUTERS_MAIN_TAB =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("opencomputers", "main"));

    public static void register(IEventBus bus) {
        bus.addListener(ModCreativeTabContents::buildContents);
    }

    private static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (OPENCOMPUTERS_MAIN_TAB.equals(event.getTabKey())) {
            event.accept(ModBlocks.ENDER_CABLE_ITEM.get());
            event.accept(ModBlocks.RADAR_ITEM.get());
            event.accept(ModBlocks.CHAT_BOX_ITEM.get());
            event.accept(ModBlocks.NETWORK_VISUALIZER.get());
        }
        if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            event.accept(ModBlocks.NETWORK_VISUALIZER.get());
        }
    }

    private ModCreativeTabContents() {
    }
}
