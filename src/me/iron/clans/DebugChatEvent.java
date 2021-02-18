package me.iron.clans;

import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameSegmentControllerConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.schine.ai.stateMachines.State;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.*;

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
                    ModPlayground.broadcastMessage("e is on server");
                    return;
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
                    for (CatalogPermission entry: getEveryBP()) {
                        DebugFile.log(entry.toString());
                    }

                }
                if (e.getText().contains("spawnAll")) {
                    spawnEveryBP(player.getCurrentSector(),firstControlledTransformable);
                }

                if (e.getText().contains("mobs")) {
                    spawnMobs(firstControlledTransformable,-1,5);
                }
                if (e.getText().contains("fleet")) {
                    SpawnFleet(firstControlledTransformable,BlueprintType.SHIP,100000,GetEnemyBlueprints(),-1);
                }
                if (e.getText().contains("AI")) {
                    //TODO get nearby pirate ships, debug those//;
                    for (SegmentController shipX: GetAllShipsInSector(player.getCurrentSector())) {
                        GetShipAI(shipX);
                    }

                }
                if (e.getText().contains("follow")) {
                    //get all ships
                    DebugFile.log("trying to move all ships towards player.");
                    for (SegmentController ship: GetAllShipsInSector(player.getCurrentSector())) {
                        ShipMoveTo(firstControlledTransformable,ship);
                    }
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

    /**
     * get every blueprint, no doubles
     * @return
     */
    public static Collection<CatalogPermission> getEveryBP() {
        Collection<CatalogPermission> entries = GameServerState.instance.getCatalogManager().getCatalog();

        for (CatalogPermission entry: entries) {
            for (CatalogPermission entry2: entries) {
                if (!entry.equals(entry2) && entry2.getUid().equals(entry.getUid())) {
                    entries.remove(entry2);
                    DebugFile.log("BP removing double: " + entry2.toString());
                    break;
                }
            }
        }
        return entries;
    }

    public static void spawnEveryBP(Vector3i sector, SimpleTransformableSendableObject playerObj) {
        Collection<CatalogPermission> entries = getEveryBP();
        for (CatalogPermission entry: entries) {
            DebugFile.log("BP: " + entry.toString());
            Transform transform = new Transform(); //initalize for use in mob
            transform.setIdentity();
            transform.set(playerObj.getWorldTransform());
            GameServer.getServerState().getMobSpawnThread().spawnMobs(1,entry.getUid(),sector,transform,0, BluePrintController.active);
        }

    }

    /**
     * will spawn x random ships of this faction.
     * @param referenceObject ship/object to spawn around
     * @param factionID faction of ships: decides what designs are availalbe
     * @param count amount of ships to spawn
     */
    public static void spawnMobs(SimpleTransformableSendableObject referenceObject, int factionID, int count)  {
        //get catalogname of faction
        CatalogPermission[] catalogEntries = GetEnemyBlueprints(); // x random (?) ships of this faction

        catalogEntries = GetClassification(catalogEntries,BlueprintClassification.ATTACK);
        Transform transform = new Transform();
        transform.setIdentity();

        //set to reference transform (basically position)
        transform.set(referenceObject.getWorldTransform());
        String s ="pirate faction has " + catalogEntries.length + " available:";
        for (CatalogPermission entry: catalogEntries) {
            s += " design: " + entry.getUid() + " mass: " + entry.mass + " classification: " + entry.getClassification().getName();
            try {
                GameServerState.instance.spawnMobs(
                        1, //amounts
                        entry.getUid(), //design to spawn
                        referenceObject.getSector(new Vector3i()), //spawn in sector
                        transform, //reference point (probably?)
                        factionID, //factionID for the newly spawned ship
                        BluePrintController.active
                );
            } catch (EntityNotFountException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (EntityAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
        DebugFile.log(s,clansMain.instance);
    }

    /**
     * will spawn a mob with given blueprint for this faction near the reference object.
     * @param referenceObject
     * @param factionID
     * @param count
     * @param design
     */
    public static void SpawnSpecificMob (SimpleTransformableSendableObject referenceObject, int factionID, int count, CatalogPermission design) {
        Transform transform = new Transform();
        transform.setIdentity();

        //set to reference transform (basically position)
        transform.set(referenceObject.getWorldTransform());
        String s ="spawning specific mob: " + design.getUid() + " count: "+ count;
        try {
            GameServerState.instance.spawnMobs(
                    count, //amounts
                    design.getUid(), //design to spawn
                    referenceObject.getSector(new Vector3i()), //spawn in sector
                    transform, //reference point (probably?)
                    factionID, //factionID for the newly spawned ship
                    BluePrintController.active
            );
        } catch (EntityNotFountException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EntityAlreadyExistsException e) {
            e.printStackTrace();
        }
        DebugFile.log(s,clansMain.instance);
    }

    /**
     * will sort out any blueprints that are not of given classification
     * @param entries
     * @param classification The blueprint classification, the player sets on saving the BP.
     * @return
     */
    public static CatalogPermission[] GetClassification(CatalogPermission[] entries,BlueprintClassification classification) {
        List<CatalogPermission> allowedEntries = new ArrayList<CatalogPermission>();
        String s = "received " + entries.length + "entries to sort for " + classification.getName();
        for (CatalogPermission entry: entries) {
            s += "checking " + entry.getUid() + " class: " + entry.getClassification().getName();
            if (entry.getClassification().getName().equals(classification.getName())) {
                allowedEntries.add(entry);
            }
        }
        DebugFile.log(s,clansMain.instance);
        CatalogPermission[] returnArray = new CatalogPermission[allowedEntries.size()];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = allowedEntries.get(i);
        }
        DebugFile.log("catalog entries with classification " + classification.getName() + ": " + returnArray.toString());
        return returnArray;
    }

    /**
     * will get all enemy usable blueprints from the catalog manager
     * @return
     */
    public static CatalogPermission[] GetEnemyBlueprints() {
        Collection<CatalogPermission> entries = GameServerState.instance.getCatalogManager().getCatalog();

        List<CatalogPermission> list = new ArrayList<CatalogPermission>();
        for (CatalogPermission entry: entries) {
            if (entry.enemyUsable()) {
                list.add(entry);
            }
        }
        CatalogPermission[] arr = new CatalogPermission[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * will spawn a fleet consisting of the given designs
     * @param type
     * @param targetMass
     * @param designs
     * @param factionID
     */
    public static void SpawnFleet(SimpleTransformableSendableObject referenceObj, BlueprintType type, int targetMass, CatalogPermission[] designs, int factionID) {
        if (designs.length < 2) {
            DebugFile.log("not enough designs for fleet given, need at least 2! ");
        }
        //TODO change fleet composition based on blueprint type.

        //use largest ship as fleet capital ship, spawn multiple support craft
        int countCapital = (int) Math.round(Math.random() + 1); //1 to 2 capital ships
        int countSupport = (int) Math.round(Math.random() * 4 + 2); //2 to 6 support Craft (frigates etc)
        //sort designs by mass
        Arrays.sort(designs,new SortByMass());
        CatalogPermission capitalShip = designs[0];
        CatalogPermission supportCraft = designs[1];
        DebugFile.log("spawning fleet: " + countCapital + "x " + capitalShip.getUid() + "(" + capitalShip.mass + ")" + countSupport + "x " + supportCraft.getUid() + "(" + supportCraft.mass + ")");
        SpawnSpecificMob(referenceObj,factionID,countCapital,capitalShip);
        SpawnSpecificMob(referenceObj,factionID,countSupport,supportCraft);
    }

    /**
     * comparator class for sorting catalog designs by mass in descending order
     */
    static class SortByMass implements Comparator<CatalogPermission> {
        @Override
        public int compare(CatalogPermission o1, CatalogPermission o2) {
            return (int) (o2.mass - o1.mass);
        }
    }

    /**
     * will try and debug the ships AI
     * @param ship
     */
    public static void GetShipAI(SegmentController ship) {
        //stolen from schema code (from segmentcontroller overheat core method)
        if (ship == null) {
            DebugFile.log("ship is null");
            return;
        }
        if (! (ship instanceof SegmentControllerAIInterface)) {
          DebugFile.log("ship" + ship.getName() + "is not instance of SegmentControllerAIInterface");
          return;
        }
        DebugFile.log("############## AI for ship " + ship.getName());
        AIGameSegmentControllerConfiguration scConfig = ((AIGameSegmentControllerConfiguration) ((SegmentControllerAIInterface) ship).getAiConfiguration());
        DebugFile.log("ship has AIGameSegmentControllerConfiguration");

        boolean activeAI = scConfig.isActiveAI();
        DebugFile.log("ship is active AI: " + activeAI);

        String lastAIEntityStateString = scConfig.getAiEntityState().toString();
        DebugFile.log("last AI entity state: " + lastAIEntityStateString);

        if (!activeAI) {
            DebugFile.log("aborting here bc not active AI.");
            return;
        }
        State lastAIState = scConfig.getLastAiState();
        if (lastAIState == null) {
            DebugFile.log("lastAiState null");
        } else {
            String lastAIStateString = scConfig.getLastAiState().toString();
            DebugFile.log("last AI state: " + lastAIEntityStateString);
        };

        DebugFile.log("last engange:" + scConfig.getAiEntityState().lastEngage,clansMain.instance);

        //try getting shipAIEntity
        if (!(scConfig.getAiEntityState() instanceof  ShipAIEntity)) {
            DebugFile.log("not ship AI type");
            return;
        }
    }

    /**
     * attempt to make ship move to a target position in its sector (trying to understand shipAIEntity.moveTo
     * @param target
     * @param ship
     */
    public static void ShipMoveTo(SimpleTransformableSendableObject target, SegmentController ship) {
        if (ship == null || !(ship instanceof SegmentControllerAIInterface) || target == null) {
            DebugFile.log("ship or target either null or not instance of AI interface");
            return;
        }
        DebugFile.log("trying to move ship: " + ship.getName() + "towards " + target.getName());

        //get ship ai config thing
        AIGameSegmentControllerConfiguration scConfig = ((AIGameSegmentControllerConfiguration) ((SegmentControllerAIInterface) ship).getAiConfiguration());
        //get actual ship AI from config thing
        ShipAIEntity shipAI = (ShipAIEntity) scConfig.getAiEntityState();
        DebugFile.log("AI is ShipAI.");
        DebugFile.log("trying to make AI ship move to 0,0,0");
        //direction = target.pos - ship.pos
        Vector3f targetPos = target.getWorldTransform().origin;
        Vector3f shipPos = ship.getWorldTransform().origin;
        DebugFile.log("target at: " + targetPos +", ship at " + shipPos);
        shipPos.negate(); //negative to subtract from targetpos
        targetPos.add(shipPos); //targetpos = targetDirection
        DebugFile.log("direction from ship to target: " + targetPos);
        shipAI.moveTo(GameServerState.instance.getController().getTimer(),targetPos,true);
    }

    /**
     * get all SegmentControllers in this sector
     * @param sector
     */
    public static SegmentController[] GetAllShipsInSector(Vector3i sector) {
        Set<SimpleTransformableSendableObject<?>> entities = null;
        try {
            entities = GameServer.getUniverse().getSector(sector).getEntities();
        } catch (IOException e) {
            DebugFile.logError(e,clansMain.instance);
            e.printStackTrace();
            return new SegmentController[0];
        };
        DebugFile.log("getting all ships in sector" + sector + ", counted " +entities.size());
        for (SimpleTransformableSendableObject entity : entities) {
           if (!entity.isSegmentController()) {
               entities.remove(entity);
           }
        }
        DebugFile.log("removed non segmentcontrollers, remaining: " + entities.size());
        SegmentController[] array = new SegmentController[entities.size()];
        SimpleTransformableSendableObject[] arrayOld = new SimpleTransformableSendableObject[entities.size()];
        entities.toArray(arrayOld);
        for (int i = 0; i < array.length; i++) { //TODO avoid having null values in array
            SimpleTransformableSendableObject obj = arrayOld[i];
            if (!(obj instanceof SegmentController)) {
                continue;
            }
            array[i] = (SegmentController) arrayOld[i];
        }
        return array;
    }
}

