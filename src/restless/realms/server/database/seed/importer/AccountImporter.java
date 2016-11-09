package restless.realms.server.database.seed.importer;

import restless.realms.server.account.Account;
import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.profession.Profession;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class AccountImporter extends DataImporter {
    private Account a = null;
    
    public AccountImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        String email = e.getValue("email");
        if(email != null && !email.equals(".")) {
            a = new Account(
                "Facebook",
                e.getValue("facebookId"),
                email, 
                e.getValue("name"),
                e.getValue("additionalInfo"),
                -5,
                "en_US",
                getBoolean(e.getValue("admin")), 
                getBoolean(e.getValue("enabled")), 
                getDate(e.getValue("created")), 
                null
            );
            persistanceProvider.save(a.getEmail(), a);
        }
        
        if(e.getValue("characterName") != null) {
            Profession profession = persistanceProvider.getObject(Profession.class, e.getValue("characterClass"));
            PlayerCharacter pc = new PlayerCharacter(e.getValue("characterName"), a.getId());
            pc.setGender(e.getValue("characterGender").charAt(0));
            pc.setProfession(profession.getId());
            pc.setMaxHitpoints(profession.getInitialHitpoints());
            pc.setHitpoints(profession.getInitialHitpoints());
            pc.setMaxMana(profession.getInitialMana());
            pc.setMana(profession.getInitialMana());
            pc.setQuickslots(profession.getInitialQuickslots());
            
            //temp boost
            int newLevel = getInt(e.getValue("characterLevel"), 1);
            pc.setLevel(newLevel);
            int additionalHitpoints = (newLevel - 1) * profession.getHitpointsPerLevel();
            pc.setMaxHitpoints(profession.getInitialHitpoints() + additionalHitpoints);
            pc.setHitpoints(profession.getInitialHitpoints() + additionalHitpoints);
            int additionalMana = (newLevel - 1) * profession.getManaPerLevel();
            pc.setMaxMana(profession.getInitialMana() + additionalMana);
            pc.setMana(profession.getInitialMana() + additionalMana);

            persistanceProvider.save(pc.getName(), pc);
        }
    }
}
