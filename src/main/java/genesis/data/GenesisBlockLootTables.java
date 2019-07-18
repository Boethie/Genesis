package genesis.data;

import com.mojang.datafixers.util.Pair;
import genesis.GenesisMod;
import genesis.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GenesisBlockLootTables implements Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceLocation, LootTable.Builder> tableBuilders = new HashMap<>();

    @Override public Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> get() {
        return this::registerAll;
    }

    private static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private boolean verifyExists(Pair<ResourceLocation, LootTable.Builder> table) {
        Objects.requireNonNull(table.getSecond(), () -> String.format("Missing loot table '%s'", table.getFirst()));
        return true;
    }

    private void registerAll(BiConsumer<ResourceLocation, LootTable.Builder> registry) {
        createLootTables();
        ForgeRegistries.BLOCKS.getEntries().stream()
                .map(e -> new Pair<ResourceLocation, Block>(e.getKey(), e.getValue()))
                .map(e -> e.mapFirst(location -> e.getSecond().getLootTable()))
                .filter(e -> e.getFirst().getNamespace().equals(GenesisMod.MODID))
                .filter(distinctBy(Pair::getFirst))
                .map(e -> e.mapSecond(block -> tableBuilders.get(e.getFirst())))
                .filter(this::verifyExists)
                .forEach(e -> registry.accept(e.getFirst(), e.getSecond()));
    }


    private void createLootTables() {
        registerSimpleBlock(ModBlocks.PEGMATITE);
    }

    private void registerSimpleBlock(RegistryObject<Block> wrapped) {
        if (!wrapped.isPresent()) {
            LOGGER.warn("Skipping creating loot table for block " + wrapped.getName() + " because it doesn't exist");
            return;
        }
        Block block = wrapped.orElseThrow(AssertionError::new);
        LootTable.Builder builder = LootTable.builder()
                .func_216040_a(LootPool.builder()
                        .rolls(ConstantRange.func_215835_a(1))
                        .addEntry(ItemLootEntry.builder(block))
                );
        tableBuilders.put(block.getLootTable(), builder);
    }
}
