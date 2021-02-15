import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.SimulationManager;

import javax.vecmath.Vector2d;
import java.io.IOException;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.02.2021
 * TIME: 20:27
 */
public class DebugChatEvent {
    public static SimulationManager simMan = GameServer.getServerState().getSimulationManager();
    public static void addDebugChatListener() {

        DebugFile.log("player chat event debug eventhandler added");
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent e) {
                DebugFile.log("playerchat event"); //FIXME debug
                PlayerState player = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(e.getMessage().sender);
                if (player == null) {
                    ModPlayground.broadcastMessage("player is null in chat event");
                    return;
                }

                if (!e.isServer()) {
                    ModPlayground.broadcastMessage("e is not on server");
                //    return;
                }
                SimpleTransformableSendableObject firstControlledTransformable = null;
                try {
                    firstControlledTransformable = player.getFirstControlledTransformable();
                } catch (PlayerControlledTransformableNotFound playerControlledTransformableNotFound) {
                    playerControlledTransformableNotFound.printStackTrace();
                    DebugFile.log("could not get first controlled object for player " + player);
                }
                if (null == firstControlledTransformable) {
                    ModPlayground.broadcastMessage("failed to get playerobject");
                    return;
                }

                if (e.getText().contains("pirates")) { //movehud 1,-1
                    //reduce string to first int
                    String s = e.getText();
                    s = s.replace("pirates ",""); //remove movehud
                    s = s.replace(" ",""); //remove space
                    DebugFile.log("after removing, string is: " + s);
                    String[] parts = s.split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    DebugFile.log("x and y are:" + new Vector2d(x,y).toString());

                    ModPlayground.broadcastMessage("spawning pirate raid");
                    PirateManager.SpawnRaid(player.getCurrentSector(),x);

                }
                if (e.getText().contains("simMan")) {
                    //debug simulation manager


                    try {
                        simMan.print(GameServer.getServerClient(player));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        DebugFile.log(ioException.toString());
                    }
                    ModPlayground.broadcastMessage("maximum groups on server allowed: " + (Integer) ServerConfig.CONCURRENT_SIMULATION.getCurrentState());
                    //debugAllBlueprints(10,1,FactionManager.TRAIDING_GUILD_ID);
                    debugEveryBP();

                }
                if (e.getText().contains("spawnAll")) {
                    spawnEveryBP(player.getCurrentSector(),firstControlledTransformable);
                }
            }

        }, clansMain.instance);
    }

    /**
     * will write all blueprints of a faction to the debugfile
     * @param amount
     * @param level
     * @param factionID
     */
    public static void debugAllBlueprints(int amount, int level, int factionID) {
        CatalogPermission[] catalogEntries = simMan.getBlueprintList(amount,level,factionID);
        DebugFile.log("listing ships for " + GameServerState.instance.getFactionManager().getFaction(factionID).getName(),clansMain.instance);
        for (CatalogPermission catalogName : catalogEntries) {
            String s = "catalog entry " + catalogName.getUid() + " has mass " + catalogName.mass + " created by " + catalogName.ownerUID;
            DebugFile.log(s,clansMain.instance);
        }
    }

    public static void debugEveryBP() {
        Collection<CatalogPermission> entries =GameServerState.instance.getCatalogManager().getCatalog();
        for (CatalogPermission entry: entries) {
            DebugFile.log("BP: " + entry.toString());
        }
    }

    public static void spawnEveryBP(Vector3i sector, SimpleTransformableSendableObject playerObj) {
        Collection<CatalogPermission> entries =GameServerState.instance.getCatalogManager().getCatalog();
        for (CatalogPermission entry: entries) {
            DebugFile.log("BP: " + entry.toString());
            Transform transform = new Transform(); //initalize for use in mob
            transform.setIdentity();
            transform.set(playerObj.getWorldTransform());
            GameServer.getServerState().getMobSpawnThread().spawnMobs(1,entry.getUid(),sector,transform,0, BluePrintController.active);
        }

    }

}
