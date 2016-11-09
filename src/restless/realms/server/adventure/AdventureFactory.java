package restless.realms.server.adventure;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import restless.realms.server.adventure.map.AdventureMap;
import restless.realms.server.adventure.map.AdventureMapLocation;
import restless.realms.server.encounter.Encounter;
import restless.realms.server.room.Room;
import restless.realms.server.room.RoomState;

@Service
public class AdventureFactory {
    @Autowired
    private HibernateTemplate hibernateTemplate;

    private Map<String, AdventureArchetype> adventureArchetypes; 
    private Map<String, List<AdventureMap>> adventureMaps; 
    private Map<String, Map<Character, Encounter>> encounters;

    public AdventureFactory() {
    }
    
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        adventureArchetypes = new HashMap<String, AdventureArchetype>();
        List<AdventureArchetype> archetypes = hibernateTemplate.findByNamedQuery("adventureArchetype.getAll");
        for(AdventureArchetype archetype : archetypes) {
            adventureArchetypes.put(archetype.getId(), archetype);
        }
        
        adventureMaps = new HashMap<String, List<AdventureMap>>();
        List<AdventureMap> maps = hibernateTemplate.findByNamedQuery("adventureMap.getAll");
        for(AdventureMap map : maps) {
            if(!adventureMaps.containsKey(map.getAdventure())) {
                adventureMaps.put(map.getAdventure(), new ArrayList<AdventureMap>());
            }
            adventureMaps.get(map.getAdventure()).add(map);
        }

        encounters = new HashMap<String, Map<Character, Encounter>>();
        List<Encounter> loadedEncounters = hibernateTemplate.findByNamedQuery("encounter.getAll");
        for(Encounter encounter : loadedEncounters) {
            String adventureArchetype = encounter.getId().substring(0, encounter.getId().length() - 2);
            char code = encounter.getId().charAt(encounter.getId().length() - 1);
            if(!encounters.containsKey(adventureArchetype )) {
                encounters.put(adventureArchetype, new HashMap<Character, Encounter>());
            }
            encounters.get(adventureArchetype).put(code, encounter);
        }
    }

    public AdventureMap getMap(Adventure adventure) {
        if(adventureMaps == null) {
            init();
        }
        List<AdventureMap> permutations = adventureMaps.get(adventure.getType());
        if(permutations == null || permutations.size() == 0) {
            throw new IllegalStateException("No maps for AdventureArchetype \"" + adventure.getType() + "\".");
        }
        int permutation = Math.abs(adventure.getSeed()) % permutations.size();
        AdventureMap map = permutations.get(permutation);
        return map;
    }
    
    public Adventure create(String type, int seed) {
        Adventure a = new Adventure();
        a.setType(type);
        a.setSeed(seed);
        a.setCreated(new Date());

        AdventureMap map = getMap(a);
        for(AdventureMapLocation location : map.getLocations()) {
            a.addRoom(new Room(location.getType()));
        }
        a.setActiveRoomIndex(map.getIntroductionRoomIndex());
        Room room = a.getRooms().get(map.getIntroductionRoomIndex());
        room.setState(RoomState.ACTIVE);
        a.setStatus(Adventure.Status.ACTIVE);
        return a;
    }

    public Encounter getEncounter(Room room) {
        char code = getMap(room.getAdventure()).getLocations().get(room.getRoomIndex()).getCode();
        Encounter encounter = encounters.get(room.getAdventure().getType()).get(code);
        if(encounter == null) {
            throw new IllegalArgumentException("No encounter \"" + code + "\" for adventure \"" + room.getAdventure().getType() + "\"");
        }
        return encounter;
    }

    public Map<String, AdventureArchetype> getAdventureArchetypes() {
        return adventureArchetypes;
        
    }
}
