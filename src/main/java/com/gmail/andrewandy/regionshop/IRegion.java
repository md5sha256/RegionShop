package com.gmail.andrewandy.regionshop;

public interface IRegion {
    boolean isDeleted();

    String getWorld();

    String getRegionID();

    String getDisplayNameRaw();

    RegionFeatureManager getFeatureManager();
}
