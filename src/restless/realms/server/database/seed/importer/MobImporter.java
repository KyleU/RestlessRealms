package restless.realms.server.database.seed.importer;

import java.util.ArrayList;
import java.util.List;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.effect.Effect;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.skill.Skill;
import restless.realms.server.treasure.TreasureTable;
import restless.realms.server.util.ScaleOptions;

import com.google.common.base.Splitter;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class MobImporter extends DataImporter {
    public MobImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        List<Skill> skills = new ArrayList<Skill>();
        Iterable<String> skillIds = Splitter.on(",").trimResults().split(e.getValue("skills"));
        for(String skillId : skillIds) {
            Skill skill = persistanceProvider.getObject(Skill.class, Integer.parseInt(skillId.trim()));
            if(skill == null) {
                throw new IllegalArgumentException("Mob " + e.getValue("id") + " references missing skill " + skillId + ".");
            }
            for(Effect effect : skill.getEffects()) {
                if(effect.isDoWeaponDamage()) {
                    throw new IllegalArgumentException("Mob " + e.getValue("id") + " references weapon damage skill " + skillId + ".");
                }
            }
            skills.add(skill);
        }
        TreasureTable table = persistanceProvider.getObject(TreasureTable.class, e.getValue("id"));

        int minHitpoints = getInt(e, "minHitpoints");
        int maxHitpoints = getInt(e, "maxHitpoints");
        if(minHitpoints <= 0) {
            throw new IllegalStateException("Hitpoints must be greater than 0.");
        }
        if(minHitpoints > maxHitpoints) {
            throw new IllegalStateException("Hitpoints: " + minHitpoints + " > " + maxHitpoints);
        }
        int minMana = getInt(e.getValue("minMana"), 0);
        int maxMana = getInt(e.getValue("maxMana"), 0);
        if(minMana > maxMana) {
            throw new IllegalStateException("Mana: " + minMana + " > " + maxMana);
        }
        MobArchetype m = new MobArchetype(
                e.getValue("id"), 
                e.getValue("name"),
                e.getValue("id"),
                getInt(e, "level"),
                getBoolean(e.getValue("boss")),
                getInt(e.getValue("upkeep"), 0),
                minHitpoints * ScaleOptions.COMBAT,
                maxHitpoints * ScaleOptions.COMBAT,
                minMana * ScaleOptions.COMBAT,
                maxMana * ScaleOptions.COMBAT,
                skills,
                table.getId()
        );
        
        String immunities = e.getValue("immunities");
        m.setImmunities(immunities);
        String resistances = e.getValue("resistances");
        m.setResistances(resistances);
        String weaknesses = e.getValue("weaknesses");
        m.setWeaknesses(weaknesses);
        
        persistanceProvider.save(m.getId(), m);
    }
}
