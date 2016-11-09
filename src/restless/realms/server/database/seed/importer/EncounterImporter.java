package restless.realms.server.database.seed.importer;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.encounter.Encounter;
import restless.realms.server.encounter.MobChance;
import restless.realms.server.mob.MobArchetype;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class EncounterImporter extends DataImporter {
    private Encounter encounter = null;
    
    public EncounterImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("adventure") != null) {
            if(encounter != null) {
                boolean guaranteedMob = false;
                int maxMobs = 0;
                for(MobChance chance : encounter.getMobChances()) {
                    if(chance.getPercentChance() == 100 && chance.getMin() > 0) {
                        guaranteedMob = true;
                    }
                    maxMobs += chance.getMax();
                }
                if(!guaranteedMob) {
                    throw new IllegalArgumentException("Encounters must have at least one mob 100% of the time.");
                }
                if(maxMobs > 4) {
                    throw new IllegalArgumentException("Encounters may have at most four mobs. This one can have " + maxMobs + ".");
                }
                persistanceProvider.save(encounter.getId(), encounter);
            }
            String adventureArchtype = e.getValue("adventure");
            char code = e.getValue("code").charAt(0);
            boolean boss = e.getValue("boss") == null ? false : e.getValue("boss").equals("TRUE"); 
            encounter = new Encounter(adventureArchtype + "-" + code, boss);
        }
        MobArchetype archetype = persistanceProvider.getObject(MobArchetype.class, e.getValue("mob"));
        MobChance mobChance = new MobChance(
                archetype.getId(),
                getInt(e, "min"),
                getInt(e, "max"),
                getInt(e.getValue("percentChance"), 100)
        );
        encounter.getMobChances().add(mobChance);
    }
    
    @Override
    public void complete() {
        persistanceProvider.save(encounter.getId(), encounter);
    }
}
