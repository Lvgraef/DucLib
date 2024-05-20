package io.github.itskillerluc.duclib;

import io.github.itskillerluc.duclib.data.animation.DucLibAnimationLoader;
import io.github.itskillerluc.duclib.data.model.DucLibModelLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

//TODO: somehow make some kind of interpolation for when you have multiple animations running at once.
@Mod(DucLib.MOD_ID)
public class DucLib
{
    public static final String MOD_ID = "duclib";
    public DucLib(IEventBus bus) {
        bus.addListener(this::addReloadListener);
    }

    private void addReloadListener(final RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new DucLibModelLoader());
        event.registerReloadListener(new DucLibAnimationLoader());
    }
}
