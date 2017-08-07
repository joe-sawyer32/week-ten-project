package com.theironyard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.EMPTY_SET;

/**
 * Created by Joe on 7/27/17.
 */
public class Processor {

    private final String WORK_ORDER_DIRECTORY = "./work-orders/";
    private static Map<Status, Set<WorkOrder>> workOrders = new LinkedHashMap<>();


    public void processWorkOrders() {
        while (true) {
            moveWorkOrders();
            readWorkOrders();
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(15000L);
        } catch (InterruptedException ex) {
            System.out.println("Couldn't fall asleep....");
            ex.printStackTrace();
        }
    }

    private void moveWorkOrders() {
        // move work orders in map from one state to another
        System.out.println("Current work orders:");
        print(workOrders);
        System.out.println("Moving work orders...");
    }

    private void print(Map<Status, Set<WorkOrder>> map) {
        for (Status status : map.keySet()) {
            System.out.println(status);
            System.out.println(map.get(status));
        }
    }

    private void readWorkOrders() {
        // read the json files into WorkOrders and put in map
        System.out.println("Searching for new work orders...");
        ObjectMapper mapper = new ObjectMapper();
        File workOrderDirectory = new File(WORK_ORDER_DIRECTORY);
        File files[] = workOrderDirectory.listFiles();
        if (files.length != 0) {
            Set<WorkOrder> initialWorkOrders = new HashSet<>();
            for (File f : files) {
                if (f.getName().endsWith(".json")) {
                    // f is a reference to a json file
                    WorkOrder newWorkOrder = convertJsonFileToObject(f, mapper);
                    // f.delete(); will delete the file
                    f.delete();
                    System.out.println("Found new work order:\n");
                    System.out.println(newWorkOrder);
                    initialWorkOrders.add(newWorkOrder);
                }
            }
            workOrders.put(Status.INITIAL, initialWorkOrders);
        } else {
            System.out.println("No new work orders found");
        }
    }

    private static WorkOrder convertJsonFileToObject(File json, ObjectMapper mapper) {
        WorkOrder wo = new WorkOrder();
        try {
            wo = mapper.readValue(json, WorkOrder.class);
        } catch (JsonProcessingException ex) {
            System.out.println("\nUnable to convert from JSON");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("\nUnsuccessful read from file");
            ex.printStackTrace();
        }
        return wo;
    }

    public static void main(String args[]) {
        initializeMap();
        Processor processor = new Processor();
        processor.processWorkOrders();
    }

    private static void initializeMap() {
        workOrders.put(Status.INITIAL, EMPTY_SET);
        workOrders.put(Status.ASSIGNED, EMPTY_SET);
        workOrders.put(Status.IN_PROGRESS, EMPTY_SET);
        workOrders.put(Status.DONE, EMPTY_SET);
    }
}
