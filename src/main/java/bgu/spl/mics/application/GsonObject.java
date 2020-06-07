package bgu.spl.mics.application;

public class GsonObject {
     String[] inventory;
     GsonService services;
     GsonAgent[] squad;

    class GsonService {
        int M;
        int Moneypenny;
        GsonIntelligence[] intelligence;
        int time;

        class GsonIntelligence {
            GsonMission[] missions;

            class GsonMission {
                String[] serialAgentsNumbers;
                int duration;
                String gadget;
                String name;
                int timeExpired;
                int timeIssued;
            }
        }
    }
    class GsonAgent {
        String name;
        String serialNumber;
    }
}