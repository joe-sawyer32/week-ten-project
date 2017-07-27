package com.theironyard;

/**
 * Created by Joe on 7/27/17.
 */
public class Processor {
    public void processWorkOrders() {
        moveIt();
        readIt();
    }

    private void moveIt() {
        // move work orders in map from one state to another
    }

    private void readIt() {
        // read the json files into WorkOrders and put in map
    }

    public static void main(String args[]) {
        Processor processor = new Processor();
        processor.processWorkOrders();
    }
}
