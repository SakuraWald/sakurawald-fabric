package fun.sakurawald.module.teleport_warmup;

import fun.sakurawald.config.ConfigManager;
import fun.sakurawald.util.MessageUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TeleportWarmupModule {

    public static final HashMap<ServerPlayer, TeleportTicket> tickets = new HashMap<>();
    private static final float MAX_VALUE = 20 * ConfigManager.configWrapper.instance().modules.teleport_warmup.warmup_second;
    private static final float DELFA_PERCENT = 1F / MAX_VALUE;
    private static final double INTERRUPT_DISTANCE = ConfigManager.configWrapper.instance().modules.teleport_warmup.interrupt_distance;

    public static void onServerTick(MinecraftServer server) {

        if (tickets.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            Iterator<Map.Entry<ServerPlayer, TeleportTicket>> iterator = tickets.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ServerPlayer, TeleportTicket> pair = iterator.next();
                ServerBossEvent bossbar = pair.getValue().bossbar;
                bossbar.setProgress(bossbar.getProgress() + DELFA_PERCENT);

                ServerPlayer player = (ServerPlayer) bossbar.getPlayers().toArray()[0];
                TeleportTicket teleportTicket = TeleportWarmupModule.tickets.get(player);

                if (((ServerPlayerAccessor) player).sakurawald$inCombat()) {
                    bossbar.setVisible(false);
                    iterator.remove();
                    MessageUtil.message(player, ConfigManager.configWrapper.instance().modules.teleport_warmup.in_combat_message, true);
                    continue;
                }

                if (player.position().distanceToSqr(teleportTicket.source.getX(), teleportTicket.source.getY(), teleportTicket.source.getZ()) >= INTERRUPT_DISTANCE) {
                    bossbar.setVisible(false);
                    iterator.remove();
                    continue;
                }

                // even the ServerPlayer is disconnected, the bossbar will still be ticked.
                if (bossbar.getProgress() >= 1.0F) {
                    bossbar.setVisible(false);

                    // don't change the order of the following two lines.
                    teleportTicket.ready = true;
                    player.teleportTo((ServerLevel) teleportTicket.destination.getLevel(), teleportTicket.destination.getX(), teleportTicket.destination.getY(), teleportTicket.destination.getZ(), teleportTicket.destination.getYaw(), teleportTicket.destination.getPitch());
                    iterator.remove();
                }
            }
        });

    }
}
