package dev.crococrystal.occeendercable.common.registry;

import dev.crococrystal.occeendercable.EnderCableMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final ResourceKey<CreativeModeTab> MAIN_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        new ResourceLocation(EnderCableMod.MOD_ID, "main")
    );

    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnderCableMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + EnderCableMod.MOD_ID + ".main"))
        .icon(() -> new ItemStack(ModBlocks.ENDER_CABLE_ITEM.get()))
        .displayItems((parameters, output) -> {
            output.accept(ModBlocks.ENDER_CABLE_ITEM.get());
            output.accept(ModBlocks.NETWORK_VISUALIZER.get());
        })
        .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    private ModCreativeTabs() {
    }
}
