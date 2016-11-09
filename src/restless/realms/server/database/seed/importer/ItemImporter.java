package restless.realms.server.database.seed.importer;

import java.util.ArrayList;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectType;
import restless.realms.server.effect.TargetingType;
import restless.realms.server.item.IconInfo;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemType;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class ItemImporter extends DataImporter {
    private Item i = null;
    
    public ItemImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("id") != null) {
            if(i != null) {
                persistanceProvider.save(i.getId(), i);
                persistanceProvider.verifyUnique("Item name", i.getName());
            }
            IconInfo icon = null;
            try {
                icon = new IconInfo(getInt(e.getValue("iconCol"), 0), getInt(e.getValue("iconRow"), 0)); 
            } catch(Exception e2) {
                throw new RuntimeException(e.getValue("name"), e2);
            }
            i = new Item(
                getInt(e, "id"),
                e.getValue("name"), 
                ItemType.valueOf(e.getValue("type")),
                getInt(e, "minLevel"), 
                e.getValue("requiredClass"),
                getInt(e, "msrp") * ScaleOptions.ECONOMY,
                getInt(e.getValue("rarity"), 0),
                icon,
                e.getValue("summary"), 
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
                throw new IllegalStateException("Items may not have weapon damage effects.");
            }
            i.getEffects().add(effect);
            
            EffectType t = effect.getEffectType();
            if(t == EffectType.DEATH || t == EffectType.DRAIN || t == EffectType.STUN) {
                if(i.getType().isEquipment()) {                
                    throw new IllegalArgumentException("Effect type \"" + t + "\" does not apply to equipment.");
                }
            }
        }
    }
    
    @Override
    public void complete() {
        persistanceProvider.save(i.getId(), i);
    }
}
