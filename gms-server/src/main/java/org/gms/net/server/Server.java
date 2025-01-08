/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.net.server;

import lombok.Getter;
import lombok.Setter;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.SkillFactory;
import org.gms.client.command.CommandsExecutor;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.ItemFactory;
import org.gms.config.GameConfig;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.PlayernpcsFieldDO;
import org.gms.model.dto.ServerShutdownDTO;
import org.gms.property.ServiceProperty;
import org.gms.util.*;
import org.gms.model.pojo.NewYearCardRecord;
import org.gms.client.processor.npc.FredrickProcessor;
import org.gms.constants.game.GameConstants;
import org.gms.constants.inventory.ItemConstants;
import org.gms.constants.net.OpcodeConstants;
import org.gms.constants.net.ServerConstants;
import org.gms.dao.entity.NxcouponsDO;
import org.gms.manager.ServerManager;
import org.gms.net.ChannelDependencies;
import org.gms.net.PacketProcessor;
import org.gms.net.netty.LoginServer;
import org.gms.net.packet.Packet;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.coordinator.session.IpAddresses;
import org.gms.net.server.coordinator.session.SessionCoordinator;
import org.gms.net.server.guild.Alliance;
import org.gms.net.server.guild.Guild;
import org.gms.net.server.guild.GuildCharacter;
import org.gms.net.server.task.*;
import org.gms.net.server.world.World;
import org.gms.server.CashShop.CashItemFactory;
import org.gms.server.SkillbookInformationProvider;
import org.gms.server.ThreadManager;
import org.gms.server.TimerManager;
import org.gms.server.expeditions.ExpeditionBossLog;
import org.gms.server.life.PlayerNPC;
import org.gms.server.quest.Quest;
import org.gms.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.*;

public class Server {
    static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false"); // Mute GraalVM warning: "The polyglot context is using an implementation that does not support runtime compilation."
    }

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static Server instance = null;

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    private static final Set<Integer> activeFly = new HashSet<>();
    private static final Map<Integer, Integer> couponRates = new HashMap<>(30);
    private static final List<Integer> activeCoupons = new LinkedList<>();
    private ChannelDependencies channelDependencies;

    private LoginServer loginServer;
    private final List<Map<Integer, String>> channels = new LinkedList<>();
    private final List<World> worlds = new ArrayList<>();
    @Getter
    private final Properties subnetInfo = new Properties();
    private final Map<Integer, Set<Integer>> accountChars = new HashMap<>();
    private final Map<Integer, Short> accountCharacterCount = new HashMap<>();
    private final Map<Integer, Integer> worldChars = new HashMap<>();
    private final Map<String, Integer> transitioningChars = new HashMap<>();
    private final List<Pair<Integer, String>> worldRecommendedList = new LinkedList<>();
    private final Map<Integer, Guild> guilds = new HashMap<>(100);
    private final Map<Client, Long> inLoginState = new HashMap<>(100);

    private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private final Map<Integer, Alliance> alliances = new HashMap<>(100);
    private final Map<Integer, NewYearCardRecord> newyears = new HashMap<>();
    private final List<Client> processDiseaseAnnouncePlayers = new LinkedList<>();
    private final List<Client> registeredDiseaseAnnouncePlayers = new LinkedList<>();

    private final List<List<Pair<String, Integer>>> playerRanking = new LinkedList<>();

    private final Lock srvLock = new ReentrantLock();
    private final Lock disLock = new ReentrantLock();

    private final Lock wldRLock;
    private final Lock wldWLock;

    private final Lock lgnRLock;
    private final Lock lgnWLock;

    private final AtomicLong currentTime = new AtomicLong(0);
    private long serverCurrentTime = 0;

    private volatile boolean availableDeveloperRoom = false;
    @Getter
    @Setter
    private boolean online = false;
    public static long uptime = System.currentTimeMillis();
    private long nextTime;

    private static final NpcService npcService = ServerManager.getApplicationContext().getBean(NpcService.class);
    private static final NxCouponService nxCouponService = ServerManager.getApplicationContext().getBean(NxCouponService.class);
    private static final CharacterService characterService = ServerManager.getApplicationContext().getBean(CharacterService.class);
    private static final AccountService accountService = ServerManager.getApplicationContext().getBean(AccountService.class);
    private static final NxCodeService nxCodeService = ServerManager.getApplicationContext().getBean(NxCodeService.class);
    private static final NewYearCardService newYearCardService = ServerManager.getApplicationContext().getBean(NewYearCardService.class);
    private static final NameChangeService nameChangeService = ServerManager.getApplicationContext().getBean(NameChangeService.class);
    private static final WorldTransferService worldTransferService = ServerManager.getApplicationContext().getBean(WorldTransferService.class);
    private static final FamilyService familyService = ServerManager.getApplicationContext().getBean(FamilyService.class);
    private static final NoteService noteService = ServerManager.getApplicationContext().getBean(NoteService.class);
    private static final HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
    private static final ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);

    private Server() {
        ReadWriteLock worldLock = new ReentrantReadWriteLock(true);
        this.wldRLock = worldLock.readLock();
        this.wldWLock = worldLock.writeLock();

        ReadWriteLock loginLock = new ReentrantReadWriteLock(true);
        this.lgnRLock = loginLock.readLock();
        this.lgnWLock = loginLock.writeLock();
    }

    public int getCurrentTimestamp() {
        return (int) (Server.getInstance().getCurrentTime() - Server.uptime);
    }

    public long getCurrentTime() {  // returns a slightly delayed time value, under frequency of UPDATE_INTERVAL
        return serverCurrentTime;
    }

    public void updateCurrentTime() {
        serverCurrentTime = currentTime.addAndGet(GameConfig.getServerLong("update_interval"));
    }

    public long forceUpdateCurrentTime() {
        long timeNow = System.currentTimeMillis();
        serverCurrentTime = timeNow;
        currentTime.set(timeNow);

        return timeNow;
    }

    public List<Pair<Integer, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    public void setNewYearCard(NewYearCardRecord nyc) {
        newyears.put(nyc.getId(), nyc);
    }

    public NewYearCardRecord getNewYearCard(int cardid) {
        return newyears.get(cardid);
    }

    public NewYearCardRecord removeNewYearCard(int cardid) {
        return newyears.remove(cardid);
    }

    public void setAvailableDeveloperRoom() {
        availableDeveloperRoom = true;
    }

    public boolean canEnterDeveloperRoom() {
        return availableDeveloperRoom;
    }

    private void loadPlayerNpcMapStepFromDb() {
        List<PlayernpcsFieldDO> playernpcsFieldDOList = npcService.getPlayerNpcFields(new PlayernpcsFieldDO());
        playernpcsFieldDOList.forEach(playernpcsFieldDO -> {
            World world = getWorld(playernpcsFieldDO.getWorld());
            if (world != null) world.setPlayerNpcMapData(playernpcsFieldDO.getMap(), playernpcsFieldDO.getStep(), playernpcsFieldDO.getPodium());
        });
    }

    public World getWorld(int id) {
        wldRLock.lock();
        try {
            try {
                return worlds.get(id);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } finally {
            wldRLock.unlock();
        }
    }

    public List<World> getWorlds() {
        wldRLock.lock();
        try {
            return Collections.unmodifiableList(worlds);
        } finally {
            wldRLock.unlock();
        }
    }

    public int getWorldsSize() {
        wldRLock.lock();
        try {
            return worlds.size();
        } finally {
            wldRLock.unlock();
        }
    }

    public Channel getChannel(int world, int channel) {
        try {
            return this.getWorld(world).getChannel(channel);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public List<Channel> getChannelsFromWorld(int world) {
        try {
            return this.getWorld(world).getChannels();
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
        }
    }

    public List<Channel> getAllChannels() {
        try {
            List<Channel> channelz = new ArrayList<>();
            for (World world : this.getWorlds()) {
                channelz.addAll(world.getChannels());
            }
            return channelz;
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
        }
    }

    public Set<Integer> getOpenChannels(int world) {
        wldRLock.lock();
        try {
            return new HashSet<>(channels.get(world).keySet());
        } finally {
            wldRLock.unlock();
        }
    }

    private String getIP(int world, int channel) {
        wldRLock.lock();
        try {
            return channels.get(world).get(channel);
        } finally {
            wldRLock.unlock();
        }
    }

    public String[] getInetSocket(Client client, int world, int channel) {
        String remoteIp = client.getRemoteAddress();

        String[] hostAddress = getIP(world, channel).split(":");
        if (IpAddresses.isLocalAddress(remoteIp)) {
            hostAddress[0] = serviceProperty.getLocalhost();
        } else if (IpAddresses.isLanAddress(remoteIp)) {
            hostAddress[0] = serviceProperty.getLanHost();
        }

        try {
            return hostAddress;
        } catch (Exception e) {
            return null;
        }
    }

    public int addChannel(int worldid) {
        World world;
        Map<Integer, String> channelInfo;
        int channelid;

        wldRLock.lock();
        try {
            if (worldid >= worlds.size()) {
                return -3;
            }

            channelInfo = channels.get(worldid);
            if (channelInfo == null) {
                return -3;
            }

            channelid = channelInfo.size();
            if (channelid >= GameConfig.getServerInt("max_channel_size")) {
                return -2;
            }

            channelid++;
            world = this.getWorld(worldid);
        } finally {
            wldRLock.unlock();
        }

        Channel channel = new Channel(worldid, channelid, getCurrentTime());
        channel.setServerMessage(GameConfig.getWorldString(worldid, "recommend_message"));

        if (world.addChannel(channel)) {
            wldWLock.lock();
            try {
                channelInfo.put(channelid, channel.getIP());
            } finally {
                wldWLock.unlock();
            }
        }

        return channelid;
    }

    public int addWorld() {
        int newWorld = initWorld();
        if (newWorld > -1) {
            reloadWorldsPlayerRanking();

            Set<Integer> accounts;
            lgnRLock.lock();
            try {
                accounts = new HashSet<>(accountChars.keySet());
            } finally {
                lgnRLock.unlock();
            }

            for (Integer accId : accounts) {
                loadAccountCharactersView(accId, 0, newWorld);
            }
        }

        return newWorld;
    }

    private int initWorld() {
        int i;

        wldRLock.lock();
        try {
            i = worlds.size();

            if (i >= GameConfig.getServerInt("max_world_size")) {
                return -1;
            }
        } finally {
            wldRLock.unlock();
        }

        log.info(I18nUtil.getLogMessage("Server.initWorld.info1"), i);

        float expRate = GameConfig.getWorldFloat(i, "exp_rate");
        float mesoRate = GameConfig.getWorldFloat(i, "meso_rate");
        float dropRate = GameConfig.getWorldFloat(i, "drop_rate");
        float bossDropRate = GameConfig.getWorldFloat(i, "boss_drop_rate");
        float questRate = GameConfig.getWorldFloat(i, "quest_rate");
        float travelRate = GameConfig.getWorldFloat(i, "travel_rate");
        float fishingRate = GameConfig.getWorldFloat(i, "fishing_rate");

        int flag = GameConfig.getWorldInt(i, "flag");
        String event_message = GameConfig.getWorldString(i, "event_message");
        String recommend_message = GameConfig.getWorldString(i, "recommend_message");

        World world = new World(i, flag, event_message, expRate, dropRate, bossDropRate, mesoRate, questRate,
                travelRate, fishingRate);

        Map<Integer, String> channelInfo = new HashMap<>();
        long bootTime = getCurrentTime();
        for (int j = 1; j <= GameConfig.getWorldInt(i, "channel_size"); j++) {
            Channel channel = new Channel(i, j, bootTime);

            world.addChannel(channel);
            channelInfo.put(j, channel.getIP());
        }

        boolean canDeploy;

        wldWLock.lock();    // thanks Ashen for noticing a deadlock issue when trying to deploy a channel
        try {
            canDeploy = world.getId() == worlds.size();
            if (canDeploy) {
                worldRecommendedList.add(new Pair<>(i, recommend_message));
                worlds.add(world);
                channels.add(i, channelInfo);
            }
        } finally {
            wldWLock.unlock();
        }

        if (canDeploy) {
            world.setServerMessage(GameConfig.getWorldString(i, "server_message"));

            log.info(I18nUtil.getLogMessage("Server.initWorld.info2"), i);
            return i;
        } else {
            log.error(I18nUtil.getLogMessage("Server.initWorld.error1"), i);
            world.shutdown();
            return -2;
        }
    }

    public boolean removeChannel(int worldid) {   //lol don't!
        World world;

        wldRLock.lock();
        try {
            if (worldid >= worlds.size()) {
                return false;
            }
            world = worlds.get(worldid);
        } finally {
            wldRLock.unlock();
        }

        if (world != null) {
            int channel = world.removeChannel();
            wldWLock.lock();
            try {
                Map<Integer, String> m = channels.get(worldid);
                if (m != null) {
                    m.remove(channel);
                }
            } finally {
                wldWLock.unlock();
            }

            return channel > -1;
        }

        return false;
    }

    public boolean removeWorld() {   //lol don't!
        World w;
        int worldid;

        wldRLock.lock();
        try {
            worldid = worlds.size() - 1;
            if (worldid < 0) {
                return false;
            }

            w = worlds.get(worldid);
        } finally {
            wldRLock.unlock();
        }

        if (w == null || !w.canUninstall()) {
            return false;
        }

        w.shutdown();

        wldWLock.lock();
        try {
            if (worldid == worlds.size() - 1) {
                worlds.remove(worldid);
                channels.remove(worldid);
                worldRecommendedList.remove(worldid);
            }
            reloadWorldsPlayerRanking();
        } finally {
            wldWLock.unlock();
        }

        return true;
    }

    private void resetServerWorlds() {  // thanks maple006 for noticing proprietary lists assigned to null
        wldWLock.lock();
        try {
            worlds.clear();
            channels.clear();
            worldRecommendedList.clear();
        } finally {
            wldWLock.unlock();
        }
    }

    private static long getTimeLeftForNextHour() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

        return Math.max(0, nextHour.getTimeInMillis() - System.currentTimeMillis());
    }

    public static long getTimeLeftForNextDay() {
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_MONTH, 1);
        nextDay.set(Calendar.HOUR_OF_DAY, 0);
        nextDay.set(Calendar.MINUTE, 0);
        nextDay.set(Calendar.SECOND, 0);

        return Math.max(0, nextDay.getTimeInMillis() - System.currentTimeMillis());
    }

    public Map<Integer, Integer> getCouponRates() {
        return couponRates;
    }

    public List<Integer> getActiveCoupons() {
        synchronized (activeCoupons) {
            return activeCoupons;
        }
    }

    public void commitActiveCoupons() {
        for (World world : getWorlds()) {
            for (Character chr : world.getPlayerStorage().getAllCharacters()) {
                if (!chr.isLoggedIn()) {
                    continue;
                }

                chr.updateCouponRates();
            }
        }
    }

    public void toggleCoupon(Integer couponId) {
        if (ItemConstants.isRateCoupon(couponId)) {
            synchronized (activeCoupons) {
                if (activeCoupons.contains(couponId)) {
                    activeCoupons.remove(couponId);
                } else {
                    activeCoupons.add(couponId);
                }

                commitActiveCoupons();
            }
        }
    }

    public void updateActiveCoupons() {
        synchronized (activeCoupons) {
            activeCoupons.clear();
            Calendar c = Calendar.getInstance();
            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            int hourDay = c.get(Calendar.HOUR_OF_DAY);
            int weekdayMask = (1 << weekDay);
            activeCoupons.addAll(nxCouponService.selectActiveCouponIds(weekdayMask, hourDay));
        }
    }

    public void runAnnouncePlayerDiseasesSchedule() {
        List<Client> processDiseaseAnnounceClients;
        disLock.lock();
        try {
            processDiseaseAnnounceClients = new LinkedList<>(processDiseaseAnnouncePlayers);
            processDiseaseAnnouncePlayers.clear();
        } finally {
            disLock.unlock();
        }

        while (!processDiseaseAnnounceClients.isEmpty()) {
            Client c = processDiseaseAnnounceClients.remove(0);
            Character player = c.getPlayer();
            if (player != null && player.isLoggedInWorld()) {
                player.announceDiseases();
                player.collectDiseases();
            }
        }

        disLock.lock();
        try {
            // this is to force the system to wait for at least one complete tick before releasing disease info for the registered clients
            while (!registeredDiseaseAnnouncePlayers.isEmpty()) {
                Client c = registeredDiseaseAnnouncePlayers.remove(0);
                processDiseaseAnnouncePlayers.add(c);
            }
        } finally {
            disLock.unlock();
        }
    }

    public void registerAnnouncePlayerDiseases(Client c) {
        disLock.lock();
        try {
            registeredDiseaseAnnouncePlayers.add(c);
        } finally {
            disLock.unlock();
        }
    }

    public List<Pair<String, Integer>> getWorldPlayerRanking(int worldid) {
        wldRLock.lock();
        try {
            return new ArrayList<>(playerRanking.get(!GameConfig.getServerBoolean("use_whole_server_ranking") ? worldid : 0));
        } finally {
            wldRLock.unlock();
        }
    }

    public void reloadWorldsPlayerRanking() {
        List<List<CharactersDO>> rankPlayers = characterService.getWorldsRankPlayers(getWorldsSize());
        if (rankPlayers.isEmpty()) {
            return;
        }
        wldWLock.lock();
        try {
            playerRanking.clear();
            rankPlayers.forEach(rankPlayer -> playerRanking.add(rankPlayer.stream().map(c -> new Pair<>(c.getName(), c.getLevel())).collect(Collectors.toList())));
        } finally {
            wldWLock.unlock();
        }
    }

    //游戏启动
    public void init() {
        Instant beforeInit = Instant.now();
        log.info(I18nUtil.getLogMessage("Server.init.info1"), ServerConstants.VERSION);

        // 发送信件
        registerChannelDependencies();

        // 利用虚拟线程，减少开销
        log.info(I18nUtil.getLogMessage("Server.init.info2"));
        try (ExecutorService initExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 加载wz
            final List<Future<?>> futures = new ArrayList<>();
            futures.add(initExecutor.submit(SkillFactory::loadAllSkills));
            futures.add(initExecutor.submit(CashItemFactory::loadAllCashItems));
            futures.add(initExecutor.submit(Quest::loadAllQuests));
            futures.add(initExecutor.submit(SkillbookInformationProvider::loadAllSkillbookInformation));
            // Wait on all async tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
            initExecutor.shutdown();
        } catch (Exception e) {
            log.error(I18nUtil.getLogMessage("Server.init.error1"), e);
            throw new IllegalStateException(e);
        }
        log.info(I18nUtil.getLogMessage("Server.init.info3"));

        TimeZone.setDefault(TimeZone.getTimeZone(GameConfig.getServerString("timezone")));

        log.info(I18nUtil.getLogMessage("Server.init.info4"));
        final int worldCount = Math.min(GameConstants.WORLD_NAMES.length, GameConfig.getConfig().getJSONObject("world").size());

        // 重置登录状态和雇佣商店状态
        accountService.resetAllLoggedIn();
        characterService.resetMerchant();

        // 清空失效的现金物品
        nxCodeService.clearExpirations();

        // 重载倍率卡
        List<NxcouponsDO> nxcouponsDOList = nxCouponService.getNxCoupons(new NxcouponsDO());
        couponRates.clear();
        nxcouponsDOList.forEach(nxcouponsDO -> couponRates.put(nxcouponsDO.getCouponid(), nxcouponsDO.getRate()));
        updateActiveCoupons();
        newYearCardService.startPendingNewYearCardRequests();
        CashIdGenerator.loadExistentCashIdsFromDb();

        // 接受未完成的改名
        nameChangeService.applyAllNameChange();

        // 接受转区
        worldTransferService.applyAllWorldTransfer();

        // 加载玩家排名
        PlayerNPC.loadRunningRankData(worldCount);

        // 主动清理每日零点需要清理的数据
        new BossLogTask().run();
        new ExtendValueTask().run();
        log.info(I18nUtil.getLogMessage("Server.init.info5"));

        ThreadManager.getInstance().start();
        initializeTimelyTasks();    // aggregated method for timely tasks thanks to lxconan

        try {
            for (int i = 0; i < worldCount; i++) {
                initWorld();
            }
            // world初始化后需要加载的
            reloadWorldsPlayerRanking();
            loadPlayerNpcMapStepFromDb();
            if (GameConfig.getServerBoolean("use_family_system")) {
                familyService.loadAllFamilies();
            }
        } catch (Exception e) {
            log.error(I18nUtil.getLogMessage("Server.init.error3"), e); //For those who get errors
            System.exit(0);
        }

        loginServer = initLoginServer(serviceProperty.getLoginPort());
        log.info(I18nUtil.getLogMessage("Server.init.info6"), serviceProperty.getLoginPort());

        OpcodeConstants.generateOpcodeNames();
        CommandsExecutor.getInstance().loadCommandsExecutor();

        log.info(I18nUtil.getLogMessage("Server.init.info7"));
        for (Channel ch : this.getAllChannels()) {
            ch.reloadEventScriptManager();
        }
        log.info(I18nUtil.getLogMessage("Server.init.info8"));
        online = true;
        Duration initDuration = Duration.between(beforeInit, Instant.now());
        log.info(I18nUtil.getLogMessage("Server.init.info9"), initDuration.toMillis() / 1000.0);
    }

    private void registerChannelDependencies() {
        FredrickProcessor fredrickProcessor = new FredrickProcessor(noteService);
        ChannelDependencies channelDependencies = new ChannelDependencies(noteService, fredrickProcessor);
        PacketProcessor.registerGameHandlerDependencies(channelDependencies);
        this.channelDependencies = channelDependencies;
    }

    private LoginServer initLoginServer(int port) {
        LoginServer loginServer = new LoginServer(port);
        loginServer.start();
        return loginServer;
    }

    private void initializeTimelyTasks() {
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), MINUTES.toMillis(5));//Purging ftw...
        disconnectIdlesOnLoginTask();

        long timeLeft = getTimeLeftForNextHour();
        tMan.register(new CharacterDiseaseTask(), GameConfig.getServerLong("update_interval"), GameConfig.getServerLong("update_interval"));
        tMan.register(new CouponTask(), HOURS.toMillis(1), timeLeft);
        tMan.register(new RankingCommandTask(), MINUTES.toMillis(5), MINUTES.toMillis(5));
        tMan.register(new RankingLoginTask(), HOURS.toMillis(1), timeLeft);
        tMan.register(new LoginCoordinatorTask(), HOURS.toMillis(1), timeLeft);
        tMan.register(new EventRecallCoordinatorTask(), HOURS.toMillis(1), timeLeft);
        tMan.register(new LoginStorageTask(), MINUTES.toMillis(2), MINUTES.toMillis(2));
        tMan.register(new DueyFredrickTask(channelDependencies.fredrickProcessor()), HOURS.toMillis(1), timeLeft);
        tMan.register(new InvitationTask(), SECONDS.toMillis(30), SECONDS.toMillis(30));
        tMan.register(new RespawnTask(), GameConfig.getServerLong("respawn_interval"), GameConfig.getServerLong("respawn_interval"));
        tMan.register(new OnlineTimeTask(), 5000, 5000);

        timeLeft = getTimeLeftForNextDay();
        ExpeditionBossLog.resetBossLogTable();
        tMan.register(new BossLogTask(), DAYS.toMillis(1), timeLeft);
        tMan.register(new ExtendValueTask(), DAYS.toMillis(1), timeLeft);
    }

    public Alliance getAlliance(int id) {
        synchronized (alliances) {
            if (alliances.containsKey(id)) {
                return alliances.get(id);
            }
            return null;
        }
    }

    public void addAlliance(int id, Alliance alliance) {
        synchronized (alliances) {
            if (!alliances.containsKey(id)) {
                alliances.put(id, alliance);
            }
        }
    }

    public void disbandAlliance(int id) {
        synchronized (alliances) {
            Alliance alliance = alliances.get(id);
            if (alliance != null) {
                for (Integer gid : alliance.getGuilds()) {
                    guilds.get(gid).setAllianceId(0);
                }
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, Packet packet, int exception, int guildex) {
        Alliance alliance = alliances.get(id);
        if (alliance != null) {
            for (Integer gid : alliance.getGuilds()) {
                if (guildex == gid) {
                    continue;
                }
                Guild guild = guilds.get(gid);
                if (guild != null) {
                    guild.broadcast(packet, exception);
                }
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) {
        Alliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.addGuild(guildId);
            guilds.get(guildId).setAllianceId(aId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId) {
        Alliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.removeGuild(guildId);
            guilds.get(guildId).setAllianceId(0);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) {
        Alliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) {
        Alliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) {
        Alliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }

    public int createGuild(int leaderId, String name) {
        return Guild.createGuild(leaderId, name);
    }

    public Guild getGuildByName(String name) {
        synchronized (guilds) {
            for (Guild mg : guilds.values()) {
                if (mg.getName().equalsIgnoreCase(name)) {
                    return mg;
                }
            }

            return null;
        }
    }

    public Guild getGuild(int id) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }

            return null;
        }
    }

    public Guild getGuild(int id, int world) {
        return getGuild(id, world, null);
    }

    public Guild getGuild(int id, int world, Character mc) {
        synchronized (guilds) {
            Guild g = guilds.get(id);
            if (g != null) {
                return g;
            }

            g = new Guild(id, world);
            if (g.getId() == -1) {
                return null;
            }

            if (mc != null) {
                GuildCharacter mgc = g.getMGC(mc.getId());
                if (mgc != null) {
                    mc.setMGC(mgc);
                    mgc.setCharacter(mc);
                } else {
                    log.error("Could not find chr {} when loading guild {}", mc.getName(), id);
                }

                g.setOnline(mc.getId(), true, mc.getClient().getChannel());
            }

            guilds.put(id, g);
            return g;
        }
    }

    public void setGuildMemberOnline(Character mc, boolean bOnline, int channel) {
        Guild g = getGuild(mc.getGuildId(), mc.getWorld(), mc);
        g.setOnline(mc.getId(), bOnline, channel);
    }

    public int addGuildMember(GuildCharacter mgc, Character chr) {
        Guild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            return g.addGuildMember(mgc, chr);
        }
        return 0;
    }

    public boolean setGuildAllianceId(int gId, int aId) {
        Guild guild = guilds.get(gId);
        if (guild != null) {
            guild.setAllianceId(aId);
            return true;
        }
        return false;
    }

    public void resetAllianceGuildPlayersRank(int gId) {
        guilds.get(gId).resetAllianceGuildPlayersRank();
    }

    public void leaveGuild(GuildCharacter mgc) {
        Guild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.leaveGuild(mgc);
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.guildChat(name, cid, msg);
        }
    }

    public void changeRank(int gid, int cid, int newRank) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.changeRank(cid, newRank);
        }
    }

    public void expelMember(GuildCharacter initiator, String name, int cid) {
        Guild g = guilds.get(initiator.getGuildId());
        if (g != null) {
            g.expelMember(initiator, name, cid, channelDependencies.noteService());
        }
    }

    public void setGuildNotice(int gid, String notice) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.setGuildNotice(notice);
        }
    }

    public void memberLevelJobUpdate(GuildCharacter mgc) {
        Guild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.memberLevelJobUpdate(mgc);
        }
    }

    public void changeRankTitle(int gid, String[] ranks) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int gid) {
        synchronized (guilds) {
            Guild g = guilds.get(gid);
            g.disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) {
        Guild g = guilds.get(gid);
        if (g != null) {
            return g.increaseCapacity();
        }
        return false;
    }

    public void gainGP(int gid, int amount) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.gainGP(amount);
        }
    }

    public void guildMessage(int gid, Packet packet) {
        guildMessage(gid, packet, -1);
    }

    public void guildMessage(int gid, Packet packet, int exception) {
        Guild g = guilds.get(gid);
        if (g != null) {
            g.broadcast(packet, exception);
        }
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void deleteGuildCharacter(Character mc) {
        setGuildMemberOnline(mc, false, (byte) -1);
        if (mc.getMGC().getGuildRank() > 1) {
            leaveGuild(mc.getMGC());
        } else {
            disbandGuild(mc.getMGC().getGuildId());
        }
    }

    public void deleteGuildCharacter(GuildCharacter mgc) {
        if (mgc.getCharacter() != null) {
            setGuildMemberOnline(mgc.getCharacter(), false, (byte) -1);
        }
        if (mgc.getGuildRank() > 1) {
            leaveGuild(mgc);
        } else {
            disbandGuild(mgc.getGuildId());
        }
    }

    public void reloadGuildCharacters(int world) {
        World worlda = getWorld(world);
        for (Character mc : worlda.getPlayerStorage().getAllCharacters()) {
            if (mc.getGuildId() > 0) {
                setGuildMemberOnline(mc, true, worlda.getId());
                memberLevelJobUpdate(mc.getMGC());
            }
        }
        worlda.reloadGuildSummary();
    }

    public void broadcastMessage(int world, Packet packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastPacket(packet);
        }
    }

    public void broadcastGMMessage(int world, Packet packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastGMPacket(packet);
        }
    }

    public boolean isGmOnline(int world) {
        for (Channel ch : getChannelsFromWorld(world)) {
            for (Character player : ch.getPlayerStorage().getAllCharacters()) {
                if (player.isGM()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void changeFly(Integer accountid, boolean canFly) {
        if (canFly) {
            activeFly.add(accountid);
        } else {
            activeFly.remove(accountid);
        }
    }

    public boolean canFly(Integer accountid) {
        return activeFly.contains(accountid);
    }

    public int getCharacterWorld(Integer chrid) {
        lgnRLock.lock();
        try {
            Integer worldid = worldChars.get(chrid);
            return worldid != null ? worldid : -1;
        } finally {
            lgnRLock.unlock();
        }
    }

    public boolean haveCharacterEntry(Integer accountid, Integer chrid) {
        lgnRLock.lock();
        try {
            Set<Integer> accChars = accountChars.get(accountid);
            return accChars.contains(chrid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountCharacterCount(Integer accountid) {
        lgnRLock.lock();
        try {
            return accountCharacterCount.get(accountid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountWorldCharacterCount(Integer accountid, Integer worldid) {
        lgnRLock.lock();
        try {
            short count = 0;

            for (Integer chr : accountChars.get(accountid)) {
                if (worldChars.get(chr).equals(worldid)) {
                    count++;
                }
            }

            return count;
        } finally {
            lgnRLock.unlock();
        }
    }

    private Set<Integer> getAccountCharacterEntries(Integer accountid) {
        lgnRLock.lock();
        try {
            return new HashSet<>(accountChars.get(accountid));
        } finally {
            lgnRLock.unlock();
        }
    }

    public void updateCharacterEntry(Character chr) {
        Character chrView = chr.generateCharacterEntry();

        lgnWLock.lock();
        try {
            World wserv = this.getWorld(chrView.getWorld());
            if (wserv != null) {
                wserv.registerAccountCharacterView(chrView.getAccountId(), chrView);
            }
        } finally {
            lgnWLock.unlock();
        }
    }

    public void createCharacterEntry(Character chr) {
        Integer accountid = chr.getAccountId(), chrid = chr.getId(), world = chr.getWorld();

        lgnWLock.lock();
        try {
            accountCharacterCount.put(accountid, (short) (accountCharacterCount.get(accountid) + 1));

            Set<Integer> accChars = accountChars.get(accountid);
            accChars.add(chrid);

            worldChars.put(chrid, world);

            Character chrView = chr.generateCharacterEntry();

            World wserv = this.getWorld(chrView.getWorld());
            if (wserv != null) {
                wserv.registerAccountCharacterView(chrView.getAccountId(), chrView);
            }
        } finally {
            lgnWLock.unlock();
        }
    }

    public void deleteCharacterEntry(Integer accountid, Integer chrid) {
        lgnWLock.lock();
        try {
            accountCharacterCount.put(accountid, (short) (accountCharacterCount.get(accountid) - 1));

            Set<Integer> accChars = accountChars.get(accountid);
            accChars.remove(chrid);

            Integer world = worldChars.remove(chrid);
            if (world != null) {
                World wserv = this.getWorld(world);
                if (wserv != null) {
                    wserv.unregisterAccountCharacterView(accountid, chrid);
                }
            }
        } finally {
            lgnWLock.unlock();
        }
    }

    public void transferWorldCharacterEntry(Character chr, Integer toWorld) { // used before setting the new worldid on the character object
        lgnWLock.lock();
        try {
            Integer chrid = chr.getId(), accountid = chr.getAccountId(), world = worldChars.get(chr.getId());
            if (world != null) {
                World wserv = this.getWorld(world);
                if (wserv != null) {
                    wserv.unregisterAccountCharacterView(accountid, chrid);
                }
            }

            worldChars.put(chrid, toWorld);

            Character chrView = chr.generateCharacterEntry();

            World wserv = this.getWorld(toWorld);
            if (wserv != null) {
                wserv.registerAccountCharacterView(chrView.getAccountId(), chrView);
            }
        } finally {
            lgnWLock.unlock();
        }
    }
    
    /*
    public void deleteAccountEntry(Integer accountid) { is this even a thing?
        lgnWLock.lock();
        try {
            accountCharacterCount.remove(accountid);
            accountChars.remove(accountid);
        } finally {
            lgnWLock.unlock();
        }
    
        for (World wserv : this.getWorlds()) {
            wserv.clearAccountCharacterView(accountid);
            wserv.unregisterAccountStorage(accountid);
        }
    }
    */

    public SortedMap<Integer, List<Character>> loadAccountCharlist(int accountId, int visibleWorlds) {
        List<World> worlds = this.getWorlds();
        if (worlds.size() > visibleWorlds) {
            worlds = worlds.subList(0, visibleWorlds);
        }

        SortedMap<Integer, List<Character>> worldChrs = new TreeMap<>();
        int chrTotal = 0;

        lgnRLock.lock();
        try {
            for (World world : worlds) {
                List<Character> chrs = world.getAccountCharactersView(accountId);
                if (chrs == null) {
                    if (!accountChars.containsKey(accountId)) {
                        accountCharacterCount.put(accountId, (short) 0);
                        accountChars.put(accountId, new HashSet<>());    // not advisable at all to write on the map on a read-protected environment
                    }                                                           // yet it's known there's no problem since no other point in the source does
                } else if (!chrs.isEmpty()) {                                  // this action.
                    worldChrs.put(world.getId(), chrs);
                }
            }
        } finally {
            lgnRLock.unlock();
        }

        return worldChrs;
    }

    private static Pair<Short, List<List<Character>>> loadAccountCharactersViewFromDb(int accId, int wlen) {
        short characterCount = 0;
        List<List<Character>> wchars = new ArrayList<>(wlen);
        for (int i = 0; i < wlen; i++) {
            wchars.add(i, new LinkedList<>());
        }

        List<Character> chars = new LinkedList<>();
        int curWorld = 0;
        try {
            List<Pair<Item, Integer>> accEquips = ItemFactory.loadEquippedItems(accId, true, true);
            Map<Integer, List<Item>> accPlayerEquips = new HashMap<>();

            for (Pair<Item, Integer> ae : accEquips) {
                List<Item> playerEquips = accPlayerEquips.get(ae.getRight());
                if (playerEquips == null) {
                    playerEquips = new LinkedList<>();
                    accPlayerEquips.put(ae.getRight(), playerEquips);
                }

                playerEquips.add(ae.getLeft());
            }


            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY world, id")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        characterCount++;

                        int cworld = rs.getByte("world");
                        if (cworld >= wlen) {
                            continue;
                        }

                        if (cworld > curWorld) {
                            wchars.add(curWorld, chars);

                            curWorld = cworld;
                            chars = new LinkedList<>();
                        }

                        Integer cid = rs.getInt("id");
                        chars.add(Character.loadCharacterEntryFromDB(rs, accPlayerEquips.get(cid)));
                    }
                }
            }

            wchars.add(curWorld, chars);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return new Pair<>(characterCount, wchars);
    }

    public void loadAllAccountsCharactersView() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM accounts");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int accountId = rs.getInt("id");
                if (isFirstAccountLogin(accountId)) {
                    loadAccountCharactersView(accountId, 0, 0);
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private boolean isFirstAccountLogin(Integer accId) {
        lgnRLock.lock();
        try {
            return !accountChars.containsKey(accId);
        } finally {
            lgnRLock.unlock();
        }
    }

    public void loadAccountCharacters(Client c) {
        Integer accId = c.getAccID();
        if (!isFirstAccountLogin(accId)) {
            Set<Integer> accWorlds = new HashSet<>();

            lgnRLock.lock();
            try {
                for (Integer chrid : getAccountCharacterEntries(accId)) {
                    accWorlds.add(worldChars.get(chrid));
                }
            } finally {
                lgnRLock.unlock();
            }

            int gmLevel = 0;
            for (Integer aw : accWorlds) {
                World wserv = this.getWorld(aw);

                if (wserv != null) {
                    for (Character chr : wserv.getAllCharactersView()) {
                        if (gmLevel < chr.gmLevel()) {
                            gmLevel = chr.gmLevel();
                        }
                    }
                }
            }

            c.setGMLevel(gmLevel);
            return;
        }

        int gmLevel = loadAccountCharactersView(c.getAccID(), 0, 0);
        c.setGMLevel(gmLevel);
    }

    private int loadAccountCharactersView(Integer accId, int gmLevel, int fromWorldid) {    // returns the maximum gmLevel found
        List<World> wlist = this.getWorlds();
        Pair<Short, List<List<Character>>> accCharacters = loadAccountCharactersViewFromDb(accId, wlist.size());

        lgnWLock.lock();
        try {
            List<List<Character>> accChars = accCharacters.getRight();
            accountCharacterCount.put(accId, accCharacters.getLeft());

            Set<Integer> chars = accountChars.get(accId);
            if (chars == null) {
                chars = new HashSet<>(5);
            }

            for (int wid = fromWorldid; wid < wlist.size(); wid++) {
                World w = wlist.get(wid);
                List<Character> wchars = accChars.get(wid);
                w.loadAccountCharactersView(accId, wchars);

                for (Character chr : wchars) {
                    int cid = chr.getId();
                    if (gmLevel < chr.gmLevel()) {
                        gmLevel = chr.gmLevel();
                    }

                    chars.add(cid);
                    worldChars.put(cid, wid);
                }
            }

            accountChars.put(accId, chars);
        } finally {
            lgnWLock.unlock();
        }

        return gmLevel;
    }

    public void loadAccountStorages(Client c) {
        int accountId = c.getAccID();
        Set<Integer> accWorlds = new HashSet<>();
        lgnWLock.lock();
        try {
            Set<Integer> chars = accountChars.get(accountId);

            for (Integer cid : chars) {
                Integer worldid = worldChars.get(cid);
                if (worldid != null) {
                    accWorlds.add(worldid);
                }
            }
        } finally {
            lgnWLock.unlock();
        }

        List<World> worldList = this.getWorlds();
        for (Integer worldid : accWorlds) {
            if (worldid < worldList.size()) {
                World wserv = worldList.get(worldid);
                wserv.loadAccountStorage(accountId);
            }
        }
    }

    private static String getRemoteHost(Client client) {
        return SessionCoordinator.getSessionRemoteHost(client);
    }

    public void setCharacteridInTransition(Client client, int charId) {
        String remoteIp = getRemoteHost(client);

        lgnWLock.lock();
        try {
            transitioningChars.put(remoteIp, charId);
        } finally {
            lgnWLock.unlock();
        }
    }

    public boolean validateCharacteridInTransition(Client client, int charId) {
        if (!GameConfig.getServerBoolean("use_ip_validation")) {
            return true;
        }

        String remoteIp = getRemoteHost(client);

        lgnWLock.lock();
        try {
            Integer cid = transitioningChars.remove(remoteIp);
            return cid != null && cid.equals(charId);
        } finally {
            lgnWLock.unlock();
        }
    }

    public Integer freeCharacteridInTransition(Client client) {
        if (!GameConfig.getServerBoolean("use_ip_validation")) {
            return null;
        }

        String remoteIp = getRemoteHost(client);

        lgnWLock.lock();
        try {
            return transitioningChars.remove(remoteIp);
        } finally {
            lgnWLock.unlock();
        }
    }

    public boolean hasCharacteridInTransition(Client client) {
        if (!GameConfig.getServerBoolean("use_ip_validation")) {
            return true;
        }

        String remoteIp = getRemoteHost(client);

        lgnRLock.lock();
        try {
            return transitioningChars.containsKey(remoteIp);
        } finally {
            lgnRLock.unlock();
        }
    }

    public void registerLoginState(Client c) {
        srvLock.lock();
        try {
            inLoginState.put(c, System.currentTimeMillis() + 600000);
        } finally {
            srvLock.unlock();
        }
    }

    public void unregisterLoginState(Client c) {
        srvLock.lock();
        try {
            inLoginState.remove(c);
        } finally {
            srvLock.unlock();
        }
    }

    private void disconnectIdlesOnLoginState() {
        List<Client> toDisconnect = new LinkedList<>();

        srvLock.lock();
        try {
            long timeNow = System.currentTimeMillis();

            for (Entry<Client, Long> mc : inLoginState.entrySet()) {
                if (timeNow > mc.getValue()) {
                    toDisconnect.add(mc.getKey());
                }
            }

            for (Client c : toDisconnect) {
                inLoginState.remove(c);
            }
        } finally {
            srvLock.unlock();
        }

        for (Client c : toDisconnect) {    // thanks Lei for pointing a deadlock issue with srvLock
            if (c.isLoggedIn()) {
                c.disconnect(false, false);
            } else {
                SessionCoordinator.getInstance().closeSession(c, true);
            }
        }
    }

    private void disconnectIdlesOnLoginTask() {
        TimerManager.getInstance().register(this::disconnectIdlesOnLoginState, 300000);
    }

    public final Runnable shutdown(final boolean restart) {//no player should be online when trying to shutdown!
        return () -> shutdownInternal(restart);
    }

    public synchronized void shutdownInternal(boolean restart) {
        log.info(I18nUtil.getLogMessage("Server.shutdownInternal.info1"), restart ?
                I18nUtil.getLogMessage("Server.shutdownInternal.info2") : I18nUtil.getLogMessage("Server.shutdownInternal.info3"));
        if (getWorlds() == null) {
            return;//already shutdown
        }
        for (World w : getWorlds()) {
            w.shutdown();
        }

        hpMpAlertService.saveAll();
        hpMpAlertService.clear();

        for (Channel ch : getAllChannels()) {
            while (!ch.finishedShutdown()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    log.error(I18nUtil.getLogMessage("Server.shutdownInternal.error1"), ie);
                }
            }
        }

        resetServerWorlds();

        ThreadManager.getInstance().stop();
        TimerManager.getInstance().purge();
        TimerManager.getInstance().stop();
        loginServer.stop();
        online = false;
        log.info(I18nUtil.getLogMessage("Server.shutdownInternal.info4"));
        if (restart) {
            log.info(I18nUtil.getLogMessage("Server.shutdownInternal.info5"));
            instance = null;
            getInstance().init();
        }
    }

    public boolean isNextTime() {
        if (nextTime == 0) {
            Random random = new Random();
            int base = 1;
            int ran = random.nextInt(4);
            nextTime = System.currentTimeMillis() + 86400000 * (base + ran);
            return false;
        }
        if (nextTime > System.currentTimeMillis()) {
            return false;
        }
        Random random = new Random();
        int base = 1;
        int ran = random.nextInt(4);
        nextTime = System.currentTimeMillis() + 86400000 * (base + ran);
        return true;
    }

    public synchronized void shutdownWithMsgAndInternal(ServerShutdownDTO serverShutdownDTO) {

        int time = 60000;
        // 原来就支持立即停止，不能忽视本地用户
        if (serverShutdownDTO.getMinutes() >= 0) {
            time *= serverShutdownDTO.getMinutes();
        }

        if (time > 1) {
            int seconds = (time / (int) SECONDS.toMillis(1)) % 60;
            int minutes = (time / (int) MINUTES.toMillis(1)) % 60;
            int hours = (time / (int) HOURS.toMillis(1)) % 24;
            int days = (time / (int) DAYS.toMillis(1));

            String strTime = "";
            if (days > 0) {
                strTime += I18nUtil.getMessage("ShutdownCommand.message3", days);
            }
            if (hours > 0) {
                strTime += I18nUtil.getMessage("ShutdownCommand.message4", hours);
            }
            strTime += I18nUtil.getMessage("ShutdownCommand.message5", minutes);
            strTime += I18nUtil.getMessage("ShutdownCommand.message6", seconds);


            String shutDownMsg = I18nUtil.getMessage("ShutdownCommand.message7", strTime);

            if (serverShutdownDTO.getShutdownMsg() != null) {
                shutDownMsg = serverShutdownDTO.getShutdownMsg();
            }

            for (World w : Server.getInstance().getWorlds()) {
                for (Character chr : w.getPlayerStorage().getAllCharacters()) {
                    if (serverShutdownDTO.getShowCenterMsg()) {
                        // 屏幕中央提示消息 (火红玫瑰)
                        chr.startMapEffect(shutDownMsg, 5121009);
                    }
                }
                if (serverShutdownDTO.getShowServerMsg()) {
                    // 添加滚动消息到顶部，因为是固定时间停服，所以短暂的通知部分玩家可能看不到。
                    w.setServerMessage(shutDownMsg);
                }
                if (serverShutdownDTO.getShowChatMsg()) {
                    // 玩家聊天框蓝色GM消息
                    w.broadcastPacket(PacketCreator.serverNotice(6, shutDownMsg));
                }

            }
        }
        TimerManager.getInstance().schedule(Server.getInstance().shutdown(false), time);
    }


}
