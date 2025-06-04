/**
 * @version 0.0.2
 */
declare global {
    const cm: NPCConversationManager;
    const rm: ReactorActionManager;
    const qm: QuestActionManager;
    const im: ItemScriptManager;
    const em: EventManager;

    /**
     * `ms` 是回调时的形参 定义为全局变量，类型会被覆写为any
     * 
     * 可以使用 `jsdoc` 手动指定类型
     * 
     * 在 `start` 方法上注释： `@param {MapScriptMethods} ms`
     * 
     * 在 传参时或者方法内部上注释： `@type {MapScriptMethods}`
     */
    const _ms: MapScriptMethods;
    /**
     * `pi` 是回调时的形参 定义为全局变量，类型会被覆写为any
     * 
     * 可以使用 `jsdoc` 手动指定类型
     * 
     * 在 `enter` 方法上注释： `@param {PortalPlayerInteraction} pi`
     * 
     * 在 传参时或者方法内部上注释： `@type {PortalPlayerInteraction}`
     */
    const _pi: PortalPlayerInteraction;
    /**
     * `eim` 是回调时的形参 定义为全局变量，类型会被覆写为any
     * 
     * 可以使用 `jsdoc` 手动指定类型
     * 
     * 在 事件 `event` 类型的函数 方法上注释： `@param {EventInstanceManager} eim`
     * 
     * 在 传参时或者方法内部上注释： `@type {EventInstanceManager}`
     */
    const _eim: EventInstanceManager;

    /*
    *  对于 Java.type 出来的变量 可以在上面 使用类型断言
    *     \@type {...}
    * */
    const Java: {
        type(clazz: string): any
        from(javaArray: any): []
        to(jsArray: [], javaType: string): any

        // isJavaObject(): void
        // isType(): void
        // typeName(): void
        // addToClasspath(): void
        // extend(): void
        // super(): void
    }
    // const java: Object;
    // const javafx: Object;
    // const javax: Object;
    // const com: Object;
    // const org: Object;
    // const edu: Object;
    // const arguments: Object;
    // const engine: Object;
    // const context: Object;
    // const Graal: Object;


    //=================================================================
    //     JDK internal
    //=================================================================
    interface JavaMap { }
    interface HashMap { }
    interface Enum { }
    interface Class { }
    interface Optional { }
    interface Consumer { }
    interface JavaIterator { }
    interface Spliterator { }
    interface SortedMap { }
    interface Properties { }
    interface Point {
        x: number  // int
        y: number  // int
        // ===================================
        clone(): Object
        distance(arg0: Point2D): number
        distance(arg0: number, arg1: number): number
        distance(arg0: number, arg1: number, arg2: number, arg3: number): number
        distanceSq(arg0: Point2D): number
        distanceSq(arg0: number, arg1: number): number
        distanceSq(arg0: number, arg1: number, arg2: number, arg3: number): number
        getLocation(): Point
        getX(): number
        getY(): number
        move(arg0: number, arg1: number): void
        setLocation(arg0: Point): void
        setLocation(arg0: Point2D): void
        setLocation(arg0: number, arg1: number): void
        translate(arg0: number, arg1: number): void
    }
    interface Point2D {
        clone(): Object
        distance(arg0: Point2D): number
        distance(arg0: number, arg1: number): number
        distance(arg0: number, arg1: number, arg2: number, arg3: number): number
        distanceSq(arg0: Point2D): number
        distanceSq(arg0: number, arg1: number): number
        distanceSq(arg0: number, arg1: number, arg2: number, arg3: number): number
        getX(): number
        getY(): number
        setLocation(arg0: Point2D): void
        setLocation(arg0: number, arg1: number): void
    }

    //=================================================================
    //     most used and powerful class
    //=================================================================
    interface Channel {
        //============ Properties =============
        //============ Functions  =============
        acceptOngoingWedding(cathedral: boolean): boolean
        addExpedition(exped: Expedition): boolean
        addHiredMerchant(chrid: number, hm: HiredMerchant): void
        addMiniDungeon(dungeonid: number): boolean
        addPlayer(chr: Character): void
        broadcastGMPacket(packet: Packet): void
        broadcastPacket(packet: Packet): void
        canInitMonsterCarnival(cpq1: boolean, field: number): boolean
        canUninstall(): boolean
        closeOngoingWedding(cathedral: boolean): void
        debugMarriageStatus(): void
        dismissDojoSchedule(dojoMapId: number, party: Party): void
        dropMessage(type: number, message: string): void
        finishMonsterCarnival(cpq1: boolean, field: number): void
        finishedShutdown(): boolean
        freeDojoSectionIfEmpty(dojoMapId: number): void
        getChannelCapacity(): number
        getDojoFinishTime(dojoMapId: number): number
        getEvent(): Event
        getEventSM(): EventScriptManager
        getExpedition(type: ExpeditionType): Expedition
        getExpeditions(): []
        getHiredMerchants(): JavaMap
        getIP(): string
        getId(): number
        getMapFactory(): MapManager
        getMiniDungeon(dungeonid: number): MiniDungeon
        getNextWeddingReservation(cathedral: boolean): Pair
        getOngoingWedding(cathedral: boolean): number
        getOngoingWeddingType(cathedral: boolean): boolean
        getPartyMembers(party: Party): []
        getPlayerStorage(): PlayerStorage
        getRelativeWeddingTicketExpireTime(resSlot: number): number
        getServerMessage(): string
        getServiceAccess(sv: ChannelServices): BaseService
        getStoredVar(key: number): number
        getWeddingCoupleForGuest(guestId: number, cathedral: boolean): Pair
        getWeddingReservationStatus(weddingId: number, cathedral: boolean): number
        getWeddingReservationTimeLeft(weddingId: number): string
        getWeddingTicketExpireTime(resSlot: number): number
        getWorld(): number
        getWorldServer(): World
        ingressDojo(isPartyDojo: boolean, fromStage: number): number
        ingressDojo(isPartyDojo: boolean, party: Party, fromStage: number): number
        initMonsterCarnival(cpq1: boolean, field: number): void
        insertPlayerAway(chrId: number): void
        isActive(): boolean
        isConnected(name: string): boolean
        isOngoingWeddingGuest(cathedral: boolean, playerId: number): boolean
        isWeddingReserved(weddingId: number): boolean
        lookupPartyDojo(party: Party): number
        multiBuddyFind(charIdFrom: number, characterIds: number[]): number[]
        pushWeddingReservation(weddingId: number, cathedral: boolean, premium: boolean, groomId: number, brideId: number): number
        registerOwnedMap(map: MapleMap): void
        reloadEventScriptManager(): void
        removeExpedition(exped: Expedition): void
        removeHiredMerchant(chrid: number): void
        removeMiniDungeon(dungeonid: number): void
        removePlayer(chr: Character): boolean
        removePlayerAway(chrId: number): void
        resetDojo(dojoMapId: number): void
        resetDojoMap(fromMapId: number): void
        runCheckOwnedMapsSchedule(): void
        setDojoProgress(dojoMapId: number): boolean
        setEvent(event: Event): void
        setOngoingWedding(cathedral: boolean, premium: boolean, weddingId: number, guests: []): void
        setServerMessage(message: string): void
        setStoredVar(key: number, val: number): void
        shutdown(): void
        unregisterOwnedMap(map: MapleMap): void
    }
    interface Character {
        //============ Properties =============
        IDLE_MOVEMENT_PACKET_LENGTH: number
        ariantColiseum: AriantColiseum
        //============ Functions  =============
        UpdateCurrentOnlineTimeFromDB(): void
        addCooldown(skillId: number, startTime: number, length: number): void
        addDojoPointsByMap(mapId: number): number
        addExcluded(petId: number, x: number): void
        addGachaExp(gain: number): void
        addHP(delta: number): void
        addJailExpirationTime(time: number): void
        addMP(delta: number): void
        addMPHP(hpDelta: number, mpDelta: number): void
        addMaxHP(delta: number): void
        addMaxMP(delta: number): void
        addMerchantMesos(add: number): void
        addMesosTraded(gain: number): void
        addNewYearRecord(newyear: NewYearCardRecord): void
        addOnlineTime(amount: number): void
        addPet(pet: Pet): void
        addPlayerRing(ring: Ring): void
        addReborns(): void
        addSummon(id: number, summon: Summon): void
        addTrockMap(): void
        addVipTrockMap(): void
        addVisibleMapObject(mo: MapObject): void
        announceBattleshipHp(): void
        announceDiseases(): void
        announceUpdateQuest(questUpdateType: DelayedQuestUpdate, params: Object[]): void
        applyConsumeOnPickup(itemId: number): boolean
        applyHpMpChange(hpCon: number, hpchange: number, mpchange: number): boolean
        applyPartyDoor(door: Door, partyUpdate: boolean): void
        assignDex(x: number): boolean
        assignHP(deltaHP: number, deltaAp: number): boolean
        assignInt(x: number): boolean
        assignLuk(x: number): boolean
        assignMP(deltaMP: number, deltaAp: number): boolean
        assignStr(x: number): boolean
        assignStrDexIntLuk(deltaStr: number, deltaDex: number, deltaInt: number, deltaLuk: number): boolean
        attemptCatchFish(baitLevel: number): boolean
        autoBan(reason: string): void
        awardQuestPoint(awardedPoints: number): void
        ban(reason: string): void
        ban(id: string, reason: string, accountId: boolean): boolean
        block(reason: number, days: number, desc: string): void
        blockPortal(scriptName: string): void
        broadcastAcquaintances(packet: Packet): void
        broadcastAcquaintances(type: number, message: string): void
        broadcastMarriageMessage(): void
        broadcastStance(): void
        broadcastStance(newStance: number): void
        buffExpireTask(): void
        calculateMaxBaseDamage(watk: number): number
        calculateMaxBaseDamage(watk: number, weapon: WeaponType): number
        calculateMaxBaseMagicDamage(matk: number): number
        canCreateChar(name: string): boolean
        canDoor(): boolean
        canGainSlots(type: number, slots: number): boolean
        canHold(itemid: number): boolean
        canHold(itemid: number, quantity: number): boolean
        canHoldMeso(gain: number): boolean
        canHoldUniques(itemids: []): boolean
        canRecoverLastBanish(): boolean
        cancelAllBuffs(softcancel: boolean): void
        cancelAllDebuffs(): void
        cancelBuffExpireTask(): void
        cancelBuffStats(stat: BuffStat): void
        cancelDiseaseExpireTask(): void
        cancelEffect(itemId: number): void
        cancelEffect(effect: StatEffect, overwrite: boolean, startTime: number): boolean
        cancelEffectFromBuffStat(stat: BuffStat): void
        cancelExpirationTask(): void
        cancelFamilyBuffTimer(): void
        cancelMagicDoor(): void
        cancelPendingNameChange(): boolean
        cancelPendingWorldTransfer(): boolean
        cancelQuestExpirationTask(): void
        cancelSkillCooldownTask(): void
        cannotEnterCashShop(): boolean
        changeCI(type: number): void
        changeFaceExpression(emote: number): void
        changeHpMp(newhp: number, newmp: number, silent: boolean): void
        changeJob(newJob: Job): void
        changeKeybinding(key: number, keybinding: KeyBinding): void
        changeMap(to: MapleMap): void
        changeMap(map: number): void
        changeMap(to: MapleMap, portal: number): void
        changeMap(target: MapleMap, pto: Portal): void
        changeMap(target: MapleMap, pos: Point): void
        changeMap(map: number, pt: Object): void
        changeMapBanish(mapid: number, portal: string, msg: string): void
        changePage(page: number): void
        changeQuickslotKeybinding(aQuickslotKeyMapped: number[]): void
        changeRemainingAp(x: number, silent: boolean): void
        changeSkillLevel(skill: Skill, newLevel: number, newMasterlevel: number, expiration: number): void
        changeTab(tab: number): void
        changeType(type: number): void
        checkBerserk(isHidden: boolean): void
        checkMessenger(): void
        checkWorldTransferEligibility(): number
        clearBanishPlayerData(): void
        clearCpqTimer(): void
        clearSavedLocation(type: SavedLocationType): void
        clearSummons(): void
        closeHiredMerchant(closeMerchant: boolean): void
        closeMiniGame(forceClose: boolean): void
        closeNpcShop(): void
        closePartySearchInteractions(): void
        closePlayerInteractions(): void
        closePlayerMessenger(): void
        closePlayerShop(): void
        closeRPS(): void
        closeTrade(): void
        collectDiseases(): void
        commitExcludedItems(): void
        containsAreaInfo(area: number, info: string): boolean
        containsSummon(summon: Summon): boolean
        controlMonster(monster: Monster): void
        countItem(itemid: number): number
        createDragon(): void
        debugListAllBuffs(): void
        decreaseBattleshipHp(decrease: number): void
        decreaseReports(): void
        deleteBuddy(otherCid: number): void
        deleteCharFromDB(player: Character, senderAccId: number): boolean
        deleteFromTrocks(map: number): void
        deleteFromVipTrocks(map: number): void
        deleteGuild(guildId: number): void
        disbandGuild(): void
        diseaseExpireTask(): void
        dispel(): void
        dispelBuffCoupons(): void
        dispelDebuff(debuff: Disease): void
        dispelDebuffs(): void
        dispelSkill(skillid: number): void
        doHurtHp(): void
        doPendingNameChange(): void
        dropMessage(message: string): void
        dropMessage(type: number, message: string): void
        empty(remove: boolean): void
        enteredScript(script: string, mapid: number): void
        equipChanged(): void
        equippedItem(equip: Equip): void
        executeRebornAs(job: Job): void
        executeRebornAsId(jobId: number): void
        existName(name: string): boolean
        expirationTask(): void
        exportExcludedItems(c: Client): void
        fetchDoorSlot(): number
        finishDojoTutorial(): void
        flushDelayedUpdateQuests(): void
        forceChangeMap(target: MapleMap, pto: Portal): void
        forceUpdateItem(item: Item): void
        forfeitExpirableQuests(): void
        fromCharactersDO(charactersDO: CharactersDO, client: Client): Character
        gainAp(deltaAp: number, silent: boolean): void
        gainAriantPoints(points: number): void
        gainCP(gain: number): void
        gainEquip(itemId: number, attStr: number, attDex: number, attInt: number, attLuk: number, attHp: number, attMp: number, pAtk: number, mAtk: number, pDef: number, mDef: number, acc: number, avoid: number, hands: number, speed: number, jump: number, upgradeSlot: number, expireTime: number): void
        gainExp(gain: number): void
        gainExp(gain: number, show: boolean, inChat: boolean): void
        gainExp(gain: number, show: boolean, inChat: boolean, white: boolean): void
        gainExp(gain: number, party: number, show: boolean, inChat: boolean, white: boolean): void
        gainFame(delta: number): void
        gainFame(delta: number, fromPlayer: Character, mode: number): boolean
        gainFestivalPoints(gain: number): void
        gainGachaExp(): void
        gainMeso(gain: number): void
        gainMeso(gain: number, show: boolean): void
        gainMeso(gain: number, show: boolean, enableActions: boolean, inChat: boolean): void
        gainSlots(type: number, slots: number): boolean
        gainSlots(type: number, slots: number, update: boolean): boolean
        gainSp(deltaSp: number, skillbook: number, silent: boolean): void
        generateCharacterEntry(): Character
        genericGuildMessage(code: number): void
        getAbstractPlayerInteraction(): AbstractPlayerInteraction
        getAccountId(): number
        getAccountIdByName(name: string): number
        getActiveCoupons(): []
        getAllBuffs(): []
        getAllCooldowns(): []
        getAllDiseases(): JavaMap
        getAlliance(): Alliance
        getAllianceRank(): number
        getAreaInfos(): JavaMap
        getAriantColiseum(): AriantColiseum
        getAriantPoints(): number
        getAutoBanManager(): AutobanManager
        getBattleshipHp(): number
        getBlockedPortals(): []
        getBossDropRate(): number
        getBuddylist(): BuddyList
        getBuffEffect(stat: BuffStat): StatEffect
        getBuffSource(stat: BuffStat): number
        getBuffedStarttime(effect: BuffStat): number
        getBuffedValue(effect: BuffStat): number
        getCP(): number
        getCardRate(itemid: number): number
        getCashShop(): CashShop
        getChair(): number
        getChalkboard(): string
        getCi(): number
        getCleanItemQuantity(itemid: number, checkEquipped: boolean): number
        getClient(): Client
        getClientMaxHp(): number
        getClientMaxMp(): number
        getCombo(): number
        getCompletedQuests(): []
        getControlledMonsters(): []
        getCouponDropRate(): number
        getCouponExpRate(): number
        getCouponMesoRate(): number
        getCrushRings(): []
        getCurrentMaxHp(): number
        getCurrentMaxMp(): number
        getCurrentOnlieTime(): number
        getCurrentPage(): number
        getCurrentTab(): number
        getCurrentType(): number
        getDefault(c: Client): Character
        getDex(): number
        getDisabledPartySearchInvites(): []
        getDiseasesSize(): number
        getDojoEnergy(): number
        getDojoPoints(): number
        getDojoStage(): number
        getDoorSlot(): number
        getDoors(): []
        getDragon(): Dragon
        getDropRate(): number
        getEditableSkills(): JavaMap
        getEnergyBar(): number
        getEventInstance(): EventInstanceManager
        getEvents(): JavaMap
        getExcluded(): JavaMap
        getExcludedItems(): []
        getExp(): number
        getExpRate(): number
        getFace(): number
        getFame(): number
        getFamily(): Family
        getFamilyDrop(): number
        getFamilyEntry(): FamilyEntry
        getFamilyExp(): number
        getFamilyId(): number
        getFestivalPoints(): number
        getFh(): number
        getFitness(): Fitness
        getFriendshipRings(): []
        getGachaExp(): number
        getGender(): number
        getGuild(): Guild
        getGuildId(): number
        getGuildRank(): number
        getHair(): number
        getHiredMerchant(): HiredMerchant
        getHp(): number
        getHpMpApUsed(): number
        getId(): number
        getIdByName(name: string): number
        getIdleMovement(): InPacket
        getInitialSpawnPoint(): number
        getInt(): number
        getInventory(type: InventoryType): Inventory
        getItemEffect(): number
        getItemQuantity(itemid: number, checkEquipped: boolean): number
        getJailExpirationTimeLeft(): number
        getJob(): Job
        getJobRank(): number
        getJobRankMove(): number
        getJobStyle(): Job
        getJobStyle(opt: number): Job
        getJobType(): number
        getKeymap(): JavaMap
        getLastBanishData(): Pair
        getLastCombo(): number
        getLastCommandMessage(): string
        getLastSnowballAttack(): number
        getLastUsedCashItem(): number
        getLastVisitedMapIds(): []
        getLastfametime(): number
        getLastmonthfameids(): []
        getLevel(): number
        getLevelExpRate(): number
        getLinkedLevel(): number
        getLinkedName(): string
        getLoggedInTime(): number
        getLoginTime(): number
        getLuk(): number
        getMGC(): GuildCharacter
        getMPC(): PartyCharacter
        getMainTownDoor(): Door
        getMap(): MapleMap
        getMapId(): number
        getMapleMount(): Mount
        getMarriageInstance(): Marriage
        getMarriageItemId(): number
        getMarriageRing(): Ring
        getMasterLevel(skill: Skill): number
        getMasterLevel(skill: number): number
        getMatchcardlosses(): number
        getMatchcardties(): number
        getMatchcardwins(): number
        getMaxClassLevel(): number
        getMaxHp(): number
        getMaxLevel(): number
        getMaxMp(): number
        getMedalText(): string
        getMerchantMeso(): number
        getMerchantNetMeso(): number
        getMeso(): number
        getMesoRate(): number
        getMesosTraded(): number
        getMessenger(): Messenger
        getMessengerPosition(): number
        getMiniGame(): MiniGame
        getMiniGamePoints(type: MiniGameResult, omok: boolean): number
        getMobExpRate(): number
        getMonsterBook(): MonsterBook
        getMonsterBookCover(): number
        getMonsterCarnival(): MonsterCarnival
        getMonsterCarnivalParty(): MonsterCarnivalParty
        getMp(): number
        getName(): string
        getNameById(id: number): string
        getNewYearRecord(cardid: number): NewYearCardRecord
        getNewYearRecords(): []
        getNoPets(): number
        getNpcCooldown(): number
        getNumControlledMonsters(): number
        getObjectId(): number
        getOla(): Ola
        getOmoklosses(): number
        getOmokties(): number
        getOmokwins(): number
        getOwlSearch(): number
        getOwnedMap(): MapleMap
        getPartnerId(): number
        getParty(): Party
        getPartyId(): number
        getPartyMembersOnSameMap(): []
        getPartyMembersOnline(): []
        getPartyQuest(): PartyQuest
        getPet(index: number): Pet
        getPetIndex(pet: Pet): number
        getPetIndex(petId: number): number
        getPets(): Pet[]
        getPlayerDoor(): Door
        getPlayerShop(): PlayerShop
        getPosition(): Point
        getPossibleReports(): number
        getQuest(quest: number): QuestStatus
        getQuest(quest: Quest): QuestStatus
        getQuestExpRate(): number
        getQuestFame(): number
        getQuestMesoRate(): number
        getQuestNAdd(quest: Quest): QuestStatus
        getQuestNoAdd(quest: Quest): QuestStatus
        getQuestStatus(quest: number): number
        getQuests(): JavaMap
        getQuickLevelExpRate(): number
        getQuickSlotLoaded(): number[]
        getRank(): number
        getRankMove(): number
        getRawDropRate(): number
        getRawExpRate(): number
        getRawMesoRate(): number
        getReborns(): number
        getReceivedNewYearRecords(): []
        getRelationshipId(): number
        getRemainingAp(): number
        getRemainingSp(): number
        getRemainingSps(): number[]
        getRewardPoints(): number
        getRingById(id: number): Ring
        getRps(): RockPaperScissor
        getSavedLocation(type: string): number
        getSavedLocations(): SavedLocation[]
        getSearch(): string
        getShop(): Shop
        getSkillExpiration(skill: Skill): number
        getSkillExpiration(skill: number): number
        getSkillLevel(skill: Skill): number
        getSkillLevel(skill: number): number
        getSkillMacros(): SkillMacro[]
        getSkills(): JavaMap
        getSkinColor(): SkinColor
        getSlot(): number
        getSlots(type: number): number
        getStance(): number
        getStartedQuests(): []
        getStatForBuff(effect: BuffStat): StatEffect
        getStorage(): Storage
        getStr(): number
        getSummonByKey(id: number): Summon
        getSummonsValues(): []
        getTargetHpBarHash(): number
        getTargetHpBarTime(): number
        getTeam(): number
        getTotalCP(): number
        getTotalDex(): number
        getTotalInt(): number
        getTotalLuk(): number
        getTotalMagic(): number
        getTotalStr(): number
        getTotalWatk(): number
        getTrade(): Trade
        getTrockMaps(): []
        getTrockSize(): number
        getType(): MapObjectType
        getVanquisherKills(): number
        getVanquisherStage(): number
        getVipTrockMaps(): []
        getVipTrockSize(): number
        getVisibleMapObjects(): MapObject[]
        getWarpMap(map: number): MapleMap
        getWhiteChat(): boolean
        getWorld(): number
        getWorldServer(): World
        giveCoolDowns(skillid: number, starttime: number, length: number): void
        giveDebuff(disease: Disease, skill: MobSkill): void
        gmLevel(): number
        gotPartyQuestItem(partyquestchar: string): boolean
        handleEnergyChargeGain(): void
        handleOrbconsume(): void
        hasActiveBuff(sourceid: number): boolean
        hasBuffFromSourceid(sourceid: number): boolean
        hasDisease(dis: Disease): boolean
        hasEmptySlot(itemId: number): boolean
        hasEmptySlot(invType: number): boolean
        hasEntered(script: string): boolean
        hasEntered(script: string, mapId: number): boolean
        hasGivenFame(to: Character): void
        hasJustMarried(): boolean
        hasMerchant(): boolean
        hasNoviceExpRate(): boolean
        haveCleanItem(itemid: number): boolean
        haveItem(itemid: number): boolean
        haveItemEquipped(itemid: number): boolean
        haveItemWithId(itemid: number, checkEquipped: boolean): boolean
        haveWeddingRing(): boolean
        healHpMp(): void
        hide(hide: boolean): void
        hide(hide: boolean, login: boolean): void
        hpChangeAction(oldHp: number): void
        increaseEquipExp(expGain: number): void
        increaseGuildCapacity(): void
        insertNewChar(recipe: CharacterFactoryRecipe): boolean
        isAlive(): boolean
        isAran(): boolean
        isAwayFromWorld(): boolean
        isBanned(): boolean
        isBeginnerJob(): boolean
        isBuffFrom(stat: BuffStat, skill: Skill): boolean
        isChallenged(): boolean
        isChangingMaps(): boolean
        isChasing(): boolean
        isCygnus(): boolean
        isEquippedItemPouch(): boolean
        isEquippedMesoMagnet(): boolean
        isEquippedPetItemIgnore(): boolean
        isFacingLeft(): boolean
        isFamilyBuff(): boolean
        isFinishedDojoTutorial(): boolean
        isGM(): boolean
        isGmJob(): boolean
        isGuildLeader(): boolean
        isHidden(): boolean
        isLoggedIn(): boolean
        isLoggedInWorld(): boolean
        isMale(): boolean
        isMapObjectVisible(mo: MapObject): boolean
        isMarried(): boolean
        isPartyLeader(): boolean
        isPartyMember(cid: number): boolean
        isPartyMember(chr: Character): boolean
        isRecvPartySearchInviteEnabled(): boolean
        isRidingBattleship(): boolean
        isSummonsEmpty(): boolean
        isTrockMap(id: number): boolean
        isUseCS(): boolean
        isVipTrockMap(id: number): boolean
        leaveMap(): void
        leaveParty(): boolean
        levelUp(takeexp: boolean): void
        loadCharFromDB(cid: number, client: Client, channelServer: boolean): Character
        loadCharacterEntryFromDB(rs: ResultSet, equipped: []): Character
        logOff(): void
        loseExp(loss: number, show: boolean, inChat: boolean): void
        loseExp(loss: number, show: boolean, inChat: boolean, white: boolean): void
        makeMapleReadable(in0: string): string
        mergeAllItemsFromName(name: string): boolean
        mergeAllItemsFromPosition(statUps: JavaMap, pos: number): void
        message(m: string): void
        mount(id: number, skillid: number): Mount
        needQuestItem(questid: number, itemid: number): boolean
        newClient(c: Client): void
        notifyMapTransferToPartner(mapid: number): void
        nullifyPosition(): void
        partyOperationUpdate(party: Party, exPartyMembers: []): void
        peekSavedLocation(type: string): number
        pickupItem(ob: MapObject): void
        pickupItem(ob: MapObject, petIndex: number): void
        portalDelay(): number
        portalDelay(delay: number): void
        purgeDebuffs(): void
        questExpirationTask(): void
        questTimeLimit(quest: Quest, seconds: number): void
        questTimeLimit2(quest: Quest, expires: number): void
        raiseQuestMobCount(id: number): void
        reapplyLocalStats(): void
        recalcLocalStats(): []
        receivePartyMemberHP(): void
        registerChairBuff(): boolean
        registerEffect(effect: StatEffect, starttime: number, expirationtime: number, isSilent: boolean): void
        registerNameChange(newName: string): boolean
        registerWorldTransfer(newWorld: number): boolean
        releaseControlledMonsters(): void
        reloadQuestExpirations(): void
        removeAllCooldownsExcept(id: number, packet: boolean): void
        removeCooldown(skillId: number): void
        removeIncomingInvites(): void
        removeJailExpirationTime(): void
        removeNewYearRecord(newyear: NewYearCardRecord): void
        removePartyDoor(partyUpdate: boolean): Door
        removePartyQuestItem(letter: string): void
        removePet(pet: Pet, shift_left: boolean): void
        removeSandboxItems(): void
        removeVisibleMapObject(mo: MapObject): void
        resetBattleshipHp(): void
        resetCP(): void
        resetEnteredScript(): void
        resetEnteredScript(script: string): void
        resetEnteredScript(mapId: number): void
        resetExcluded(petId: number): void
        resetPlayerAggro(): void
        resetPlayerRates(): void
        resetStats(): void
        respawn(returnMap: number): void
        respawn(eim: EventInstanceManager, returnMap: number): void
        revertLastPlayerRates(): void
        revertPlayerRates(): void
        revertWorldRates(): void
        runFullnessSchedule(petSlot: number): void
        runTirednessSchedule(): boolean
        safeAddHP(delta: number): number
        saveCharToDB(): void
        saveCharToDB(notAutosave: boolean): void
        saveCooldowns(): void
        saveGuildStatus(): void
        saveLocation(type: string): void
        saveLocationOnWarp(): void
        sellAllItemsFromName(invTypeId: number, name: string): number
        sellAllItemsFromPosition(ii: ItemInformationProvider, type: InventoryType, pos: number): number
        sendDestroyData(client: Client): void
        sendKeymap(): void
        sendMacros(): void
        sendPacket(packet: Packet): void
        sendPolice(text: string): void
        sendPolice(greason: number, reason: string, duration: number): void
        sendQuickmap(): void
        sendSpawnData(client: Client): void
        setAccountId(accountId: number): void
        setAllianceRank(allianceRank: number): void
        setAriantColiseum(ariantColiseum: AriantColiseum): void
        setAriantPoints(ariantPoints: number): void
        setAutoBanManager(autoBan: AutobanManager): void
        setAwayFromChannelWorld(): void
        setBanishPlayerData(banishMap: number, banishSp: number, banishTime: number): void
        setBanned(banned: boolean): void
        setBattleshipHp(battleshipHp: number): void
        setBookCover(bookCover: number): void
        setBuddyCapacity(capacity: number): void
        setBuddylist(buddylist: BuddyList): void
        setBuffedValue(effect: BuffStat, value: number): void
        setCP(a: number): void
        setCS(cs: boolean): void
        setCanRecvPartySearchInvite(canRecvPartySearchInvite: boolean): void
        setCashShop(cashShop: CashShop): void
        setChalkboard(text: string): void
        setChallenged(challenged: boolean): void
        setChasing(chasing: boolean): void
        setClient(client: Client): void
        setCombo(count: number): void
        setCouponRates(): void
        setCpqTimer(timer: ScheduledFuture): void
        setCurrentOnlieTime(iTime: number): void
        setDataString(dataString: string): void
        setDex(dex: number): void
        setDisconnectedFromChannelWorld(): void
        setDojoEnergy(x: number): void
        setDojoPoints(dojoPoints: number): void
        setDojoStage(dojoStage: number): void
        setDragon(dragon: Dragon): void
        setEnergyBar(energyBar: number): void
        setEnteredChannelWorld(): void
        setEventInstance(eventInstance: EventInstanceManager): void
        setExp(amount: number): void
        setFace(face: number): void
        setFame(fame: number): void
        setFamilyBuff(type: boolean, exp: number, drop: number): void
        setFamilyEntry(entry: FamilyEntry): void
        setFamilyId(familyId: number): void
        setFestivalPoints(FestivalPoints: number): void
        setFinishedDojoTutorial(finishedDojoTutorial: boolean): void
        setFitness(fitness: Fitness): void
        setGM(level: number): void
        setGMLevel(level: number): void
        setGachaExp(exp: number): void
        setGender(gender: number): void
        setGuildId(guildId: number): void
        setGuildRank(guildRank: number): void
        setHair(hair: number): void
        setHasMerchant(set: boolean): void
        setHasSandboxItem(): void
        setHiredMerchant(hiredMerchant: HiredMerchant): void
        setHpMpApUsed(mpApUsed: number): void
        setId(id: number): void
        setInitialSpawnPoint(initialSpawnPoint: number): void
        setInt(int_: number): void
        setItemEffect(itemEffect: number): void
        setJailExpiration(jailExpiration: number): void
        setJob(job: Job): void
        setJobRank(jobRank: number): void
        setJobRankMove(jobRankMove: number): void
        setLastCombo(lastCombo: number): void
        setLastCommandMessage(text: string): void
        setLastExpGainTime(lastExpGainTime: number): void
        setLastSnowballAttack(time: number): void
        setLastUsedCashItem(lastUsedCashItem: number): void
        setLastfametime(lastfametime: number): void
        setLastmonthfameids(lastmonthfameids: []): void
        setLevel(level: number): void
        setLinkedLevel(linkedLevel: number): void
        setLinkedName(linkedName: string): void
        setLoggedIn(loggedIn: boolean): void
        setLoginTime(loginTime: number): void
        setLuk(luk: number): void
        setMGC(mgc: GuildCharacter): void
        setMPC(mpc: PartyCharacter): void
        setMap(PmapId: number): void
        setMap(map: MapleMap): void
        setMapId(mapId: number): void
        setMapTransitionComplete(): void
        setMapleMount(mapleMount: Mount): void
        setMarriageItemId(marriageItemId: number): void
        setMarriageRing(marriageRing: Ring): void
        setMasteries(jobId: number): void
        setMatchcardlosses(matchcardlosses: number): void
        setMatchcardties(matchcardties: number): void
        setMatchcardwins(matchcardwins: number): void
        setMerchantMeso(set: number): void
        setMeso(meso: number): void
        setMessenger(messenger: Messenger): void
        setMessengerPosition(position: number): void
        setMiniGame(miniGame: MiniGame): void
        setMiniGamePoints(visitor: Character, winnerslot: number, omok: boolean): void
        setMonsterBook(monsterBook: MonsterBook): void
        setMonsterCarnival(monsterCarnival: MonsterCarnival): void
        setMonsterCarnivalParty(monsterCarnivalParty: MonsterCarnivalParty): void
        setName(name: string): void
        setNpcCooldown(d: number): void
        setObjectId(id: number): void
        setOla(ola: Ola): void
        setOmoklosses(omoklosses: number): void
        setOmokties(omokties: number): void
        setOmokwins(omokwins: number): void
        setOwlSearch(owlSearch: number): void
        setOwnedMap(map: MapleMap): void
        setPartnerId(partnerId: number): void
        setParty(p: Party): void
        setPartyQuest(partyQuest: PartyQuest): void
        setPartyQuestItemObtained(partyquestchar: string): void
        setPlayerAggro(mobHash: number): void
        setPlayerRates(): void
        setPlayerShop(playerShop: PlayerShop): void
        setPosition(position: Point): void
        setQuestFame(questFame: number): void
        setQuestProgress(id: number, infoNumber: number, progress: string): void
        setQuickSlotKeyMapped(quickSlotKeyMapped: QuickslotBinding): void
        setQuickSlotLoaded(quickSlotLoaded: number[]): void
        setRPS(rps: RockPaperScissor): void
        setRank(rank: number): void
        setRankMove(rankMove: number): void
        setReborns(value: number): void
        setRemainingAp(remainingAp: number): void
        setRemainingSp(remainingSp: number, skillbook: number): void
        setRewardPoints(value: number): void
        setSearch(search: string): void
        setSessionTransitionState(): void
        setShop(shop: Shop): void
        setSkinColor(skinColor: SkinColor): void
        setSlot(slotid: number): void
        setStance(stance: number): void
        setStorage(storage: Storage): void
        setStr(str: number): void
        setTargetHpBarHash(targetHpBarHash: number): void
        setTargetHpBarTime(targetHpBarTime: number): void
        setTeam(team: number): void
        setTotalCP(a: number): void
        setTrade(trade: Trade): void
        setUsedStorage(): void
        setVanquisherKills(vanquisherKills: number): void
        setVanquisherStage(vanquisherStage: number): void
        setWorld(world: number): void
        setWorldRates(): void
        shiftPetsRight(): void
        showAllEquipFeatures(): void
        showDojoClock(): void
        showHint(msg: string): void
        showHint(msg: string, length: number): void
        showMapOwnershipInfo(mapOwner: Character): void
        showUnderLeveledInfo(mob: Monster): void
        silentApplyDiseases(diseaseMap: JavaMap): void
        silentGiveBuffs(buffs: []): void
        silentPartyUpdate(): void
        sitChair(itemId: number): void
        skillCooldownTask(): void
        skillIsCooling(skillId: number): boolean
        startFamilyBuffTimer(delay: number): void
        startMapEffect(msg: string, itemId: number): void
        startMapEffect(msg: string, itemId: number, duration: number): void
        stopControllingMonster(monster: Monster): void
        toCharactersDO(chr: Character): CharactersDO
        toggleBlockCashShop(): void
        toggleExpGain(): void
        toggleHide(login: boolean): void
        toggleRecvPartySearchInvite(): boolean
        toggleWhiteChat(): void
        unEquipAllPets(): void
        unEquipPet(pet: Pet, shift_left: boolean): void
        unEquipPet(pet: Pet, shift_left: boolean, hunger: boolean): void
        unblockPortal(scriptName: string): void
        unequippedItem(equip: Equip): void
        unregisterChairBuff(): boolean
        updateActiveEffects(): void
        updateAreaInfo(area: number, info: string): void
        updateAriantScore(): void
        updateAriantScore(dropQty: number): void
        updateCouponRates(): void
        updateHp(hp: number): void
        updateHpMaxHp(hp: number, maxhp: number): void
        updateHpMp(x: number): void
        updateHpMp(newhp: number, newmp: number): void
        updateMacros(position: number, updateMacro: SkillMacro): void
        updateMaxHp(maxhp: number): void
        updateMaxHpMaxMp(maxhp: number, maxmp: number): void
        updateMaxMp(maxmp: number): void
        updateMobExpRate(): void
        updateMp(mp: number): void
        updateMpMaxMp(mp: number, maxmp: number): void
        updateOnlineTime(amount: number): void
        updatePartyMemberHP(): void
        updatePartySearchAvailability(pSearchAvailable: boolean): void
        updateQuestStatus(qs: QuestStatus): void
        updateRemainingSp(remainingSp: number): void
        updateSingleStat(stat: Stat, newval: number): void
        updateStrDexIntLuk(x: number): void
        visitMap(map: MapleMap): void
        warpAhead(map: number): void
        withdrawMerchantMesos(): void
        yellowMessage(m: string): void
    }
    interface Client {
        //============ Properties =============
        LOGIN_LOGGEDIN: number
        LOGIN_NOTLOGGEDIN: number
        LOGIN_SERVER_TRANSITION: number
        //============ Functions  =============
        acceptToS(): boolean
        addVotePoints(points: number): void
        announceBossHpBar(mm: Monster, mobHash: number, packet: Packet): void
        announceHint(msg: string, length: number): void
        announceServerMessage(): void
        attemptCsCoupon(): boolean
        banHWID(): void
        banMacs(): void
        canBypassPic(): boolean
        canBypassPin(): boolean
        canClickNPC(): boolean
        canGainCharacterSlot(): boolean
        canRequestCharlist(): boolean
        changeChannel(channel: number): void
        channelActive(ctx: ChannelHandlerContext): void
        channelInactive(ctx: ChannelHandlerContext): void
        channelRead(ctx: ChannelHandlerContext, msg: Object): void
        channelReadComplete(arg0: ChannelHandlerContext): void
        channelRegistered(arg0: ChannelHandlerContext): void
        channelUnregistered(arg0: ChannelHandlerContext): void
        channelWritabilityChanged(arg0: ChannelHandlerContext): void
        checkBirthDate(date: Calendar): boolean
        checkChar(accid: number): void
        checkIfIdle(event: IdleStateEvent): void
        checkPic(other: string): boolean
        checkPin(other: string): boolean
        closePlayerScriptInteractions(): void
        closeSession(): void
        createChannelClient(sessionId: number, remoteAddress: string, packetProcessor: PacketProcessor, world: number, channel: number): Client
        createLoginClient(sessionId: number, remoteAddress: string, packetProcessor: PacketProcessor, world: number, channel: number): Client
        createMock(): Client
        deleteCharacter(cid: number, senderAccId: number): boolean
        disconnect(shutdown: boolean, cashshop: boolean): void
        disconnectSession(): void
        dottedQuadToLong(dottedQuad: string): number
        enableCSActions(): void
        exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): void
        finishLogin(): number
        forceDisconnect(): void
        gainCharacterSlot(): boolean
        getAbstractPlayerInteraction(): AbstractPlayerInteraction
        getAccID(): number
        getAccountName(): string
        getAvailableCharacterSlots(): number
        getAvailableCharacterWorldSlots(): number
        getAvailableCharacterWorldSlots(world: number): number
        getCM(): NPCConversationManager
        getChannel(): number
        getChannelServer(): Channel
        getChannelServer(channel: number): Channel
        getCharacterSlots(): number
        getEventManager(event: string): EventManager
        getGMLevel(): number
        getGReason(): number
        getGender(): number
        getHwid(): Hwid
        getLanguage(): number
        getLastPacket(): number
        getLoginState(): number
        getMacs(): []
        getPic(): string
        getPin(): string
        getPlayer(): Character
        getQM(): QuestActionManager
        getRemoteAddress(): string
        getScriptEngine(name: string): ScriptEngine
        getSessionId(): number
        getTempBanCalendar(): Calendar
        getTempBanCalendarFromDB(): Calendar
        getVisibleWorlds(): number
        getVotePoints(): number
        getVoteTime(): number
        getWorld(): number
        getWorldServer(): World
        handlerAdded(arg0: ChannelHandlerContext): void
        handlerRemoved(arg0: ChannelHandlerContext): void
        hasBannedHWID(): boolean
        hasBannedIP(): boolean
        hasBannedMac(): boolean
        hasBeenBanned(): boolean
        hasVotedAlready(): boolean
        isInTransition(): boolean
        isLoggedIn(): boolean
        isSharable(): boolean
        loadCharacterNames(worldId: number): []
        loadCharacters(serverId: number): []
        lockClient(): void
        login(login: string, pwd: string, hwid: Hwid): number
        pongReceived(): void
        releaseClient(): void
        removeClickedNPC(): void
        removeScriptEngine(name: string): void
        requestedServerlist(worlds: number): void
        resetCsCoupon(): void
        resetVoteTime(): void
        sendCharList(server: number): void
        sendPacket(packet: Packet): void
        setAccID(id: number): void
        setAccountName(a: string): void
        setChannel(channel: number): void
        setCharacterOnSessionTransitionState(cid: number): void
        setCharacterSlots(slots: number): void
        setClickedNPC(): void
        setGMLevel(level: number): void
        setGender(m: number): void
        setHwid(hwid: Hwid): void
        setLanguage(lingua: number): void
        setPic(pic: string): void
        setPin(pin: string): void
        setPlayer(player: Character): void
        setScriptEngine(name: string, e: ScriptEngine): void
        setWorld(world: number): void
        tryacquireClient(): boolean
        tryacquireEncoder(): boolean
        unlockClient(): void
        unlockEncoder(): void
        updateHwid(hwid: Hwid): void
        updateLastPacket(): void
        updateLoginState(newState: number): void
        updateMacs(macData: string): void
        useVotePoints(points: number): void
        userEventTriggered(ctx: ChannelHandlerContext, event: Object): void
    }
    interface Equip extends Item {
        //============ Properties =============
        //============ Functions  =============
        copy(): Item
        gainItemExp(c: Client, gain: number): void
        gainStats(stats: []): Pair
        getAcc(): number
        getAvoid(): number
        getDex(): number
        getFlag(): number
        getHands(): number
        getHp(): number
        getInt(): number
        getItemExp(): number
        getItemLevel(): number
        getItemType(): number
        getJump(): number
        getLevel(): number
        getLuk(): number
        getMatk(): number
        getMdef(): number
        getMp(): number
        getRingId(): number
        getSpeed(): number
        getStats(): JavaMap
        getStr(): number
        getUpgradeSlots(): number
        getVicious(): number
        getWatk(): number
        getWdef(): number
        isWearing(): boolean
        setAcc(acc: number): void
        setAvoid(avoid: number): void
        setDex(dex: number): void
        setFlag(flag: number): void
        setHands(hands: number): void
        setHp(hp: number): void
        setInt(_int: number): void
        setItemExp(exp: number): void
        setItemLevel(level: number): void
        setJump(jump: number): void
        setLevel(level: number): void
        setLuk(luk: number): void
        setMatk(matk: number): void
        setMdef(mdef: number): void
        setMp(mp: number): void
        setQuantity(quantity: number): void
        setRingId(id: number): void
        setSpeed(speed: number): void
        setStr(str: number): void
        setUpgradeSlots(upgradeSlots: number): void
        setUpgradeSlots(i: number): void
        setVicious(i: number): void
        setVicious(vicious: number): void
        setWatk(watk: number): void
        setWdef(wdef: number): void
        showEquipFeatures(c: Client): string
        wear(yes: boolean): void
    }
    interface ExpTable {
        //============ Properties =============
        //============ Functions  =============
        getEquipExpNeededForLevel(level: number): number
        getExpNeededForLevel(level: number): number
        getMountExpNeededForLevel(level: number): number
        getTamenessNeededForLevel(level: number): number
    }
    interface GameConfig {
        //============ Properties =============
        //============ Functions  =============
        add(gameConfigDO: GameConfigDO): void
        get(key: string): Object
        get(key: string, defaultValue: Object): Object
        get(type: string, key: string): Object
        get(type: string, key: string, defaultVal: Object): Object
        get(type: string, subType: string, key: string): Object
        get(type: string, subType: string, key: string, defaultVal: Object): Object
        getBoolean(key: string): boolean
        getBooleanValue(key: string): boolean
        getByte(key: string): number
        getByteValue(key: string): number
        getConfig(): JSONObject
        getDouble(key: string): number
        getDoubleValue(key: string): number
        getFloat(key: string): number
        getFloatValue(key: string): number
        getIntValue(key: string): number
        getInteger(key: string): number
        getLong(key: string): number
        getLongValue(key: string): number
        getObject(key: string): Object
        getObject(key: string, clz: Class): Object
        getServer(key: string): Object
        getServerBoolean(key: string): boolean
        getServerByte(key: string): number
        getServerDouble(key: string): number
        getServerFloat(key: string): number
        getServerInt(key: string): number
        getServerLong(key: string): number
        getServerObject(key: string, type: TypeReference): Object
        getServerObject(key: string, defaultVal: Object): Object
        getServerObject(key: string, clz: Class): Object
        getServerShort(key: string): number
        getServerString(key: string): string
        getShort(key: string): number
        getShortValue(key: string): number
        getString(key: string): string
        getStringValue(key: string): string
        getValueProp(type: string, key: string): JSONObject
        getValueProp(type: string, subType: string, key: string): JSONObject
        getWorld(worldId: number, key: string): Object
        getWorldBoolean(worldId: number, key: string): boolean
        getWorldByte(worldId: number, key: string): number
        getWorldDouble(worldId: number, key: string): number
        getWorldFloat(worldId: number, key: string): number
        getWorldInt(worldId: number, key: string): number
        getWorldLong(worldId: number, key: string): number
        getWorldObject(worldId: number, key: string, clz: Class): Object
        getWorldObject(worldId: number, key: string, type: TypeReference): Object
        getWorldObject(worldId: number, key: string, defaultVal: Object): Object
        getWorldShort(worldId: number, key: string): number
        getWorldString(worldId: number, key: string): string
        remove(gameConfigDO: GameConfigDO): void
        update(gameConfigDO: GameConfigDO): void
    }
    interface GameConstants {
        //============ Properties =============
        CASH_DATA: number[]
        CPQ_DISEASES: Disease[]
        GAME_SONGS: []
        GOTO_AREAS: JavaMap
        GOTO_TOWNS: JavaMap
        MAX_CLEAN_PACK_SIZE: number
        MAX_FIELD_MOB_DAMAGE: number
        WORLD_NAMES: string[]
        goldrewards: number[]
        stats: string[]
        //============ Functions  =============
        bannedBindSkills(skill: number): boolean
        canPnpcBranchUseScriptId(branch: number, scriptId: number): boolean
        getChangeJobSpUpgrade(jobbranch: number): number
        getCustomAction(customKeyset: boolean): number[]
        getCustomKey(customKeyset: boolean): number[]
        getCustomType(customKeyset: boolean): number[]
        getEnc(): Pair
        getHallOfFameBranch(job: Job, mapid: number): number
        getHallOfFameMapid(job: Job): number
        getJobBranch(job: Job): number
        getJobMaxLevel(job: Job): number
        getJobName(jobid: number): string
        getJobUpgradeLevelRange(jobbranch: number): number
        getMonsterHP(level: number): number
        getOverallJobRankByScriptId(scriptId: number): number
        getPlayerBonusDropRate(slot: number): number
        getPlayerBonusExpRate(slot: number): number
        getPlayerBonusMesoRate(slot: number): number
        getSkillBook(job: number): number
        hasSPTable(job: Job): boolean
        isAran(job: number): boolean
        isAranSkills(skill: number): boolean
        isAriantColiseumArena(mapid: number): boolean
        isAriantColiseumLobby(mapid: number): boolean
        isCygnus(job: number): boolean
        isDojoBossArea(mapid: number): boolean
        isFinisherSkill(skillId: number): boolean
        isFreeMarketRoom(mapid: number): boolean
        isGMSkills(skill: number): boolean
        isHallOfFameMap(mapid: number): boolean
        isHiddenSkills(skill: number): boolean
        isInJobTree(skillId: number, jobId: number): boolean
        isMedalQuest(questid: number): boolean
        isMerchantLocked(map: MapleMap): boolean
        isPodiumHallOfFameMap(mapid: number): boolean
        isPqSkill(skill: number): boolean
        isPqSkillMap(mapid: number): boolean
        numberWithCommas(i: number): string
        ordinal(i: number): string
        parseNumber(value: string): Number
        selectRandomReward(rewards: number[]): number
    }
    interface InformationType {
        //============ Properties =============
        CASH: InformationType
        CONSUME: InformationType
        EQP: InformationType
        ETC: InformationType
        INS: InformationType
        MAP: InformationType
        MOB: InformationType
        NPC: InformationType
        PET: InformationType
        SKILL: InformationType
        //============ Functions  =============
        compareTo(arg0: Object): number
        compareTo(arg0: Enum): number
        describeConstable(): Optional
        getDeclaringClass(): Class
        getType(): string
        name(): string
        ofType(type: string): InformationType
        ordinal(): number
        valueOf(name: string): InformationType
        valueOf(arg0: Class, arg1: string): Enum
        values(): InformationType[]
    }
    interface Inventory {
        //============ Properties =============
        //============ Functions  =============
        addItem(item: Item): number
        addItemFromDB(item: Item): void
        checkSpot(chr: Character, items: []): boolean
        checkSpot(chr: Character, item: Item): boolean
        checkSpots(chr: Character, items: []): boolean
        checkSpots(chr: Character, items: [], useProofInv: boolean): boolean
        checkSpots(chr: Character, items: [], typesSlotsUsed: [], useProofInv: boolean): boolean
        checkSpotsAndOwnership(chr: Character, items: []): boolean
        checkSpotsAndOwnership(chr: Character, items: [], useProofInv: boolean): boolean
        checkSpotsAndOwnership(chr: Character, items: [], typesSlotsUsed: [], useProofInv: boolean): boolean
        checked(): boolean
        checked(yes: boolean): void
        countById(itemId: number): number
        countNotOwnedById(itemId: number): number
        dispose(): void
        findByCashId(cashId: number): Item
        findById(itemId: number): Item
        findByName(name: string): Item
        forEach(arg0: Consumer): void
        freeSlotCountById(itemId: number, required: number): number
        getItem(slot: number): Item
        getNextFreeSlot(): number
        getNumFreeSlot(): number
        getSlotLimit(): number
        getType(): InventoryType
        isEquipInventory(): boolean
        isExtendableInventory(): boolean
        isFull(): boolean
        isFull(margin: number): boolean
        isFullAfterSomeItems(margin: number, used: number): boolean
        iterator(): JavaIterator
        linkedListById(itemId: number): []
        list(): []
        listById(itemId: number): []
        lockInventory(): void
        move(sSlot: number, dSlot: number, slotMax: number): void
        removeItem(slot: number): void
        removeItem(slot: number, quantity: number, allowZero: boolean): void
        removeSlot(slot: number): void
        setSlotLimit(newLimit: number): void
        spliterator(): Spliterator
        unlockInventory(): void
    }
    interface InventoryManipulator {
        //============ Properties =============
        //============ Functions  =============
        addById(c: Client, itemId: number, quantity: number): boolean
        addById(c: Client, itemId: number, quantity: number, expiration: number): boolean
        addById(c: Client, itemId: number, quantity: number, owner: string, petid: number): boolean
        addById(c: Client, itemId: number, quantity: number, owner: string, petid: number, expiration: number): boolean
        addById(c: Client, itemId: number, quantity: number, owner: string, petid: number, flag: number, expiration: number): boolean
        addFromDrop(c: Client, item: Item): boolean
        addFromDrop(c: Client, item: Item, show: boolean): boolean
        addFromDrop(c: Client, item: Item, show: boolean, petId: number): boolean
        checkSpace(c: Client, itemid: number, quantity: number, owner: string): boolean
        checkSpaceProgressively(c: Client, itemid: number, quantity: number, owner: string, usedSlots: number, useProofInv: boolean): number
        drop(c: Client, type: InventoryType, src: number, quantity: number): void
        equip(c: Client, src: number, dst: number): void
        isSandboxItem(it: Item): boolean
        move(c: Client, type: InventoryType, src: number, dst: number): void
        removeById(c: Client, type: InventoryType, itemId: number, quantity: number, fromDrop: boolean, consume: boolean): void
        removeFromSlot(c: Client, type: InventoryType, slot: number, quantity: number, fromDrop: boolean): void
        removeFromSlot(c: Client, type: InventoryType, slot: number, quantity: number, fromDrop: boolean, consume: boolean): void
        unequip(c: Client, src: number, dst: number): void
    }
    interface InventoryType {
        //============ Properties =============
        CANHOLD: InventoryType
        CASH: InventoryType
        EQUIP: InventoryType
        EQUIPPED: InventoryType
        ETC: InventoryType
        SETUP: InventoryType
        UNDEFINED: InventoryType
        USE: InventoryType
        //============ Functions  =============
        canChangeSlotMax(): boolean
        compareTo(arg0: Object): number
        compareTo(arg0: Enum): number
        describeConstable(): Optional
        getBitfieldEncoding(): number
        getByType(type: number): InventoryType
        getByWZName(name: string): InventoryType
        getDeclaringClass(): Class
        getName(): string
        getType(): number
        isEquip(): boolean
        name(): string
        ordinal(): number
        valueOf(name: string): InventoryType
        valueOf(arg0: Class, arg1: string): Enum
        values(): InventoryType[]
    }
    interface Item {
        //============ Properties =============
        //============ Functions  =============
        compareTo(other: Item): number
        compareTo(other: Object): number
        copy(): Item
        getCashId(): number
        getExpiration(): number
        getFlag(): number
        getGiftFrom(): string
        getInventoryType(): InventoryType
        getItemId(): number
        getItemLog(): []
        getItemType(): number
        getOwner(): string
        getPet(): Pet
        getPetId(): number
        getPosition(): number
        getQuantity(): number
        getSN(): number
        isUntradeable(): boolean
        setExpiration(expire: number): void
        setFlag(b: number): void
        setGiftFrom(giftFrom: string): void
        setOwner(owner: string): void
        setPosition(position: number): void
        setQuantity(quantity: number): void
        setSN(sn: number): void
    }
    interface ItemId {
        //============ Properties =============
        ADVANCED_MONSTER_CRYSTAL_1: number
        ADVANCED_MONSTER_CRYSTAL_2: number
        ADVANCED_MONSTER_CRYSTAL_3: number
        AIR_BUBBLE: number
        ALL_CURE_POTION: number
        ANTI_BANISH_SCROLL: number
        AP_RESET: number
        ARPQ_ELEMENT_ROCK: number
        ARPQ_SHIELD: number
        ARPQ_SPIRIT_JEWEL: number
        BALANCED_FURY: number
        BASIC_MONSTER_CRYSTAL_1: number
        BASIC_MONSTER_CRYSTAL_2: number
        BASIC_MONSTER_CRYSTAL_3: number
        BATTLESHIP: number
        BEGINNERS_GUIDE: number
        BELT_STR_100_SCROLL: number
        BLACK_MARTIAL_ARTS_PANTS: number
        BLAZE_CAPSULE: number
        BLUE_CARZEN_BOOTS: number
        BLUE_KORBEN: number
        BLUE_POTION: number
        BLUE_PRIMROSE_SEED: number
        BLUE_SNAIL_SHELL: number
        BLUE_WIZARD_ROBE: number
        BRONZE_CHAIN_BOOTS: number
        BROWN_PAULIE_BOOTS: number
        BROWN_POLLARD: number
        BROWN_PRIMROSE_SEED: number
        BULLET: number
        BUMMER_EFFECT: number
        CARAT_RING_BASE: number
        CARAT_RING_BOX_BASE: number
        CASH_SHOP_SURPRISE: number
        CHALKBOARD_1: number
        CHALKBOARD_2: number
        CHAOS_SCROll_60: number
        CIRCLE_WINDED_STAFF: number
        CLEAN_SLATE_1: number
        CLEAN_SLATE_20: number
        CLEAN_SLATE_3: number
        CLEAN_SLATE_5: number
        COLD_MIND: number
        COLD_PROTECTION_SCROLl: number
        CRYSTAL_ILBI_THROWING_STARS: number
        DARK_BROWN_STEALER: number
        DARK_BROWN_STEALER_PANTS: number
        DARK_ENGRIT: number
        DEVIL_RAIN_THROWING_STAR: number
        DRAGON_PET: number
        DRAGON_STONE_SCROLL: number
        DROP_COUPON_2X_4H: number
        EASTER_BASKET: number
        EASTER_CHARM: number
        EMPTY_ENGAGEMENT_BOX_GOLDEN: number
        EMPTY_ENGAGEMENT_BOX_MOONSTONE: number
        EMPTY_ENGAGEMENT_BOX_SILVER: number
        EMPTY_ENGAGEMENT_BOX_STAR: number
        ENGAGEMENT_BOX_GOLDEN: number
        ENGAGEMENT_BOX_MAX: number
        ENGAGEMENT_BOX_MIN: number
        ENGAGEMENT_BOX_MOONSTONE: number
        ENGAGEMENT_BOX_SILVER: number
        ENGAGEMENT_BOX_STAR: number
        ENGAGEMENT_RING_GOLDEN: number
        ENGAGEMENT_RING_MOONSTONE: number
        ENGAGEMENT_RING_SILVER: number
        ENGAGEMENT_RING_STAR: number
        EPQ_MONSTER_MARBLE: number
        EPQ_PURIFICATION_MARBLE: number
        EXP_COUPON_2X_4H: number
        EXP_COUPON_3X_2H: number
        EYEDROP: number
        FIREMANS_AXE: number
        FISHING_CHAIR: number
        FISH_NET: number
        FISH_NET_WITH_A_CATCH: number
        FREESIA_SCENT: number
        GHOST_SACK: number
        GLADIUS: number
        GLAZE_CAPSULE: number
        GOLDEN_CHICKEN_EFFECT: number
        GOLDEN_MAPLE_LEAF: number
        GREEN_HEADBAND: number
        GREEN_HUNTERS_ARMOR: number
        GREEN_HUNTERS_PANTS: number
        GREEN_HUNTER_BOOTS: number
        GREEN_HUNTRESS_ARMOR: number
        GREEN_HUNTRESS_PANTS: number
        GREEN_PRIMROSE_SEED: number
        HAPPY_BIRTHDAY: number
        HEART_SHAPED_CHOCOLATE: number
        HOG: number
        HOLY_WATER: number
        HWABI_THROWING_STARS: number
        INTERMEDIATE_MONSTER_CRYSTAL_1: number
        INTERMEDIATE_MONSTER_CRYSTAL_2: number
        INTERMEDIATE_MONSTER_CRYSTAL_3: number
        INVITATION_CATHEDRAL: number
        INVITATION_CHAPEL: number
        ITEM_IGNORE: number
        ITEM_POUCH: number
        LAVENDER_SCENT: number
        LEGENDS_GUIDE: number
        LIAR_TREE_SAP: number
        MAGICAL_MITTEN: number
        MAGIC_CANE: number
        MAGIC_ROCK: number
        MANA_ELIXIR: number
        MAPLE_LIFE_B: number
        MAPLE_SYRUP: number
        MATCH_CARDS: number
        MESO_MAGNET: number
        MINI_GAME_BASE: number
        MITHRIL_BATTLE_GRIEVES: number
        MITHRIL_MAUL: number
        MITHRIL_PLATINE: number
        MITHRIL_PLATINE_PANTS: number
        MITHRIL_POLE_ARM: number
        MITHRIL_WAND: number
        MONSTER_MARBLE_1: number
        MONSTER_MARBLE_2: number
        MONSTER_MARBLE_3: number
        MOON_BUNNYS_RICE_CAKE: number
        MOUNTAIN_CROSSBOW: number
        NAME_CHANGE: number
        NEW_YEARS_CARD: number
        NEW_YEARS_CARD_RECEIVED: number
        NEW_YEARS_CARD_SEND: number
        NOBLESSE_GUIDE: number
        NORMAL_CATHEDRAL_RESERVATION_RECEIPT: number
        NORMAL_CHAPEL_RESERVATION_RECEIPT: number
        NORMAL_WEDDING_TICKET_CATHEDRAL: number
        NORMAL_WEDDING_TICKET_CHAPEL: number
        NPC_WEATHER_GROWLIE: number
        NX_CARD_100: number
        NX_CARD_250: number
        OFFICIATORS_PERMISSION: number
        ONYX_CHEST_FOR_COUPLE: number
        ORANGE_POTION: number
        PARENTS_BLESSING: number
        PENDANT_OF_THE_SPIRIT: number
        PERFECT_PITCH: number
        PET_SNAIL: number
        PHARAOHS_BLESSING_1: number
        PHARAOHS_BLESSING_2: number
        PHARAOHS_BLESSING_3: number
        PHARAOHS_BLESSING_4: number
        PHEROMONE_PERFUME: number
        PINK_PRIMROSE_SEED: number
        POUCH: number
        PREMIUM_CATHEDRAL_RESERVATION_RECEIPT: number
        PREMIUM_CHAPEL_RESERVATION_RECEIPT: number
        PREMIUM_WEDDING_TICKET_CATHEDRAL: number
        PREMIUM_WEDDING_TICKET_CHAPEL: number
        PRIME_HANDS: number
        PURPLE_FAIRY_SKIRT: number
        PURPLE_FAIRY_TOP: number
        PURPLE_PRIMROSE_SEED: number
        QUICK_DELIVERY_TICKET: number
        RECEIVED_INVITATION_CATHEDRAL: number
        RECEIVED_INVITATION_CHAPEL: number
        RED_BEAN_PORRIDGE: number
        RED_HWARANG_SHIRT: number
        RED_MAGICSHOES: number
        RED_SNAIL_SHELL: number
        RED_STEAL: number
        RED_STEAL_PANTS: number
        REEF_CLAW: number
        RELAXER: number
        REMOTE_GACHAPON_TICKET: number
        RING_STR_100_SCROLL: number
        ROARING_TIGER_MESSENGER: number
        ROBO_PET: number
        ROSE_SCENT: number
        RPS_CERTIFICATE_BASE: number
        RUSSELLONS_PILLS: number
        RYDEN: number
        SAFETY_CHARM: number
        SNAIL_SHELL: number
        SOFT_WHITE_BUN: number
        SORCERERS_POTION: number
        SPIKES_SCROLL: number
        STEEL_GUARDS: number
        SUBI_THROWING_STARS: number
        TAMED_RUDOLPH: number
        TIMELESS_NIBLEHEIM: number
        TONIC: number
        TRANSPARENT_MARBLE_1: number
        TRANSPARENT_MARBLE_2: number
        TRANSPARENT_MARBLE_3: number
        VEGAS_SPELL_10: number
        VEGAS_SPELL_60: number
        VICIOUS_HAMMER: number
        WEDDING_RING_GOLDEN: number
        WEDDING_RING_MOONSTONE: number
        WEDDING_RING_SILVER: number
        WEDDING_RING_STAR: number
        WHEEL_OF_FORTUNE: number
        WHITE_ELIXIR: number
        WHITE_POTION: number
        WHITE_SCROLL: number
        WORLD_TRANSFER: number
        YELLOW_PRIMROSE_SEED: number
        //============ Functions  =============
        allBulletIds(): number[]
        allThrowingStarIds(): number[]
        getOwlItems(): number[]
        getPermaPets(): number[]
        isCashPackage(itemId: number): boolean
        isChair(itemId: number): boolean
        isCygnusMount(itemId: number): boolean
        isDojoBuff(itemId: number): boolean
        isExpIncrease(itemId: number): boolean
        isExplorerMount(itemId: number): boolean
        isFaceExpression(itemId: number): boolean
        isMonsterCard(itemId: number): boolean
        isNxCard(itemId: number): boolean
        isPartyAllCure(itemId: number): boolean
        isPet(itemId: number): boolean
        isPyramidBuff(itemId: number): boolean
        isRateCoupon(itemId: number): boolean
        isWeddingRing(itemId: number): boolean
        isWeddingToken(itemId: number): boolean
    }
    interface ItemInformationProvider {
        //============ Properties =============
        //============ Functions  =============
        canPetConsume(petId: number, itemId: number): Pair
        canUseCleanSlate(equip: Equip): boolean
        canWearEquipment(chr: Character, items: []): []
        canWearEquipment(chr: Character, equip: Equip, dst: number): boolean
        getAllEtcItems(): []
        getAllItems(): []
        getCardMobId(id: number): number
        getCreateItem(itemId: number): number
        getEquipById(equipId: number): Item
        getEquipLevel(itemId: number, getMaxLevel: boolean): number
        getEquipLevelReq(itemId: number): number
        getEquipStats(itemId: number): JavaMap
        getExpById(itemId: number): number
        getInstance(): ItemInformationProvider
        getItemDataByName(name: string): []
        getItemEffect(itemId: number): StatEffect
        getItemIdsInRange(minId: number, maxId: number, ignoreCashItem: boolean): []
        getItemLevelupStats(itemId: number, level: number): []
        getItemReward(itemId: number): Pair
        getMakerCrystalFromEquip(equipId: number): number
        getMakerCrystalFromLeftover(leftoverId: number): number
        getMakerDisassembledFee(itemId: number): number
        getMakerDisassembledItems(itemId: number): []
        getMakerItemEntry(toCreate: number): MakerItemCreateEntry
        getMakerReagentStatUpgrade(itemId: number): Pair
        getMakerStimulant(itemId: number): number
        getMakerStimulantFromEquip(equipId: number): number
        getMaxLevelById(itemId: number): number
        getMeso(itemId: number): number
        getMobHP(itemId: number): number
        getMobItem(itemId: number): number
        getMsg(itemId: number): string
        getName(itemId: number): string
        getNameDesc(itemId: number): Pair
        getPrice(itemId: number, quantity: number): number
        getQuestConsumablesInfo(itemId: number): QuestConsItem
        getReplaceOnExpire(itemId: number): Pair
        getScriptedItemInfo(itemId: number): ScriptedItem
        getScrollReqs(itemId: number): []
        getSkillStats(itemId: number, playerJob: number): JavaMap
        getSlotMax(c: Client, itemId: number): number
        getStateChangeItem(itemId: number): number
        getSummonMobs(itemId: number): number[][]
        getUnitPrice(itemId: number): number
        getUseDelay(itemId: number): number
        getWatkForProjectile(itemId: number): number
        getWeaponType(itemId: number): WeaponType
        getWhoDrops(itemId: number): []
        getWholePrice(itemId: number): number
        improveEquipStats(nEquip: Equip, stats: JavaMap): void
        isAccountRestricted(itemId: number): boolean
        isCash(itemId: number): boolean
        isConsumeOnPickup(itemId: number): boolean
        isDropRestricted(itemId: number): boolean
        isKarmaAble(itemId: number): boolean
        isLootRestricted(itemId: number): boolean
        isPartyQuestItem(itemId: number): boolean
        isPickupRestricted(itemId: number): boolean
        isQuestItem(itemId: number): boolean
        isTwoHanded(itemId: number): boolean
        isUnmerchable(itemId: number): boolean
        isUntradeableOnEquip(itemId: number): boolean
        isUntradeableRestricted(itemId: number): boolean
        isUpgradeable(itemId: number): boolean
        noCancelMouse(itemId: number): boolean
        randomizeStats(equip: Equip): Equip
        randomizeUpgradeStats(equip: Equip): Equip
        rollSuccessChance(propPercent: number): boolean
        scrollEquipWithId(equip: Item, scrollId: number, usingWhiteScroll: boolean, vegaItemId: number, isGM: boolean): Item
        scrollOptionEquipWithChaos(nEquip: Equip, range: number, option: boolean): void
        usableMasteryBooks(player: Character): []
        usableSkillBooks(player: Character): []
    }
    interface Job {
        //============ Properties =============
        ARAN1: Job
        ARAN2: Job
        ARAN3: Job
        ARAN4: Job
        ASSASSIN: Job
        BANDIT: Job
        BEGINNER: Job
        BISHOP: Job
        BLAZEWIZARD1: Job
        BLAZEWIZARD2: Job
        BLAZEWIZARD3: Job
        BLAZEWIZARD4: Job
        BOWMAN: Job
        BOWMASTER: Job
        BRAWLER: Job
        BUCCANEER: Job
        CHIEFBANDIT: Job
        CLERIC: Job
        CORSAIR: Job
        CROSSBOWMAN: Job
        CRUSADER: Job
        DARKKNIGHT: Job
        DAWNWARRIOR1: Job
        DAWNWARRIOR2: Job
        DAWNWARRIOR3: Job
        DAWNWARRIOR4: Job
        DRAGONKNIGHT: Job
        EVAN: Job
        EVAN1: Job
        EVAN10: Job
        EVAN2: Job
        EVAN3: Job
        EVAN4: Job
        EVAN5: Job
        EVAN6: Job
        EVAN7: Job
        EVAN8: Job
        EVAN9: Job
        FIGHTER: Job
        FP_ARCHMAGE: Job
        FP_MAGE: Job
        FP_WIZARD: Job
        GM: Job
        GUNSLINGER: Job
        HERMIT: Job
        HERO: Job
        HUNTER: Job
        IL_ARCHMAGE: Job
        IL_MAGE: Job
        IL_WIZARD: Job
        LEGEND: Job
        MAGICIAN: Job
        MAPLELEAF_BRIGADIER: Job
        MARAUDER: Job
        MARKSMAN: Job
        NIGHTLORD: Job
        NIGHTWALKER1: Job
        NIGHTWALKER2: Job
        NIGHTWALKER3: Job
        NIGHTWALKER4: Job
        NOBLESSE: Job
        OUTLAW: Job
        PAGE: Job
        PALADIN: Job
        PIRATE: Job
        PRIEST: Job
        RANGER: Job
        SHADOWER: Job
        SNIPER: Job
        SPEARMAN: Job
        SUPERGM: Job
        THIEF: Job
        THUNDERBREAKER1: Job
        THUNDERBREAKER2: Job
        THUNDERBREAKER3: Job
        THUNDERBREAKER4: Job
        WARRIOR: Job
        WHITEKNIGHT: Job
        WINDARCHER1: Job
        WINDARCHER2: Job
        WINDARCHER3: Job
        WINDARCHER4: Job
        //============ Functions  =============
        compareTo(arg0: Object): number
        compareTo(arg0: Enum): number
        describeConstable(): Optional
        getBy5ByteEncoding(encoded: number): Job
        getById(id: number): Job
        getDeclaringClass(): Class
        getId(): number
        getJobNiche(): number
        getJobStyleInternal(jobid: number, opt: number): Job
        getMax(): number
        getName(): string
        isA(basejob: Job): boolean
        name(): string
        ordinal(): number
        valueOf(name: string): Job
        valueOf(arg0: Class, arg1: string): Enum
        values(): Job[]
    }
    interface LifeFactory {
        //============ Properties =============
        //============ Functions  =============
        getLife(id: number, type: string): AbstractLoadedLife
        getMonster(mid: number): Monster
        getMonsterLevel(mid: number): number
        getNPC(nid: number): NPC
        getNPCDefaultTalk(nid: number): string
        getNPCName(nid: number): string
    }
    interface MapId {
        //============ Properties =============
        AMHERST: number
        AMORIA: number
        ANT_TUNNEL_2: number
        AQUARIUM: number
        ARAN_INTRO: number
        ARAN_MAHA: number
        ARAN_POLEARM: number
        ARAN_TUTORIAL_MAX: number
        ARAN_TUTORIAL_START: number
        ARAN_TUTO_1: number
        ARAN_TUTO_2: number
        ARAN_TUTO_3: number
        ARAN_TUTO_4: number
        ARIANT: number
        ARPQ_ARENA_1: number
        ARPQ_ARENA_2: number
        ARPQ_ARENA_3: number
        ARPQ_KINGS_ROOM: number
        ARPQ_LOBBY: number
        BATTLEFIELD_OF_FIRE_AND_WATER: number
        BEIDOU_BEGINNER: number
        BOAT_QUAY_TOWN: number
        CATHEDRAL_WEDDING_ALTAR: number
        CAVE_OF_MUSHROOMS_BASE: number
        CAVE_OF_PIANUS: number
        CHAPEL_WEDDING_ALTAR: number
        COLD_CRADLE: number
        CRIMSONWOOD_KEEP: number
        CRIMSONWOOD_VALLEY_1: number
        CRIMSONWOOD_VALLEY_2: number
        CRITICAL_ERROR_BASE: number
        CURSED_SANCTUARY: number
        CYGNUS_INTRO_BOWMAN: number
        CYGNUS_INTRO_CONCLUSION: number
        CYGNUS_INTRO_LEAD: number
        CYGNUS_INTRO_MAGE: number
        CYGNUS_INTRO_PIRATE: number
        CYGNUS_INTRO_THIEF: number
        CYGNUS_INTRO_WARRIOR: number
        DANGEROUS_FOREST: number
        DESTROYED_DRAGON_NEST: number
        DEVELOPERS_HQ: number
        DOJO_EXIT: number
        DOJO_PARTY_BASE: number
        DOJO_PARTY_MAX: number
        DOJO_SOLO_BASE: number
        DOOR_TO_ZAKUM: number
        DRAGON_NEST_LEFT_BEHIND: number
        DRAKES_BLUE_CAVE_BASE: number
        DRUMMER_BUNNYS_LAIR_BASE: number
        ELLINIA: number
        ELLINIA_SKY_FERRY: number
        ELLIN_FOREST: number
        EL_NATH: number
        ENTRANCE_TO_HORNTAILS_CAVE: number
        EOS_TOWER_76TH_TO_90TH_FLOOR: number
        EREVE: number
        EVENT_COCONUT_HARVEST: number
        EVENT_EXIT: number
        EVENT_FIND_THE_JEWEL: number
        EVENT_OLA_OLA_0: number
        EVENT_OLA_OLA_1: number
        EVENT_OLA_OLA_2: number
        EVENT_OLA_OLA_3: number
        EVENT_OLA_OLA_4: number
        EVENT_OX_QUIZ: number
        EVENT_PHYSICAL_FITNESS: number
        EVENT_SNOWBALL: number
        EVENT_SNOWBALL_ENTRANCE: number
        EVENT_WINNER: number
        EXCAVATION_SITE: number
        EXCLUSIVE_TRAINING_CENTER: number
        FANTASY_THEME_PARK_3: number
        FITNESS_EVENT_LAST: number
        FLORINA_BEACH: number
        FM_ENTRANCE: number
        FORGOTTEN_TWILIGHT: number
        FROM_ELLINIA_TO_EREVE: number
        FROM_EREVE_TO_ELLINIA: number
        FROM_EREVE_TO_ORBIS: number
        FROM_LITH_TO_RIEN: number
        FROM_ORBIS_TO_EREVE: number
        FROM_RIEN_TO_LITH: number
        GM_MAP: number
        GOLEMS_CASTLE_RUINS_BASE: number
        GRIFFEY_FOREST: number
        GUILD_HQ: number
        HALL_OF_BOWMEN: number
        HALL_OF_MAGICIANS: number
        HALL_OF_THIEVES: number
        HALL_OF_WARRIORS: number
        HAPPYVILLE: number
        HENESYS: number
        HENESYS_PARK: number
        HENESYS_PIG_FARM_BASE: number
        HENESYS_PQ: number
        HERB_TOWN: number
        HILL_OF_SANDSTORMS_BASE: number
        HOLLOWED_GROUND: number
        INTERNET_CAFE: number
        JAIL: number
        KAMPUNG_VILLAGE: number
        KERNING_CITY: number
        KERNING_SQUARE: number
        KNIGHTS_CHAMBER: number
        KNIGHTS_CHAMBER_2: number
        KNIGHTS_CHAMBER_3: number
        KNIGHTS_CHAMBER_LARGE: number
        KOREAN_FOLK_TOWN: number
        LAB_AREA_C1: number
        LEAFRE: number
        LITH_HARBOUR: number
        LONGEST_RIDE_ON_BYEBYE_STATION: number
        LUDIBRIUM: number
        MAGATIA: number
        MANONS_FOREST: number
        MUSHROOM_KINGDOM: number
        MUSHROOM_SHRINE: number
        MUSHROOM_TOWN: number
        MU_LUNG: number
        MU_LUNG_DOJO_HALL: number
        NAUTILUS_HARBOR: number
        NAUTILUS_TRAINING_ROOM: number
        NEO_CITY: number
        NETTS_PYRAMID: number
        NETTS_PYRAMID_PARTY_BASE: number
        NETTS_PYRAMID_SOLO_BASE: number
        NEWT_SECURED_ZONE_BASE: number
        NEW_LEAF_CITY: number
        NONE: number
        OLA_EVENT_LAST_1: number
        OLA_EVENT_LAST_2: number
        OMEGA_SECTOR: number
        ORBIS: number
        ORBIS_STATION: number
        ORBIS_TOWER_BOTTOM: number
        ORIGIN_OF_CLOCKTOWER: number
        PALACE_OF_THE_MASTER: number
        PERION: number
        PILLAGE_OF_TREASURE_ISLAND_BASE: number
        RAIN_FOREST_EAST_OF_HENESYS: number
        RED_NOSE_PIRATE_DEN_2: number
        RESTORING_MEMORY_BASE: number
        RIEN: number
        ROUND_TABLE_OF_KENTAURUS_BASE: number
        SAHEL_2: number
        SHOWA_SPA_F: number
        SHOWA_SPA_M: number
        SHOWA_TOWN: number
        SINGAPORE: number
        SKY_FERRY: number
        SLEEPYWOOD: number
        SLEEPY_DUNGEON_4: number
        SOMEONE_ELSES_HOUSE: number
        SOUTHPERRY: number
        STARTING_MAP_NOBLESSE: number
        TEMPLE_OF_TIME: number
        WEDDING_EXIT: number
        WEDDING_PHOTO: number
        WITCH_TOWER_ENTRANCE: number
        //============ Functions  =============
        isBossRush(mapId: number): boolean
        isCygnusIntro(mapId: number): boolean
        isDojo(mapId: number): boolean
        isFishingArea(mapId: number): boolean
        isGodlyStatMap(mapId: number): boolean
        isMapleIsland(mapId: number): boolean
        isNettsPyramid(mapId: number): boolean
        isOlaOla(mapId: number): boolean
        isPartyDojo(mapId: number): boolean
        isPhysicalFitness(mapId: number): boolean
        isSelfLootableOnly(mapId: number): boolean
    }
    interface MapleMap {
        //============ Properties =============
        //============ Functions  =============
        addAllMonsterSpawn(monster: Monster, mobTime: number, team: number): void
        addGuardianSpawnPoint(a: GuardianSpawnPoint): void
        addMapObject(mapobject: MapObject): void
        addMapleArea(rec: Rectangle): void
        addMobSpawn(mobId: number, spendCP: number): void
        addMonsterSpawn(monster: Monster, mobTime: number, team: number): void
        addPartyMember(chr: Character, partyid: number): void
        addPlayer(chr: Character): void
        addPlayerNPCMapObject(pnpcobject: PlayerNPC): void
        addPlayerPuppet(player: Character): void
        addPortal(myPortal: Portal): void
        addSelfDestructive(mob: Monster): void
        addSkillId(z: number): void
        allowSummonState(b: boolean): void
        broadcastBalrogVictory(leaderName: string): void
        broadcastBossHpMessage(mm: Monster, bossHash: number, packet: Packet): void
        broadcastBossHpMessage(mm: Monster, bossHash: number, packet: Packet, rangedFrom: Point): void
        broadcastEnemyShip(state: boolean): void
        broadcastGMMessage(packet: Packet): void
        broadcastGMMessage(source: Character, packet: Packet, repeatToSource: boolean): void
        broadcastGMPacket(source: Character, packet: Packet): void
        broadcastGMSpawnPlayerMapObjectMessage(source: Character, player: Character, enteringField: boolean): void
        broadcastHorntailVictory(): void
        broadcastMessage(packet: Packet): void
        broadcastMessage(packet: Packet, rangedFrom: Point): void
        broadcastMessage(source: Character, packet: Packet, rangedFrom: Point): void
        broadcastMessage(source: Character, packet: Packet, repeatToSource: boolean): void
        broadcastMessage(source: Character, packet: Packet, repeatToSource: boolean, ranged: boolean): void
        broadcastNONGMMessage(source: Character, packet: Packet, repeatToSource: boolean): void
        broadcastNightEffect(): void
        broadcastPacket(source: Character, packet: Packet): void
        broadcastPinkBeanVictory(channel: number): void
        broadcastShip(state: boolean): void
        broadcastSpawnPlayerMapObjectMessage(source: Character, player: Character, enteringField: boolean): void
        broadcastStringMessage(type: number, message: string): void
        broadcastUpdateCharLookMessage(source: Character, player: Character): void
        broadcastZakumVictory(): void
        buffMonsters(team: number, skill: MCSkill): void
        calcDropPos(initial: Point, fallback: Point): Point
        canDeployDoor(pos: Point): boolean
        changeEnvironment(mapObj: string, newState: number): void
        checkMapOwnerActivity(): void
        claimOwnership(chr: Character): boolean
        clearBuffList(): void
        clearDrops(): void
        clearDrops(player: Character): void
        clearMapObjects(): void
        closeMapSpawnPoints(): void
        containsNPC(npcid: number): boolean
        countAlivePlayers(): number
        countBosses(): number
        countItems(): number
        countMonster(id: number): number
        countMonster(minid: number, maxid: number): number
        countMonsters(): number
        countPlayers(): number
        countReactors(): number
        damageMonster(chr: Character, monster: Monster, damage: number): boolean
        destroyNPC(npcid: number): void
        destroyReactor(oid: number): void
        destroyReactors(first: number, last: number): void
        disappearingItemDrop(dropper: MapObject, owner: Character, item: Item, pos: Point): void
        disappearingMesoDrop(meso: number, dropper: MapObject, owner: Character, pos: Point): void
        dismissRemoveAfter(monster: Monster): void
        dispose(): void
        dropFromFriendlyMonster(chr: Character, mob: Monster): void
        dropFromReactor(chr: Character, reactor: Reactor, drop: Item, dropPos: Point, questid: number): void
        dropItemsFromMonster(list: [], chr: Character, mob: Monster): void
        dropMessage(type: number, message: string): void
        eventStarted(): boolean
        findClosestPlayerSpawnpoint(from: Point): Portal
        findClosestPortal(from: Point): Portal
        findClosestSpawnpoint(from: Point): SpawnPoint
        findClosestTeleportPortal(from: Point): Portal
        findMarketPortal(): Portal
        generateMapDropRangeCache(): void
        getAggroCoordinator(): MonsterAggroCoordinator
        getAllMonsters(): []
        getAllPlayer(): []
        getAllPlayers(): []
        getAllReactors(): []
        getAnyCharacterFromParty(partyid: number): Character
        getArea(index: number): Rectangle
        getAreas(): []
        getBlueTeamBuffs(): []
        getChannelServer(): Channel
        getCharacterById(id: number): Character
        getCharacterByName(name: string): Character
        getCharacters(): []
        getCoconut(): Coconut
        getCurrentPartyId(): number
        getDeathCP(): number
        getDocked(): boolean
        getDoorPortal(doorid: number): Portal
        getDoorPositionStatus(pos: Point): Pair
        getDroppedItemCount(): number
        getDroppedItemsCountById(itemid: number): number
        getEnvironment(): JavaMap
        getEventInstance(): EventInstanceManager
        getEventNPC(): string
        getEverlast(): boolean
        getFieldLimit(): number
        getFootholds(): FootholdTree
        getForcedReturnId(): number
        getForcedReturnMap(): MapleMap
        getGroundBelow(pos: Point): Point
        getHPDec(): number
        getHPDecProtect(): number
        getId(): number
        getItems(): []
        getMapAllPlayers(): JavaMap
        getMapArea(): Rectangle
        getMapName(): string
        getMapObject(oid: number): MapObject
        getMapObjects(): []
        getMapObjectsInBox(box: Rectangle, types: []): []
        getMapObjectsInRange(from: Point, rangeSq: number, types: []): []
        getMapObjectsInRect(box: Rectangle, types: []): []
        getMapPlayers(): JavaMap
        getMaxMobs(): number
        getMaxReactors(): number
        getMobInterval(): number
        getMobsToSpawn(): []
        getMonsterById(id: number): Monster
        getMonsterByOid(oid: number): Monster
        getMonsters(): []
        getNPCById(id: number): NPC
        getNumPlayersInArea(index: number): number
        getNumPlayersInRect(rect: Rectangle): number
        getNumPlayersItemsInArea(index: number): number
        getNumPlayersItemsInRect(rect: Rectangle): number
        getOnFirstUserEnter(): string
        getOnUserEnter(): string
        getOx(): OxQuiz
        getPlayers(): []
        getPlayersInRange(box: Rectangle): []
        getPointBelow(pos: Point): Point
        getPortal(portalname: string): Portal
        getPortal(portalid: number): Portal
        getRandomGuardianSpawn(team: number): GuardianSpawnPoint
        getRandomPlayerSpawnpoint(): Portal
        getRandomSP(team: number): Point
        getReactorById(Id: number): Reactor
        getReactorByName(name: string): Reactor
        getReactorByOid(oid: number): Reactor
        getReactors(): []
        getReactorsByIdRange(first: number, last: number): []
        getRecovery(): number
        getRedTeamBuffs(): []
        getReturnMap(): MapleMap
        getReturnMapId(): number
        getRoundedCoordinate(angle: number): string
        getSeats(): number
        getSkillIds(): []
        getSnowball(team: number): Snowball
        getSpawnedMonstersOnMap(): number
        getStreetName(): string
        getSummonState(): boolean
        getTimeDefault(): number
        getTimeExpand(): number
        getTimeLeft(): number
        getTimeLimit(): number
        getTimeMob(): Pair
        getWorld(): number
        getWorldServer(): World
        hasClock(): boolean
        hasEventNPC(): boolean
        instanceMapFirstSpawn(difficulty: number, isPq: boolean): void
        instanceMapForceRespawn(): void
        instanceMapRespawn(): void
        isAllReactorState(reactorId: number, state: number): boolean
        isBlueCPQMap(): boolean
        isCPQLobby(): boolean
        isCPQLoserMap(): boolean
        isCPQMap(): boolean
        isCPQMap2(): boolean
        isCPQWinnerMap(): boolean
        isEventMap(): boolean
        isHorntailDefeated(): boolean
        isMuted(): boolean
        isOwnershipRestricted(chr: Character): boolean
        isOxQuiz(): boolean
        isPurpleCPQMap(): boolean
        isStartingEventMap(): boolean
        isTown(): boolean
        killAllMonsters(): void
        killAllMonstersNotFriendly(): void
        killFriendlies(mob: Monster): void
        killMonster(mobId: number): void
        killMonster(monster: Monster, chr: Character, withDrops: boolean): void
        killMonster(monster: Monster, chr: Character, withDrops: boolean, animation: number): void
        killMonsterWithDrops(mobId: number): void
        limitReactor(rid: number, num: number): void
        makeDisappearItemFromMap(mapobj: MapObject): boolean
        makeDisappearItemFromMap(mapitem: MapItem): boolean
        makeMonsterReal(monster: Monster): void
        mobMpRecovery(): void
        moveEnvironment(ms: string, type: number): void
        moveMonster(monster: Monster, reportedPos: Point): void
        movePlayer(player: Character, newPosition: Point): void
        pickItemDrop(pickupPacket: Packet, mdrop: MapItem): void
        registerCharacterStatUpdate(r: Runnable): void
        removeAllMonsterSpawn(mobId: number, x: number, y: number): void
        removeMapObject(num: number): void
        removeMapObject(obj: MapObject): void
        removeMonsterSpawn(mobId: number, x: number, y: number): void
        removeParty(partyid: number): void
        removePartyMember(chr: Character, partyid: number): void
        removePlayer(chr: Character): void
        removePlayerPuppet(player: Character): void
        removeSelfDestructive(mapobjectid: number): boolean
        reportMonsterSpawnPoints(chr: Character): void
        resetFully(): void
        resetMapObjects(): void
        resetMapObjects(difficulty: number, isPq: boolean): void
        resetPQ(): void
        resetPQ(difficulty: number): void
        resetReactors(): void
        resetReactors(list: []): void
        respawn(): void
        restoreMapSpawnPoints(): void
        runCharacterStatUpdate(): void
        searchItemReactors(react: Reactor): void
        sendNightEffect(chr: Character): void
        setAllowSpawnPointInBox(allow: boolean, box: Rectangle): void
        setAllowSpawnPointInRange(allow: boolean, from: Point, rangeSq: number): void
        setBackgroundTypes(backTypes: HashMap): void
        setBoat(hasBoat: boolean): void
        setClock(hasClock: boolean): void
        setCoconut(nut: Coconut): void
        setDeathCP(deathCP: number): void
        setDocked(isDocked: boolean): void
        setEventInstance(eim: EventInstanceManager): void
        setEventStarted(event: boolean): void
        setEverlast(everlast: boolean): void
        setFieldLimit(fieldLimit: number): void
        setFieldType(fieldType: number): void
        setFootholds(footholds: FootholdTree): void
        setForcedReturnMap(map: number): void
        setHPDec(delta: number): void
        setHPDecProtect(delta: number): void
        setMapLineBoundings(vrTop: number, vrBottom: number, vrLeft: number, vrRight: number): void
        setMapName(mapName: string): void
        setMapPointBoundings(px: number, py: number, h: number, w: number): void
        setMaxMobs(maxMobs: number): void
        setMaxReactors(maxReactors: number): void
        setMobCapacity(capacity: number): void
        setMobInterval(interval: number): void
        setMuted(mute: boolean): void
        setOnFirstUserEnter(onFirstUserEnter: string): void
        setOnUserEnter(onUserEnter: string): void
        setOx(set: OxQuiz): void
        setOxQuiz(b: boolean): void
        setReactorState(): void
        setRecovery(recRate: number): void
        setSeats(seats: number): void
        setSnowball(team: number, ball: Snowball): void
        setStreetName(streetName: string): void
        setTimeDefault(timeDefault: number): void
        setTimeExpand(timeExpand: number): void
        setTimeLimit(timeLimit: number): void
        setTimeMob(id: number, msg: string): void
        setTown(isTown: boolean): void
        shuffleReactors(): void
        shuffleReactors(list: []): void
        shuffleReactors(first: number, last: number): void
        softKillAllMonsters(): void
        spawnAllMonsterIdFromMapSpawnList(id: number): void
        spawnAllMonsterIdFromMapSpawnList(id: number, difficulty: number, isPq: boolean): void
        spawnAllMonstersFromMapSpawnList(): void
        spawnAllMonstersFromMapSpawnList(difficulty: number, isPq: boolean): void
        spawnCPQMonster(mob: Monster, pos: Point, team: number): void
        spawnDojoMonster(monster: Monster): void
        spawnDoor(door: DoorObject): void
        spawnFakeMonster(monster: Monster): void
        spawnFakeMonsterOnGroundBelow(mob: Monster, pos: Point): void
        spawnGuardian(team: number, num: number): number
        spawnHorntailOnGroundBelow(targetPoint: Point): void
        spawnItemDrop(dropper: MapObject, owner: Character, item: Item, pos: Point, ffaDrop: boolean, playerDrop: boolean): void
        spawnItemDrop(dropper: MapObject, owner: Character, item: Item, pos: Point, dropType: number, playerDrop: boolean): void
        spawnItemDropList(list: [], dropper: MapObject, owner: Character, pos: Point): void
        spawnItemDropList(list: [], minCopies: number, maxCopies: number, dropper: MapObject, owner: Character, pos: Point): void
        spawnItemDropList(list: [], minCopies: number, maxCopies: number, dropper: MapObject, owner: Character, pos: Point, ffaDrop: boolean, playerDrop: boolean): void
        spawnKite(kite: Kite): void
        spawnMesoDrop(meso: number, position: Point, dropper: MapObject, owner: Character, playerDrop: boolean, droptype: number): void
        spawnMist(mist: Mist, duration: number, poison: boolean, fake: boolean, recovery: boolean): void
        spawnMonster(monster: Monster): void
        spawnMonster(monster: Monster, difficulty: number, isPq: boolean): void
        spawnMonsterOnGroundBelow(mob: Monster, pos: Point): void
        spawnMonsterOnGroundBelow(id: number, x: number, y: number): void
        spawnMonsterWithEffect(monster: Monster, effect: number, pos: Point): void
        spawnReactor(reactor: Reactor): void
        spawnRevives(monster: Monster): void
        spawnSummon(summon: Summon): void
        startEvent(): void
        startEvent(chr: Character): void
        startMapEffect(msg: string, itemId: number): void
        startMapEffect(msg: string, itemId: number, time: number): void
        toggleDrops(): void
        toggleEnvironment(ms: string): void
        toggleHiddenNPC(id: number): void
        unclaimOwnership(): Character
        unclaimOwnership(chr: Character): boolean
        updatePartyItemDropsToNewcomer(newcomer: Character, partyItems: []): void
        updatePlayerItemDropsToParty(partyid: number, charid: number, partyMembers: [], partyLeaver: Character): []
        warpEveryone(to: number): void
        warpEveryone(to: number, pto: number): void
        warpOutByTeam(team: number, mapid: number): void
    }
    interface MobId {
        //============ Properties =============
        ANGRY_SCARLION: number
        ANGRY_TARGA: number
        ANNOYED_ZOMBIE_MUSHROOM: number
        ARPQ_BOMB: number
        ARPQ_SCORPION: number
        BLOODY_BOOM: number
        DEAD_HORNTAIL_MAX: number
        DEAD_HORNTAIL_MIN: number
        DEJECTED_GREEN_MUSHROOM: number
        DELLI: number
        FAUST_DOJO: number
        FURIOUS_SCARLION: number
        FURIOUS_TARGA: number
        GHOST: number
        GHOST_STUMP: number
        GHOST_STUMP_QUEST: number
        GIANT_CAKE: number
        GIANT_SNOWMAN_LV1_EASY: number
        GIANT_SNOWMAN_LV1_HARD: number
        GIANT_SNOWMAN_LV1_MEDIUM: number
        GIANT_SNOWMAN_LV5_EASY: number
        GIANT_SNOWMAN_LV5_HARD: number
        GIANT_SNOWMAN_LV5_MEDIUM: number
        GREEN_MUSHROOM: number
        GREEN_MUSHROOM_QUEST: number
        HIGH_DARKSTAR: number
        HORNTAIL: number
        HORNTAIL_HAND_LEFT: number
        HORNTAIL_HAND_RIGHT: number
        HORNTAIL_HEAD_A: number
        HORNTAIL_HEAD_B: number
        HORNTAIL_HEAD_C: number
        HORNTAIL_LEGS: number
        HORNTAIL_PREHEAD_LEFT: number
        HORNTAIL_PREHEAD_RIGHT: number
        HORNTAIL_TAIL: number
        HORNTAIL_WINGS: number
        JULIET: number
        KING_SLIME_DOJO: number
        LOST_RUDOLPH: number
        LOW_DARKSTAR: number
        MOON_BUNNY: number
        MUSHMOM_DOJO: number
        PAPULATUS_CLOCK: number
        PIANUS_R: number
        PINK_BEAN: number
        POISON_FLOWER: number
        P_JUNIOR: number
        ROMEO: number
        SCARLION: number
        SCARLION_STATUE: number
        SMIRKING_GHOST_STUMP: number
        SUMMON_HORNTAIL: number
        TAMABLE_HOG: number
        TARGA: number
        TARGA_STATUE: number
        TRANSPARENT_ITEM: number
        TYLUS: number
        WATCH_HOG: number
        ZAKUM_1: number
        ZAKUM_2: number
        ZAKUM_3: number
        ZAKUM_ARM_1: number
        ZAKUM_ARM_2: number
        ZAKUM_ARM_3: number
        ZAKUM_ARM_4: number
        ZAKUM_ARM_5: number
        ZAKUM_ARM_6: number
        ZAKUM_ARM_7: number
        ZAKUM_ARM_8: number
        ZOMBIE_MUSHROOM: number
        ZOMBIE_MUSHROOM_QUEST: number
        //============ Functions  =============
        isDeadHorntailPart(mobId: number): boolean
        isDojoBoss(mobId: number): boolean
        isZakumArm(mobId: number): boolean
    }
    interface NPC {
        //============ Properties =============
        IDLE_MOVEMENT_PACKET_LENGTH: number
        //============ Functions  =============
        getCy(): number
        getF(): number
        getFh(): number
        getId(): number
        getIdleMovement(): InPacket
        getName(): string
        getObjectId(): number
        getPosition(): Point
        getRx0(): number
        getRx1(): number
        getStance(): number
        getStartFh(): number
        getType(): MapObjectType
        hasShop(): boolean
        isFacingLeft(): boolean
        isHidden(): boolean
        nullifyPosition(): void
        sendDestroyData(client: Client): void
        sendShop(c: Client): void
        sendSpawnData(client: Client): void
        setCy(cy: number): void
        setF(f: number): void
        setFh(fh: number): void
        setHide(hide: boolean): void
        setObjectId(id: number): void
        setPosition(position: Point): void
        setRx0(rx0: number): void
        setRx1(rx1: number): void
        setStance(stance: number): void
    }
    interface NpcId {
        //============ Properties =============
        BEI_DOU_NPC_BASE: number
        BILLY: number
        CUSTOM_DEV: number
        DIMENSIONAL_MIRROR: number
        DUEY: number
        FREDRICK: number
        GACHAPON_ELLINIA: number
        GACHAPON_EL_NATH: number
        GACHAPON_HENESYS: number
        GACHAPON_KERNING: number
        GACHAPON_LUDIBRIUM: number
        GACHAPON_MAX: number
        GACHAPON_MIN: number
        GACHAPON_MUSHROOM_SHRINE: number
        GACHAPON_NAUTILUS: number
        GACHAPON_NLC: number
        GACHAPON_PERION: number
        GACHAPON_SHOWA_FEMALE: number
        GACHAPON_SHOWA_MALE: number
        GACHAPON_SLEEPYWOOD: number
        GRANDPA_MOON_BUNNY: number
        HERACLE: number
        LILIN: number
        MAPLE_ADMINISTRATOR: number
        MAR_THE_FAIRY: number
        MIMO: number
        PLAYER_NPC_BASE: number
        RPS_ADMIN: number
        SPINEL: number
        STEWARD: number
        TEMPLE_KEEPER: number
        //============ Functions  =============
    }
    interface PacketCreator {
        //============ Properties =============
        EMPTY_STATUPDATE: []
        ZERO_TIME: number
        //============ Functions  =============
        CPQMessage(message: number): Packet
        CPUpdate(party: boolean, curCP: number, totalCP: number, team: number): Packet
        MTSConfirmBuy(): Packet
        MTSConfirmSell(): Packet
        MTSConfirmTransfer(quantity: number, pos: number): Packet
        MTSFailBuy(): Packet
        MTSWantedListingOver(nx: number, items: number): Packet
        MobDamageMobFriendly(mob: Monster, damage: number, remainingHp: number): Packet
        OnAskQuiz(nSpeakerTypeID: number, nSpeakerTemplateID: number, nResCode: number, sTitle: string, sProblemText: string, sHintText: string, nMinInput: number, nMaxInput: number, tRemainInitialQuiz: number): Packet
        OnAskSpeedQuiz(nSpeakerTypeID: number, nSpeakerTemplateID: number, nResCode: number, nType: number, dwAnswer: number, nCorrect: number, nRemain: number, tRemainInitialQuiz: number): Packet
        OnCoupleMessage(fiance: string, text: string, spouse: boolean): Packet
        QuickslotMappedInit(pQuickslot: QuickslotBinding): Packet
        UseTreasureBox(type: number): Packet
        addCard(full: boolean, cardid: number, level: number): Packet
        addCashItemInformation(p: OutPacket, item: Item, accountId: number): void
        addCashItemInformation(p: OutPacket, item: Item, accountId: number, giftMessage: string): void
        addMatchCardBox(chr: Character, amount: number, type: number): Packet
        addMessengerPlayer(from: string, chr: Character, position: number, channel: number): Packet
        addNewCharEntry(chr: Character): Packet
        addOmokBox(chr: Character, amount: number, type: number): Packet
        addQuestTimeLimit(quest: number, time: number): Packet
        applyMonsterStatus(oid: number, mse: MonsterStatusEffect, reflection: []): Packet
        aranGodlyStats(): Packet
        arrangeStorage(slots: number, items: []): Packet
        blockedMessage(type: number): Packet
        blockedMessage2(type: number): Packet
        boatPacket(type: boolean): Packet
        buddylistMessage(message: number): Packet
        bunnyPacket(): Packet
        byeAvatarMega(): Packet
        cancelBuff(statups: []): Packet
        cancelChair(id: number): Packet
        cancelDebuff(mask: number): Packet
        cancelFamilyBuff(): Packet
        cancelForeignBuff(chrId: number, statups: []): Packet
        cancelForeignChairSkillEffect(chrId: number): Packet
        cancelForeignDebuff(cid: number, mask: number): Packet
        cancelForeignFirstDebuff(cid: number, mask: number): Packet
        cancelForeignSlowDebuff(chrId: number): Packet
        cancelMonsterStatus(oid: number, stats: JavaMap): Packet
        catchMessage(message: number): Packet
        catchMonster(mobOid: number, success: number): Packet
        catchMonster(mobOid: number, itemid: number, success: number): Packet
        changeBackgroundEffect(remove: boolean, layer: number, transition: number): Packet
        changeCover(cardid: number): Packet
        changePetName(chr: Character, newname: string, slot: number): Packet
        charInfo(chr: Character): Packet
        charNameResponse(charname: string, nameUsed: boolean): Packet
        closeRangeAttack(chr: Character, skill: number, skilllevel: number, stance: number, numAttackedAndDamage: number, damage: JavaMap, speed: number, direction: number, display: number): Packet
        coconutScore(team1: number, team2: number): Packet
        commandResponse(cid: number, index: number, talk: boolean, animation: number, balloonType: boolean): Packet
        completeQuest(quest: number, time: number): Packet
        controlMonster(life: Monster, newSpawn: boolean, aggro: boolean): Packet
        crogBoatPacket(type: boolean): Packet
        customPacket(packet: number[]): Packet
        customPacket(packet: string): Packet
        customShowBossHP(call: number, oid: number, currHP: number, maxHP: number, tagColor: number, tagBgColor: number): Packet
        damageMonster(oid: number, damage: number): Packet
        damagePlayer(skill: number, monsteridfrom: number, cid: number, damage: number, fake: number, direction: number, pgmr: boolean, pgmr_1: number, is_pg: boolean, oid: number, pos_x: number, pos_y: number): Packet
        damageSummon(cid: number, oid: number, damage: number, monsterIdFrom: number): Packet
        deleteCashItem(item: Item): Packet
        deleteCharResponse(cid: number, state: number): Packet
        destroyReactor(reactor: Reactor): Packet
        disableMinimap(): Packet
        disableUI(enable: boolean): Packet
        dojoWarpUp(): Packet
        dropItemFromMapObject(player: Character, drop: MapItem, dropfrom: Point, dropto: Point, mod: number): Packet
        earnTitleMessage(msg: string): Packet
        enableActions(): Packet
        enableCSUse(mc: Character): Packet
        enableReport(): Packet
        enableTV(): Packet
        environmentChange(env: string, mode: number): Packet
        environmentMove(env: string, mode: number): Packet
        environmentMoveList(envList: []): Packet
        environmentMoveReset(): Packet
        facialExpression(from: Character, expression: number): Packet
        familyBuff(type: number, buffnr: number, amount: number, time: number): Packet
        findMerchantResponse(map: boolean, extra: number): Packet
        finishedSort(inv: number): Packet
        finishedSort2(inv: number): Packet
        forfeitQuest(quest: number): Packet
        fredrickMessage(operation: number): Packet
        gachaponMessage(item: Item, town: string, player: Character): Packet
        getAfterLoginError(reason: number): Packet
        getAuthSuccess(c: Client): Packet
        getAvatarMega(chr: Character, medal: string, channel: number, itemId: number, message: [], ear: boolean): Packet
        getChannelChange(inetAddr: InetAddress, port: number): Packet
        getCharInfo(chr: Character): Packet
        getCharList(c: Client, serverId: number, status: number): Packet
        getChatText(cidfrom: number, text: string, gm: boolean, show: number): Packet
        getClock(time: Number): Packet
        getClockTime(hour: number, min: number, sec: number): Packet
        getDimensionalMirror(talk: string): Packet
        getDojoInfo(info: string): Packet
        getDojoInfoMessage(message: string): Packet
        getEndOfServerList(): Packet
        getEnergy(info: string, amount: number): Packet
        getFamilyInfo(f: FamilyEntry): Packet
        getFindResult(target: Character, type: number, fieldOrChannel: number, flag: number): Packet
        getFredrick(op: number): Packet
        getFredrick(chr: Character): Packet
        getGMEffect(type: number, mode: number): Packet
        getGPMessage(gpChange: number): Packet
        getHello(mapleVersion: number, sendIv: InitializationVector, recvIv: InitializationVector): Packet
        getHiredMerchant(chr: Character, hm: HiredMerchant, firstTime: boolean): Packet
        getInventoryFull(): Packet
        getItemMessage(itemid: number): Packet
        getKeymap(keybindings: JavaMap): Packet
        getLoginFailed(reason: number): Packet
        getMacros(macros: SkillMacro[]): Packet
        getMatchCard(c: Client, minigame: MiniGame, owner: boolean, piece: number): Packet
        getMatchCardNewVisitor(minigame: MiniGame, chr: Character, slot: number): Packet
        getMatchCardSelect(game: MiniGame, turn: number, slot: number, firstslot: number, type: number): Packet
        getMatchCardStart(game: MiniGame, loser: number): Packet
        getMiniGame(c: Client, minigame: MiniGame, owner: boolean, piece: number): Packet
        getMiniGameClose(visitor: boolean, type: number): Packet
        getMiniGameDenyTie(game: MiniGame): Packet
        getMiniGameMoveOmok(game: MiniGame, move1: number, move2: number, move3: number): Packet
        getMiniGameNewVisitor(minigame: MiniGame, chr: Character, slot: number): Packet
        getMiniGameOwnerWin(game: MiniGame, forfeit: boolean): Packet
        getMiniGameReady(game: MiniGame): Packet
        getMiniGameRemoveVisitor(): Packet
        getMiniGameRequestTie(game: MiniGame): Packet
        getMiniGameSkipOwner(game: MiniGame): Packet
        getMiniGameSkipVisitor(game: MiniGame): Packet
        getMiniGameStart(game: MiniGame, loser: number): Packet
        getMiniGameTie(game: MiniGame): Packet
        getMiniGameUnReady(game: MiniGame): Packet
        getMiniGameVisitorWin(game: MiniGame, forfeit: boolean): Packet
        getMiniRoomError(status: number): Packet
        getMultiMegaphone(messages: string[], channel: number, showEar: boolean): Packet
        getNPCShop(c: Client, sid: number, items: []): Packet
        getNPCTalk(npc: number, msgType: number, talk: string, endBytes: string, speaker: number): Packet
        getNPCTalkNum(npc: number, talk: string, def: number, min: number, max: number): Packet
        getNPCTalkNum(npc: number, talk: string, def: number, min: number, max: number, speaker: number): Packet
        getNPCTalkStyle(npc: number, talk: string, styles: number[]): Packet
        getNPCTalkText(npc: number, talk: string, def: string): Packet
        getNPCTalkText(npc: number, talk: string, def: string, speaker: number): Packet
        getOwlMessage(msg: number): Packet
        getOwlOpen(owlLeaderboards: []): Packet
        getPermBan(reason: number): Packet
        getPing(): Packet
        getPlayerNPC(npc: PlayerNPC): Packet
        getPlayerShop(shop: PlayerShop, owner: boolean): Packet
        getPlayerShopChat(chr: Character, chat: string, slot: number): Packet
        getPlayerShopChat(chr: Character, chat: string, owner: boolean): Packet
        getPlayerShopItemUpdate(shop: PlayerShop): Packet
        getPlayerShopNewVisitor(chr: Character, slot: number): Packet
        getPlayerShopOwnerUpdate(item: SoldItem, position: number): Packet
        getPlayerShopRemoveVisitor(slot: number): Packet
        getRelogResponse(): Packet
        getScrollEffect(chr: number, scrollSuccess: ScrollResult, legendarySpirit: boolean, whiteScroll: boolean): Packet
        getSeniorMessage(name: string): Packet
        getServerIP(inetAddr: InetAddress, port: number, clientId: number): Packet
        getServerList(serverId: number, serverName: string, flag: number, eventmsg: string, channelLoad: []): Packet
        getServerStatus(status: number): Packet
        getShowExpGain(gain: number, equip: number, party: number, inChat: boolean, white: boolean): Packet
        getShowFameGain(gain: number): Packet
        getShowInventoryFull(): Packet
        getShowInventoryStatus(mode: number): Packet
        getShowItemGain(itemId: number, quantity: number): Packet
        getShowItemGain(itemId: number, quantity: number, inChat: boolean): Packet
        getShowMesoGain(gain: number): Packet
        getShowMesoGain(gain: number, inChat: boolean): Packet
        getShowQuestCompletion(id: number): Packet
        getStorage(npcId: number, slots: number, items: [], meso: number): Packet
        getStorageError(i: number): Packet
        getTempBan(timestampTill: number, reason: number): Packet
        getTime(utcTimestamp: number): number
        getTradeChat(chr: Character, chat: string, owner: boolean): Packet
        getTradeConfirmation(): Packet
        getTradeItemAdd(number: number, item: Item): Packet
        getTradeMesoSet(number: number, meso: number): Packet
        getTradePartnerAdd(chr: Character): Packet
        getTradeResult(number: number, operation: number): Packet
        getTradeStart(c: Client, trade: Trade, number: number): Packet
        getWarpToMap(to: MapleMap, spawnPoint: number, chr: Character): Packet
        getWarpToMap(to: MapleMap, spawnPoint: number, spawnPosition: Point, chr: Character): Packet
        getWhisperReceive(sender: string, channel: number, fromAdmin: boolean, message: string): Packet
        getWhisperResult(target: string, success: boolean): Packet
        giveBuff(buffid: number, bufflength: number, statups: []): Packet
        giveDebuff(statups: [], skill: MobSkill): Packet
        giveFameErrorResponse(status: number): Packet
        giveFameResponse(mode: number, charname: string, newfame: number): Packet
        giveFinalAttack(skillid: number, time: number): Packet
        giveForeignBuff(chrId: number, statups: []): Packet
        giveForeignChairSkillEffect(cid: number): Packet
        giveForeignDebuff(chrId: number, statups: [], skill: MobSkill): Packet
        giveForeignPirateBuff(cid: number, buffid: number, time: number, statups: []): Packet
        giveForeignSlowDebuff(chrId: number, statups: [], skill: MobSkill): Packet
        giveForeignWKChargeEffect(cid: number, buffid: number, statups: []): Packet
        givePirateBuff(statups: [], buffid: number, duration: number): Packet
        guideHint(hint: number): Packet
        healMonster(oid: number, heal: number, curhp: number, maxhp: number): Packet
        hiredMerchantBox(): Packet
        hiredMerchantChat(message: string, slot: number): Packet
        hiredMerchantMaintenanceMessage(): Packet
        hiredMerchantOwnerLeave(): Packet
        hiredMerchantOwnerMaintenanceLeave(): Packet
        hiredMerchantVisitorAdd(chr: Character, slot: number): Packet
        hiredMerchantVisitorLeave(slot: number): Packet
        hitCoconut(spawn: boolean, id: number, type: number): Packet
        hitSnowBall(what: number, damage: number): Packet
        hpqMessage(text: string): Packet
        incubatorResult(): Packet
        itemEffect(characterid: number, itemid: number): Packet
        itemExpired(itemid: number): Packet
        itemMegaphone(msg: string, whisper: boolean, channel: number, item: Item): Packet
        jobMessage(type: number, job: number, charname: string): Packet
        joinMessenger(position: number): Packet
        killMonster(objId: number, animation: number): Packet
        killMonster(objId: number, animation: boolean): Packet
        leaveHiredMerchant(slot: number, status2: number): Packet
        leftKnockBack(): Packet
        levelUpMessage(type: number, level: number, charname: string): Packet
        loadExceptionList(cid: number, petId: number, petIdx: number, data: []): Packet
        loadFamily(player: Character): Packet
        lockUI(enable: boolean): Packet
        magicAttack(chr: Character, skill: number, skilllevel: number, stance: number, numAttackedAndDamage: number, damage: JavaMap, charge: number, speed: number, direction: number, display: number): Packet
        makeMonsterInvisible(life: Monster): Packet
        makeMonsterReal(life: Monster): Packet
        makerEnableActions(): Packet
        makerResult(success: boolean, itemMade: number, itemCount: number, mesos: number, itemsLost: [], catalystID: number, INCBuffGems: []): Packet
        makerResultCrystal(itemIdGained: number, itemIdLost: number): Packet
        makerResultDesynth(itemId: number, mesos: number, itemsGained: []): Packet
        mapEffect(path: string): Packet
        mapSound(path: string): Packet
        marriageMessage(type: number, charname: string): Packet
        mesoStorage(slots: number, meso: number): Packet
        messengerChat(text: string): Packet
        messengerInvite(from: string, messengerid: number): Packet
        messengerNote(text: string, mode: number, mode2: number): Packet
        modifyInventory(updateTick: boolean, mods: []): Packet
        moveDragon(dragon: Dragon, startPos: Point, movementPacket: InPacket, movementDataLength: number): Packet
        moveMonster(oid: number, skillPossible: boolean, skill: number, skillId: number, skillLevel: number, pOption: number, startPos: Point, movementPacket: InPacket, movementDataLength: number): Packet
        moveMonsterResponse(objectid: number, moveid: number, currentMp: number, useSkills: boolean): Packet
        moveMonsterResponse(objectid: number, moveid: number, currentMp: number, useSkills: boolean, skillId: number, skillLevel: number): Packet
        movePet(cid: number, pid: number, slot: number, moves: []): Packet
        movePlayer(chrId: number, movementPacket: InPacket, movementDataLength: number): Packet
        moveSummon(cid: number, oid: number, startPos: Point, movementPacket: InPacket, movementDataLength: number): Packet
        multiChat(name: string, chattext: string, mode: number): Packet
        musicChange(song: string): Packet
        notYetSoldInv(items: []): Packet
        noteError(error: number): Packet
        onCashGachaponOpenSuccess(accountid: number, boxCashId: number, remainingBoxes: number, reward: Item, rewardItemId: number, rewardQuantity: number, bJackpot: boolean): Packet
        onCashItemGachaponOpenFailed(): Packet
        onNewYearCardRes(user: Character, cardId: number, mode: number, msg: number): Packet
        onNewYearCardRes(user: Character, newyear: NewYearCardRecord, mode: number, msg: number): Packet
        onNotifyHPDecByField(change: number): Packet
        openCashShop(c: Client, mts: boolean): Packet
        openRPSNPC(): Packet
        openUI(ui: number): Packet
        owlOfMinerva(c: Client, itemId: number, hmsAvailable: []): Packet
        partyCreated(party: Party, partycharid: number): Packet
        partyInvite(from: Character): Packet
        partyPortal(townId: number, targetId: number, position: Point): Packet
        partySearchInvite(from: Character): Packet
        partyStatusMessage(message: number): Packet
        partyStatusMessage(message: number, charname: string): Packet
        petChat(cid: number, index: number, act: number, text: string): Packet
        petFoodResponse(cid: number, index: number, success: boolean, balloonType: boolean): Packet
        petStatUpdate(chr: Character): Packet
        pinAccepted(): Packet
        pinRegistered(): Packet
        playPortalSound(): Packet
        playSound(sound: string): Packet
        playerDiedMessage(name: string, lostCP: number, team: number): Packet
        playerSummoned(name: string, tab: number, number: number): Packet
        putIntoCashInventory(item: Item, accountId: number): Packet
        pyramidGauge(gauge: number): Packet
        pyramidScore(score: number, exp: number): Packet
        questError(quest: number): Packet
        questExpire(quest: number): Packet
        questFailure(type: number): Packet
        rangedAttack(chr: Character, skill: number, skilllevel: number, stance: number, numAttackedAndDamage: number, projectile: number, damage: JavaMap, speed: number, direction: number, display: number): Packet
        receiveFame(mode: number, charnameFrom: string): Packet
        refundCashItem(item: Item, maplePoints: number): Packet
        registerPin(): Packet
        remoteChannelChange(ch: number): Packet
        removeClock(): Packet
        removeDoor(ownerId: number, town: boolean): Packet
        removeDragon(chrId: number): Packet
        removeHiredMerchantBox(id: number): Packet
        removeItemFromDuey(remove: boolean, Package: number): Packet
        removeItemFromMap(objId: number, animation: number, chrId: number): Packet
        removeItemFromMap(objId: number, animation: number, chrId: number, pet: boolean, slot: number): Packet
        removeKite(objId: number, animationType: number): Packet
        removeMapEffect(): Packet
        removeMessengerPlayer(position: number): Packet
        removeMinigameBox(chr: Character): Packet
        removeMist(objId: number): Packet
        removeMonsterInvisibility(life: Monster): Packet
        removeNPC(objId: number): Packet
        removeNPCController(objId: number): Packet
        removePlayerFromMap(chrId: number): Packet
        removePlayerNPC(oid: number): Packet
        removePlayerShopBox(shop: PlayerShop): Packet
        removeQuestTimeLimit(quest: number): Packet
        removeSummon(summon: Summon, animated: boolean): Packet
        removeTV(): Packet
        reportResponse(mode: number): Packet
        requestBuddylistAdd(chrIdFrom: number, chrId: number, nameFrom: string): Packet
        requestPin(): Packet
        requestPinAfterFailure(): Packet
        resetForcedStats(): Packet
        retrieveFirstMessage(): Packet
        rollSnowBall(entermap: boolean, state: number, ball0: Snowball, ball1: Snowball): Packet
        rpsMesoError(mesos: number): Packet
        rpsMode(mode: number): Packet
        rpsSelection(selection: number, answer: number): Packet
        selectWorld(world: number): Packet
        sendAutoHpPot(itemId: number): Packet
        sendAutoMpPot(itemId: number): Packet
        sendCannotSpawnKite(): Packet
        sendDojoAnimation(firstByte: number, animation: string): Packet
        sendDuey(operation: number, packages: []): Packet
        sendDueyMSG(operation: number): Packet
        sendDueyParcelNotification(quick: boolean): Packet
        sendDueyParcelReceived(from: string, quick: boolean): Packet
        sendFamilyInvite(playerId: number, inviter: string): Packet
        sendFamilyJoinResponse(accepted: boolean, added: string): Packet
        sendFamilyLoginNotice(name: string, loggedIn: boolean): Packet
        sendFamilyMessage(type: number, mesos: number): Packet
        sendFamilySummonRequest(familyName: string, from: string): Packet
        sendGainRep(gain: number, from: string): Packet
        sendGuestTOS(): Packet
        sendHammerData(hammerUsed: number): Packet
        sendHammerMessage(): Packet
        sendHint(hint: string, width: number, height: number): Packet
        sendMTS(items: [], tab: number, type: number, page: number, pages: number): Packet
        sendMapleLifeCharacterInfo(): Packet
        sendMapleLifeError(code: number): Packet
        sendMapleLifeNameError(): Packet
        sendMesoLimit(): Packet
        sendNameTransferCheck(availableName: string, canUseName: boolean): Packet
        sendNameTransferRules(error: number): Packet
        sendPolice(): Packet
        sendPolice(text: string): Packet
        sendRecommended(worlds: []): Packet
        sendTV(chr: Character, messages: [], type: number, partner: Character): Packet
        sendVegaScroll(op: number): Packet
        sendWorldTransferRules(error: number, c: Client): Packet
        sendYellowTip(tip: string): Packet
        serverMessage(message: string): Packet
        serverNotice(type: number, message: string): Packet
        serverNotice(type: number, channel: number, message: string): Packet
        serverNotice(type: number, message: string, npc: number): Packet
        serverNotice(type: number, channel: number, message: string, smegaEar: boolean): Packet
        setExtraPendantSlot(toggleExtraSlot: boolean): Packet
        setNPCScriptable(scriptableNpcIds: JavaMap): Packet
        sheepRanchClothes(id: number, clothes: number): Packet
        sheepRanchInfo(wolf: number, sheep: number): Packet
        shopErrorMessage(error: number, type: number): Packet
        shopTransaction(code: number): Packet
        showAllCharacter(totalWorlds: number, totalChrs: number): Packet
        showAllCharacterInfo(worldid: number, chars: [], usePic: boolean): Packet
        showAriantScoreBoard(): Packet
        showBerserk(chrId: number, skillLv: number, berserk: boolean): Packet
        showBossHP(oid: number, currHP: number, maxHP: number, tagColor: number, tagBgColor: number): Packet
        showBoughtCashItem(item: Item, accountId: number): Packet
        showBoughtCashPackage(cashPackage: [], accountId: number): Packet
        showBoughtCashRing(ring: Item, recipient: string, accountId: number): Packet
        showBoughtCharacterSlot(slots: number): Packet
        showBoughtInventorySlots(type: number, slots: number): Packet
        showBoughtQuestItem(itemId: number): Packet
        showBoughtStorageSlots(slots: number): Packet
        showBuffEffect(chrId: number, skillId: number, effectId: number): Packet
        showBuffEffect(chrId: number, skillId: number, effectId: number, direction: number): Packet
        showBuffEffect(chrId: number, skillId: number, skillLv: number, effectId: number, direction: number): Packet
        showCash(mc: Character): Packet
        showCashInventory(c: Client): Packet
        showCashShopMessage(message: number): Packet
        showChair(characterid: number, itemid: number): Packet
        showCombo(count: number): Packet
        showCouponRedeemedItems(accountId: number, maplePoints: number, mesos: number, cashItems: [], items: []): Packet
        showEffect(effect: string): Packet
        showEquipmentLevelUp(): Packet
        showEventInstructions(): Packet
        showForcedEquip(team: number): Packet
        showForeignCardEffect(id: number): Packet
        showForeignEffect(effect: number): Packet
        showForeignEffect(chrId: number, effect: number): Packet
        showForeignInfo(cid: number, path: string): Packet
        showForeignMakerEffect(cid: number, makerSucceeded: boolean): Packet
        showGainCard(): Packet
        showGiftSucceed(to: string, item: ModifiedCashItemDO): Packet
        showGifts(gifts: []): Packet
        showHpHealed(cid: number, amount: number): Packet
        showInfo(path: string): Packet
        showInfoText(text: string): Packet
        showIntro(path: string): Packet
        showItemLevelup(): Packet
        showItemUnavailable(): Packet
        showMTSCash(chr: Character): Packet
        showMakerEffect(makerSucceeded: boolean): Packet
        showMonsterBookPickup(): Packet
        showMonsterHP(oid: number, remhppercentage: number): Packet
        showMonsterRiding(cid: number, mount: Mount): Packet
        showNameChangeCancel(success: boolean): Packet
        showNameChangeSuccess(item: Item, accountId: number): Packet
        showOXQuiz(questionSet: number, questionId: number, askQuestion: boolean): Packet
        showOwnBerserk(skilllevel: number, Berserk: boolean): Packet
        showOwnBuffEffect(skillId: number, effectId: number): Packet
        showOwnPetLevelUp(index: number): Packet
        showOwnRecovery(heal: number): Packet
        showPedigree(entry: FamilyEntry): Packet
        showPet(chr: Character, pet: Pet, remove: boolean, hunger: boolean): Packet
        showPetLevelUp(chr: Character, index: number): Packet
        showRecovery(chrId: number, amount: number): Packet
        showSpecialEffect(effect: number): Packet
        showWheelsLeft(left: number): Packet
        showWishList(mc: Character, update: boolean): Packet
        showWorldTransferCancel(success: boolean): Packet
        showWorldTransferSuccess(item: Item, accountId: number): Packet
        silentRemoveItemFromMap(objId: number): Packet
        skillBookResult(chr: Character, skillid: number, maxlevel: number, canuse: boolean, success: boolean): Packet
        skillCancel(from: Character, skillId: number): Packet
        skillCooldown(sid: number, time: number): Packet
        skillEffect(from: Character, skillId: number, level: number, flags: number, speed: number, direction: number): Packet
        snowballMessage(team: number, message: number): Packet
        spawnDoor(ownerid: number, pos: Point, launched: boolean): Packet
        spawnDragon(dragon: Dragon): Packet
        spawnFakeMonster(life: Monster, effect: number): Packet
        spawnGuide(spawn: boolean): Packet
        spawnHiredMerchantBox(hm: HiredMerchant): Packet
        spawnKite(objId: number, itemId: number, name: string, msg: string, pos: Point, ft: number): Packet
        spawnMist(objId: number, ownerId: number, skill: number, level: number, mist: Mist): Packet
        spawnMobMist(objId: number, ownerMobId: number, msId: MobSkillId, mist: Mist): Packet
        spawnMonster(life: Monster, newSpawn: boolean): Packet
        spawnMonster(life: Monster, newSpawn: boolean, effect: number): Packet
        spawnNPC(life: NPC): Packet
        spawnNPCRequestController(life: NPC, miniMap: boolean): Packet
        spawnPlayerMapObject(target: Client, chr: Character, enteringField: boolean): Packet
        spawnPlayerNPC(npc: PlayerNPC): Packet
        spawnPortal(townId: number, targetId: number, pos: Point): Packet
        spawnReactor(reactor: Reactor): Packet
        spawnSummon(summon: Summon, animated: boolean): Packet
        startMapEffect(msg: string, itemId: number, active: boolean): Packet
        startMonsterCarnival(chr: Character, team: number, opposition: number): Packet
        stopControllingMonster(oid: number): Packet
        storeStorage(slots: number, type: InventoryType, items: []): Packet
        summonAttack(cid: number, summonOid: number, direction: number, allDamage: []): Packet
        summonSkill(cid: number, summonSkillId: number, newStance: number): Packet
        takeFromCashInventory(item: Item): Packet
        takeOutStorage(slots: number, type: InventoryType, items: []): Packet
        talkGuide(talk: string): Packet
        throwGrenade(cid: number, pos: Point, keyDown: number, skillId: number, skillLevel: number): Packet
        tradeInvite(chr: Character): Packet
        transferInventory(items: []): Packet
        trembleEffect(type: number, delay: number): Packet
        triggerReactor(reactor: Reactor, stance: number): Packet
        trockRefreshMapList(chr: Character, delete0: boolean, vip: boolean): Packet
        updateAreaInfo(area: number, info: string): Packet
        updateAriantPQRanking(playerScore: JavaMap): Packet
        updateAriantPQRanking(chr: Character, score: number): Packet
        updateBuddyCapacity(capacity: number): Packet
        updateBuddyChannel(characterid: number, channel: number): Packet
        updateBuddylist(buddylist: []): Packet
        updateCharLook(target: Client, chr: Character): Packet
        updateDojoStats(chr: Character, belt: number): Packet
        updateGender(chr: Character): Packet
        updateHiredMerchant(hm: HiredMerchant, chr: Character): Packet
        updateHiredMerchantBox(hm: HiredMerchant): Packet
        updateHpMpAlert(hp: number, mp: number): Packet
        updateInventorySlotLimit(type: number, newLimit: number): Packet
        updateMapItemObject(drop: MapItem, giveOwnership: boolean): Packet
        updateMessengerPlayer(from: string, chr: Character, position: number, channel: number): Packet
        updateMount(charid: number, mount: Mount, levelup: boolean): Packet
        updateParty(forChannel: number, party: Party, op: PartyOperation, target: PartyCharacter): Packet
        updatePartyMemberHP(cid: number, curhp: number, maxhp: number): Packet
        updatePlayerShopBox(shop: PlayerShop): Packet
        updatePlayerStats(stats: [], enableActions: boolean, chr: Character): Packet
        updateQuest(chr: Character, qs: QuestStatus, infoUpdate: boolean): Packet
        updateQuestFinish(quest: number, npc: number, nextquest: number): Packet
        updateQuestInfo(quest: number, npc: number): Packet
        updateSkill(skillId: number, level: number, masterlevel: number, expiration: number): Packet
        updateWitchTowerScore(score: number): Packet
        useChalkboard(chr: Character, close: boolean): Packet
        viewMerchantBlacklist(chrNames: []): Packet
        viewMerchantVisitorHistory(pastVisitors: []): Packet
        wrongPic(): Packet
    }
    interface Party {
        //============ Properties =============
        //============ Functions  =============
        addDoor(owner: number, door: Door): void
        addMember(member: PartyCharacter): void
        assignNewLeader(c: Client): void
        containsMembers(member: PartyCharacter): boolean
        createParty(player: Character, silentCheck: boolean): boolean
        expelFromParty(party: Party, c: Client, expelCid: number): void
        getDoors(): JavaMap
        getEligibleMembers(): []
        getEnemy(): Party
        getId(): number
        getLeader(): PartyCharacter
        getLeaderId(): number
        getMemberById(id: number): PartyCharacter
        getMemberByPos(pos: number): PartyCharacter
        getMembers(): []
        getMembersSortedByHistory(): []
        getPartyDoor(cid: number): number
        getPartyMembers(): []
        getPartyMembersOnline(): []
        joinParty(player: Character, partyid: number, silentCheck: boolean): boolean
        leaveParty(party: Party, c: Client): void
        removeDoor(owner: number): void
        removeMember(member: PartyCharacter): void
        setEligibleMembers(eliParty: []): void
        setEnemy(enemy: Party): void
        setId(id: number): void
        setLeader(victim: PartyCharacter): void
        updateMember(member: PartyCharacter): void
    }
    interface PartyCharacter {
        //============ Properties =============
        //============ Functions  =============
        getChannel(): number
        getGuildId(): number
        getId(): number
        getJob(): Job
        getJobId(): number
        getLevel(): number
        getMapId(): number
        getName(): string
        getPlayer(): Character
        getWorld(): number
        isLeader(): boolean
        isOnline(): boolean
        setChannel(channel: number): void
        setMapId(mapid: number): void
        setOnline(online: boolean): void
    }
    interface PartyQuest {
        //============ Properties =============
        //============ Functions  =============
        getExp(PQ: string, level: number): number
        getParticipants(): []
        getParty(): Party
        removeParticipant(chr: Character): void
    }
    interface PlayerNPC {
        //============ Properties =============
        //============ Functions  =============
        addPlayerNPCMapObject(map: MapleMap): void
        canSpawnPlayerNpc(name: string, mapid: number): boolean
        getCY(): number
        getDir(): number
        getEquips(): JavaMap
        getFH(): number
        getFace(): number
        getGender(): number
        getHair(): number
        getJob(): number
        getName(): string
        getObjectId(): number
        getOverallJobRank(): number
        getOverallRank(): number
        getPosition(): Point
        getRX0(): number
        getRX1(): number
        getScriptId(): number
        getSkin(): number
        getType(): MapObjectType
        getWorldJobRank(): number
        getWorldRank(): number
        loadRunningRankData(worlds: number): void
        multicastSpawnPlayerNPC(mapid: number, world: number): void
        nullifyPosition(): void
        removeAllPlayerNPC(): void
        removePlayerNPC(chr: Character): void
        sendDestroyData(client: Client): void
        sendSpawnData(client: Client): void
        setObjectId(id: number): void
        setPosition(position: Point): void
        spawnPlayerNPC(mapid: number, chr: Character): boolean
        spawnPlayerNPC(mapid: number, pos: Point, chr: Character): boolean
        updatePlayerNPCPosition(map: MapleMap, newPos: Point): void
    }
    interface Quest {
        //============ Properties =============
        //============ Functions  =============
        canComplete(chr: Character, npcid: number): boolean
        canQuestByInfoProgress(chr: Character): boolean
        canStart(chr: Character, npcid: number): boolean
        canStartQuestByStatus(chr: Character): boolean
        clearCache(): void
        clearCache(quest: number): void
        complete(chr: Character, npc: number): void
        complete(chr: Character, npc: number, selection: number): void
        expireQuest(chr: Character): void
        forceComplete(chr: Character, npc: number): boolean
        forceStart(chr: Character, npc: number): boolean
        forfeit(chr: Character): boolean
        getCompleteItemAmountNeeded(itemid: number): number
        getId(): number
        getInfoEx(qs: Status): []
        getInfoEx(qs: Status, index: number): string
        getInfoNumber(qs: Status): number
        getInstance(id: number): Quest
        getInstanceFromInfoNumber(infoNumber: number): Quest
        getMatchedQuests(search: string): []
        getMedalRequirement(): number
        getMobAmountNeeded(mid: number): number
        getName(): string
        getNpcRequirement(checkEnd: boolean): number
        getParentName(): string
        getRelevantMobs(): []
        getStartItemAmountNeeded(itemid: number): number
        getTimeLimit(): number
        hasNextQuestAction(): boolean
        hasScriptRequirement(checkEnd: boolean): boolean
        isAutoComplete(): boolean
        isAutoStart(): boolean
        isExploitableQuest(questid: number): boolean
        isSameDayRepeatable(): boolean
        loadAllQuests(): void
        reset(chr: Character): void
        restoreLostItem(chr: Character, itemid: number): boolean
        start(chr: Character, npc: number): void
    }
    interface Server {
        //============ Properties =============
        uptime: number
        //============ Functions  =============
        OnlineTimer(time: number): void
        addAlliance(id: number, alliance: Alliance): void
        addChannel(worldid: number): number
        addGuildMember(mgc: GuildCharacter, chr: Character): number
        addGuildtoAlliance(aId: number, guildId: number): boolean
        addWorld(): number
        allianceMessage(id: number, packet: Packet, exception: number, guildex: number): void
        broadcastGMMessage(world: number, packet: Packet): void
        broadcastMessage(world: number, packet: Packet): void
        canEnterDeveloperRoom(): boolean
        canFly(accountid: number): boolean
        changeFly(accountid: number, canFly: boolean): void
        changeRank(gid: number, cid: number, newRank: number): void
        changeRankTitle(gid: number, ranks: string[]): void
        commitActiveCoupons(): void
        createCharacterEntry(chr: Character): void
        createGuild(leaderId: number, name: string): number
        deleteCharacterEntry(accountid: number, chrid: number): void
        deleteGuildCharacter(mc: Character): void
        deleteGuildCharacter(mgc: GuildCharacter): void
        disbandAlliance(id: number): void
        disbandGuild(gid: number): void
        expelMember(initiator: GuildCharacter, name: string, cid: number): void
        forceUpdateCurrentTime(): number
        freeCharacteridInTransition(client: Client): number
        gainGP(gid: number, amount: number): void
        getAccountCharacterCount(accountid: number): number
        getAccountWorldCharacterCount(accountid: number, worldid: number): number
        getActiveCoupons(): []
        getAllChannels(): []
        getAlliance(id: number): Alliance
        getChannel(world: number, channel: number): Channel
        getChannelsFromWorld(world: number): []
        getCharacterWorld(chrid: number): number
        getCouponRates(): JavaMap
        getCurrentTime(): number
        getCurrentTimestamp(): number
        getGuild(id: number): Guild
        getGuild(id: number, world: number): Guild
        getGuild(id: number, world: number, mc: Character): Guild
        getGuildByName(name: string): Guild
        getInetSocket(client: Client, world: number, channel: number): string[]
        getInstance(): Server
        getNewYearCard(cardid: number): NewYearCardRecord
        getOpenChannels(world: number): []
        getPlayerBuffStorage(): PlayerBuffStorage
        getSubnetInfo(): Properties
        getTimeLeftForNextDay(): number
        getWorld(id: number): World
        getWorldPlayerRanking(worldid: number): []
        getWorlds(): []
        getWorldsSize(): number
        guildChat(gid: number, name: string, cid: number, msg: string): void
        guildMessage(gid: number, packet: Packet): void
        guildMessage(gid: number, packet: Packet, exception: number): void
        hasCharacteridInTransition(client: Client): boolean
        haveCharacterEntry(accountid: number, chrid: number): boolean
        increaseAllianceCapacity(aId: number, inc: number): boolean
        increaseGuildCapacity(gid: number): boolean
        init(): void
        isGmOnline(world: number): boolean
        isNextTime(): boolean
        isOnline(): boolean
        leaveGuild(mgc: GuildCharacter): void
        loadAccountCharacters(c: Client): void
        loadAccountCharlist(accountId: number, visibleWorlds: number): SortedMap
        loadAccountStorages(c: Client): void
        loadAllAccountsCharactersView(): void
        memberLevelJobUpdate(mgc: GuildCharacter): void
        registerAnnouncePlayerDiseases(c: Client): void
        registerLoginState(c: Client): void
        reloadGuildCharacters(world: number): void
        reloadWorldsPlayerRanking(): void
        removeChannel(worldid: number): boolean
        removeGuildFromAlliance(aId: number, guildId: number): boolean
        removeNewYearCard(cardid: number): NewYearCardRecord
        removeWorld(): boolean
        resetAllianceGuildPlayersRank(gId: number): void
        runAnnouncePlayerDiseasesSchedule(): void
        setAllianceNotice(aId: number, notice: string): boolean
        setAllianceRanks(aId: number, ranks: string[]): boolean
        setAvailableDeveloperRoom(): void
        setCharacteridInTransition(client: Client, charId: number): void
        setGuildAllianceId(gId: number, aId: number): boolean
        setGuildEmblem(gid: number, bg: number, bgcolor: number, logo: number, logocolor: number): void
        setGuildMemberOnline(mc: Character, bOnline: boolean, channel: number): void
        setGuildNotice(gid: number, notice: string): void
        setNewYearCard(nyc: NewYearCardRecord): void
        setOnline(online: boolean): void
        shutdown(restart: boolean): Runnable
        shutdownInternal(restart: boolean): void
        toggleCoupon(couponId: number): void
        transferWorldCharacterEntry(chr: Character, toWorld: number): void
        unregisterLoginState(c: Client): void
        updateActiveCoupons(): void
        updateCharacterEntry(chr: Character): void
        updateCurrentTime(): void
        validateCharacteridInTransition(client: Client, charId: number): boolean
        worldRecommendedList(): []
    }
    interface Shop {
        //============ Properties =============
        //============ Functions  =============
        buy(c: Client, slot: number, itemId: number, quantity: number): void
        createFromDB(id: number, isShopId: boolean): Shop
        getId(): number
        getNpcId(): number
        recharge(c: Client, slot: number): void
        sell(c: Client, type: InventoryType, slot: number, quantity: number): void
        sendShop(c: Client): void
    }
    interface Skill {
        //============ Properties =============
        //============ Functions  =============
        addLevelEffect(effect: StatEffect): void
        getAction(): boolean
        getAnimationTime(): number
        getEffect(level: number): StatEffect
        getElement(): Element
        getId(): number
        getMaxLevel(): number
        incAnimationTime(time: number): void
        isBeginnerSkill(): boolean
        isFourthJob(): boolean
        setAction(act: boolean): void
        setAnimationTime(time: number): void
        setElement(elem: Element): void
    }
    interface World {
        //============ Properties =============
        //============ Functions  =============
        addCashItemBought(snid: number): void
        addChannel(channel: Channel): boolean
        addFamily(id: number, f: Family): void
        addMarriageGuest(marriageId: number, playerId: number): boolean
        addMessengerPlayer(messenger: Messenger, namefrom: string, fromchannel: number, position: number): void
        addOwlItemSearch(itemid: number): void
        addPlayer(chr: Character): void
        addPlayerHpDecrease(chr: Character): void
        broadcastPacket(packet: Packet): void
        buddyChanged(cid: number, cidFrom: number, name: string, channel: number, operation: BuddyOperation): void
        buddyChat(recipientCharacterIds: number[], cidFrom: number, nameFrom: string, chattext: string): void
        canUninstall(): boolean
        changeEmblem(gid: number, affectedPlayers: [], mgs: GuildSummary): void
        clearAccountCharacterView(accountId: number): void
        createMessenger(chrfor: MessengerCharacter): Messenger
        createParty(chrfor: PartyCharacter): Party
        createRelationship(groomId: number, brideId: number): number
        debugMarriageStatus(): void
        declineChat(sender: string, player: Character): void
        deleteRelationship(playerId: number, partnerId: number): void
        dropMessage(type: number, message: string): void
        find(id: number): number
        find(name: string): number
        getAccountCharactersView(accountId: number): []
        getAccountStorage(accountId: number): Storage
        getActiveMerchants(): []
        getActivePlayerShops(): []
        getAllCharactersView(): []
        getAvailableItemBundles(itemid: number): []
        getBossDropRate(): number
        getChannel(channel: number): Channel
        getChannels(): []
        getChannelsSize(): number
        getCharacterPartyid(chrid: number): number
        getDropRate(): number
        getEventMessage(): string
        getExpRate(): number
        getFamilies(): []
        getFamily(id: number): Family
        getFishingRate(): number
        getFlag(): number
        getGuild(mgc: GuildCharacter): Guild
        getGuildSummary(gid: number, wid: number): GuildSummary
        getHiredMerchant(ownerid: number): HiredMerchant
        getId(): number
        getMarriageQueuedCouple(marriageId: number): Pair
        getMarriageQueuedLocation(marriageId: number): Pair
        getMatchCheckerCoordinator(): MatchCheckerCoordinator
        getMesoRate(): number
        getMessenger(messengerid: number): Messenger
        getMostSellerCashItems(): []
        getOwlSearchedItems(): []
        getParty(partyid: number): Party
        getPartySearchCoordinator(): PartySearchCoordinator
        getPlayerNpcMapPodiumData(mapid: number): number
        getPlayerNpcMapStep(mapid: number): number
        getPlayerShop(ownerid: number): PlayerShop
        getPlayerStorage(): PlayerStorage
        getQuestRate(): number
        getRelationshipCouple(relationshipId: number): Pair
        getRelationshipId(playerId: number): number
        getServiceAccess(sv: WorldServices): BaseService
        getTransportationTime(travelTime: number): number
        getTravelRate(): number
        getWeddingCoupleForGuest(guestId: number, cathedral: boolean): Pair
        getWorldCapacityStatus(): number
        isConnected(charName: string): boolean
        isGuildQueued(guildId: number): boolean
        isMarriageQueued(marriageId: number): boolean
        isWorldCapacityFull(): boolean
        joinMessenger(messengerid: number, target: MessengerCharacter, from: string, fromchannel: number): void
        leaveMessenger(messengerid: number, target: MessengerCharacter): void
        loadAccountCharactersView(accountId: number, chars: []): void
        loadAccountStorage(accountId: number): void
        loadAndGetAllCharactersView(): []
        loggedOff(name: string, characterId: number, channel: number, buddies: number[]): void
        loggedOn(name: string, characterId: number, channel: number, buddies: number[]): void
        messengerChat(messenger: Messenger, chattext: string, namefrom: string): void
        messengerInvite(sender: string, messengerid: number, target: string, fromchannel: number): void
        multiBuddyFind(charIdFrom: number, characterIds: number[]): CharacterIdChannelPair[]
        partyChat(party: Party, chattext: string, namefrom: string): void
        putGuildQueued(guildId: number): void
        putMarriageQueued(marriageId: number, cathedral: boolean, premium: boolean, groomId: number, brideId: number): void
        registerAccountCharacterView(accountId: number, chr: Character): void
        registerDisabledServerMessage(chrid: number): boolean
        registerFisherPlayer(chr: Character, baitLevel: number): boolean
        registerHiredMerchant(hm: HiredMerchant): void
        registerMountHunger(chr: Character): void
        registerPetHunger(chr: Character, petSlot: number): void
        registerPlayerShop(ps: PlayerShop): void
        registerTimedMapObject(r: Runnable, duration: number): void
        reloadGuildSummary(): void
        removeChannel(): number
        removeFamily(id: number): void
        removeGuildQueued(guildId: number): void
        removeMapPartyMembers(partyid: number): void
        removeMarriageQueued(marriageId: number): Pair
        removeMessengerPlayer(messenger: Messenger, position: number): void
        removePlayer(chr: Character): void
        removePlayerHpDecrease(chr: Character): void
        requestBuddyAdd(addName: string, channelFrom: number, cidFrom: number, nameFrom: string): BuddyAddResult
        resetDisabledServerMessages(): void
        resetPlayerNpcMapData(): void
        runCheckFishingSchedule(): void
        runDisabledServerMessagesSchedule(): void
        runHiredMerchantSchedule(): void
        runMountSchedule(): void
        runPartySearchUpdateSchedule(): void
        runPetSchedule(): void
        runPlayerHpDecreaseSchedule(): void
        runTimedMapObjectSchedule(): void
        sendPacket(targetIds: [], packet: Packet, exception: number): void
        setBossDropRate(bossDropRate: number): void
        setDropRate(drop: number): void
        setExpRate(exp: number): void
        setFishingRate(quest: number): void
        setFlag(b: number): void
        setGuildAndRank(cid: number, guildid: number, rank: number): void
        setGuildAndRank(cids: [], guildid: number, rank: number, exception: number): void
        setMesoRate(meso: number): void
        setOfflineGuildStatus(guildid: number, guildrank: number, cid: number): void
        setPlayerNpcMapData(mapid: number, step: number, podium: number): void
        setPlayerNpcMapPodiumData(mapid: number, podium: number): void
        setPlayerNpcMapStep(mapid: number, step: number): void
        setQuestRate(questRate: number): void
        setServerMessage(msg: string): void
        setTravelRate(travelRate: number): void
        shutdown(): void
        silentJoinMessenger(messengerid: number, target: MessengerCharacter, position: number): void
        silentLeaveMessenger(messengerid: number, target: MessengerCharacter): void
        unregisterAccountCharacterView(accountId: number, chrId: number): void
        unregisterAccountStorage(accountId: number): void
        unregisterDisabledServerMessage(chrid: number): boolean
        unregisterFisherPlayer(chr: Character): number
        unregisterHiredMerchant(hm: HiredMerchant): void
        unregisterMountHunger(chr: Character): void
        unregisterPetHunger(chr: Character, petSlot: number): void
        unregisterPlayerShop(ps: PlayerShop): void
        updateGuildSummary(gid: number, mgs: GuildSummary): void
        updateMessenger(messengerid: number, namefrom: string, fromchannel: number): void
        updateMessenger(messenger: Messenger, namefrom: string, position: number, fromchannel: number): void
        updateParty(partyid: number, operation: PartyOperation, target: PartyCharacter): void
    }
    //=================================================================
    //     misc class
    //=================================================================
    interface AbstractLoadedLife { }
    interface Alliance { }
    interface AriantColiseum { }
    interface AutobanManager { }
    interface BaseService { }
    interface BuddyAddResult { }
    interface BuddyList { }
    interface BuddyOperation { }
    interface BuffStat { }
    interface Calendar { }
    interface CashShop { }
    interface ChannelHandlerContext { }
    interface ChannelServices { }
    interface CharacterFactoryRecipe { }
    interface CharacterIdChannelPair { }
    interface CharactersDO { }
    interface Coconut { }
    interface ConfigService { }
    interface DelayedQuestUpdate { }
    interface Disease { }
    interface Door { }
    interface DoorObject { }
    interface Dragon { }
    interface EventScheduledFuture { }
    interface EventScriptManager { }
    interface Expedition { }
    interface ExpeditionType { }
    interface Family { }
    interface FamilyEntry { }
    interface Fitness { }
    interface FootholdTree { }
    interface GameConfigDO { }
    interface GuardianSpawnPoint { }
    interface Guild { }
    interface GuildCharacter { }
    interface GuildSummary { }
    interface HiredMerchant { }
    interface Hwid { }
    interface IdleStateEvent { }
    interface InPacket { }
    interface InetAddress { }
    interface InitializationVector { }
    interface Invocable { }
    interface ItemInformationProvider { }
    interface JSONObject { }
    interface KeyBinding { }
    interface Kite { }
    interface MCSkill { }
    interface MakerItemCreateEntry { }
    interface MapItem { }
    interface MapManager { }
    interface MapObject { }
    interface MapObjectType { }
    interface Marriage { }
    interface MatchCheckerCoordinator { }
    interface Messenger { }
    interface MessengerCharacter { }
    interface MiniDungeon { }
    interface MiniGame { }
    interface MiniGameResult { }
    interface Mist { }
    interface MobSkill { }
    interface MobSkillId { }
    interface ModifiedCashItemDO { }
    interface Monster { }
    interface MonsterAggroCoordinator { }
    interface MonsterBook { }
    interface MonsterCarnival { }
    interface MonsterCarnivalParty { }
    interface MonsterStatusEffect { }
    interface Mount { }
    interface NewYearCardRecord { }
    interface NextLevelContext { }
    interface Ola { }
    interface OutPacket { }
    interface OxQuiz { }
    interface Packet { }
    interface PacketProcessor { }
    interface Pair { }
    interface PartyOperation { }
    interface PartySearchCoordinator { }
    interface Pet { }
    interface PlayerBuffStorage { }
    interface PlayerShop { }
    interface PlayerStorage { }
    interface Portal { }
    interface Pyramid { }
    interface QuestConsItem { }
    interface QuestStatus { }
    interface QuickslotBinding { }
    interface Reactor { }
    interface Rectangle { }
    interface ResultSet { }
    interface Ring { }
    interface RockPaperScissor { }
    interface Runnable { }
    interface SavedLocation { }
    interface SavedLocationType { }
    interface ScheduledFuture { }
    interface ScriptEngine { }
    interface ScriptedItem { }
    interface ScrollResult { }
    interface SkillMacro { }
    interface SkinColor { }
    interface Snowball { }
    interface SoldItem { }
    interface SpawnPoint { }
    interface Stat { }
    interface StatEffect { }
    interface Status { }
    interface Summon { }
    interface Throwable { }
    interface Trade { }
    interface TypeReference { }
    interface WeaponType { }
    interface WorldServices { }
    //=================================================================
    //     Script Manager class
    //=================================================================
    interface AbstractPlayerInteraction {
        //============ Properties =============
        c: Client
        //============ Functions  =============
        canGetFirstJob(jobType: number): boolean
        canHold(itemid: number): boolean
        canHold(itemid: number, quantity: number): boolean
        canHold(itemid: number, quantity: number, removeItemid: number, removeQuantity: number): boolean
        canHoldAll(itemids: []): boolean
        canHoldAll(itemids: [], quantity: []): boolean
        canHoldAllAfterRemoving(toAddItemids: [], toAddQuantity: [], toRemoveItemids: [], toRemoveQuantity: []): boolean
        cancelItem(id: number): void
        changeMusic(songName: string): void
        completeQuest(id: number): boolean
        completeQuest(id: number, npc: number): boolean
        containsAreaInfo(area: number, info: string): boolean
        countAllMonstersOnMap(map: number): number
        countMonster(): number
        createExpedition(type: ExpeditionType): number
        createExpedition(type: ExpeditionType, silent: boolean, minPlayers: number, maxPlayers: number): number
        disableMinimap(): void
        displayAranIntro(): void
        displayGuide(num: number): void
        dojoEnergy(): void
        dropMessage(type: number, message: string): void
        earnTitle(msg: string): void
        enableActions(): void
        endExpedition(exped: Expedition): void
        environmentChange(env: string, mode: number): void
        evolvePet(slot: number, afterId: number): Item
        forceCompleteQuest(id: number): boolean
        forceCompleteQuest(id: number, npc: number): boolean
        forceStartQuest(id: number): boolean
        forceStartQuest(id: number, npc: number): boolean
        gainAndEquip(itemid: number, slot: number): void
        gainEquip(equip: Equip): void
        gainFame(delta: number): void
        gainItem(id: number): void
        gainItem(id: number, show: boolean): void
        gainItem(id: number, quantity: number): void
        gainItem(id: number, quantity: number, show: boolean): void
        gainItem(id: number, quantity: number, randomStats: boolean, showMessage: boolean): Item
        gainItem(id: number, quantity: number, randomStats: boolean, showMessage: boolean, expires: number): Item
        gainItem(id: number, quantity: number, randomStats: boolean, showMessage: boolean, expires: number, from: Pet): Item
        getAccountExtendValue(extendName: string): string
        getAccountExtendValue(extendName: string, isDaily: boolean): string
        getChar(): Character
        getCharacterExtendValue(extendName: string): string
        getCharacterExtendValue(extendName: string, isDaily: boolean): string
        getClient(): Client
        getCurrentTime(): number
        getDriedPets(): []
        getEventInstance(): EventInstanceManager
        getEventManager(event: string): EventManager
        getExpedition(type: ExpeditionType): Expedition
        getExpeditionMemberNames(type: ExpeditionType): string
        getFirstJobStatRequirement(jobType: number): string
        getGuild(): Guild
        getHourOfDay(): number
        getInventory(type: InventoryType): Inventory
        getInventory(type: number): Inventory
        getItemQuantity(itemid: number): number
        getJailTimeLeft(): number
        getJob(): Job
        getJobId(): number
        getLevel(): number
        getMap(): MapleMap
        getMap(map: number): MapleMap
        getMapId(): number
        getMarketPortalId(mapId: number): number
        getMonsterLifeFactory(mid: number): Monster
        getOnlineTime(): number
        getParty(): Party
        getPlayer(): Character
        getPlayerCount(mapid: number): number
        getPyramid(): Pyramid
        getQuestNoRecord(id: number): QuestStatus
        getQuestProgress(id: number): string
        getQuestProgress(id: number, infoNumber: number): string
        getQuestProgressInt(id: number): number
        getQuestProgressInt(id: number, infoNumber: number): number
        getQuestRecord(id: number): QuestStatus
        getQuestStatus(id: number): number
        getUnclaimedMarriageGifts(): []
        getWarpMap(map: number): MapleMap
        giveCharacterExp(amount: number, chr: Character): void
        givePartyExp(PQ: string): void
        givePartyExp(PQ: string, instance: boolean): void
        givePartyExp(amount: number, party: []): void
        givePartyItems(id: number, quantity: number, party: []): void
        goDojoUp(): void
        guideHint(hint: number): void
        guildMessage(type: number, message: string): void
        hasItem(itemid: number): boolean
        hasItem(itemid: number, quantity: number): boolean
        haveItem(itemid: number): boolean
        haveItem(itemid: number, quantity: number): boolean
        haveItemWithId(itemid: number): boolean
        haveItemWithId(itemid: number, checkEquipped: boolean): boolean
        isAllReactorState(reactorId: number, state: number): boolean
        isEventLeader(): boolean
        isGuildLeader(): boolean
        isLeader(): boolean
        isLeaderExpedition(type: ExpeditionType): boolean
        isPartyLeader(): boolean
        isQuestActive(id: number): boolean
        isQuestCompleted(id: number): boolean
        isQuestStarted(id: number): boolean
        lockUI(): void
        mapEffect(path: string): void
        mapMessage(type: number, message: string): void
        mapSound(path: string): void
        message(message: string): void
        npcTalk(npcid: number, message: string): void
        numberWithCommas(number: number): string
        openNpc(npcid: number): void
        openNpc(npcid: number, script: string): void
        openUI(ui: number): void
        playSound(sound: string): void
        playerMessage(type: number, message: string): void
        removeAll(id: number): void
        removeAll(id: number, cl: Client): void
        removeEquipFromSlot(slot: number): void
        removeFromParty(id: number, party: []): void
        removeGuide(): void
        removeHPQItems(): void
        removePartyItems(id: number): void
        resetAllQuestProgress(id: number): void
        resetDojoEnergy(): void
        resetMap(mapid: number): void
        resetMapObjects(mapid: number): void
        resetPartyDojoEnergy(): void
        resetQuestProgress(id: number, infoNumber: number): void
        saveOrUpdateAccountExtendValue(extendName: string, extendValue: string): void
        saveOrUpdateAccountExtendValue(extendName: string, extendValue: string, isDaily: boolean): void
        saveOrUpdateCharacterExtendValue(extendName: string, extendValue: string): void
        saveOrUpdateCharacterExtendValue(extendName: string, extendValue: string, isDaily: boolean): void
        setQuestProgress(id: number, progress: number): void
        setQuestProgress(id: number, progress: string): void
        setQuestProgress(id: number, infoNumber: number, progress: number): void
        setQuestProgress(id: number, infoNumber: number, progress: string): void
        showEffect(effect: string): void
        showInfo(path: string): void
        showInfoText(msg: string): void
        showInstruction(msg: string, width: number, height: number): void
        showIntro(path: string): void
        spawnGuide(): void
        spawnMonster(id: number, x: number, y: number): void
        spawnNpc(npcId: number, pos: Point, map: MapleMap): void
        startDungeonInstance(dungeonid: number): boolean
        startQuest(id: number): boolean
        startQuest(id: number, npc: number): boolean
        talkGuide(message: string): void
        teachSkill(skillid: number, level: number, masterLevel: number, expiration: number): void
        teachSkill(skillid: number, level: number, masterLevel: number, expiration: number, force: boolean): void
        unlockUI(): void
        updateAreaInfo(area: number, info: string): void
        useItem(id: number): void
        warp(mapid: number): void
        warp(map: number, portal: string): void
        warp(map: number, portal: number): void
        warpMap(map: number): void
        warpParty(id: number): void
        warpParty(map: number, portalName: string): void
        warpParty(id: number, portalId: number): void
        warpParty(id: number, fromMinId: number, fromMaxId: number): void
        warpParty(id: number, portalId: number, fromMinId: number, fromMaxId: number): void
        weakenAreaBoss(monsterId: number, message: string): void
    }
    interface EventInstanceManager {
        //============ Properties =============
        //============ Functions  =============
        activatedAllReactorsOnMap(mapId: number, minReactorId: number, maxReactorId: number): boolean
        activatedAllReactorsOnMap(map: MapleMap, minReactorId: number, maxReactorId: number): boolean
        addEventTimer(time: number): void
        afterChangedMap(chr: Character, mapId: number): void
        applyEventPlayersItemBuff(itemId: number): void
        applyEventPlayersSkillBuff(skillId: number): void
        applyEventPlayersSkillBuff(skillId: number, skillLv: number): void
        changedLeader(ldr: PartyCharacter): void
        changedMap(chr: Character, mapId: number): void
        checkEventTeamLacking(leavingEventMap: boolean, minPlayers: number): boolean
        clearPQ(): void
        disbandParty(): void
        dispatchRaiseQuestMobCount(mobid: number, mapid: number): void
        dispose(): void
        dispose(shutdown: boolean): void
        disposeIfPlayerBelow(size: number, towarp: number): boolean
        dropMessage(type: number, message: string): void
        exitPlayer(chr: Character): void
        friendlyDamaged(mob: Monster): void
        friendlyItemDrop(mob: Monster): void
        friendlyKilled(mob: Monster, hasKiller: boolean): void
        getClearStageBonus(stage: number): []
        getClearStageExp(stage: number): number
        getClearStageMeso(stage: number): number
        getEm(): EventManager
        getEventPlayersJobs(): number
        getInstanceMap(mapid: number): MapleMap
        getIntProperty(key: string): number
        getKillCount(chr: Character): number
        getLeader(): Character
        getLeaderId(): number
        getMapFactory(): MapManager
        getMapInstance(mapId: number): MapleMap
        getMonster(mid: number): Monster
        getName(): string
        getObjectProperty(key: string): Object
        getPlayerById(id: number): Character
        getPlayerCount(): number
        getPlayers(): []
        getProperty(key: string): string
        getTimeLeft(): number
        giveEventPlayersExp(gain: number): void
        giveEventPlayersExp(gain: number, mapId: number): void
        giveEventPlayersMeso(gain: number): void
        giveEventPlayersMeso(gain: number, mapId: number): void
        giveEventPlayersStageReward(thisStage: number): void
        giveEventReward(player: Character): boolean
        giveEventReward(player: Character, eventLevel: number): boolean
        gridCheck(chr: Character): number
        gridClear(): void
        gridInsert(chr: Character, newStatus: number): void
        gridRemove(chr: Character): void
        gridSize(): number
        invokeScriptFunction(name: string, args: Object[]): Object
        isEventCleared(): boolean
        isEventDisposed(): boolean
        isEventLeader(chr: Character): boolean
        isEventTeamLackingNow(leavingEventMap: boolean, minPlayers: number, quitter: Character): boolean
        isEventTeamTogether(): boolean
        isExpeditionTeamLackingNow(leavingEventMap: boolean, minPlayers: number, quitter: Character): boolean
        isLeader(chr: Character): boolean
        isTimerStarted(): boolean
        leftParty(chr: Character): void
        linkPortalToScript(thisStage: number, portalName: string, scriptName: string, thisMapId: number): void
        linkToNextStage(thisStage: number, eventFamily: string, thisMapId: number): void
        monsterKilled(mob: Monster, hasKiller: boolean): void
        monsterKilled(chr: Character, mob: Monster): void
        movePlayer(chr: Character): void
        playerDisconnected(chr: Character): void
        playerKilled(chr: Character): void
        recoverOpenedGate(chr: Character, thisMapId: number): void
        registerExpedition(exped: Expedition): void
        registerMonster(mob: Monster): void
        registerParty(chr: Character): void
        registerParty(party: Party, map: MapleMap): void
        registerPlayer(chr: Character): void
        registerPlayer(chr: Character, runEntryScript: boolean): void
        removePlayer(chr: Character): void
        restartEventTimer(time: number): void
        reviveMonster(mob: Monster): void
        revivePlayer(chr: Character): boolean
        schedule(methodName: string, delay: number): void
        setEventClearStageExp(gain: []): void
        setEventClearStageMeso(gain: []): void
        setEventCleared(): void
        setEventRewards(rwds: [], qtys: []): void
        setEventRewards(rwds: [], qtys: [], expGiven: number): void
        setEventRewards(eventLevel: number, rwds: [], qtys: []): void
        setEventRewards(eventLevel: number, rwds: [], qtys: [], expGiven: number): void
        setExclusiveItems(items: []): void
        setIntProperty(key: string, value: number): void
        setLeader(chr: Character): void
        setName(name: string): void
        setObjectProperty(key: string, obj: Object): void
        setProperty(key: string, value: number): void
        setProperty(key: string, value: string): void
        setProperty(key: string, value: string, prev: boolean): Object
        showClearEffect(): void
        showClearEffect(hasGate: boolean): void
        showClearEffect(mapId: number): void
        showClearEffect(hasGate: boolean, mapId: number): void
        showClearEffect(mapId: number, mapObj: string, newState: number): void
        showClearEffect(hasGate: boolean, mapId: number, mapObj: string, newState: number): void
        showWrongEffect(): void
        showWrongEffect(mapId: number): void
        spawnNpc(npcId: number, pos: Point, map: MapleMap): void
        startEvent(): void
        startEventTimer(time: number): void
        stopEventTimer(): void
        unregisterPlayer(chr: Character): void
        warpEventTeam(warpTo: number): void
        warpEventTeam(warpFrom: number, warpTo: number): void
        warpEventTeamToMapSpawnPoint(warpTo: number, toSp: number): void
        warpEventTeamToMapSpawnPoint(warpFrom: number, warpTo: number, toSp: number): void
    }
    interface EventManager {
        //============ Properties =============
        //============ Functions  =============
        addGuildToQueue(guildId: number, leaderId: number): number
        attemptStartGuildInstance(): boolean
        cancel(): void
        clearPQ(eim: EventInstanceManager): void
        clearPQ(eim: EventInstanceManager, toMap: MapleMap): void
        completeQuest(chr: Character, id: number, npcid: number): void
        disposeInstance(name: string): void
        getChannelServer(): Channel
        getEligibleParty(party: Party): []
        getInstance(name: string): EventInstanceManager
        getInstances(): []
        getIntProperty(key: string): number
        getIv(): Invocable
        getLobbyDelay(): number
        getMonster(mid: number): Monster
        getName(): string
        getProperty(key: string): string
        getQueueSize(): number
        getTransportationTime(travelTime: number): number
        getWorldServer(): World
        isQueueFull(): boolean
        newInstance(name: string): EventInstanceManager
        newMarriage(name: string): Marriage
        schedule(methodName: string, delay: number): EventScheduledFuture
        schedule(methodName: string, eim: EventInstanceManager, delay: number): EventScheduledFuture
        scheduleAtTimestamp(methodName: string, timestamp: number): EventScheduledFuture
        setIntProperty(key: string, value: number): void
        setProperty(key: string, value: string): void
        setProperty(key: string, value: number): void
        startInstance(exped: Expedition): boolean
        startInstance(chr: Character): boolean
        startInstance(party: Party, map: MapleMap): boolean
        startInstance(eim: EventInstanceManager, ldr: Character): boolean
        startInstance(eim: EventInstanceManager, ldr: string): boolean
        startInstance(lobbyId: number, exped: Expedition): boolean
        startInstance(lobbyId: number, leader: Character): boolean
        startInstance(party: Party, map: MapleMap, difficulty: number): boolean
        startInstance(lobbyId: number, party: Party, map: MapleMap): boolean
        startInstance(lobbyId: number, eim: EventInstanceManager, ldr: string): boolean
        startInstance(lobbyId: number, exped: Expedition, leader: Character): boolean
        startInstance(lobbyId: number, party: Party, map: MapleMap, difficulty: number): boolean
        startInstance(lobbyId: number, party: Party, map: MapleMap, leader: Character): boolean
        startInstance(lobbyId: number, eim: EventInstanceManager, ldr: string, leader: Character): boolean
        startInstance(lobbyId: number, chr: Character, leader: Character, difficulty: number): boolean
        startInstance(lobbyId: number, party: Party, map: MapleMap, difficulty: number, leader: Character): boolean
        startQuest(chr: Character, id: number, npcid: number): void
    }
    interface MapScriptMethods extends AbstractPlayerInteraction {
        //============ Properties =============
        //============ Functions  =============
        displayAranIntro(): void
        displayCygnusIntro(): void
        explorerQuest(questid: number, questName: string): void
        goAdventure(): void
        goLith(): void
        startExplorerExperience(): void
        touchTheSky(): void
    }
    interface NPCConversationManager extends AbstractPlayerInteraction {
        //============ Properties =============
        //============ Functions  =============
        answerCPQChallenge(accept: boolean): void
        canBeUsedAllianceName(name: string): boolean
        canSpawnPlayerNpc(mapid: number): boolean
        cancelCPQLobby(): void
        challengeParty(field: number): void
        challengeParty2(field: number): void
        changeJob(job: Job): void
        changeJobById(a: number): void
        completeQuest(id: number): boolean
        cpqCalcAvgLvl(map: number): number
        cpqLobby(field: number): void
        cpqLobby2(field: number): void
        createAlliance(name: string): Alliance
        createMarriageWishlist(): boolean
        createPyramid(mode: string, party: boolean): boolean
        disbandAlliance(c: Client, allianceId: number): void
        displayGuildRanks(): void
        dispose(): void
        divideTeams(): void
        doGachapon(): void
        fieldLobbied(field: number): boolean
        fieldLobbied2(field: number): boolean
        fieldTaken(field: number): boolean
        fieldTaken2(field: number): boolean
        forceCompleteQuest(id: number): boolean
        forceStartQuest(id: number): boolean
        gainExp(gain: number): void
        gainMeso(gain: number): void
        gainTameness(tameness: number): void
        getAllianceCapacity(): number
        getAvailableMasteryBooks(): Object[]
        getAvailableSkillBooks(): Object[]
        getChrById(id: number): Character
        getCosmeticItem(itemid: number): number
        getEvent(): Event
        getGender(): number
        getInputNumberLevel(nextLevel: string, text: string, def: number, min: number, max: number): void
        getInputTextLevel(nextLevel: string, text: string): void
        getItemEffect(itemId: number): StatEffect
        getJobName(id: number): string
        getMapleCharacter(player: string): Character
        getMeso(): number
        getName(): string
        getNamesWhoDropsItem(itemId: number): Object[]
        getNextLevelContext(): NextLevelContext
        getNpc(): number
        getNpcObjectId(): number
        getParty(): Party
        getPlayerNPCByScriptid(scriptId: number): PlayerNPC
        getPnpcInputNumberLevel(nextLevel: string, text: string, def: number, min: number, max: number, speaker: number): void
        getPnpcInputTextLevel(nextLevel: string, text: string, speaker: number): void
        getScriptName(): string
        getSkillBookInfo(itemid: number): string
        getText(): string
        hasMerchant(): boolean
        hasMerchantItems(): boolean
        isCosmeticEquipped(itemid: number): boolean
        isItemScript(): boolean
        isUsingOldPqNpcStyle(): boolean
        itemExists(itemid: number): boolean
        itemQuantity(itemid: number): number
        logLeaf(prize: string): void
        mapClock(time: number): void
        maxMastery(): void
        openShopNPC(id: number): void
        partyMembersInMap(): number
        resetItemScript(): void
        resetMap(mapid: number): void
        resetStats(): void
        sendAcceptDecline(text: string): void
        sendAcceptDecline(text: string, speaker: number): void
        sendAcceptDeclineLevel(decLineLevel: string, acceptLevel: string, text: string): void
        sendAcceptDeclineLevel(decLineLevel: string, acceptLevel: string, text: string, speaker: number): void
        sendCPQMapLists(): boolean
        sendCPQMapLists2(): boolean
        sendDefault(): void
        sendDimensionalMirror(text: string): void
        sendGetNumber(text: string, def: number, min: number, max: number): void
        sendGetNumber(text: string, def: number, min: number, max: number, speaker: number): void
        sendGetText(text: string): void
        sendGetText(text: string, speaker: number): void
        sendLastLevel(lastLevel: string, text: string): void
        sendLastLevel(lastLevel: string, text: string, speaker: number): void
        sendLastNextLevel(lastLevel: string, nextLevel: string, text: string): void
        sendLastNextLevel(lastLevel: string, nextLevel: string, text: string, speaker: number): void
        sendMarriageGifts(gifts: []): void
        sendMarriageWishlist(groom: boolean): void
        sendNext(text: string): void
        sendNext(text: string, speaker: number): void
        sendNextLevel(nextLevel: string, text: string): void
        sendNextLevel(nextLevel: string, text: string, speaker: number): void
        sendNextPrev(text: string): void
        sendNextPrev(text: string, speaker: number): void
        sendNextSelectLevel(nextLevel: string, text: string): void
        sendNextSelectLevel(nextLevel: string, text: string, speaker: number): void
        sendOk(text: string): void
        sendOk(text: string, speaker: number): void
        sendOkLevel(nextLevel: string, text: string): void
        sendOkLevel(nextLevel: string, text: string, speaker: number): void
        sendPrev(text: string): void
        sendPrev(text: string, speaker: number): void
        sendSelectLevel(text: string): void
        sendSelectLevel(text: string, speaker: number): void
        sendSelectLevel(prefix: string, text: string): void
        sendSelectLevel(prefix: string, text: string, speaker: number): void
        sendSimple(text: string): void
        sendSimple(text: string, speaker: number): void
        sendStyle(text: string, styles: number[]): void
        sendYesNo(text: string): void
        sendYesNo(text: string, speaker: number): void
        sendYesNoLevel(noLevel: string, yesLevel: string, text: string): void
        sendYesNoLevel(noLevel: string, yesLevel: string, text: string, speaker: number): void
        setFace(face: number): void
        setGetText(text: string): void
        setHair(hair: number): void
        setSkin(color: number): void
        showEffect(effect: string): void
        showFredrick(): void
        startAriantBattle(expedType: ExpeditionType, mapid: number): string
        startCPQ(challenger: Character, field: number): void
        startCPQ2(challenger: Character, field: number): void
        startQuest(id: number): boolean
        upgradeAlliance(): void
    }
    interface PortalPlayerInteraction extends AbstractPlayerInteraction {
        //============ Properties =============
        //============ Functions  =============
        blockPortal(): void
        getPortal(): Portal
        hasLevel30Character(): boolean
        playPortalSound(): void
        runMapScript(): void
        unblockPortal(): void
    }
    interface ReactorActionManager extends AbstractPlayerInteraction {
        //============ Properties =============
        //============ Functions  =============
        createMapMonitor(mapId: number, portal: string): void
        destroyNpc(npcId: number): void
        dispelAllMonsters(num: number, team: number): void
        dropItems(): void
        dropItems(meso: boolean, mesoChance: number, minMeso: number, maxMeso: number): void
        dropItems(meso: boolean, mesoChance: number, minMeso: number, maxMeso: number, minItems: number): void
        dropItems(posX: number, posY: number, meso: boolean, mesoChance: number, minMeso: number, maxMeso: number, minItems: number): void
        dropItems(delayed: boolean, posX: number, posY: number, meso: boolean, mesoChance: number, minMeso: number, maxMeso: number, minItems: number): void
        getPosition(): Point
        getReactor(): Reactor
        hitReactor(): void
        killMonster(id: number): void
        killMonster(id: number, withDrops: boolean): void
        spawnFakeMonster(id: number): void
        spawnMonster(id: number): void
        spawnMonster(id: number, qty: number): void
        spawnMonster(id: number, qty: number, pos: Point): void
        spawnMonster(id: number, qty: number, x: number, y: number): void
        spawnNpc(npcId: number): void
        spawnNpc(npcId: number, pos: Point): void
        sprayItems(): void
        sprayItems(meso: boolean, mesoChance: number, minMeso: number, maxMeso: number): void
        sprayItems(meso: boolean, mesoChance: number, minMeso: number, maxMeso: number, minItems: number): void
        sprayItems(posX: number, posY: number, meso: boolean, mesoChance: number, minMeso: number, maxMeso: number, minItems: number): void
        summonBossDelayed(mobId: number, delayMs: number, x: number, y: number, bgm: string, summonMessage: string): void
    }
    //=================================================================

    type ItemScriptManager = NPCConversationManager

    // @ts-ignore
    interface QuestActionManager extends NPCConversationManager {
        completeQuest(): void
        dispose(): void
        forceCompleteQuest(): boolean
        forceStartQuest(): boolean
        gainExp(gain: number): void
        gainMeso(gain: number): void
        getMedalName(): string
        getQuest(): number
        isStart(): boolean
        startQuest(): void
    }
}

export { };