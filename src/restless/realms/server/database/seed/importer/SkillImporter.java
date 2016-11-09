package restless.realms.server.database.seed.importer;

import java.util.ArrayList;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectType;
import restless.realms.server.effect.TargetingType;
import restless.realms.server.item.IconInfo;
import restless.realms.server.skill.Skill;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class SkillImporter extends DataImporter {
    private Skill s = null;

    public SkillImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("id") != null) {
            if(s != null) {
                persistanceProvider.save(s.getId(), s);
                persistanceProvider.verifyUnique("Skill name", s.getName());
            }
            s = new Skill(
                getInt(e, "id"),
                e.getValue("name"), 
                getInt(e.getValue("warmup"), 0), 
                getInt(e.getValue("cooldown"), 0),
                getInt(e.getValue("manaCost"), 0) * ScaleOptions.COMBAT,
                getInt(e.getValue("manaCostPerLevel"), 0) * ScaleOptions.COMBAT,
                new IconInfo(getInt(e.getValue("iconCol"), 0), getInt(e.getValue("iconRow"), 0)),
                e.getValue("summary"), 
                new ArrayList<Effect>()
            );
        }
        
        if(e.getValue("effectType") != null) {
            EffectType effectType = EffectType.valueOf(e.getValue("effectType"));
            int minQuantity = getInt(e.getValue("minQuantity"), 1);
            int maxQuantity = getInt(e.getValue("maxQuantity"), 1);
            int quantityPerLevel = getInt(e.getValue("quantityPerLevel"), 0);

            if(effectType != EffectType.DEATH && effectType != EffectType.STUN) {
                minQuantity = minQuantity * ScaleOptions.COMBAT;
                maxQuantity = maxQuantity * ScaleOptions.COMBAT;
                quantityPerLevel = quantityPerLevel * ScaleOptions.COMBAT;
            }
            
            if(quantityPerLevel > 0) {
                quantityPerLevel = quantityPerLevel + 0;
            }

            Effect effect = new Effect(
                    null, 
                    effectType,
                    TargetingType.valueOf(e.getValue("targeting")),
                    getInt(e.getValue("percentChance"), 100),
                    getInt(e.getValue("percentChancePerLevel"), 0),
                    minQuantity,
                    maxQuantity,
                    quantityPerLevel,
                    getInt(e.getValue("minAdditionalRounds"), 0),
                    getInt(e.getValue("maxAdditionalRounds"), 0)                        
            );
            s.getEffects().add(effect);
        }
    }
    
    @Override
    public void complete() {
        persistanceProvider.save(s.getId(), s);
        persistanceProvider.verifyUnique("Skill name", s.getName());
    }
}
