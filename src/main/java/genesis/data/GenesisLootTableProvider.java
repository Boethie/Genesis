package genesis.data;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.ValidationResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenesisLootTableProvider implements IDataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator dataGenerator;

    // comsumer<registerMethod>
    private final Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> blockTableGenerator = new GenesisBlockLootTables();

    public GenesisLootTableProvider(DataGenerator dataGeneratorIn) {
        this.dataGenerator = dataGeneratorIn;
    }

    /**
     * Performs this provider's action.
     */
    @Override public void act(DirectoryCache cache) {
        Path path = this.dataGenerator.getOutputFolder();

        Map<ResourceLocation, LootTable> lootTables = new HashMap<>();
        BiConsumer<ResourceLocation, LootTable.Builder> register = (loc, builder) -> registerTable(lootTables, loc, builder);
        blockTableGenerator.get().accept(register);

        validate(lootTables);
        writeJsons(cache, path, lootTables);
    }


    private void registerTable(Map<ResourceLocation, LootTable> lootTables, ResourceLocation location, LootTable.Builder builder) {
        if (lootTables.put(location, builder.func_216039_a(LootParameterSets.BLOCK).build()) != null) {
            throw new IllegalStateException("Duplicate loot table " + location);
        }
    }

    private void validate(Map<ResourceLocation, LootTable> lootTables) {
        ValidationResults validationresults = new ValidationResults();

        lootTables.forEach((location, table) -> {
            LootTableManager.func_215302_a(validationresults, location, table, lootTables::get);
        });
        Multimap<String, String> problems = validationresults.getProblems();
        if (!problems.isEmpty()) {
            problems.forEach((p_218435_0_, p_218435_1_) -> {
                LOGGER.warn("Found validation problem in " + p_218435_0_ + ": " + p_218435_1_);
            });
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
    }

    private void writeJsons(DirectoryCache cache, Path path, Map<ResourceLocation, LootTable> lootTables) {
        lootTables.forEach((location, table) -> {
            Path fileLocation = getPath(path, location);
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(table), fileLocation);
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't save loot table {}", fileLocation, ioexception);
            }
        });
    }

    Path getPath(Path pathIn, ResourceLocation id) {
        return pathIn.resolve("data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json");
    }

    @Override public String getName() {
        return "GenesisLootTables";
    }
}