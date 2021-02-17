import api.DebugFile;
import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.02.2021
 * TIME: 20:10
 */
public class clansMain extends StarMod {
    public static StarMod instance;
    public static void main(String[] a) {
        System.out.println("hi");
    }

    public clansMain() {
        super();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        DebugFile.log("clans mod running OnServerCreated",instance);
        DebugChatEvent.addDebugChatListener();

    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    }

    @Override
    public StarMod setModName(String modName) {
        return super.setModName(modName);
    }

    @Override
    public StarMod setModAuthor(String modAuthor) {
        return super.setModAuthor(modAuthor);
    }

    @Override
    public StarMod setModDescription(String modDescription) {
        return super.setModDescription(modDescription);
    }

    @Override
    public StarMod setModVersion(String modVersion) {
        return super.setModVersion(modVersion);
    }

    @Override
    public StarMod setModSMVersion(String modSMVersion) {
        return super.setModSMVersion(modSMVersion);
    }

    @Override
    public StarMod setServerSide(boolean server) {
        return super.setServerSide(server);
    }

    @Override
    public void onBlockConfigLoad(BlockConfig config) {
        super.onBlockConfigLoad(config);
    }

    @Override
    public void onPreEnableServer() {
        super.onPreEnableServer();
    }
}
