package com.gmail.andrewandy.regionshop.region.settings;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

public interface ISetting<T> {

    @NotNull NodePath path();

    @NotNull TypeToken<T> type();

}
