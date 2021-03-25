package com.github.camotoy.geyserblockplatform.common;

import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.DeviceOs;

import java.util.UUID;

public class FloodgateBedrockPlatformChecker implements BedrockPlatformChecker {
    @Override
    public DeviceOs getBedrockPlatform(UUID uuid) {
        FloodgatePlayer player = FloodgateApi.getInstance().getPlayer(uuid);
        if (player != null) {
            return player.getDeviceOs();
        }

        return null;
    }
}
