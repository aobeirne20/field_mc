package com.gptwe;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;

public class WorldEditCommandRunner {
    public static void runAllWorldEditLines(String text) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.getConnection() == null) {
            return;
        }

        String[] lines = text.replace("\r\n", "\n").split("\n");
        for (String line : lines) {
            String cmd = line.trim();
            if (!cmd.startsWith("//")) {
                continue;
            }

            System.out.println("[gptwe] executing: " + cmd);
            runOnServer(client, cmd);
        }
    }

    /**
     * Run command on integrated server (singleplayer) to avoid sendCommand packet handling.
     * Falls back to sendCommand for multiplayer.
     */
    private static void runOnServer(Minecraft client, String command) {
        if (client.player == null) return;
        var player = client.player;
        Object server = client.getSingleplayerServer();
        if (server != null) {
            runCommandOnIntegratedServer(server, player.getUUID(), command);
        } else {
            player.connection.sendCommand(command);
        }
    }

    private static void runCommandOnIntegratedServer(Object server, java.util.UUID playerUuid, String command) {
        try {
            Method execute = server.getClass().getMethod("execute", Runnable.class);
            execute.invoke(server, (Runnable) () -> {
                try {
                    Method getPlayerList = server.getClass().getMethod("getPlayerList");
                    Object playerList = getPlayerList.invoke(server);
                    Method getPlayer = playerList.getClass().getMethod("getPlayer", java.util.UUID.class);
                    Object serverPlayer = getPlayer.invoke(playerList, playerUuid);
                    if (serverPlayer == null) return;

                    Method createCommandSourceStack = serverPlayer.getClass().getMethod("createCommandSourceStack");
                    Object source = createCommandSourceStack.invoke(serverPlayer);

                    Method getCommands = server.getClass().getMethod("getCommands");
                    Object commands = getCommands.invoke(server);
                    Method performPrefixedCommand = commands.getClass().getMethod("performPrefixedCommand", getCommandSourceStackType(server), String.class);
                    performPrefixedCommand.invoke(commands, source, command);
                } catch (Exception e) {
                    System.err.println("[gptwe] Failed to run command on server: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[gptwe] Failed to schedule command on server: " + e.getMessage());
        }
    }

    private static Class<?> getCommandSourceStackType(Object server) throws Exception {
        return Class.forName("net.minecraft.commands.CommandSourceStack");
    }
}
