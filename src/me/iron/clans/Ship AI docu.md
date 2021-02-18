# Ship AI documentation
im trying to write down what i figure out during my hunt for ship AI.

Segmentcontroller -> cast to SegmentControllerAIInterface (dead class), extends AIInterface (dead class), has getter for AIConfigurationInterface
-> inherits to AIGameConfiguration -> AIGameCreatureConfiguration, AIGameSegmentControllerConfiguration

method to get AIGameSegmentControllerConfiguration:
((AIGameSegmentControllerConfiguration) ((SegmentControllerAIInterface) mySegmentController).getAiConfiguration())

AIGameSegmentControllerConfiguration offers:
(from AIConfigInterface)
```
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
``` 
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
    