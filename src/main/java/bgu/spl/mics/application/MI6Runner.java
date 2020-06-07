package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

import bgu.spl.mics.MessageBroker;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.*;
import com.google.gson.Gson;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) {
        MessageBroker mb = MessageBrokerImpl.getInstance();
        Inventory inventory = Inventory.getInstance();
        Squad squad = Squad.getInstance();
        LinkedList<Thread> threads = new LinkedList<>();

        /****************************************************************************
         -------------------------------READ JSON FILE--------------------------------
         ****************************************************************************/
        try (Reader reader = new FileReader(args[0])) {
            Gson gson = new Gson();
            GsonObject gsonObject = gson.fromJson(reader, GsonObject.class);

            //Load Gadgets for Inventory
            inventory.load(gsonObject.inventory);

            //Load Services:

            //M instances
            for (int i = 1; i <= gsonObject.services.M; i++)
                threads.add(new Thread(new M("M", i)));

            //MoneyPenny instances
            for (int i = 1; i <= gsonObject.services.Moneypenny; i++)
                threads.add(new Thread(new Moneypenny("Moneypenny", i)));

            //Intelligence instances
            for (int i = 1; i <= gsonObject.services.intelligence.length; i++) {
                List<MissionInfo> missions = new LinkedList<>();

                //get the missions for each Intelligence
                for (GsonObject.GsonService.GsonIntelligence.GsonMission mission : gsonObject.services.intelligence[i - 1].missions) {
                    //-----------Mission----------
                    MissionInfo missionInfo = new MissionInfo();
                    List<String> serialList = new LinkedList<>();

                    //get the serialAgentsNumbers
                    for (String serial : mission.serialAgentsNumbers)
                        serialList.add(serial);

                    //Set all of the data fields for the mission
                    missionInfo.setSerialAgentsNumbers(serialList);
                    missionInfo.setDuration(mission.duration);
                    missionInfo.setGadget(mission.gadget);
                    missionInfo.setMissionName(mission.name);
                    missionInfo.setTimeExpired(mission.timeExpired);
                    missionInfo.setTimeIssued(mission.timeIssued);
                    //---------Mission--------------
                    //add the current mission to the missions of the current intelligence
                    missions.add(missionInfo);
                }

                threads.add(new Thread(new Intelligence("Intelligence", missions)));
            }

            //Load Time
            TimeService timeService = new TimeService("TimeService", gsonObject.services.time);

            //Load Squad
            Agent[] agents = new Agent[gsonObject.squad.length];
            for (int i = 0; i < agents.length; i++) {
                Agent agent = new Agent();
                agent.setName(gsonObject.squad[i].name);
                agent.setSerialNumber(gsonObject.squad[i].serialNumber);
                agents[i] = agent;
            }
            squad.load(agents);
            /****************************************************************************
             ---------------------------END OF READ JSON FILE----------------------------
             ****************************************************************************/


            /****************************************************************************
             ------------------------------RUN THE PROGRAM-------------------------------
             ****************************************************************************/

            threads.add(new Thread(new Q("Q", inventory)));

            //start all the threads except for time service
            for (Thread t : threads)
                t.start();

            //wait until the threads initialize
            LinkedList<Thread> threadsToWait = new LinkedList<>(threads);
            while (!threadsToWait.isEmpty()) {
                //if a thread is in Blocked/Wait state - it means that he has finished the initialization and waits for the messages
                while (!threadsToWait.isEmpty() && (((threadsToWait.peek().getState() == Thread.State.BLOCKED) || (threadsToWait.peek().getState() == Thread.State.WAITING))))
                    threadsToWait.poll();
            }

            //TimeService starts after all others initialized
            Thread ts = new Thread(timeService);
            ts.start();


            /****************************************************************************
             ----------------------END OF THE PROGRAM------------------------------------
             ****************************************************************************/

            //Finish - Wait until all threads are done
            for (Thread t : threads)
                t.join();

            ts.join();

            /****************************************************************************
             ---------------------------PRINTING OUTPUT FILES-----------------------------
             ****************************************************************************/

            inventory.printToFile(args[1]);
            Diary.getInstance().printToFile(args[2]);

            /****************************************************************************
             -----------------------END OF PRINTING OUTPUT FILE--------------------------
             ****************************************************************************/
        } catch (Exception e) { }
    }
}