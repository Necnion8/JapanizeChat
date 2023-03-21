package com.gmail.necnionch.mymod.japanizechat;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.gmail.necnionch.mymod.japanizechat.config.MainConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import io.github.apple502j.kanaify.Kanaifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(JapanizeChatMod.MODID)
public class JapanizeChatMod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "japanizechat";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MainConfig mainConfig = new MainConfig();

    public JapanizeChatMod() {
        Objects.requireNonNull(Kanaifier.INSTANCE);

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, mainConfig.config);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        if (!mainConfig.config.isLoaded())
            return;

        MinecraftServer server = event.getPlayer().getServer();
        if (server == null)
            return;

        event.setCanceled(true);

        japanizeByPlayer(event.getPlayer(), event.getRawText())
                .thenApply(japanized -> formatMessage(event.getUsername(), event.getRawText(), japanized))
                .thenApply(Component::literal)
                .whenComplete((formatted, error) -> {
                    if (formatted != null) {
                        broadcast(server, formatted);
                        return;
                    }

                    error.printStackTrace();
                });
    }

    private static void broadcast(MinecraftServer server, Component component) {
        server.getPlayerList().broadcastSystemMessage(component, false);
    }

    private CompletableFuture<String> japanizeByPlayer(ServerPlayer player, String raw) {
        return japanize(raw);
    }

    private CompletableFuture<String> japanize(String raw) {
        if (raw.isBlank() || !Japanizer.needsJapanize(raw))
            return CompletableFuture.completedFuture(null);

        return Kanaifier.INSTANCE.convert(raw)
                .exceptionally((exc) -> {
                    LOGGER.warn("Error while kanaifying message {}", raw);
                    LOGGER.warn("Stack trace:", exc);
                    return null;
                });
    }

    public String formatMessage(String playerName, String message, @Nullable String japanized) {
        String coloredMessage = message.replaceAll("&([0-9A-FK-OR])", "§$1");

        return mainConfig.format.get()
                .replace("{player}", playerName)
                .replace("{message}", Optional.ofNullable(japanized)
                        .map(s -> mainConfig.formatJapanize.get().replace("{japanized}", s).replace("{message}", coloredMessage))
                        .orElse(coloredMessage));
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }


    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("japanizemod")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("reload")
                        .executes(this::cmdReload)));

        dispatcher.register(Commands.literal("japanize")
                .executes(this::cmdJapanizeToggle));

        dispatcher.register(Commands.literal("jp")
                .executes(this::cmdJapanizeToggle));
    }

    private int cmdReload(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(Component.literal("executed! reload"), false);
        return 0;
    }

    private int cmdJapanizeToggle(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(Component.literal("executed! toggle"), false);
        return 0;
    }


}
