package dev.crococrystal.occeendercable.common.blockentity;

import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public final class RadarBlockEntity extends AbstractOCComponentBlockEntity {
    private static final String TAG_RANGE = "range";
    private static final int DEFAULT_RANGE = 16;
    private static final int MAX_RANGE = 64;

    private int range = DEFAULT_RANGE;

    public RadarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR.get(), "radar", pos, state);
    }

    @Callback(direct = true, doc = "function():number -- Returns the current radar scan range.")
    public Object[] getRange(Context context, Arguments arguments) {
        return new Object[] { range };
    }

    @Callback(direct = true, doc = "function(range:number):number -- Sets the radar scan range, clamped to 1..64.")
    public Object[] setRange(Context context, Arguments arguments) {
        range = clamp(arguments.checkInteger(0), 1, MAX_RANGE);
        setChanged();
        return new Object[] { range };
    }

    @Callback(doc = "function([range:number]):table -- Scans nearby living entities and players.")
    public Object[] scan(Context context, Arguments arguments) {
        if (level == null) {
            return new Object[] { new Object[0] };
        }

        int scanRange = arguments.count() > 0 ? clamp(arguments.checkInteger(0), 1, MAX_RANGE) : range;
        AABB box = new AABB(worldPosition).inflate(scanRange);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, box, entity -> entity instanceof LivingEntity && entity.isAlive());
        List<Map<String, Object>> result = new ArrayList<>(entities.size());

        for (Entity entity : entities) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", entity.getName().getString());
            entry.put("type", entityTypeName(entity));
            entry.put("isPlayer", entity instanceof Player);
            entry.put("x", entity.getX() - xPosition());
            entry.put("y", entity.getY() - yPosition());
            entry.put("z", entity.getZ() - zPosition());
            entry.put("distance", Math.sqrt(entity.distanceToSqr(xPosition(), yPosition(), zPosition())));
            if (entity instanceof LivingEntity living) {
                entry.put("health", living.getHealth());
                entry.put("maxHealth", living.getMaxHealth());
            }
            result.add(entry);
        }

        context.pause(0.05D);
        return new Object[] { result.toArray(new Map[0]) };
    }

    @Callback(doc = "function([range:number]):number -- Counts nearby living entities and players.")
    public Object[] count(Context context, Arguments arguments) {
        if (level == null) {
            return new Object[] { 0 };
        }
        int scanRange = arguments.count() > 0 ? clamp(arguments.checkInteger(0), 1, MAX_RANGE) : range;
        AABB box = new AABB(worldPosition).inflate(scanRange);
        int count = level.getEntitiesOfClass(Entity.class, box, entity -> entity instanceof LivingEntity && entity.isAlive()).size();
        return new Object[] { count };
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_RANGE)) {
            range = clamp(tag.getInt(TAG_RANGE), 1, MAX_RANGE);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_RANGE, range);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String entityTypeName(Entity entity) {
        ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? entity.getType().toString() : key.toString();
    }
}
