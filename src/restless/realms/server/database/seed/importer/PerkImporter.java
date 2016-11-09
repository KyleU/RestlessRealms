package restless.realms.server.database.seed.importer;

import java.util.ArrayList;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectType;
import restless.realms.server.effect.TargetingType;
import restless.realms.server.item.IconInfo;
import restless.realms.server.perk.Perk;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class PerkImporter extends DataImporter {
    private Perk p = null;
    
    public PerkImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("id") != null) {
            if(p != null) {
                persistanceProvider.save(p.getId(), p);
                persistanceProvider.verifyUnique("Perk name", p.getName());
            }
            IconInfo icon = null;
            try {
                icon = new IconInfo(getInt(e.getValue("iconCol"), 0), getInt(e.getValue("iconRow"), 0)); 
            } catch(Exception e2) {
                throw new RuntimeException(e.getValue("name"), e2);
            }
            p = new Perk(
                getInt(e, "id"),
                e.getValue("name"), 
                getInt(e, "minLevel"), 
                getInt(e, "msrp"),
                icon,
                e.getValue("summary"), 
                e.getValue("description"), 
                new ArrayList<Effect>()
            );
        }
        
        if(e.getValue("effectType") != null) {
            Effect effect = new Effect(
                    null, 
                    EffectType.valueOf(e.getValue("effectType")),
                    TargetingType.valueOf(e.getValue("targetingType")),
                    getInt(e.getValue("percentChance"), 100),
                    0,
                    getInt(e.getValue("minQuantity"), 1),
                    getInt(e.getValue("maxQuantity"), 1),
                    0,
                    getInt(e.getValue("minAdditionalRounds"), 0),
                    getInt(e.getValue("maxAdditionalRounds"), 0)                        
            );
            
            if(effect.getEffectType() != EffectType.DEATH && effect.getEffectType() != EffectType.STUN) {
                effect.setMinQuantity(effect.getMinQuantity() * ScaleOptions.COMBAT);
                effect.setMaxQuantity(effect.getMaxQuantity() * ScaleOptions.COMBAT);
            }

            if(effect.isDoWeaponDamage()) {
                throw new IllegalStateException("Perks may not have weapon damage effects.");
            }
            p.getEffects().add(effect);
        }
    }
    
    @Override
    public void complete() {
        persistanceProvider.save(p.getId(), p);
    }
}
