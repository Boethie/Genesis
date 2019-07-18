package genesis;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;

import java.util.Objects;

import static genesis.GenesisMod.resource;

public class ModItems {


    public static void register(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                blockItem(ModBlocks.TEST, ItemGroup.MISC),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(resource("testitem"))
        );
    }

    private static Item blockItem(RegistryObject<Block> block, ItemGroup group) {
        return block.map(b ->
                new BlockItem(b, new Item.Properties().group(group))
                        .setRegistryName(Objects.requireNonNull(b.getRegistryName()))
        ).orElseThrow(() -> new IllegalStateException("Block " + block.getName() + " didn't exist when registering BlockItem"));
    }
}
