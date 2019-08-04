package genesis;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;

import static genesis.GenesisMod.name;
import static genesis.GenesisMod.location;

public class ModBlocks {
    public static final RegistryObject<Block> PEGMATITE = RegistryObject.of(name("pegmatite"), () -> Block.class);
    public static final RegistryObject<Block> KOMATIITE = RegistryObject.of(name("komatiite"), () -> Block.class);
    public static final RegistryObject<Block> GRANODIORITE = RegistryObject.of(name("granodiorite"), () -> Block.class);

    public static void register(final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new Block(Block.Properties.from(Blocks.STONE)).setRegistryName(PEGMATITE.getName()),
                new Block(Block.Properties.from(Blocks.STONE)).setRegistryName(KOMATIITE.getName()),
                new Block(Block.Properties.from(Blocks.STONE)).setRegistryName(GRANODIORITE.getName())
        );
    }
}
