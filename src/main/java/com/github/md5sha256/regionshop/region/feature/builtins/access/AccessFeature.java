package com.github.md5sha256.regionshop.region.feature.builtins.access;

import com.github.md5sha256.regionshop.region.feature.RegionFeature;
import com.github.md5sha256.regionshop.region.IRegion;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;

public abstract class AccessFeature implements RegionFeature {

    protected final LogUtils logUtils;
    protected final IRegion region;
    protected boolean deleted;

    @AssistedInject
    public AccessFeature(@Assisted IRegion region, @NotNull LogUtils logUtils) {
        this.region = region;
        this.logUtils = logUtils;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    public abstract @NotNull AccessData getAccessData();

}
