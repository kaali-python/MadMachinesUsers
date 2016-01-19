package com.sports.unity.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by amandeep on 12/1/16.
 */
public class GlobalEventHandler {

    private static GlobalEventHandler GLOBAL_EVENT_HANDLER = null;

    synchronized public static GlobalEventHandler getInstance(){
        if( GLOBAL_EVENT_HANDLER == null ){
            GLOBAL_EVENT_HANDLER = new GlobalEventHandler();
        }
        return GLOBAL_EVENT_HANDLER;
    }

    private HashMap<String, GlobalEventListener> globalEventListenerMap = new HashMap<>();

    private GlobalEventHandler(){

    }

    public void addGlobalEventListener(String key, GlobalEventListener globalEventListener){
        globalEventListenerMap.put(key, globalEventListener);
    }

    public void removeGlobalEventListener(String key){
        globalEventListenerMap.remove(key);
    }

    public void internetStateChanged(boolean connected){
        Iterator<String> iterator = globalEventListenerMap.keySet().iterator();
        while( iterator.hasNext() ){
            String key = iterator.next();
            GlobalEventListener listener = globalEventListenerMap.get(key);
            if( listener != null ){
                listener.onInternetStateChanged(connected);
            }
        }
    }

    public void xmppServerConnected(boolean connected){
        Iterator<String> iterator = globalEventListenerMap.keySet().iterator();
        while( iterator.hasNext() ){
            String key = iterator.next();
            GlobalEventListener listener = globalEventListenerMap.get(key);
            if( listener != null ){
                listener.onXMPPServerConnected(connected);
            }
        }
    }

}