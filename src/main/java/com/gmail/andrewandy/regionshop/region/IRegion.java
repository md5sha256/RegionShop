package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.region.feature.RegionFeatureManager;
import com.gmail.andrewandy.regionshop.region.settings.SettingAccessor;
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
