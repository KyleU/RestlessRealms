package restless.realms.server.database.seed.merge;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.achievement.Achievement;
import restless.realms.server.adventure.AdventureArchetype;
import restless.realms.server.adventure.map.AdventureMap;
import restless.realms.server.encounter.Encounter;
import restless.realms.server.item.Item;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.perk.Perk;
import restless.realms.server.profession.Profession;
import restless.realms.server.profession.SkillTree;
import restless.realms.server.quest.Quest;
import restless.realms.server.shop.Shop;
import restless.realms.server.skill.Skill;
import restless.realms.server.tip.Tip;
import restless.realms.server.treasure.TreasureTable;

@Component
public class MergeManager {
    private static final Log log = LogFactory.getLog(MergeManager.class);
    
    @Autowired
    private HibernateTemplate template;

    public MergeManager() {
        super();
    }
    
    public List<MergeDifference> findDifferences(final Map<Class<?>, Map<Serializable, Object>> cache) {
        List<MergeDifference> ret = new ArrayList<MergeDifference>();
        ret.addAll(findDifferences(Item.class, cache));
        ret.addAll(findDifferences(Skill.class, cache));
        ret.addAll(findDifferences(Profession.class, cache));
        ret.addAll(findDifferences(SkillTree.class, cache));
        ret.addAll(findDifferences(Shop.class, cache));
        ret.addAll(findDifferences(AdventureArchetype.class, cache));
        ret.addAll(findDifferences(TreasureTable.class, cache));
        ret.addAll(findDifferences(MobArchetype.class, cache));
        ret.addAll(findDifferences(Encounter.class, cache));
        ret.addAll(findDifferences(AdventureMap.class, cache));
        ret.addAll(findDifferences(Quest.class, cache));
        ret.addAll(findDifferences(Achievement.class, cache));
        ret.addAll(findDifferences(Perk.class, cache));
        ret.addAll(findDifferences(Tip.class, cache));
        return ret;
    }

    public List<MergeDifference> findDifferences(Class<?> type, final Map<Class<?>, Map<Serializable, Object>> cache) {
        log.info("Merging objects of type \"" + type.getSimpleName() + "\".");
        List<MergeDifference> ret = new ArrayList<MergeDifference>();
        Map<Serializable, Object> objects = cache.get(type);
        for(Entry<Serializable, Object> objectEntry : objects.entrySet()) {
            Serializable key = objectEntry.getKey();
            Object currentObject = template.get(type, key);
            Object newObject = objectEntry.getValue();

            if(currentObject == null) {
                MergeDifference difference = new MergeDifference(type, key, null, null, null);
                ret.add(difference);
            } else {
                ret.addAll(findDifferences(type, key, currentObject, newObject));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public List<MergeDifference> findDifferences(Class<?> type, Serializable key, Object currentObject, Object newObject) {
        List<MergeDifference> ret = new ArrayList<MergeDifference>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(type);
        for(PropertyDescriptor propertyDescriptor : descriptors) {
            String propertyName = propertyDescriptor.getName();

            Object currentValue;
            Object newValue;
            try {
                Method readMethod = propertyDescriptor.getReadMethod();
                if(readMethod == null) {
                    throw new RuntimeException("Error reading " + type.getSimpleName() + ":" + propertyName + ".");
                }
                currentValue = readMethod.invoke(currentObject);
                newValue = readMethod.invoke(newObject);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            
            if(newValue != null) {
                if(
                    "locations".equals(propertyName) || 
                    "skillIdsByLevel".equals(propertyName) ||
                    "immunitiesArray".equals(propertyName) ||
                    "resistancesArray".equals(propertyName) ||
                    "weaknessesArray".equals(propertyName)
                ) {
                    //no op
                } else if(
                    "mobChances".equals(propertyName) || 
                    "itemChances".equals(propertyName) || 
                    "effects".equals(propertyName)
                ) {
                    List<Object> currentCollection = (List<Object>)currentValue;
                    List<Object> newCollection = (List<Object>)newValue;
                    if(newCollection.size() < currentCollection.size()) {
                        MergeDifference difference = new MergeDifference(type, key, propertyName, "size:" + currentCollection.size(), "size:" + newCollection.size());
                        ret.add(difference);
                    } else {
                        for(int i = 0; i < newCollection.size(); i++) {
                            if(i >= currentCollection.size()) {
                                MergeDifference difference = new MergeDifference(type, key, propertyName + "[" + i + "]", null, null);
                                ret.add(difference);
                            } else {
                                Object currentChild = currentCollection.get(i);
                                Object newChild = newCollection.get(i);
                                List<MergeDifference> childDifferences = findDifferences(currentChild.getClass(), key, currentChild, newChild);
                                for(MergeDifference childDifference : childDifferences) {
                                    MergeDifference difference = new MergeDifference(type, key, propertyName + "[" + i + "]." + childDifference.getPropertyName(), childDifference.getCurrentValue(), childDifference.getNewValue());
                                    ret.add(difference);
                                }
                            }
                        }
                    }
                } else if(!newValue.equals(currentValue)) {
                    MergeDifference difference = new MergeDifference(type, key, propertyName, currentValue, newValue);
                    ret.add(difference);
                }
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public String applyMerge(MergeDifference difference, Object newObject) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String ret = "OK";        
        String propertyName = difference.getPropertyName();
        
        if(propertyName == null) {
            ret = "NEW";
            template.save(newObject);
            log.info("Saving new " + difference.getType().getSimpleName() + ".");
        } else {
            Object currentObject = template.get(difference.getType(), difference.getId());
            if(propertyName.contains("[")) {
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(difference.getType(), propertyName.substring(0, propertyName.indexOf("[")));
                Object currentValue = descriptor.getReadMethod().invoke(currentObject);
                Object newValue = descriptor.getReadMethod().invoke(newObject);

                int index = Integer.valueOf(propertyName.substring(propertyName.indexOf("[") + 1, propertyName.indexOf("]")));
                
                List<Object> currentValues = (List<Object>)currentValue;
                List<Object> newValues = (List<Object>)newValue;
                
                if(index == currentValues.size()) {
                    currentValues.add(newValues.get(index));
                    ret = "NEW";
                } else {
                    String childProp = propertyName.substring(propertyName.indexOf(".") + 1);

                    Object currentChild = currentValues.get(index);
                    Object newChild = newValues.get(index);
    
                    PropertyDescriptor childDescriptor = BeanUtils.getPropertyDescriptor(currentChild.getClass(), childProp);
    
                    Object currentChildValue = childDescriptor.getReadMethod().invoke(currentChild);
                    Object newChildValue = childDescriptor.getReadMethod().invoke(newChild);
                    
                    log.info("Setting property \"" + propertyName + "\" on " + difference.getType().getSimpleName() + ":" + difference.getId());
                    if(newChildValue.equals(currentChildValue)) {
                        throw new IllegalStateException(difference.getType().getSimpleName() + ":" + difference.getId() + "." + difference.getPropertyName() + " is already equal to \"" + newValue + "\". No merge is required.");
                    }
                    childDescriptor.getWriteMethod().invoke(currentChild, newChildValue);
                    currentChildValue = childDescriptor.getReadMethod().invoke(currentChild);
                    if(!newChildValue.equals(currentChildValue)) {
                        throw new IllegalStateException(difference.getType().getSimpleName() + ":" + difference.getId() + "." + difference.getPropertyName() + " is not equal to \"" + newValue + "\".");
                    }
                }
            } else if(
                "mobChances".equals(propertyName) || 
                "itemChances".equals(propertyName) || 
                "effects".equals(propertyName)
            ) {
                ret = "IGNORED";
            } else {
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(difference.getType(), propertyName);
                Object currentValue = descriptor.getReadMethod().invoke(currentObject);
                Object newValue = descriptor.getReadMethod().invoke(newObject);
                log.info("Setting property \"" + propertyName + "\" on " + difference.getType().getSimpleName() + ":" + difference.getId());
                if(newValue.equals(currentValue)) {
                    throw new IllegalStateException(difference.getType().getSimpleName() + ":" + difference.getId() + "." + difference.getPropertyName() + " is already equal to \"" + newValue + "\". No merge is required.");
                }
                descriptor.getWriteMethod().invoke(currentObject, newValue);
                currentValue = descriptor.getReadMethod().invoke(currentObject);
                if(!newValue.equals(currentValue)) {
                    throw new IllegalStateException(difference.getType().getSimpleName() + ":" + difference.getId() + "." + difference.getPropertyName() + " is not equal to \"" + newValue + "\".");
                }
            }
        }
        return ret;
    }
}