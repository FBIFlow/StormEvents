package me.fbiflow.stormevents.model.event;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.model.config.DataContainer;
import me.fbiflow.stormevents.model.enums.TaskType;
import me.fbiflow.stormevents.model.enums.TimeUnit;
import me.fbiflow.stormevents.util.TimeConverter;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Event {

    protected final DataContainer dataContainer;
    //k - time in ticks, v = runnable
    private final HashMap<Integer, ScheduledTask> scheduledTasks;
    protected final List<BukkitTask> bukkitTasks;

    protected boolean initialized;

    /**
     * Помимо фактического вызова ивента этот конструктор будет вызван так-же при регистрации события в EventHolder
     * это стоит учитывать при внесении сюда какой-либо логики <p>
     * Плохо: <br>
     * Регистрировать здесь слушатели событий bukkit <br>
     * Вызывать здесь логику, относящуюся к конкретному экземпляру ивента <p>
     * Хорошо: <br>
     * Создать конфигурационный файл для этого ивента <br>
     * Запустить какие-то общие действия, не связанные с конкретным экземпляром ивента
     */
    public Event() {
        this.scheduledTasks = new HashMap<>();
        this.dataContainer = Plugin.instance.getPluginContainer().getConfiguration().getDataContainer(getId());
        this.bukkitTasks = new ArrayList<>();
    }

    /**
     * Идентификатор ивента, по которому он будет определяться в файле конфигурации
     *
     * @return id
     */
    public abstract String getId();

    /**
     * Время жизни ивента, он будет остановлен когда это время выйдет, даже если останутся запланированные задачи
     *
     * @return
     */
    public abstract int getLiveTimeTicks();

    /**
     * Эта логика будет вызвана при инициализации ивента, не при создании экземпляра,
     * а когда ивент будет фактически запущен в игре
     */
    public abstract void onInit();

    /**
     * Необходимо чтобы гарантировать, что onInit() завершил своё выполнение, прежде чем приступить к вызову onTick()
     * @return инициализирован ли ивент
     */
    public abstract boolean isInitialized();

    /**
     * Эта логика будет вызываться каждый тик существования ивента
     *
     * @param eventTick текущий тик жизни ивента
     */
    public abstract void onTick(int eventTick);

    /**
     * Эта логика будет вызвана при принудительном прерывании события или окончании времени жизни (getLiveTimeTicks()) <p>
     * Здесь стоит определить логику завершения всех возможных задач, которые могут быть активны при прерывании <p>
     * Мануально запущенная задача Bukkit или любой иной планировщик отложенных задач следует прервать здесь, даже если
     * предполагается, что он будет завершен до конца live time. <p>
     * Как пример, если нам необходимо несколько раз за ивент вставлять и откатывать схематики, стоит продублировать
     * логику отката в этот метод, чтобы при незапланированном прерывании ивента(например отключении сервера) схематики
     * всеравно откатились
     */
    public abstract void onRemove();


    public Map<Integer, ScheduledTask> getScheduledTasks() {
        return scheduledTasks;
    }

    /**
     * Если создаётся мануально новый BukkitRunnable, то экземпляр BukkitTask стоит поместить в bukkitTasks, чтобы
     * корректно завершать задачу при непредвиденной остановке или неправильно установленном времени в bukkit задаче,
     * например в случае, если ивент завершится, а задача останется запланированной
     * @return лист буккит задач
     */
    public List<BukkitTask> getBukkitTasks() {
        return this.bukkitTasks;
    }

    /**
     * Регистрация запланированной задачи. <p>
     * Это безопасный способ планировки задач, при котором она не будет сразу внесена в очередь, а будет запущена
     * внутренней логикой в тот момент, когда она должна быть запущена. <p>
     * Пользуясь этим можно быть уверенным, что запланированная задача не запустится, если событие будет прервано, как в
     * случае, если бы мы в ручную создавали планировщики задач bukkit или стандартные отложенные задачи
     *
     * @param task     Выполняемый код
     * @param taskType Как будет выполнена задача, через bukkit поток, или стандартно, при взаимодействии с миром, ивентарями
     *                 или любыми иными игровыми данными стоит использовать bukkit
     * @param timeUnit Юнит времени
     * @param time     тик, когда задача будет выполняться. <br>
     *                 Он не всегда эквивалентен текущему тику жизни, задачу можно
     *                 запланировать на отрицательное значение времени, тогда счётчик начнётся именно с наименьшего значения и эта задача
     *                 будет выполнена сразу. <br>
     *                 Это лишь синтаксический сахар, если хочется за 0 принять какое-то определенное событие, а не
     *                 начало ивента, то можно выполнить какую-то логику в отрицательном значении
     */
    protected void registerTask(Runnable task, TaskType taskType, TimeUnit timeUnit, int time) {
        ScheduledTask scheduledTask = new ScheduledTask(
                task,
                taskType,
                TimeConverter.ticksOf(timeUnit, time));
        scheduledTasks.put(scheduledTask.getTimeTicks(), scheduledTask);
    }

    /**
     * @param task      задача
     * @param taskType  тип задачи
     * @param timeTicks время выполнения <p>
     * @see #registerTask(Runnable, TaskType, TimeUnit, int)
     */
    protected void registerTask(Runnable task, TaskType taskType, int timeTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(
                task,
                taskType,
                timeTicks);
        scheduledTasks.put(scheduledTask.getTimeTicks(), scheduledTask);
    }
}