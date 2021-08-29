package com.github.md5sha256.regionshop.region;

import com.github.md5sha256.regionshop.region.feature.RegionFeatureManager;
import com.github.md5sha256.regionshop.region.settings.SettingAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IRegion {

    @NotNull UUID uuid();

    boolean deleted();

    @NotNull CompletableFuture<Void> queueDeletion();

    @NotNull String world();

    @NotNull String regionId();

    @NotNull String displayNameRaw();

    @NotNull RegionFeatureManager featureManager();

    @NotNull SettingAccessor settingAccessor();

}
