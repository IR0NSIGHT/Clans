# Ship AI documentation
im trying to write down what i figure out during my hunt for ship AI.

Segmentcontroller -> cast to SegmentControllerAIInterface (dead class), extends AIInterface (dead class), has getter for AIConfigurationInterface
-> inherits to AIGameConfiguration -> AIGameCreatureConfiguration, AIGameSegmentControllerConfiguration

method to get AIGameSegmentControllerConfiguration:
((AIGameSegmentControllerConfiguration) ((SegmentControllerAIInterface) mySegmentController).getAiConfiguration())

AIGameSegmentControllerConfiguration offers:
(from AIConfigInterface)
```java
	public AiEntityStateInterface getAiEntityState();

	public boolean isActiveAI();

	public boolean isAIActiveClient();

	public void callBack(AIConfiguationElementsInterface aiConfiguationElements, boolean send);

	public void update(Timer timer);

	public void updateFromNetworkObject(NetworkObject o);

	public void updateToFullNetworkObject(NetworkObject networkObject);

	public void updateToNetworkObject(NetworkObject networkObject);

	public void initFromNetworkObject(NetworkObject from);

	public void applyServerSettings();

	public AIConfiguationElementsInterface get(E active);
```
(from SCConfigInterface)
```java
    	public void initialize(
    			Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements) {
    		elements.put(Types.AIM_AT.ordinal(), new AIConfiguationElements(Types.AIM_AT, "Any", new StaticStates("Any", "Selected Target", "Ships", "Stations", "Missiles", "Astronauts"), this));

    		elements.put(Types.TYPE.ordinal(), new AIConfiguationElements(Types.TYPE, "Ship", new StaticStates("Turret", "Ship", "Fleet"), this));

    		elements.put(Types.ACTIVE.ordinal(), new AIConfiguationElements(Types.ACTIVE, false, new StaticStates(false, true), this));

    		elements.put(Types.MANUAL.ordinal(), new AIConfiguationElements(Types.MANUAL, false, new StaticStates(false, true), this));

    		elements.put(Types.PRIORIZATION.ordinal(), new AIConfiguationElements(Types.PRIORIZATION, "Highest", new StaticStates("Highest", "Lowest", "Random"), this));

    		elements.put(Types.FIRE_MODE.ordinal(), new AIConfiguationElements(Types.FIRE_MODE, "Simultaneous", new StaticStates("Simultaneous", "Volley"), this));

    	}
```
~~This looks like a method for giving orders to the ship?~~
it seems to fill the input hashmap. Mapping Type enums to AIConfiguationElements objects

This method is called from AIGameConfiguration (highest parent class of SCConfigInterface)
Interesting finds: 
- class has a OnProximity method, which is called from Ship.java, through CubeCollisionAlgorythm.java, but method is empty.
- lots of reaction methods: onDamageServer, onStartOverheating, onCoreDestroyed etc
- getters and setters for AIState: getLastAIState(), setLastAIState(), seems to be the state of an FSM

whats the difference between State.java and AiEntityState.java and StateInterface?
AiEntityState has getter for StateInterface (getState())
SegConConfig has getter for AiEntityState (getLastAIState)

Debugging the AI Entity States for ships in my sector:
example, spawned pirate, attacking outcast ships:
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ############## AI for ship MOB_Venom 1_1613655265575_1[Pirates]
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship has AIGameSegmentControllerConfiguration
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship is active AI: true
[StarLoader] 18-02-2021 at 14:34:32 MEZ  last AI entity state: S&D_ENT
->[SimpleSearchAndDestroyProgram->ShipGettingToTarget]
....Ship[greenship_s_jump_1613651770347](11) hans1 IN_SAFE_SHOP_DIST 
; WEP-Range: 9000.0 salv-Range: Infinity; 
[StarLoader] 18-02-2021 at 14:34:32 MEZ  lastAiState null

example trader ships under attack:

[StarLoader] 18-02-2021 at 14:34:32 MEZ  ############## AI for ship greenship_s_jump_1613651770347 <hans1>
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship has AIGameSegmentControllerConfiguration
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship is active AI: false
[StarLoader] 18-02-2021 at 14:34:32 MEZ  last AI entity state: S&D_ENT
->[SimpleSearchAndDestroyProgram->Waiting]
; WEP-Range: 0.0 salv-Range: 0.0; 
[StarLoader] 18-02-2021 at 14:34:32 MEZ  aborting here bc not active AI.
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ############## AI for ship GFLTSHP_-10000000_4rl3[Traders]
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship has AIGameSegmentControllerConfiguration
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship is active AI: true
[StarLoader] 18-02-2021 at 14:34:32 MEZ  last AI entity state: S&D_ENT
->[SimpleSearchAndDestroyProgram->ShipEngagingTarget]
....Ship[greenship_s_jump_1613651770347](11) hans1 IN_SAFE_SHOP_DIST 
; WEP-Range: Infinity salv-Range: Infinity; 
[StarLoader] 18-02-2021 at 14:34:32 MEZ  lastAiState null
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ############## AI for ship GFLTSHP_-10000000_4rl5[Traders]
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship has AIGameSegmentControllerConfiguration
[StarLoader] 18-02-2021 at 14:34:32 MEZ  ship is active AI: true
[StarLoader] 18-02-2021 at 14:34:32 MEZ  last AI entity state: S&D_ENT
->[SimpleSearchAndDestroyProgram->ShipGettingToTarget]
....Ship[greenship_s_jump_1613651770347](11) hans1 IN_SAFE_SHOP_DIST 
; WEP-Range: Infinity salv-Range: Infinity; 
[StarLoader] 18-02-2021 at 14:34:32 MEZ  lastAiState null
[StarLoader] 18-02-2021 at 14:34:32 MEZ  shipShopis not instance of SegmentControllerAIInterface



-------------------------------------------------------
snooping through the states of the searchAndDestroyProgram, a lot seems to be handled by the ShipGameState.java, which some of the states inherit from:
EvadingTarget, SearchingForTarget
In ShipGameState.findTarget()
this seems to be the ID of the targeted object:
int specificTargetId = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getSpecificTargetId();

seems to be attached to a SegmentControllerAIEntity<Ship> -> is this the AI object? how to access?

ShipAIEntity extends SegmentControllerAIEntity<Ship>
Ship.java has getter for aiConfiguration which uses ShipAIEntity and has getter for getAiEntityState(), returns aIEntity
maybe AIGameConfiguration.aIEntity returns the entity?