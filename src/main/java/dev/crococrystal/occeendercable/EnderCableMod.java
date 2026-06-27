package dev.crococrystal.occeendercable;

import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import dev.crococrystal.occeendercable.common.registry.ModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnderCableMod.MOD_ID)
public final class EnderCableMod {
    public static final String MOD_ID = "occe_ender_cable";

    public EnderCableMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(bus);
        ModBlockEntities.register(bus);
    }
}
