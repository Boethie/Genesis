package genesis;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;

import static genesis.GenesisMod.name;
import static genesis.GenesisMod.resource;

public class ModBlocks {
    public static final RegistryObject<Block> TEST = RegistryObject.of(name("testblock"), () -> Block.class);

    public static void register(final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new Block(Block.Properties.create(Material.ROCK)).setRegistryName(resource("testblock"))
        );
    }
}
