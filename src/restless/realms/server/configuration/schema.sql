
    create table Account (
        id integer not null auto_increment,
        additionalInfo varchar(2500),
        admin bit not null,
        adventurePoints integer not null,
        created date,
        email varchar(250) unique,
        enabled bit not null,
        identifier varchar(250) unique,
        lastSignedIn datetime,
        locale varchar(10) not null,
        name varchar(100) not null,
        provider varchar(30),
        timezone integer not null check (timezone>=-24 and timezone<=24),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Achievement (
        id varchar(20) not null,
        completionKey varchar(40) not null,
        completionQuantity integer not null check (completionQuantity>=0),
        description varchar(100) not null,
        x integer not null,
        y integer not null,
        name varchar(60) not null unique,
        orderIndex integer not null check (orderIndex>=0),
        pointValue integer not null check (pointValue>=0),
        primary key (id)
    ) ENGINE=InnoDB;

    create table AchievementSet (
        playerName varchar(15) not null,
        pointsTotal integer not null check (pointsTotal>=0),
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table AchievementSet_achievementIds (
        playerName varchar(15) not null,
        element varchar(255)
    ) ENGINE=InnoDB;

    create table Adventure (
        id integer not null auto_increment,
        activeRoomIndex integer not null,
        created datetime not null,
        seed integer not null,
        status varchar(10) not null,
        type varchar(20) not null,
        updated datetime,
        primary key (id)
    ) ENGINE=InnoDB;

    create table AdventureArchetype (
        id varchar(20) not null,
        description varchar(1000) not null,
        mapCode varchar(30),
        maxLevel integer not null check (maxLevel>=0 and maxLevel<=50),
        minLevel integer not null check (minLevel>=0 and minLevel<=50),
        name varchar(30) not null,
        worldMapHeight integer not null check (worldMapHeight>=0 and worldMapHeight<=220),
        worldMapWidth integer not null check (worldMapWidth>=0 and worldMapWidth<=500),
        worldMapX integer not null check (worldMapX>=-100 and worldMapX<=1000),
        worldMapY integer not null check (worldMapY>=-100 and worldMapY<=250),
        primary key (id)
    ) ENGINE=InnoDB;

    create table AdventureMap (
        id integer not null auto_increment,
        adventure varchar(20) not null,
        serializedForm varchar(2500),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Adventure_PlayerCharacter (
        Adventure_id integer not null,
        participants_name varchar(15) not null,
        orderIndex integer not null,
        primary key (Adventure_id, orderIndex)
    ) ENGINE=InnoDB;

    create table ChatMessage (
        id integer not null auto_increment,
        channel varchar(20) not null,
        content varchar(1000) not null,
        occurred datetime,
        playerName varchar(15) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Combat (
        id integer not null auto_increment,
        activeRoundNumber integer not null,
        combatType varchar(3) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table CombatAction (
        id integer not null auto_increment,
        actionId integer,
        actionType char(1) not null,
        source integer not null check (source>=0 and source<=7),
        target integer not null check (target>=0 and target<=7),
        combatRoundId integer not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table CombatAction_effectResults (
        combatActionId integer not null,
        additionalRounds integer not null,
        quantity integer not null,
        source integer not null,
        target integer not null,
        type varchar(10),
        orderIndex integer not null,
        primary key (combatActionId, orderIndex)
    ) ENGINE=InnoDB;

    create table CombatRound (
        id integer not null auto_increment,
        roundNumber integer not null,
        state varchar(10) not null,
        combatId integer not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Combat_participants (
        combatId integer not null,
        name varchar(255),
        role integer,
        type integer,
        orderIndex integer not null,
        primary key (combatId, orderIndex)
    ) ENGINE=InnoDB;

    create table Effect (
        id integer not null auto_increment,
        effectType varchar(10) not null,
        maxAdditionalRounds integer not null check (maxAdditionalRounds>=0),
        maxQuantity integer not null check (maxQuantity>=0),
        minAdditionalRounds integer not null check (minAdditionalRounds>=0),
        minQuantity integer not null check (minQuantity>=0),
        percentChance integer not null check (percentChance>=0 and percentChance<=100),
        percentChancePerLevel integer not null check (percentChancePerLevel>=0 and percentChancePerLevel<=100),
        quantityPerLevel integer not null check (quantityPerLevel>=0),
        targeting varchar(10) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Encounter (
        id varchar(50) not null,
        boss bit not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Encounter_mobChances (
        encounterId varchar(50) not null,
        max integer not null,
        min integer not null,
        mobArchetype varchar(20),
        percentChance integer not null
    ) ENGINE=InnoDB;

    create table Equipment (
        playerName varchar(15) not null,
        accessory integer,
        chest integer,
        head integer,
        legs integer,
        perkOne integer,
        perkTwo integer,
        weapon integer,
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table Inventory (
        playerName varchar(15) not null,
        currency integer not null check (currency>=0),
        tokens integer not null check (tokens>=0),
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table Inventory_Item (
        inventoryPlayerName varchar(15) not null,
        itemId integer not null,
        orderIndex integer not null,
        primary key (inventoryPlayerName, orderIndex)
    ) ENGINE=InnoDB;

    create table Item (
        id integer not null,
        x integer not null,
        y integer not null,
        name varchar(50) not null unique,
        summary varchar(40),
        minLevel integer not null check (minLevel>=1 and minLevel<=50),
        msrp integer not null check (msrp>=0 and msrp<=10000000),
        rarity integer not null check (rarity>=0 and rarity<=4),
        requiredProfession varchar(255),
        type varchar(10) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Item_Effect (
        Item_id integer not null,
        effects_id integer not null,
        orderIndex integer not null,
        primary key (Item_id, orderIndex),
        unique (effects_id)
    ) ENGINE=InnoDB;

    create table MailMessage (
        id integer not null auto_increment,
        attachment1 integer,
        attachment2 integer,
        attachment3 integer,
        attachment4 integer,
        attachment5 integer,
        attachmentsRetrieved bit not null,
        content varchar(4000) not null,
        currency integer not null check (currency>=0),
        deleted bit not null,
        fromName varchar(15),
        readTimestamp datetime,
        sentTimestamp datetime,
        toName varchar(15),
        tokens integer not null check (tokens>=0),
        xp integer not null check (xp>=0),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Mob (
        id integer not null auto_increment,
        hitpoints integer not null check (hitpoints>=0),
        level integer not null check (level>=1 and level<=50),
        mana integer not null check (mana>=0),
        maxHitpoints integer not null check (maxHitpoints>=1),
        maxMana integer not null check (maxMana>=0),
        archetype varchar(20),
        primary key (id)
    ) ENGINE=InnoDB;

    create table MobArchetype (
        id varchar(20) not null,
        boss bit not null,
        image varchar(20) not null,
        immunities varchar(40),
        level integer not null check (level>=1 and level<=50),
        maxHitpoints integer not null check (maxHitpoints>=1),
        maxMana integer not null check (maxMana>=0),
        minHitpoints integer not null check (minHitpoints>=1),
        minMana integer not null check (minMana>=0),
        name varchar(30) not null,
        resistances varchar(40),
        treasureTable varchar(20) not null,
        upkeepCost integer not null check (upkeepCost>=0),
        weaknesses varchar(40),
        primary key (id)
    ) ENGINE=InnoDB;

    create table MobArchetype_Skill (
        id varchar(20) not null,
        skillId integer not null,
        orderIndex integer not null,
        primary key (id, orderIndex)
    ) ENGINE=InnoDB;

    create table Party (
        id integer not null auto_increment,
        leader varchar(15) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Party_members (
        partyId integer not null,
        element varchar(255),
        orderIndex integer not null,
        primary key (partyId, orderIndex)
    ) ENGINE=InnoDB;

    create table Perk (
        id integer not null,
        x integer not null,
        y integer not null,
        name varchar(50) not null unique,
        summary varchar(40),
        description varchar(100),
        minLevel integer not null check (minLevel>=1 and minLevel<=50),
        msrp integer not null check (msrp>=0 and msrp<=1000000),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Perk_Effect (
        Perk_id integer not null,
        effects_id integer not null,
        orderIndex integer not null,
        primary key (Perk_id, orderIndex),
        unique (effects_id)
    ) ENGINE=InnoDB;

    create table Perkset (
        playerName varchar(15) not null,
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table Perkset_Perk (
        playerName varchar(15) not null,
        perkId integer not null,
        orderIndex integer not null,
        primary key (playerName, orderIndex)
    ) ENGINE=InnoDB;

    create table PlayerCharacter (
        name varchar(15) not null,
        hitpoints integer not null check (hitpoints>=0),
        level integer not null check (level>=1 and level<=50),
        mana integer not null check (mana>=0),
        maxHitpoints integer not null check (maxHitpoints>=1),
        maxMana integer not null check (maxMana>=0),
        accountId integer not null,
        activeAdventureId integer,
        created date not null,
        enabled bit not null,
        gender char(1) not null,
        lastSignedIn datetime,
        profession varchar(20) not null,
        quickslots varchar(80) not null,
        xp integer not null check (xp>=0),
        primary key (name)
    ) ENGINE=InnoDB;

    create table Profession (
        id varchar(20) not null,
        hitpointsPerLevel integer not null check (hitpointsPerLevel>=0),
        initialEquipment varchar(100),
        initialHitpoints integer not null check (initialHitpoints>=1),
        initialItems varchar(300),
        initialMana integer not null check (initialMana>=1),
        initialQuickslots varchar(80) not null,
        initialSkills varchar(300),
        manaPerLevel integer not null check (manaPerLevel>=0),
        minLevel integer not null check (minLevel>=1 and minLevel<=50),
        name varchar(30),
        primary key (id)
    ) ENGINE=InnoDB;

    create table PvpCombat (
        id integer not null auto_increment,
        combat_id integer,
        primary key (id)
    ) ENGINE=InnoDB;

    create table PvpCombat_Mob (
        pvpId integer not null,
        mobId integer not null,
        orderIndex integer not null,
        primary key (pvpId, orderIndex),
        unique (mobId)
    ) ENGINE=InnoDB;

    create table PvpDefenses (
        playerName varchar(15) not null,
        enemiesString varchar(255),
        skillsString varchar(255),
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table Quest (
        id varchar(20) not null,
        completionAdventure varchar(255),
        completionItem integer,
        completionMobArchetype varchar(20),
        completionQuantity integer not null check (completionQuantity>=1 and completionQuantity<=100),
        completionText varchar(4000) not null,
        introText varchar(4000) not null,
        name varchar(60) not null unique,
        progressText varchar(80) not null,
        qualificationItem integer,
        qualificationLevel integer not null check (qualificationLevel>=0 and qualificationLevel<=50),
        qualificationProfession varchar(20),
        repeatable bit not null,
        rewardSkill integer,
        rewardXp integer not null check (rewardXp>=0 and rewardXp<=1000000),
        suggestedLevel integer not null check (suggestedLevel>=1 and suggestedLevel<=50),
        primary key (id)
    ) ENGINE=InnoDB;

    create table QuestProgress (
        id integer not null auto_increment,
        completions integer not null,
        currentProgress integer not null check (currentProgress>=0 and currentProgress<=100),
        currentStatus varchar(255) not null,
        playerCharacter varchar(15) not null,
        quest varchar(60) not null,
        primary key (id),
        unique (playerCharacter, quest)
    ) ENGINE=InnoDB;

    create table Room (
        id integer not null auto_increment,
        roomIndex integer not null,
        state varchar(10) not null,
        type varchar(10) not null,
        adventureId integer not null,
        combatId integer,
        primary key (id),
        unique (adventureId, roomIndex)
    ) ENGINE=InnoDB;

    create table Room_Item (
        Room_id integer not null,
        contents_id integer not null,
        orderIndex integer not null,
        primary key (Room_id, orderIndex)
    ) ENGINE=InnoDB;

    create table Room_Mob (
        roomId integer not null,
        mobId integer not null,
        orderIndex integer not null,
        primary key (roomId, orderIndex),
        unique (mobId)
    ) ENGINE=InnoDB;

    create table Session (
        id varchar(36) not null,
        accountId integer,
        active bit not null,
        characterName varchar(15),
        started datetime not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Shop (
        id varchar(20) not null,
        name varchar(60) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Shop_Item (
        shopId varchar(20) not null,
        itemId integer not null,
        orderIndex integer not null,
        primary key (shopId, orderIndex)
    ) ENGINE=InnoDB;

    create table Skill (
        id integer not null,
        x integer not null,
        y integer not null,
        name varchar(50) not null unique,
        summary varchar(40),
        cooldown integer not null check (cooldown>=0 and cooldown<=10),
        manaCost integer not null,
        manaCostPerLevel integer not null,
        warmup integer not null check (warmup>=0 and warmup<=10),
        primary key (id)
    ) ENGINE=InnoDB;

    create table SkillTree (
        profession varchar(20) not null,
        tree varchar(1000),
        primary key (profession)
    ) ENGINE=InnoDB;

    create table Skill_Effect (
        Skill_id integer not null,
        effects_id integer not null,
        orderIndex integer not null,
        primary key (Skill_id, orderIndex),
        unique (effects_id)
    ) ENGINE=InnoDB;

    create table Skillset (
        playerName varchar(15) not null,
        primary key (playerName)
    ) ENGINE=InnoDB;

    create table Skillset_Skill (
        playerName varchar(15) not null,
        skillId integer not null,
        orderIndex integer not null,
        primary key (playerName, orderIndex)
    ) ENGINE=InnoDB;

    create table Stash (
        accountId integer not null,
        currency integer not null check (currency>=0),
        primary key (accountId)
    ) ENGINE=InnoDB;

    create table Stash_Item (
        stashAccountId integer not null,
        itemId integer not null,
        orderIndex integer not null,
        primary key (stashAccountId, orderIndex)
    ) ENGINE=InnoDB;

    create table Statistic (
        playerName varchar(255) not null,
        statistic varchar(255) not null,
        amount integer not null check (amount>=0),
        primary key (playerName, statistic)
    ) ENGINE=InnoDB;

    create table Tip (
        id integer not null,
        content varchar(1000),
        maxLevel integer not null check (maxLevel>=0 and maxLevel<=50),
        minLevel integer not null check (minLevel>=0 and minLevel<=50),
        primary key (id)
    ) ENGINE=InnoDB;

    create table TreasureTable (
        id varchar(20) not null,
        maxCurrency integer not null check (maxCurrency>=0),
        maxTokens integer not null check (maxTokens>=0),
        minCurrency integer not null check (minCurrency>=0),
        minTokens integer not null check (minTokens>=0),
        primary key (id)
    ) ENGINE=InnoDB;

    create table TreasureTable_itemChances (
        treasureTableId varchar(20) not null,
        itemId integer,
        tenthPercentChance integer not null
    ) ENGINE=InnoDB;

    create index account_name on Account (name);

    alter table AchievementSet_achievementIds 
        add index FK44139BF53D38BA0 (playerName), 
        add constraint FK44139BF53D38BA0 
        foreign key (playerName) 
        references AchievementSet (playerName);

    alter table Adventure_PlayerCharacter 
        add index FK71E431D91B564E6A (participants_name), 
        add constraint FK71E431D91B564E6A 
        foreign key (participants_name) 
        references PlayerCharacter (name);

    alter table Adventure_PlayerCharacter 
        add index FK71E431D9C3DD893A (Adventure_id), 
        add constraint FK71E431D9C3DD893A 
        foreign key (Adventure_id) 
        references Adventure (id);

    alter table CombatAction 
        add index FK2158B66A164F71D7 (combatRoundId), 
        add constraint FK2158B66A164F71D7 
        foreign key (combatRoundId) 
        references CombatRound (id);

    alter table CombatAction_effectResults 
        add index FKD6BF6C103BDCB92F (combatActionId), 
        add constraint FKD6BF6C103BDCB92F 
        foreign key (combatActionId) 
        references CombatAction (id);

    alter table CombatRound 
        add index FK9EEFA2DAAC4FA7DB (combatId), 
        add constraint FK9EEFA2DAAC4FA7DB 
        foreign key (combatId) 
        references Combat (id);

    alter table Combat_participants 
        add index FK324516EBAC4FA7DB (combatId), 
        add constraint FK324516EBAC4FA7DB 
        foreign key (combatId) 
        references Combat (id);

    alter table Encounter_mobChances 
        add index FK406BD74BD71CF2A4 (encounterId), 
        add constraint FK406BD74BD71CF2A4 
        foreign key (encounterId) 
        references Encounter (id);

    alter table Inventory_Item 
        add index FKC239D456AD1AD170 (inventoryPlayerName), 
        add constraint FKC239D456AD1AD170 
        foreign key (inventoryPlayerName) 
        references Inventory (playerName);

    alter table Inventory_Item 
        add index FKC239D4565846111A (itemId), 
        add constraint FKC239D4565846111A 
        foreign key (itemId) 
        references Item (id);

    alter table Item_Effect 
        add index FKFD59C43DC4FB0FA4 (effects_id), 
        add constraint FKFD59C43DC4FB0FA4 
        foreign key (effects_id) 
        references Effect (id);

    alter table Item_Effect 
        add index FKFD59C43D1CA9BB73 (Item_id), 
        add constraint FKFD59C43D1CA9BB73 
        foreign key (Item_id) 
        references Item (id);

    create index MailMessage_toName on MailMessage (toName);

    create index MailMessage_fromName on MailMessage (fromName);

    alter table MobArchetype_Skill 
        add index MobArchetype_Id_FK (id), 
        add constraint MobArchetype_Id_FK 
        foreign key (id) 
        references MobArchetype (id);

    alter table MobArchetype_Skill 
        add index MobArchetype_Skill_FK (skillId), 
        add constraint MobArchetype_Skill_FK 
        foreign key (skillId) 
        references Skill (id);

    alter table Party_members 
        add index FK4BD774A0302919B7 (partyId), 
        add constraint FK4BD774A0302919B7 
        foreign key (partyId) 
        references Party (id);

    alter table Perk_Effect 
        add index FK77045002C4FB0FA4 (effects_id), 
        add constraint FK77045002C4FB0FA4 
        foreign key (effects_id) 
        references Effect (id);

    alter table Perk_Effect 
        add index FK770450021A0AA298 (Perk_id), 
        add constraint FK770450021A0AA298 
        foreign key (Perk_id) 
        references Perk (id);

    alter table Perkset_Perk 
        add index Perkset_Perk_FK (perkId), 
        add constraint Perkset_Perk_FK 
        foreign key (perkId) 
        references Perk (id);

    alter table Perkset_Perk 
        add index Perkset_Player_FK (playerName), 
        add constraint Perkset_Player_FK 
        foreign key (playerName) 
        references Perkset (playerName);

    alter table PvpCombat 
        add index FKD48B369E64BD2712 (combat_id), 
        add constraint FKD48B369E64BD2712 
        foreign key (combat_id) 
        references Combat (id);

    alter table PvpCombat_Mob 
        add index FK98DF15BF9D8F71E7 (pvpId), 
        add constraint FK98DF15BF9D8F71E7 
        foreign key (pvpId) 
        references PvpCombat (id);

    alter table PvpCombat_Mob 
        add index FK98DF15BF10069B0B (mobId), 
        add constraint FK98DF15BF10069B0B 
        foreign key (mobId) 
        references Mob (id);

    create index playerCharacterIndex on QuestProgress (playerCharacter);

    alter table Room 
        add index FK26F4FBAC4FA7DB (combatId), 
        add constraint FK26F4FBAC4FA7DB 
        foreign key (combatId) 
        references Combat (id);

    alter table Room 
        add index FK26F4FBE06CA8BB (adventureId), 
        add constraint FK26F4FBE06CA8BB 
        foreign key (adventureId) 
        references Adventure (id);

    alter table Room_Item 
        add index FK37BF97F730CADDAB (Room_id), 
        add constraint FK37BF97F730CADDAB 
        foreign key (Room_id) 
        references Room (id);

    alter table Room_Item 
        add index FK37BF97F787630A0C (contents_id), 
        add constraint FK37BF97F787630A0C 
        foreign key (contents_id) 
        references Item (id);

    alter table Room_Mob 
        add index FKF98A5D9CA76421E2 (roomId), 
        add constraint FKF98A5D9CA76421E2 
        foreign key (roomId) 
        references Room (id);

    alter table Room_Mob 
        add index FKF98A5D9C10069B0B (mobId), 
        add constraint FKF98A5D9C10069B0B 
        foreign key (mobId) 
        references Mob (id);

    alter table Shop_Item 
        add index FKF9FA56BC6AF17A1D (shopId), 
        add constraint FKF9FA56BC6AF17A1D 
        foreign key (shopId) 
        references Shop (id);

    alter table Shop_Item 
        add index FKF9FA56BC5846111A (itemId), 
        add constraint FKF9FA56BC5846111A 
        foreign key (itemId) 
        references Item (id);

    alter table Skill_Effect 
        add index FK69281F7FC4FB0FA4 (effects_id), 
        add constraint FK69281F7FC4FB0FA4 
        foreign key (effects_id) 
        references Effect (id);

    alter table Skill_Effect 
        add index FK69281F7F5BEC431B (Skill_id), 
        add constraint FK69281F7F5BEC431B 
        foreign key (Skill_id) 
        references Skill (id);

    alter table Skillset_Skill 
        add index Skillset_Skill_FK (skillId), 
        add constraint Skillset_Skill_FK 
        foreign key (skillId) 
        references Skill (id);

    alter table Skillset_Skill 
        add index Skillset_Player_FK (playerName), 
        add constraint Skillset_Player_FK 
        foreign key (playerName) 
        references Skillset (playerName);

    alter table Stash_Item 
        add index FK396640DD5846111A (itemId), 
        add constraint FK396640DD5846111A 
        foreign key (itemId) 
        references Item (id);

    alter table Stash_Item 
        add index FK396640DDFC3A01CD (stashAccountId), 
        add constraint FK396640DDFC3A01CD 
        foreign key (stashAccountId) 
        references Stash (accountId);

    create index StatisticKeyIndex on Statistic (statistic);

    create index StatisticPlayerNameIndex on Statistic (playerName);

    alter table TreasureTable_itemChances 
        add index FK204B0B2C6EBDE15C (treasureTableId), 
        add constraint FK204B0B2C6EBDE15C 
        foreign key (treasureTableId) 
        references TreasureTable (id);
