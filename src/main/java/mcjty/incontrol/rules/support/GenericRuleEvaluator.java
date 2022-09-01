package mcjty.incontrol.rules.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.tools.rules.CommonRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.typed.AttributeMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class GenericRuleEvaluator extends CommonRuleEvaluator {

    public GenericRuleEvaluator(AttributeMap map) {
        super(map, InControl.setup.getLogger(), new ModRuleCompatibilityLayer());
    }

    @Override
    protected void addChecks(AttributeMap map) {
        super.addChecks(map);

        if (map.has(HOSTILE)) {
            addHostileCheck(map);
        }
        if (map.has(PASSIVE)) {
            addPassiveCheck(map);
        }
        if (map.has(MOD)) {
            addModsCheck(map);
        }
        if (map.has(MOB)) {
            addMobsCheck(map);
        }
        if (map.has(CANSPAWNHERE)) {
            addCanSpawnHereCheck(map);
        }
        if (map.has(NOTCOLLIDING)) {
            addNotCollidingCheck(map);
        }
        if (map.has(SPAWNER)) {
            addSpawnerCheck(map);
        }
        if (map.has(PLAYER)) {
            addPlayerCheck(map);
        }
        if (map.has(REALPLAYER)) {
            addRealPlayerCheck(map);
        }
        if (map.has(FAKEPLAYER)) {
            addFakePlayerCheck(map);
        }
        if (map.has(EXPLOSION)) {
            addExplosionCheck(map);
        }
        if (map.has(PROJECTILE)) {
            addProjectileCheck(map);
        }
        if (map.has(FIRE)) {
            addFireCheck(map);
        }
        if (map.has(MAGIC)) {
            addMagicCheck(map);
        }

        if (map.has(SOURCE)) {
            addSourceCheck(map);
        }

        if (map.has(MINCOUNT)) {
            addMinCountCheck(map);
        }
        if (map.has(MAXCOUNT)) {
            addMaxCountCheck(map);
        }
    }

    private void addCanSpawnHereCheck(AttributeMap map) {
        boolean c = map.get(CANSPAWNHERE);
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof MobEntity) {
                    return MobEntity.canSpawnOn((EntityType<? extends MobEntity>) entity.getType(), entity.getEntityWorld(), SpawnReason.NATURAL, entity.getPosition(), null);
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof MobEntity) {
                    return !MobEntity.canSpawnOn((EntityType<? extends MobEntity>) entity.getType(), entity.getEntityWorld(), SpawnReason.NATURAL, entity.getPosition(), null);
                } else {
                    return true;
                }
            });
        }
    }

    private void addNotCollidingCheck(AttributeMap map) {
        boolean c = map.get(NOTCOLLIDING);
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof MobEntity) {
                    return ((MobEntity) entity).isNotColliding(entity.getEntityWorld());
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof MobEntity) {
                    return !((MobEntity) entity).isNotColliding(entity.getEntityWorld());
                } else {
                    return true;
                }
            });
        }
    }

    private void addSpawnerCheck(AttributeMap map) {
        boolean c = map.get(SPAWNER);
        if (c) {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn) {
                    LivingSpawnEvent.CheckSpawn checkSpawn = (LivingSpawnEvent.CheckSpawn) event;
                    return checkSpawn.isSpawner();
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn) {
                    LivingSpawnEvent.CheckSpawn checkSpawn = (LivingSpawnEvent.CheckSpawn) event;
                    return !checkSpawn.isSpawner();
                } else {
                    return false;
                }
            });
        }
    }

    private void addHostileCheck(AttributeMap map) {
        if (map.get(HOSTILE)) {
            checks.add((event, query) -> query.getEntity(event) instanceof IMob);
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof IMob));
        }
    }

    private void addPassiveCheck(AttributeMap map) {
        if (map.get(PASSIVE)) {
            checks.add((event, query) -> (query.getEntity(event) instanceof AnimalEntity && !(query.getEntity(event) instanceof IMob)));
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof AnimalEntity && !(query.getEntity(event) instanceof IMob)));
        }
    }
private boolean entityTypeCheck(AttributeMap map,Set<EntityType> classes,IEventQuery query,Event event){
    if(map.has(NAME)){
        String name = map.get(NAME);
        //System.out.println("name : " + name);
    }
    EntityType eventType=query.getEntity(event).getType();
    //System.out.println(eventType.getRegistryName());
    for (EntityType entityType : classes) {
        //System.out.println(entityType.getRegistryName());
    }
    return classes.contains(eventType);

}
    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);

        //System.out.println("mobs.size()"+mobs.size());
        if (mobs.size() == 1) {
            String id = mobs.get(0);
            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            if (type != null) {
                checks.add((event, query) -> type.equals(query.getEntity(event).getType()));
            } else {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + id + "'!");
            }
        } else {
            Set<EntityType> classes = new HashSet<>();
            for (String id : mobs) {
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));

                if (type != null) {
                    classes.add(type);
                } else {
                    InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + id +"'!");
                }
            }
            if (!classes.isEmpty()) {
                checks.add((event, query) -> entityTypeCheck(map,classes,query, event));
            }
        }
    }

    private void addModsCheck(AttributeMap map) {
        List<String> mods = map.getList(MOD);
        
        if (mods.size() == 1) {
            String modid = mods.get(0);
            checks.add((event, query) -> {
                String mod = query.getEntity(event).getType().getRegistryName().getNamespace();
                //this.logger.log(Level.INFO,mods.toString()+"=>"+mod);
                return modid.equals(mod);
            });
        } else {
            Set<String> modids = new HashSet<>();
            for (String modid : mods) {
                modids.add(modid);
            }
            checks.add((event, query) -> {
                String mod = query.getEntity(event).getType().getRegistryName().getNamespace();
                //this.logger.log(Level.INFO,mods.toString()+"=>"+mod);
                return modids.contains(mod);
            });
        }
    }

    private static class CountInfo {
        private int amount;
        private List<EntityType> entityTypes = new ArrayList<>();
        private boolean scaledPerPlayer = false;
        private boolean scaledPerChunk = false;
        private boolean passive = false;
        private boolean hostile = false;
        private String mod = null;

        public CountInfo() {
        }

        public CountInfo setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public CountInfo addEntityType(EntityType entityClass) {
            if (entityClass != null) {
                this.entityTypes.add(entityClass);
            }
            return this;
        }

        public CountInfo setScaledPerPlayer(boolean scaledPerPlayer) {
            this.scaledPerPlayer = scaledPerPlayer;
            return this;
        }

        public CountInfo setScaledPerChunk(boolean scaledPerChunk) {
            this.scaledPerChunk = scaledPerChunk;
            return this;
        }

        public CountInfo setPassive(boolean passive) {
            this.passive = passive;
            return this;
        }

        public CountInfo setHostile(boolean hostile) {
            this.hostile = hostile;
            return this;
        }

        public CountInfo setMod(String mod) {
            this.mod = mod;
            return this;
        }

        public String validate() {
            if (scaledPerPlayer && scaledPerChunk) {
                return "You cannot combine 'perchunk' and 'perplayer'!";
            }
            if (mod != null && !entityTypes.isEmpty()) {
                return "You cannot combine 'mod' with 'mob'!";
            }
            if (passive && hostile) {
                return "Don't use passive and hostile at the same time!";
            }
            if ((passive || hostile) && !entityTypes.isEmpty()) {
                return "You cannot combine 'passive' or 'hostile' with 'mob'!";
            }
            return null;
        }
    }

    @Nullable
    private CountInfo parseCountInfo(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                String[] splitted = StringUtils.split(element.getAsString(), ',');
                int amount;
                try {
                    amount = Integer.parseInt(splitted[0]);
                } catch (NumberFormatException e) {
                    InControl.setup.getLogger().log(Level.ERROR, "Bad amount for mincount '" + splitted[0] + "'!");
                    return null;
                }
                EntityType   entityClass = null;
                if (splitted.length > 1) {
                    entityClass = findEntity(splitted[1]);
                    if (entityClass == null) {
                        InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '" + splitted[1] + "'!");
                        return null;
                    }
                }
                return new CountInfo().setAmount(amount).addEntityType(entityClass);
            } else {
                int amount = element.getAsInt();
                return new CountInfo().setAmount(amount);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            int amount = obj.get("amount").getAsInt();
            CountInfo info = new CountInfo().setAmount(amount);
            if (obj.has("mob")) {
                if (obj.get("mob").isJsonPrimitive()) {
                    String entity = obj.get("mob").getAsString();
                    EntityType entityType = findEntity(entity);
                    if (entityType == null) return null;
                    info.addEntityType(entityType);
                } else if (obj.get("mob").isJsonArray()) {
                    JsonArray array = obj.get("mob").getAsJsonArray();
                    for (JsonElement el : array) {
                        String entity = el.getAsString();
                        EntityType entityType = findEntity(entity);
                        if (entityType == null) {
                            InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '" + entity + "'!");
                            return null;
                        }
                        info.addEntityType(entityType);
                    }
                } else {
                    InControl.setup.getLogger().log(Level.ERROR, "Bad entity tag in count description!");
                    return null;
                }
            }
            if (obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                info.setMod(mod);
            }
            if (obj.has("perplayer")) {
                info.setScaledPerPlayer(obj.get("perplayer").getAsBoolean());
            }
            if (obj.has("perchunk")) {
                info.setScaledPerChunk(obj.get("perchunk").getAsBoolean());
            }
            if (obj.has("passive")) {
                info.setPassive(obj.get("passive").getAsBoolean());
            }
            if (obj.has("hostile")) {
                info.setHostile(obj.get("hostile").getAsBoolean());
            }
            String error = info.validate();
            if (error != null) {
                InControl.setup.getLogger().log(Level.ERROR, error);
                return null;
            }
            return info;
        } else {
            InControl.setup.getLogger().log(Level.ERROR, "Count description '" + json + "' is not valid!");
            return null;
        }
    }

    private EntityType findEntity(String id) {
        EntityType<?> ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        if (ee == null) {
            InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + id + "'!");
            return null;
        }
        return ee;
    }


    private void addMinCountCheck(AttributeMap map) {
        final String json = map.get(MINCOUNT);
        CountInfo info = parseCountInfo(json);
        if (info == null) {
            return;
        }

        BiFunction<World, Entity, Integer> counter = getCounter(info);
        Function<World, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);

        checks.add((event, query) -> {
            World world = query.getWorld(event);
            Entity entity = query.getEntity(event);
            int count = counter.apply(world, entity);
            int amount = amountAdjuster.apply(world);
            return count >= amount;
        });
    }

    private void addMaxCountCheck(AttributeMap map) {
        final String json = map.get(MAXCOUNT);
        CountInfo info = parseCountInfo(json);

        BiFunction<World, Entity, Integer> counter = getCounter(info);
        Function<World, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);

        checks.add((event, query) -> {
            World world = query.getWorld(event);
            Entity entity = query.getEntity(event);
            int count = counter.apply(world, entity);
            int amount = amountAdjuster.apply(world);
            return count < amount;
        });
    }

    private Function<World, Integer> getAmountAdjuster(CountInfo info, int infoAmount) {
        Function<World, Integer> amountAdjuster;
        if (info.scaledPerChunk) {
            amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidSpawnChunks(world) / 289;
        } else if (info.scaledPerPlayer) {
            amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidPlayers(world);
        } else {
            amountAdjuster = world -> infoAmount;
        }
        return amountAdjuster;
    }

    private BiFunction<World, Entity, Integer> getCounter(CountInfo info) {
        BiFunction<World, Entity, Integer> counter;
        if (info.mod != null) {
            if (info.hostile) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModHostile(world, info.mod);
            } else if (info.passive) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModPassive(world, info.mod);
            } else {
                counter = (world, entity) -> InControl.setup.cache.getCountPerMod(world, info.mod);
            }
        } else if (info.hostile) {
            counter = (world, entity) -> InControl.setup.cache.getCountHostile(world);
        } else if (info.passive) {
            counter = (world, entity) -> InControl.setup.cache.getCountPassive(world);
        } else {
            List<EntityType> infoEntityType = info.entityTypes;
            if (infoEntityType.isEmpty()) {
                counter = (world, entity) -> InControl.setup.cache.getCount(world, entity.getType());
            } else if (infoEntityType.size() == 1) {
                counter = (world, entity) -> {
                    EntityType entityType = infoEntityType.get(0);
                    return InControl.setup.cache.getCount(world, entityType);
                };
            } else {
                counter = (world, entity) -> {
                    int amount = 0;
                    for (EntityType cls : infoEntityType) {
                        amount += InControl.setup.cache.getCount(world, cls);
                    }
                    return amount;
                };
            }
        }
        return counter;
    }


    private void addPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(PLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) instanceof PlayerEntity);
        } else {
            checks.add((event, query) -> query.getAttacker(event) instanceof PlayerEntity);
        }
    }


    private boolean isFakePlayer(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        if (entity instanceof FakePlayer) {
            return true;
        }

        // If this returns false it is still possible we have a fake player. Try to find the player in the list of online players
        PlayerList playerList = entity.getEntityWorld().getServer().getPlayerList();
        ServerPlayerEntity playerByUUID = playerList.getPlayerByUUID(((PlayerEntity) entity).getGameProfile().getId());
        if (playerByUUID == null) {
            // The player isn't online. Then it can't be real
            return true;
        }

        // The player is in the list. But is it this player?
        return entity != playerByUUID;
    }

    private boolean isRealPlayer(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        return !isFakePlayer(entity);
    }

    private void addRealPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(REALPLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : isRealPlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !isRealPlayer(query.getAttacker(event)));
        }
    }

    private void addFakePlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(FAKEPLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : isFakePlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !isFakePlayer(query.getAttacker(event)));
        }
    }

    private void addExplosionCheck(AttributeMap map) {
        boolean explosion = map.get(EXPLOSION);
        if (explosion) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isExplosion());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isExplosion());
        }
    }

    private void addProjectileCheck(AttributeMap map) {
        boolean projectile = map.get(PROJECTILE);
        if (projectile) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isProjectile());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isProjectile());
        }
    }

    private void addFireCheck(AttributeMap map) {
        boolean fire = map.get(FIRE);
        if (fire) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isFireDamage());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isFireDamage());
        }
    }

    private void addMagicCheck(AttributeMap map) {
        boolean magic = map.get(MAGIC);
        if (magic) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isMagicDamage());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isMagicDamage());
        }
    }

    private void addSourceCheck(AttributeMap map) {
        List<String> sources = map.getList(SOURCE);
        Set<String> sourceSet = new HashSet<>(sources);
        checks.add((event, query) -> {
            if (query.getSource(event) == null) {
                return false;
            }
            return sourceSet.contains(query.getSource(event).getDamageType());
        });
    }


    @Override
    public boolean match(Event event, IEventQuery query) {
        for (BiFunction<Event, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

}
