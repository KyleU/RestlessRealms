package restless.realms.server.combat.pvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.account.AccountDao;
import restless.realms.server.chat.ChatMessageDao;
import restless.realms.server.combat.Combat;
import restless.realms.server.combat.Combat.CombatType;
import restless.realms.server.combat.CombatParticipant;
import restless.realms.server.combat.CombatParticipant.Role;
import restless.realms.server.combat.CombatParticipant.Type;
import restless.realms.server.combat.CombatRound;
import restless.realms.server.combat.CombatState;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.mail.MailDao;
import restless.realms.server.mob.Mob;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.session.Session;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.ScaleOptions;

@Repository
public class PvpCombatDao extends AbstractDao<PvpCombat> {
    private static final Log log = LogFactory.getLog(PvpCombatDao.class);
    
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private StatisticsDao statisticsDao;

    @Autowired
    private MobDao mobDao;
    
    @Autowired
    private ChatMessageDao chatMessageDao;
    
    @Autowired
    private MailDao mailDao;
    
    @Autowired
    private SkillDao skillDao;

    private Map<String, PvpDefenses> defaultDefensesByProfession = new HashMap<String, PvpDefenses>();
    
	public PvpCombatDao() {
	}
	
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public int start(Session session, String enemy) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        if(playerCharacter.getActiveAdventureId() != null) {
            throw new IllegalStateException("Please finish your current adventure before attempting to duel.");
        }

        accountDao.spendAdventurePoints(session.getAccountId(), 1);

        List<CombatParticipant> participants = new ArrayList<CombatParticipant>();

        participants.add(new CombatParticipant(Role.ALLY, playerCharacter.getName(), Type.PLAYER));
        enemy = playerCharacterDao.getProperName(enemy);
        PlayerCharacter enemyCharacter = playerCharacterDao.get(enemy);
        if(enemyCharacter == null) {
            throw new IllegalStateException("There is no player named \"" + enemy + "\" to duel.");
        }
        if(playerCharacter.getName().equalsIgnoreCase(enemyCharacter.getName())) {
            throw new IllegalArgumentException("You can't duel yourself, silly.");
        }
        PvpDefenses enemyDefenses = getDefenses(enemyCharacter);
        
        participants.add(new CombatParticipant(Role.ENEMY, enemyCharacter.getName(), Type.PLAYER));

        List<Mob> enemyMobs = new ArrayList<Mob>();
        int hp = enemyCharacter.getMaxHitpoints();
        int mana = enemyCharacter.getMaxMana();
        Mob playerMob = new Mob(null, enemyCharacter.getName(), enemyCharacter.getLevel(), hp, mana);
        enemyMobs.add(playerMob);

        for(String mobType : enemyDefenses.getEnemies()) {
            Mob mob = mobDao.createMob(mobType);
            participants.add(new CombatParticipant(Role.ENEMY, mob.getArchetype(), Type.MOB));
            enemyMobs.add(mob);
        }
        
        Combat combat = new Combat(null, null, CombatType.PvP, participants);
        template.save(combat);
        startRound(combat);
        
        PvpCombat pvpCombat = new PvpCombat();
        pvpCombat.setCombat(combat);
        pvpCombat.setEnemies(enemyMobs);
        template.save(pvpCombat);
        
        log.info("Created PvP combat " + combat.getId() + ": " + playerCharacter.getName() + " vs. " + enemyCharacter.getName() + ".");
        
        int negAdventureId = (-combat.getId());
        playerCharacter.setActiveAdventureId(negAdventureId);
        
        statisticsDao.increment(playerCharacter.getName(), "spend-ap");

        return combat.getId();
    }

    private void startRound(Combat combat) {
        CombatRound round = new CombatRound();
        round.setCombat(combat);
        round.setRoundNumber(combat.getRounds().size());
        template.save(round);
        combat.getRounds().add(round);
        combat.setActiveRoundNumber(round.getRoundNumber());
    }

    public void onVictory(CombatState combatState) {
        log.debug("PvP victory for combat " + combatState.getCombat().getId() + "!");
        
        int numAllies = combatState.getAllies().size();
		if(numAllies != 1) {
			throw new IllegalStateException("Not yet.");
		}
        
        int totalDuelScore = 0;
        int totalXp = 0;
        int totalCurrency = 0;
        int totalTokens = 0;
        for(EffectTarget enemy : combatState.getEnemies()) {
            if(enemy instanceof Mob) {
                Mob mob = (Mob)enemy;
                MobArchetype archetype = mobDao.getArchetype(mob.getArchetype());
                int level = archetype.getLevel();
                int xp = level * ScaleOptions.MOB_XP_PER_LEVEL;
                if(archetype.isBoss()) {
                    xp = xp * 10;
                }
                totalXp += xp;
                log.debug("PvP death of enemy mob " + archetype.getName() + " resulted in " + xp + " xp.");
            } else {
                PlayerCharacter playerCharacter = (PlayerCharacter)enemy;
                PlayerCharacter ally = combatState.getAllies().get(0);
                Map<String, Object> rewards = getRewards(ally.getName(), playerCharacter.getName());
                Integer duelScore = (Integer)rewards.get("duelscore");
                totalDuelScore += duelScore;
                totalCurrency += (Integer)rewards.get("gold");
                totalTokens += (Integer)rewards.get("tokens");
                totalXp += (Integer)rewards.get("xp");
                String message = PvpResultStrings.getVictoryMessage(playerCharacter.getName());
                chatMessageDao.post(ally.getName(), "Pvp", message);
                mailDao.send(ally.getName(), playerCharacter.getName(), "You were just defeated in a duel by " + ally.getName() + ", a level " + ally.getLevel() + " " + ally.getProfession().toLowerCase() + ". Your Duel Score has been reduced by " + (duelScore * ScaleOptions.DUEL_DEFENSIVE_LOSS_MULTIPLIER) + ". Try to take your revenge!");
                statisticsDao.increment(playerCharacter.getName(), "duelscore", duelScore * ScaleOptions.DUEL_DEFENSIVE_LOSS_MULTIPLIER);
                statisticsDao.increment(playerCharacter.getName(), "pvp-defensive-loss");
                statisticsDao.increment(playerCharacter.getName(), "pvp-defensive-loss-" + ally.getProfession());
            }
        }

        for(PlayerCharacter pc : combatState.getAllies()) {
            playerCharacterDao.addXp(pc, (totalXp / numAllies));
            if(totalDuelScore > 0) {
                statisticsDao.increment(pc.getName(), "duelscore", (totalDuelScore * ScaleOptions.DUEL_DEFENSIVE_WIN_MULTIPLIER) / numAllies);
            }
            if(totalCurrency > 0) {
                inventoryDao.addCurrency(pc.getName(), totalCurrency / numAllies);
            }
            if(totalTokens > 0) {
                inventoryDao.addTokens(pc.getName(), totalTokens / numAllies);
            }
            for(EffectTarget enemy : combatState.getEnemies()) {
                if(enemy instanceof Mob) {
                    Mob mob = (Mob)enemy;
                    statisticsDao.increment(pc.getName(), "kill-" + mob.getArchetype());
                    MobArchetype archetype = mobDao.getArchetype(mob.getArchetype());
                    if(archetype.isBoss()) {
                        statisticsDao.increment(pc.getName(), "kill-boss");
                    }
                } else {
                    PlayerCharacter enemyPlayer = (PlayerCharacter)enemy;
                    statisticsDao.increment(pc.getName(), "pvp-offensive-win");
                    statisticsDao.increment(pc.getName(), "pvp-offensive-win-" + enemyPlayer.getProfession());
                }
            }
            pc.setActiveAdventureId(null);
            pc.setHitpoints(pc.getMaxHitpoints());
            pc.setMana(pc.getMaxMana());
        }
    }

    public void onLoss(CombatState combatState) {
        String enemyName = ((PlayerCharacter)combatState.getEnemies().get(0)).getName();
        for(PlayerCharacter pc : combatState.getAllies()) {
            pc.setHitpoints(pc.getMaxHitpoints());
            pc.setMana(pc.getMaxMana());
            pc.setActiveAdventureId(null);

            int oldXp = pc.getXp();
            
            int requiredXp = playerCharacterDao.requiredXpByLevel[pc.getLevel()];
            int xpLoss = (requiredXp / 20);
            
            pc.setXp(oldXp - xpLoss );
            if(pc.getXp() < 0) {
                pc.setXp(0);
            }

            Map<String, Object> rewards = getRewards(pc.getName(), enemyName);
            Integer duelScore = (Integer)rewards.get("duelscore");
            statisticsDao.increment(pc.getName(), "duelscore", duelScore * ScaleOptions.DUEL_OFFENSIVE_LOSS_MULTIPLIER);
            statisticsDao.increment(pc.getName(), "pvp-offensive-loss");
            statisticsDao.increment(pc.getName(), "pvp-offensive-loss-" + pc.getProfession());
        }
        String allyName = combatState.getAllies().get(0).getName();
        for(EffectTarget enemy : combatState.getEnemies()) {
            if(enemy instanceof PlayerCharacter) {
                PlayerCharacter pc = (PlayerCharacter)enemy;
                Map<String, Object> rewards = getRewards(pc.getName(), allyName);
                Integer duelScore = (Integer)rewards.get("duelscore");
                int defensiveWinScoreIncrease = duelScore * ScaleOptions.DUEL_DEFENSIVE_WIN_MULTIPLIER;
                statisticsDao.increment(pc.getName(), "duelscore", defensiveWinScoreIncrease);
                statisticsDao.increment(pc.getName(), "pvp-defensive-win");
                statisticsDao.increment(pc.getName(), "pvp-defensive-win-" + pc.getProfession());

                String message = PvpResultStrings.getDefeatMessage(allyName);
                chatMessageDao.post(pc.getName(), "Pvp", message);
                
                int numGold = ((Integer)rewards.get("gold")) / 10;
                int numTokens = ((Integer)rewards.get("tokens")) / 10;
                int numXp = ((Integer)rewards.get("xp")) / 10;
                mailDao.send(allyName, pc.getName(), "You defended yourself in a duel attack by " + allyName + ". Congratulations! Your Duel Score has increased by " + defensiveWinScoreIncrease + ", and we've attached your reward to this message.", null, null, null, numGold, numTokens, numXp, false);
            }
        }
    }

    public Map<String, Object> getRewards(String allyName, String enemyName) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        
        PlayerCharacter ally = playerCharacterDao.get(allyName); 
        PlayerCharacter enemy = playerCharacterDao.get(enemyName);

        int levelDelta = ally.getLevel() - enemy.getLevel();
        if(levelDelta < -5) {
            throw new IllegalStateException("You cannot duel someone more than five levels above you.");
        }
        if(levelDelta > 5) {
            throw new IllegalStateException("You cannot duel someone more than five levels below you.");
        }
        
        if(ally.getLevel() < 5) {
            throw new IllegalStateException("You must be level 5 before you duel.");
        }
        if(enemy.getLevel() < 5) {
            throw new IllegalStateException("Your opponent must be at least level 5 to duel.");
        }

        float multiplier = (float)(enemy.getLevel() * enemy.getLevel()) / ScaleOptions.MAX_LEVEL;
        multiplier = multiplier + 5;
        
        int levelDifference = enemy.getLevel() - ally.getLevel();
        float differenceScaling = 1 + (levelDifference  * .1f);
        if(differenceScaling < 0.1f) {
            differenceScaling = 0.1f;
        }
        multiplier = multiplier * differenceScaling;
        
        PvpDefenses defenses = getDefenses(enemy);
        int numAdditionalEnemies = defenses.getEnemies().length;
        float additionalEnemiesScaling = 1 + (numAdditionalEnemies  * .1f);
        multiplier = multiplier * additionalEnemiesScaling;
        
        int allyScore = statisticsDao.get(allyName, "duelscore");
        int enemyScore = statisticsDao.get(enemyName, "duelscore");
        float duelScoreScaling = (float)enemyScore / (float)allyScore;
        if(duelScoreScaling < .5f) {
            duelScoreScaling = .5f;
        }
        if(duelScoreScaling > 5) {
            duelScoreScaling = 5;
        }
        multiplier = multiplier * duelScoreScaling;
        
        ret.put("multiplier", multiplier);
        ret.put("duelscore", (int)(multiplier * 100));
        ret.put("gold", (int)(multiplier * 1000));
        ret.put("tokens", (int)(multiplier * 1));
        ret.put("xp", (int)(multiplier * 5000));
        
        ret.put("penalty", playerCharacterDao.requiredXpByLevel[ally.getLevel()] / 20);

        
        return ret;
    }

    @Transactional
    public PvpDefenses getDefenses(PlayerCharacter playerCharacter) {
        PvpDefenses ret = template.get(PvpDefenses.class, playerCharacter.getName());
        if(ret == null) {
            ret = defaultDefensesByProfession.get(playerCharacter.getProfession());
            if(ret == null) {
                ret = new PvpDefenses();
                if(playerCharacter.getProfession().equals("wizard")) {
                    ret.setSkillsString("2002,2000,2001,1");
                } else if(playerCharacter.getProfession().equals("warrior")) {
                    ret.setSkillsString("1000,1002,1001,2");
                } else if(playerCharacter.getProfession().equals("cleric")) {
                    ret.setSkillsString("3000,3001,3002,3");
                } else {
                    throw new IllegalArgumentException("Invalid profession \"" + playerCharacter.getProfession() + "\".");
                }
                ret.setEnemiesString("");
                defaultDefensesByProfession.put(playerCharacter.getProfession(), ret);
            }
        }
        return ret;
    }
    
    @Transactional
    public void setDefenses(String playerName, String defenses) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(playerName);
        PvpDefenses ret = template.get(PvpDefenses.class, playerName);
        if(ret == null) {
            ret = new PvpDefenses();
            ret.setPlayerName(playerName);
        }
        ret.setSkillsString(defenses);
        for(int skillId : ret.getSkills()) {
            skillDao.validateAction(playerCharacter, skillId);
        }
        template.saveOrUpdate(ret);
    }

    @Transactional
    public int setBodyguard(String playerName, String type) {
        PvpDefenses ret = template.get(PvpDefenses.class, playerName);
        if(ret == null) {
            ret = new PvpDefenses();
            ret.setPlayerName(playerName);
            ret.setSkillsString(getDefenses(playerCharacterDao.get(playerName)).getSkillsString());
        }
        if(ret.getEnemies() != null) {
            if(ret.getEnemies().length >= 3) {
                throw new IllegalArgumentException("You have already hired three bodyguards.");
            }
            for(String bodyguard : ret.getEnemies()) {
                if(bodyguard.equals(type)) {
                    throw new IllegalArgumentException("You have already hired the \"" + type + "\" bodyguard.");
                }
            }
        }
        MobArchetype bodyguard = mobDao.getArchetype(type);
        if(bodyguard == null) {
            throw new IllegalArgumentException("Now you're just making up critters.");
        }
        if(bodyguard.isBoss()) {
            throw new IllegalArgumentException("You may not hire bosses.");
        }

        Inventory inv = inventoryDao.get(playerName);
        if(inv.getCurrency() < bodyguard.getUpkeepCost()) {
            throw new IllegalStateException("You do not have the " + bodyguard.getUpkeepCost() + " gold required to hire this bodyguard.");
        }
        inv.setCurrency(inv.getCurrency() - bodyguard.getUpkeepCost());
        
        String bodyguardsString = ret.getEnemiesString();
        if(bodyguardsString == null) {
            bodyguardsString = "";
        }
        if(bodyguardsString.length() > 0) {
            bodyguardsString = bodyguardsString + ",";
        }
        bodyguardsString = bodyguardsString + type;
        ret.setEnemiesString(bodyguardsString);
        template.saveOrUpdate(ret);
        
        statisticsDao.increment(playerName, "hire-bodyguard");

        return inv.getCurrency();
    }

    @Transactional
    public void clearBodyguard(String playerName, String type) {
        PvpDefenses ret = template.get(PvpDefenses.class, playerName);
        if(ret == null) {
            ret = new PvpDefenses();
            ret.setPlayerName(playerName);
            ret.setSkillsString(getDefenses(playerCharacterDao.get(playerName)).getSkillsString());
        }
        boolean removed = false;
        for(String bodyguard : ret.getEnemies()) {
            if(bodyguard.equals(type)) {
                removed = true;
            }
        }
        if(!removed) {
            throw new IllegalArgumentException("You have not hired the \"" + type + "\" bodyguard.");
        }
        String bodyguardsString = ret.getEnemiesString();
        bodyguardsString = bodyguardsString.replace("," + type, "");
        bodyguardsString = bodyguardsString.replace(type, "");
        ret.setEnemiesString(bodyguardsString);
        template.saveOrUpdate(ret);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void chargeUpkeepCosts() {
        List<PvpDefenses> defensesWithBodyguards = template.findByNamedQuery("pvpDefenses.getAllWithBodyguards");
        for(PvpDefenses pvpDefenses : defensesWithBodyguards) {
            int totalUpkeep = 0;
            for(String enemy : pvpDefenses.getEnemies()) {
                MobArchetype mob = mobDao.getArchetype(enemy);
                totalUpkeep += mob.getUpkeepCost();
            }
            log.info("Applying upkeep cost of " + totalUpkeep + " gold for " + pvpDefenses.getPlayerName() + "'s bodyguards (" + pvpDefenses.getEnemiesString() + ").");
            Inventory inventory = inventoryDao.get(pvpDefenses.getPlayerName());
            if(inventory.getCurrency() < totalUpkeep) {
                log.info("Removing (" + pvpDefenses.getEnemiesString() + ") from \"" + pvpDefenses.getPlayerName() + "\", as they don't have " + totalUpkeep + " gold.");
                mailDao.send("System", pvpDefenses.getPlayerName(), "Your bodyguards have left you, as you did not have the " + totalUpkeep + " required gold.");
                pvpDefenses.setEnemiesString(null);
            } else {
                inventory.setCurrency(inventory.getCurrency() - totalUpkeep);
            }
        }
    }

    @Override
    protected Class<?> getManagedClass() {
        return Combat.class;
    }
}
