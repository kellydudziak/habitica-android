package com.habitrpg.android.habitica.models;

/**
 * Created by krh12 on 11/28/2016.
 */

import android.util.Log;

import com.habitrpg.android.habitica.models.notifications.NotificationData;

import java.util.HashMap;
import java.util.Map;

public class Notification {
    private static final String DEBUG_TAG = "Notification";

    public NotificationData data;
    private String type;
    private String createdAt;
    private String id;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The type
     */
    public String getType() {
        Log.d(DEBUG_TAG, "in getType");
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        Log.d(DEBUG_TAG, "in setType");
        this.type = type;
    }

    /**
     * @return The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt The createdAt
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     * @return
     * The data
     */
//    public T getData() {
//        return data;
//    }

    /**
     *
     * @param data
     * The data
     */
//    public void setData(T data) {
//        this.data = data;
//    }

    /**
     * @return The id
     */
    public String getId() {
    Log.d(DEBUG_TAG, "in getId");
    return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        Log.d(DEBUG_TAG, "in setId");
        this.id = id;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}