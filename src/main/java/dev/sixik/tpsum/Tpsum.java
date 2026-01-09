package dev.sixik.tpsum;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Tpsum.MODID)
public class Tpsum {

    public static final String MODID = "tpsum";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Tpsum(IEventBus modEventBus, ModContainer modContainer) { }

}
