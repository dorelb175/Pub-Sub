package bgu.spl.mics.application;

import java.util.List;

public class GsonDiary {
    public GsonReport[] reports;
    public int total;

    public static class GsonReport {
        String missionName;
        int m;
        int moneypenny;
        List<String> agentsSerialNumbers;
        List<String> agentsNames;
        String gadgetName;
        int timeCreated;
        int timeIssued;
        int qTime;

        public GsonReport(String missionName, int m, int moneypenny, List<String> agentsSerialNumbers, List<String> agentsNames, String gadgetName, int timeCreated, int timeIssued, int qTime) {
            this.missionName = missionName;
            this.m = m;
            this.moneypenny = moneypenny;
            this.agentsSerialNumbers = agentsSerialNumbers;
            this.agentsNames = agentsNames;
            this.gadgetName = gadgetName;
            this.timeCreated = timeCreated;
            this.timeIssued = timeIssued;
            this.qTime = qTime;
        }
    }
}