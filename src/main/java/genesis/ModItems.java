package genesis;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.GameData;

import java.util.Objects;

import static genesis.GenesisMod.location;

public class ModItems {


    public static void register(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                blockItem(ModBlocks.PEGMATITE, ItemGroup.BUILDING_BLOCKS),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(location("testitem"))
        );
    }

    private static Item blockItem(RegistryObject<Block> wrapped, ItemGroup group) {
        Block block = wrapped.orElseThrow(() -> new IllegalStateException("Block " + wrapped.getName() + " didn't exist when registering BlockItem"));
        BlockItem item = new BlockItem(block, new Item.Properties().group(group));
        item.setRegistryName(Objects.requireNonNull(block.getRegistryName()));

        GameData.getBlockItemMap().put(block, item);
        return item;
    }
}
