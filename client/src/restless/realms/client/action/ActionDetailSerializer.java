package restless.realms.client.action;

public class ActionDetailSerializer {
    // This becomes JS, so no need for StringBuilder. 
    // Also, this is the largest class I've ever written without imports...
    public static String getEffectString(final String sourceType, final String targetingType, final int effectIndex, final String effectType, final int percentChance, final int minQuantity, final int maxQuantity, final int minAdditionalRounds, final int maxAdditionalRounds) {
        String ret = "";

        if(
            "HEAD".equals(sourceType) ||
            "CHEST".equals(sourceType) ||
            "LEGS".equals(sourceType) ||
            "ACCESSORY".equals(sourceType) ||
            "perk".equals(sourceType)
        ){
            assert minQuantity == maxQuantity : maxQuantity;
            ret += "+" + minQuantity;
            if(effectType.equals("HEALING")) {
                ret += " hitpoints";
            } else if(effectType.equals("REPLENISH")) {
                ret += " mana";
            } else {
                ret += " to all " + effectType.toLowerCase() + " damage.";
            }
        } else {
            // not equipment or perk
            if(percentChance != 100) {
                ret += percentChance + "% chance of ";
            }
            
            if(effectType.equals("STUN")) {
                if(percentChance != 100) {
                    ret += "stunning";
                } else {
                    ret += "Stuns";
                }
                ret += getRounds(minAdditionalRounds, maxAdditionalRounds);
            } else if(effectType.equals("DEATH")) {
                assert minQuantity == 1 : minQuantity;
                assert maxQuantity == 1 : maxQuantity;
                assert minAdditionalRounds == 0 : minAdditionalRounds;
                assert maxAdditionalRounds == 0 : maxAdditionalRounds;
                if(percentChance == 100) {
                    ret += "Opponent death";
                } else { 
                    ret += "opponent death";
                }
            } else if(minQuantity == 0 && maxQuantity == 0) {
                if(percentChance == 100) {
                    ret += "Weapon damage";
                } else { 
                    ret += "weapon damage";
                }
            } else {
                ret += minQuantity;
                if(maxQuantity != minQuantity) {
                    ret += " - " + maxQuantity;
                }
                
                if(effectType.equals("HEALING")) {
                    ret += " healing";
                } else if(effectType.equals("DRAIN")) {
                    ret += " mana drain";
                } else if(effectType.equals("REPLENISH")) {
                    ret += " mana";
                } else {
                    ret += " " + effectType.toLowerCase() + " damage";
                }
    
                if(maxAdditionalRounds > 0) {
                    ret += getRounds(minAdditionalRounds, maxAdditionalRounds);
                }
            }
            
            if(targetingType.equals("SELF")) {
                ret += " to self";
            } else if(targetingType.equals("ALLIES")) {
                ret += " to all allies";
            } else if(targetingType.equals("ENEMIES")) {
                ret += " to all enemies";
            }
            ret += ".";
        }
        return ret;
    }

    private static String getRounds(int minAdditionalRounds, int maxAdditionalRounds) {
        String ret = " for " + (minAdditionalRounds + 1);
        if(maxAdditionalRounds != minAdditionalRounds) {
            ret += " - " + (maxAdditionalRounds + 1);
        }
        ret += " round";
        if(maxAdditionalRounds > 0) {
            ret += "s";
        }
        return ret;
    }
}
