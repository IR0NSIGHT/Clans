package me.iron.clans; /**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.02.2021
 * TIME: 20:16
 */

import api.DebugFile;
import api.ModPlayground;
import org.schema.common.util.linAlg.Vector3i;

import java.util.Random;

/**
 * generic class for doing stuff with pirates, dont know yet
 */
public class PirateManager {
    public static final Random random = new Random();
    public static void SpawnRaid (Vector3i fromSector, int amount) {         //spawn some pirates
        Vector3i toSector = new Vector3i(fromSector);
        toSector.add(new Vector3i(5,5,5));
        String s = "spawning pirate patrol from " + fromSector.toString() + " to " + toSector.toString();
        ModPlayground.broadcastMessage(s);
        DebugFile.log(s,clansMain.instance);

        DebugFile.log(s,clansMain.instance);
        ModPlayground.broadcastMessage(s);
       // GameServerState.instance.getController().spaw
    }

}

//SimulationManager simMan = GameServerState.instance.getSimulationManager(); //programm that controls ai groups, one instance on server (i think)
//simMan.createRandomPirateGroup(fromSector ,amount);
//simMan.addJob(new SpawnPiratePatrolPartyJob(fromSector,toSector,amount));
//GameServerState.instance.getSimulationManager().addJob(new SpawnTradingPartyJob(toSector,fromSector,3));
//s = ("spawning was successfull: " + success);
