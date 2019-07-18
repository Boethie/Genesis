package genesis;

import genesis.data.GenesisLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GenesisMod.MODID)
public class GenesisMod {

    public static final String MODID = "genesis";

    private static final Logger LOGGER = LogManager.getLogger();

    public GenesisMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::setup);
        modBus.addListener(this::doClientStuff);
        modBus.addListener(this::gatherData);

        modBus.addGenericListener(Block.class, ModBlocks::register);
        modBus.addGenericListener(Item.class, ModItems::register);

        Config.register();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static String name(String name) {
        return MODID + ":" + name;
    }

    private void gatherData(GatherDataEvent event) {
        LOGGER.info("Adding Genesis data providers");
        DataGenerator gen = event.getGenerator();
        if (event.includeServer()) {
            gen.addProvider(new GenesisLootTableProvider(gen));
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

}
