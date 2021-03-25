package com.github.camotoy.geyserblockplatform.spigot;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.camotoy.geyserblockplatform.common.FloodgateBedrockPlatformChecker;
import com.github.camotoy.geyserblockplatform.common.GeyserBedrockPlatformChecker;
import com.github.camotoy.geyserblockplatform.common.BedrockPlatformChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.util.DeviceOs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class GeyserBlockPlatform extends JavaPlugin implements Listener {
    private BedrockPlatformChecker platformChecker;
    private Set<DeviceOs> supportedDeviceOsList;

    @Override
    public void onEnable() {
        boolean hasFloodgate = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        boolean hasGeyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
        if (!hasFloodgate && !hasGeyser) {
            getLogger().warning("There is no Geyser or Floodgate plugin detected! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        File configFile = getDataFolder().toPath().resolve("config.yml").toFile();
        if (!configFile.exists()) {
            try (InputStream in = GeyserBlockPlatform.class.getResourceAsStream("/config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not copy the default config! " + e);
            }
        }

        Config config;
        try {
            config = new YAMLMapper().readValue(configFile, Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not understand the contents of the config! " + e);
        }

        supportedDeviceOsList = new HashSet<>();

        addValueIfTrue(supportedDeviceOsList, DeviceOs.UNKNOWN, config::isUnknownEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.GOOGLE, config::isAndroidEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.IOS, config::isIosEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.OSX, config::isMacOsEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.AMAZON, config::isFireOsEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.GEARVR, config::isGearVrEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.UWP, config::isWindows10Enabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.WIN32, config::isWindowsEduEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.PS4, config::isPs4Enabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.NX, config::isSwitchEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.XBOX, config::isXboxOneEnabled);
        addValueIfTrue(supportedDeviceOsList, DeviceOs.WINDOWS_PHONE, config::isWindowsPhoneEnabled);

        if (hasFloodgate) {
            this.platformChecker = new FloodgateBedrockPlatformChecker();
        } else {
            this.platformChecker = new GeyserBedrockPlatformChecker();
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void addValueIfTrue(Set<DeviceOs> set, DeviceOs deviceOS, Supplier<Boolean> function) {
        if (function.get()) {
            set.add(deviceOS);
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DeviceOs deviceOS = this.platformChecker.getBedrockPlatform(event.getPlayer().getUniqueId());
        if (deviceOS == null) {
            return;
        }
        if (!supportedDeviceOsList.contains(deviceOS)) {
            event.getPlayer().kickPlayer("This server cannot be joined with your Bedrock platform!");
        }
    }
}
