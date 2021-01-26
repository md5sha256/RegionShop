package com.gmail.andrewandy.regionshop.region.feature.builtins.access;

import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.region.feature.builtins.ownership.JsonOwnershipFeatureImpl;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.UUID;
import java.util.logging.Level;

public class JsonAccessFeatureImpl extends AccessFeature {

    private static final String DATA_KEY = "access-data";
    private final ConfigurationNode container;
    @Inject
    private TaskChainFactory factory;
    private JsonAccessDataImpl accessData = new JsonAccessDataImpl();

    private ConfigurationLoader<?> configurationLoader;


    @AssistedInject
    public JsonAccessFeatureImpl(@Assisted IRegion region, @NotNull LogUtils logUtils) {
        super(region, logUtils);
        this.container = region.getFeatureManager().getOrCreateDataContainer(JsonOwnershipFeatureImpl.class);
        loadData();
    }

    @Override
    public void delete() {
        if (this.deleted) {
            return;
        }
        this.deleted = true;
        this.accessData = null;
        this.container.removeChild(DATA_KEY);
    }

    @Override
    public void loadData() {
        final ConfigurationNode node = this.container.node(DATA_KEY);
        if (node.virtual()) {
            return;
        }
        try {
            this.accessData = node.get(JsonAccessDataImpl.class);
        } catch (SerializationException ex) {
            logUtils.logException(ex);
            logUtils.log(Level.SEVERE, "<red>Failed to deserialize access data for region: " + region.getRegionID());
        }
    }

    @Override
    public boolean saveData() {
        final ConfigurationNode node = this.configurationLoader.createNode();
        try {
            node.set(this.accessData);
            this.container.node(DATA_KEY).set(node);
            return true;
        } catch (SerializationException ex) {
            logUtils.logException(ex);
            logUtils.log(Level.WARNING, "<yellow>Failed to save access data for region: " + region.getRegionID());
            return false;
        }
    }

    @NotNull
    @Override
    public JsonAccessDataImpl getAccessData() {
        return this.accessData;
    }

    @Override
    public void removeData(@NotNull UUID user) {
        this.accessData.clearPrivileges(user);
    }


}
