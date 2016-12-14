package com.capstone.locker.Buletooth.presenter;

import java.util.HashMap;

/**
 * Created by HANJIEUN on 2016-05-22.
 */

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String RGB_CHARACTERISTIC_CONFIG="0003CBB1-0000-1000-8000-00805F9B0131";

    public static String CAPSENSE_SERVICE = "0000cab5-0000-1000-8000-00805f9b34fb";
    public static String CAPSENSE_SERVICE_CUSTOM = "0003cab5-0000-1000-8000-00805f9b0131";

    public static final String CAPSENSE_PROXIMITY = "0000caa1-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_SLIDER = "0000caa2-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_BUTTONS = "0000caa3-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_PROXIMITY_CUSTOM = "0003caa1-0000-1000-8000-00805f9b0131";
    public static final String CAPSENSE_SLIDER_CUSTOM = "0003caa2-0000-1000-8000-00805f9b0131";
    public static final String CAPSENSE_BUTTONS_CUSTOM = "0003caa3-0000-1000-8000-00805f9b0131";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0003CBB1-0000-1000-8000-00805F9B0131","RGB Service");


        attributes.put(CAPSENSE_SERVICE, "CapSense Service");
        attributes.put(CAPSENSE_SERVICE_CUSTOM, "CapSense Service");

        attributes.put(CAPSENSE_BUTTONS, "CapSense Button");
        attributes.put(CAPSENSE_PROXIMITY, "CapSense Proximity");
        attributes.put(CAPSENSE_SLIDER, "CapSense Slider");
        attributes.put(CAPSENSE_BUTTONS_CUSTOM, "CapSense Button");
        attributes.put(CAPSENSE_PROXIMITY_CUSTOM, "CapSense Proximity");
        attributes.put(CAPSENSE_SLIDER_CUSTOM, "CapSense Slider");


        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("0003ABBB-0000-1000-8000-00805F9B0132","Sound Recognition Boolean");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}



