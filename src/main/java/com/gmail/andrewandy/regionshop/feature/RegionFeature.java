package com.gmail.andrewandy.regionshop.feature;

import java.util.concurrent.CompletableFuture;

public interface RegionFeature {

    void delete();

    void loadData();

    boolean saveData();

    default CompletableFuture<Boolean> saveDataAsynchronously() {
        return CompletableFuture.completedFuture(saveData());
    }

}