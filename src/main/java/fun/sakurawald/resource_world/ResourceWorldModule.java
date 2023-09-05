package fun.sakurawald.resource_world;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Lifecycle;
import fun.sakurawald.ModMain;
import fun.sakurawald.mixin.MinecraftServerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;

public class ResourceWorldModule {

    private static final String DEFAULT_WORLD_PREFIX = "resource_world";
    private static final String DEFAULT_THE_NETHER_PATH = "the_nether";
    private static final String DEFAULT_THE_END_PATH = "the_end";
    private static final String DEFAULT_OVERWORLD_PATH = "overworld";
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    public static void registerScheduleTask(MinecraftServer server) {
        LocalTime now = LocalTime.now();
        long initialDelay = LocalTime.of(20, 0).toSecondOfDay() - now.toSecondOfDay();
        if (initialDelay < 0) {
            initialDelay += TimeUnit.DAYS.toSeconds(1);
        }
        executorService.scheduleAtFixedRate(() -> {
            ModMain.LOGGER.info("Start to reset resource worlds.");
            server.execute(() -> ResourceWorldModule.resetWorlds(server));
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public static LiteralCommandNode<ServerCommandSource> registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        return dispatcher.register(
                CommandManager.literal("rw")
                        .then(literal("reset").requires(source -> source.hasPermissionLevel(4)).executes(ResourceWorldModule::resetWorlds))
                        .then(literal("create").requires(source -> source.hasPermissionLevel(4)).then(literal(DEFAULT_OVERWORLD_PATH).executes(ResourceWorldModule::createWorld))
                                .then(literal(DEFAULT_THE_NETHER_PATH).executes(ResourceWorldModule::createWorld))
                                .then(literal(DEFAULT_THE_END_PATH).executes(ResourceWorldModule::createWorld)))
                        .then(literal("delete").requires(source -> source.hasPermissionLevel(4)).then(literal(DEFAULT_OVERWORLD_PATH).executes(ResourceWorldModule::deleteWorld))
                                .then(literal(DEFAULT_THE_NETHER_PATH).executes(ResourceWorldModule::deleteWorld))
                                .then(literal(DEFAULT_THE_END_PATH).executes(ResourceWorldModule::deleteWorld)))
                        .then(literal("tp").then(literal(DEFAULT_OVERWORLD_PATH).executes(ResourceWorldModule::teleportWorld))
                                .then(literal(DEFAULT_THE_NETHER_PATH).executes(ResourceWorldModule::teleportWorld))
                                .then(literal(DEFAULT_THE_END_PATH).executes(ResourceWorldModule::teleportWorld))
                        )
        );
    }

    private static int resetWorlds(CommandContext<ServerCommandSource> ctx) {
        resetWorlds(ctx.getSource().getServer());
        return 1;
    }

    private static void resetWorlds(MinecraftServer server) {
        broadcast(server, "Start to reset resource worlds...");
        deleteWorld(server, DEFAULT_OVERWORLD_PATH);
        deleteWorld(server, DEFAULT_THE_NETHER_PATH);
        deleteWorld(server, DEFAULT_THE_END_PATH);
    }

    public static void loadWorlds(MinecraftServer server) {
        createWorld(server, DimensionTypes.OVERWORLD, DEFAULT_OVERWORLD_PATH);
        createWorld(server, DimensionTypes.THE_NETHER, DEFAULT_THE_NETHER_PATH);
        createWorld(server, DimensionTypes.THE_END, DEFAULT_THE_END_PATH);
    }

    @SuppressWarnings("DataFlowIssue")
    private static ChunkGenerator getChunkGenerator(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        if (dimensionTypeRegistryKey == DimensionTypes.OVERWORLD) {
            return server.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator();
        }
        if (dimensionTypeRegistryKey == DimensionTypes.THE_NETHER) {
            return server.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
        }
        if (dimensionTypeRegistryKey == DimensionTypes.THE_END) {
            return server.getWorld(World.END).getChunkManager().getChunkGenerator();
        }
        return null;
    }

    private static DimensionOptions createDimensionOptions(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        RegistryEntry<DimensionType> dimensionTypeRegistryEntry = getDimensionTypeRegistryEntry(server, dimensionTypeRegistryKey);
        ChunkGenerator chunkGenerator = getChunkGenerator(server, dimensionTypeRegistryKey);
        return new DimensionOptions(dimensionTypeRegistryEntry, chunkGenerator);
    }

    private static RegistryEntry<DimensionType> getDimensionTypeRegistryEntry(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        return server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getEntry(dimensionTypeRegistryKey).orElse(null);
    }

    private static RegistryKey<DimensionType> getDimensionTypeRegistryKeyByPath(String path) {
        if (path.equals(DEFAULT_OVERWORLD_PATH)) return DimensionTypes.OVERWORLD;
        if (path.equals(DEFAULT_THE_NETHER_PATH)) return DimensionTypes.THE_NETHER;
        if (path.equals(DEFAULT_THE_END_PATH)) return DimensionTypes.THE_END;
        return null;
    }

    private static void feedback(ServerCommandSource source, String content) {
        source.sendFeedback(() -> Text.literal(content).formatted(Formatting.RED), false);
    }

    private static void broadcast(MinecraftServer server, String content) {
        server.getPlayerManager().broadcast(Text.literal(content).formatted(Formatting.RED), false);
    }

    private static int createWorld(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String path = ctx.getNodes().get(2).getNode().getName();
        RegistryKey<DimensionType> dimensionTypeRegistryKey = getDimensionTypeRegistryKeyByPath(path);
        createWorld(server, dimensionTypeRegistryKey, path);
        return 1;
    }

    private static SimpleRegistry<DimensionOptions> getDimensionOptionsRegistry(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        return (SimpleRegistry<DimensionOptions>) registryManager.get(RegistryKeys.DIMENSION);
    }

    public static void createWorld(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey, String path) {
        /* create the world */
        long seed = RandomSeed.getSeed();
        ResourceWorldProperties resourceWorldProperties = new ResourceWorldProperties(server.getSaveProperties(), seed);
        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(DEFAULT_WORLD_PREFIX, path));
        DimensionOptions dimensionOptions = createDimensionOptions(server, dimensionTypeRegistryKey);
        ServerWorld world = new ResourceWorld(server,
                Util.getMainWorkerExecutor(),
                ((MinecraftServerAccessor) server).getSession(),
                resourceWorldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(seed),
                ImmutableList.of(),
                true,
                null);

        if (dimensionTypeRegistryKey == DimensionTypes.THE_END) {
            world.setEnderDragonFight(new EnderDragonFight(world, world.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        /* register the world */
        ((FantasyDimensionOptions) (Object) dimensionOptions).sakurawald$setSaveProperties(false);

        SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionOptionsRegistry(server);
        boolean isFrozen = ((RemoveFromRegistry<?>) dimensionsRegistry).sakurawald$isFrozen();
        ((RemoveFromRegistry<?>) dimensionsRegistry).sakurawald$setFrozen(false);
        var dimensionOptionsRegistryKey = RegistryKey.of(RegistryKeys.DIMENSION, worldRegistryKey.getValue());
        if (!dimensionsRegistry.contains(dimensionOptionsRegistryKey)) {
            dimensionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, Lifecycle.stable());
        }
        ((RemoveFromRegistry<?>) dimensionsRegistry).sakurawald$setFrozen(isFrozen);

        ((MinecraftServerAccessor) server).getWorlds().put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().onWorldLoad(server, world);
        world.tick(() -> true);

        broadcast(server, String.format("Create resource world %s done.", path));
    }

    private static ServerWorld getResourceWorldByPath(MinecraftServer server, String path) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(DEFAULT_WORLD_PREFIX, path));
        return server.getWorld(worldKey);
    }

    private static BlockPos createSafePlatform(ServerWorld world, BlockPos pos) {
        world.setBlockState(pos.down(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().north(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().north().west(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().north().east(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().south(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().south().west(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().south().east(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().west(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos.down().east(), Blocks.BEDROCK.getDefaultState());
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
        return pos;
    }

    private static int teleportWorld(CommandContext<ServerCommandSource> ctx) {
        String path = ctx.getNodes().get(2).getNode().getName();
        ServerWorld world = getResourceWorldByPath(ctx.getSource().getServer(), path);
        if (world == null) {
            feedback(ctx.getSource(), String.format("Target resource world %s doesn't exist.", path));
            return 0;
        }

        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        BlockPos destPos;
        float destYaw = 90;
        float destPitch = 0;
        if (world.getDimensionKey() == DimensionTypes.THE_END) {
            ServerWorld.createEndSpawnPlatform(world);
            destPos = ServerWorld.END_SPAWN_POS;
        } else {
            destPos = createSafePlatform(world, new BlockPos(0, 96, 0));
        }

        player.teleport(world, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, destYaw, destPitch);
        return 1;
    }


    public static void deleteWorld(MinecraftServer server, String path) {
        ServerWorld world = getResourceWorldByPath(server, path);
        if (world == null) return;

        ResourceWorldManager.enqueueWorldDeletion(world);
        broadcast(server, String.format("Delete resource world %s done.", path));
    }


    private static int deleteWorld(CommandContext<ServerCommandSource> ctx) {
        String path = ctx.getNodes().get(2).getNode().getName();
        ServerWorld world = getResourceWorldByPath(ctx.getSource().getServer(), path);
        if (world == null) {
            feedback(ctx.getSource(), String.format("Target resource world %s doesn't exist.", path));
            return 0;
        }

        deleteWorld(ctx.getSource().getServer(), path);
        return 1;
    }


    public static void onWorldUnload(MinecraftServer server, ServerWorld world) {
        if (server.isRunning()) {
            String namespace = world.getRegistryKey().getValue().getNamespace();
            String path = world.getRegistryKey().getValue().getPath();
            // Important: only delete the world if it's a resource world
            if (!namespace.equals(DEFAULT_WORLD_PREFIX)) return;

            server.sendMessage(Text.of(String.format("Creating world %s ...", path)));
            ResourceWorldModule.createWorld(server, ResourceWorldModule.getDimensionTypeRegistryKeyByPath(path), path);
        }
    }
}
