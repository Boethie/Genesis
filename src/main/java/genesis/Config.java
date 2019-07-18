package genesis;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Config {

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    public static Client CLIENT = new Client(CLIENT_BUILDER);
    public static Common COMMON = new Common(COMMON_BUILDER);
    public static Server SERVER = new Server(SERVER_BUILDER);

    public static ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
    public static ForgeConfigSpec COMMON_CONFIG = COMMON_BUILDER.build();
    public static ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onConfigUpdate);


    }

    private static void onConfigUpdate(ModConfig.ModConfigEvent event) {
    }

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<Integer> testconfig;

        public Client(ForgeConfigSpec.Builder builder) {
            testconfig = builder.comment("testclient")
                    .translation("testclient")
                    .define("client_testconfig", 1);
        }
    }

    public static class Common {

        public final ForgeConfigSpec.ConfigValue<Integer> testconfig;

        public Common(ForgeConfigSpec.Builder builder) {
            testconfig = builder.comment("testcommon")
                    .translation("testcommon")
                    .define("common_testconfig", 1);
        }
    }

    public static class Server {

        public final ForgeConfigSpec.ConfigValue<Integer> testconfig;

        public Server(ForgeConfigSpec.Builder builder) {
            testconfig = builder.comment("testserver")
                    .translation("testserver")
                    .define("server_testconfig", 1);
        }
    }
}
