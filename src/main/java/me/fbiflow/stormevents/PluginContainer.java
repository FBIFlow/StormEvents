package me.fbiflow.stormevents;

import me.fbiflow.stormevents.model.config.Configuration;
import me.fbiflow.stormevents.service.EventHolder;
import me.fbiflow.stormevents.service.EventLoopService;
import me.fbiflow.stormevents.service.EventExecutorService;

public class PluginContainer {

    private final Configuration configuration;
    private EventHolder eventHolder;
    private EventLoopService eventLoopService;
    private EventExecutorService eventExecutorService;

    public PluginContainer(Configuration configuration) {
        this.configuration = configuration;
/*        this.eventHolder = new EventHolder();
        this.eventLoopService = new EventLoopService();
        this.eventExecutorService = new EventExecutorService();*/
    }

    public void setEventHolder(EventHolder eventHolder) {
        this.eventHolder = eventHolder;
    }

    public void setEventLoopService(EventLoopService eventLoopService) {
        this.eventLoopService = eventLoopService;
    }

    public void setEventExecutorService(EventExecutorService eventExecutorService) {
        this.eventExecutorService = eventExecutorService;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public EventHolder getEventHolder() {
        return this.eventHolder;
    }

    public EventLoopService getEventLoopService() {
        return this.eventLoopService;
    }

    public EventExecutorService getExecutorService() {
        return this.eventExecutorService;
    }
}
