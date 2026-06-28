package dev.crococrystal.occeendercable.common.registry;

import dev.crococrystal.occeendercable.EnderCableMod;
import dev.crococrystal.occeendercable.common.blockentity.ChatBoxBlockEntity;
import dev.crococrystal.occeendercable.common.blockentity.EnderCableBlockEntity;
import dev.crococrystal.occeendercable.common.blockentity.RadarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EnderCableMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<EnderCableBlockEntity>> ENDER_CABLE = BLOCK_ENTITIES.register(
        "ender_cable",
        () -> BlockEntityType.Builder.of(EnderCableBlockEntity::new, ModBlocks.ENDER_CABLE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<RadarBlockEntity>> RADAR = BLOCK_ENTITIES.register(
        "radar",
        () -> BlockEntityType.Builder.of(RadarBlockEntity::new, ModBlocks.RADAR.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ChatBoxBlockEntity>> CHAT_BOX = BLOCK_ENTITIES.register(
        "chat_box",
        () -> BlockEntityType.Builder.of(ChatBoxBlockEntity::new, ModBlocks.CHAT_BOX.get()).build(null)
    );

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private ModBlockEntities() {
    }
}
