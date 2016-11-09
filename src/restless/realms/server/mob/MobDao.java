package restless.realms.server.mob;

import java.util.List;

import org.springframework.stereotype.Repository;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.util.RandomUtils;


@Repository
public class MobDao extends AbstractDao<Mob> {
    public MobDao() {
    }
    
    public MobArchetype getArchetype(String id) {
        MobArchetype ret = template.get(MobArchetype.class, id);
        if(ret == null) {
            throw new IllegalArgumentException("Invalid mob archetype \"" + id + "\".");
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public List<MobArchetype> getAllArchetypes() {
        return template.find("from MobArchetype m order by m.level desc, m.name asc");
    }

    public Mob createMob(String archetypeId) {
        MobArchetype archetype = getArchetype(archetypeId);

        int hitpoints = RandomUtils.getInt(archetype.getMinHitpoints(), archetype.getMaxHitpoints());
        int mana = RandomUtils.getInt(archetype.getMinMana(), archetype.getMaxMana());

        Mob ret = new Mob(null, archetype.getId(), archetype.getLevel(), hitpoints, mana);
        return ret;
    }

    @Override
    protected Class<?> getManagedClass() {
        return Mob.class;
    }
}
