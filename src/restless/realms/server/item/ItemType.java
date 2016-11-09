package restless.realms.server.item;

public enum ItemType {
	WEAPON,
	HEAD,
	CHEST,
	LEGS,
	ACCESSORY,
	CONSUMABLE,
	TRASH,
	QUEST;
	
	private ItemType() {
    }

    public boolean isEquipment() {
        return 
            this == ItemType.HEAD ||
            this == ItemType.CHEST ||
            this == ItemType.LEGS ||
            this == ItemType.ACCESSORY;
    }
}
