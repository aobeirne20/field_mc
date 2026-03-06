package com.gptwe;

import net.minecraft.client.Minecraft;

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

            client.player.connection.sendCommand(cmd);
        }
    }
}