package bgu.spl.mics.application.passiveObjects;

import java.util.*;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {
    private Map<String, Agent> agents;

    private static class SquadHolder {
        private static Squad instance = new Squad();
    }

    private Squad() {
        agents = new HashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Squad getInstance() {
        return SquadHolder.instance;
    }

    public Map<String, Agent> getAgentsMap() {return agents;}

    /**
     * Initializes the squad. This method adds all the agents to the squad.
     * <p>
     *
     * @param agents Data structure containing all data necessary for initialization
     *               of the squad.
     */
    public void load(Agent[] agents) {
        for (Agent agent : agents)
            this.agents.put(agent.getSerialNumber(), agent);
    }

    /**
     * Releases agents.
     */
    public void releaseAgents(List<String> serials) {
        for (String serialNumber : serials) {
            if (agents.get(serialNumber) != null) {
                synchronized (agents.get(serialNumber)) {
                    agents.get(serialNumber).release();

                    //notify that the agent is available
                    agents.get(serialNumber).notifyAll();
                }
            }

        }
    }

    /**
     * simulates executing a mission by calling sleep.
     *
     * @param time ticks to sleep
     */
    public void sendAgents(List<String> serials, int time) {
        try {
            Thread.sleep(time * 100);
        }
        catch (InterruptedException e) { }
        finally { releaseAgents(serials); }
    }

    /**
     * acquires an agent, i.e. holds the agent until the caller is done with it
     *
     * @param serials the serial numbers of the agents
     * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
     */
    public boolean getAgents(List<String> serials) {
        for (String serialNumber : serials) {
            if (agents.get(serialNumber) == null)
                return false;

            synchronized (agents.get(serialNumber)) {
                //wait until the agent is available
                while ((!agents.get(serialNumber).isAvailable())) {
                    try { agents.get(serialNumber).wait(); }
                    catch (InterruptedException e) { }
                }
                //acquire this agent
                agents.get(serialNumber).acquire();
            }
        }
        return true;
    }

    /**
     * gets the agents names
     *
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials) {
        List<String> agentsNames = new LinkedList<String>();
        for (String serialNumber : serials) {
            agentsNames.add(agents.get(serialNumber).getName());
        }
        return agentsNames;
    }
}
