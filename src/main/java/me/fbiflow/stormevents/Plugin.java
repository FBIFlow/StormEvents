package me.fbiflow.stormevents;

import me.fbiflow.stormevents.command.ReloadCommand;
import me.fbiflow.stormevents.model.config.implementation.yaml.ConfigurationImpl;
import me.fbiflow.stormevents.model.event.events.stormevent.StormEvent;
import me.fbiflow.stormevents.service.EventExecutorService;
import me.fbiflow.stormevents.service.EventHolder;
import me.fbiflow.stormevents.service.EventLoopService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {

    public static Plugin instance;
    private PluginContainer pluginContainer;
    private boolean active = true;

    @SuppressWarnings({"unchecked"})
    @Override
    public void onEnable() {
        getLogger().info(String.format("Starting %s...", getName()));

        getServer().getPluginCommand("reload-plugin").setExecutor(new ReloadCommand());
        init();
        getPluginContainer().getEventHolder()
                .registerEvents(StormEvent.class);
        start();
    }

    @Override
    public void onDisable() {
        this.active = false;
        getLogger().info(String.format("Disabling %s...", getName()));
        EventExecutorService executorService = getPluginContainer().getExecutorService();
        executorService.getEventProcessors().keySet().forEach(event -> executorService.interruptTask(event.event, -1));
    }

    public boolean isActive() {
        return this.active;
    }

    public PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    public void reloadPlugin() {
        pluginContainer.getConfiguration().reloadConfig();
        pluginContainer.getExecutorService().reload();
        pluginContainer.getEventLoopService().reload();
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    private void init() {
        instance = this;
        instance.pluginContainer = new PluginContainer(new ConfigurationImpl());
        pluginContainer.setEventHolder(new EventHolder());
        pluginContainer.setEventLoopService(new EventLoopService());
        pluginContainer.setEventExecutorService(new EventExecutorService());
    }

    private void start() {
        pluginContainer.getEventLoopService().execute();
    }


}