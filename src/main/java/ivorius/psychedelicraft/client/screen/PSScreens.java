/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;


import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Created by lukas on 26.10.14.
 */
public interface PSScreens {
    static void bootstrap() {
        HandledScreens.register(PSScreenHandlers.DRYING_TABLE, DryingTableScreen::new);
        HandledScreens.register(PSScreenHandlers.BARREL, BarrelScreen::new);
        HandledScreens.register(PSScreenHandlers.DISTILLERY, DistilleryScreen::new);
        HandledScreens.register(PSScreenHandlers.FLASK, FlaskScreen::new);
        HandledScreens.register(PSScreenHandlers.MASH_TUB, MushTubScreen::new);
    }
}
