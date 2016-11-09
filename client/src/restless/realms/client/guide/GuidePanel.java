package restless.realms.client.guide;

import restless.realms.client.util.AuditManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.widget.NavigationPanel;

public class GuidePanel extends NavigationPanel {
	private GuideSpeechPanel guideSpeechPanel;

	private String[][] speech = new String[][] {
        {"character", "Character", "This is you. See all the icons surrounding you? That's your equipment. You can drag items - assuming you meet the level requirement and class restriction - from your gear panel to your character and the items will snap into their appropriate locations. The bar towards the bottom is your experience. Once it fills up, you level."},
        {"mainwindow","Main Window","Just about everything you do within the Realms is indicated here. Travel, combat, exploration, training, shops, and the guide you're currently reading shows up here. There isn't much else you need to know that you can't discover with some trial and error. Oh, and there's a back button in the upper right which will take you back to the previous screen. For example, hitting that button now will return you to where you came - most likely, an adventure location or the world map."},
        {"gear","Gear","The Realms provide you with a lot of gear. Not only that, but as you level up you'll be given new skills to equip on your quick slots. The small icons on the top of the gear bar allow you to switch between weapons, armor, consumable items, skills, and so on. Click around. Go ahead, I'll wait. It won't change the main window so you'll have plenty of time to take a look and get familiarized with the different filters within that window while I'm here. Some items can be consumed in or out of combat. Armor, accessories and weapons can be equipped in or out of combat as well. You can also see the quest items you've obtained as you progress."},
        {"skills","Skills","Skills are the bread and butter of combat. As you level up, you'll want to visit the Outpost and train new skills as they become available. Once you've purchased the training, drag them to your Quickslots on your belt to use them in combat. Each skill has a cooldown and warmup. It's simple enough: warmups are rounds you must wait before using a skill. Cooldowns are rounds you must wait until the skill is usable. Skills range from area attack, damage over time, support, direct damage, and utilities such as stun or instant-kill. Experiment and learn them before you dig into the more dangerous areas. Also, each skill takes mana to use. Keep that in mind when employing a particular strategy. However, all classes have a skill that can be used every round regardless of your mana pool. These are your default attacks, and each class has it's own unique benefit when used. For example, warriors may critically hit, clerics may stun, and wizards may make a wide sweep with their staves hitting all enemies."},
        {"chat","Chat","Speak to other players, send us feedback, or drool over your combat history. Simply select one of the icons to change views, and you'll be given a new window. Chat lets you speak to people also in the Realms. Combat let's you see every statistic of combat hit by hit, and feedback let's you submit valuable information to the developers so they can improve the game. Or you can just whine a lot. They absolutely love that. No, really."},
        {"adventures","Adventures","Adventures are defined by traversing through a dungeon, basically. If you're crawling through the cellar, wandering the woods, or climbing the tower, you're on an adventure. In order to travel through these locations you'll need a special currency called Adventure Points. If you're new, you should be sitting on about ten Adventure Points. When they run out, don't be sad, we'll replenish them once a day. Then again, you could consider a small donation to show your love and we'll see about bumping your Adventure Points up a bit. Throughout adventures, you'll find a few different room types: combat, shrines, bosses, and treasures. It should be self-explanatory with the exception of shrines. These fountains allow you to replenish all of your health and mana, but only once, so use them wisely. You can leave a Shrine where you found it and return later to use them. Good luck out there - you'll probably need it."},
        {"quests","Quests","Check out the Outpost from the World Map. Take a look at the quest board - the location on the far left - and grab some quests. These are vital to both character development and equipment. In the Realms, not everyone is a hero. These common people are looking for men and women like you to fulfill whatever it is they need. Find out what that is, get it done, and you'll be swimming in coin and items - not to mention a nice chunk of experience. Some quests are repeatable as well, which means you can do them over and over again for the same rewards. Once you accept a quest, you'll be able to track the progress through the Quest Tracker. Yeah, it's the button at the top. Click it, and view each quest to see where you stand. You can turn in quests at any time when you're done with the exception of combat. If you're fighting, concentrate on just that and turn in your quest when it's over."},
        {"levelingup","Leveling Up","The gold bar below your character's portrait is your experience bar. Leveling up requires a certain amount of experience before you can reach the next level. Once you do, check equipment you've acquired to see if you now meet the requirements to put them on. You'll gain health and mana automatically, but you've got to train your skills manually. This can be done at the skill trainer at the Outpost - the furthest location on the right. Every few levels you'll have the option to train a new skill and use it on your adventures. Make sure you visit the trainer every few levels and keep up with your skills - it could mean the difference between life and death."},
        {"death","Death","Dying sucks. But fortunately for you, we're pretty lenient. You don't have to run back to collect your belongings, crawling naked through the depths of a dangerous territory. However, there are penalties. For one, you'll be removed from the adventure and resurrected in town. You'll also lose some experience for your failings, but you can never lose a level. Lastly, the adventure you were on will reset and you'll need to spend another Adventure Point to start a new adventure. This prevents players from rushing to a boss fight, dying, and simply returning to the room with full health and mana. Every adventure in the game requires a certain amount of skill - once you're in, you're in it to win it. Or die trying."},
        {"belt","Belt","The belt is your utility for combat. It also displays your gold, health, mana, and achievements. You should already be familiar with skills if you're viewing the guide in order. If not, take a look at the Skills tab for further explanation. Consumable items and skills can be dragged to your belt and used as you see fit, just watch your cooldowns and warmups during combat. Some items, such as consumables like potions or scrolls can be used outside of combat as well. When you're weak, use these items to refill your health and mana before limping into the next fight."},
        {"combat","Combat","Ah yes. Combat. That's what it's all about, right? Fighting your way to fame and glory. Slaying your opponents and collecting their loot. Each encounter is random in an adventure, so you'll never know exactly what you'll be facing. That said, use the skills on your belt to fell your opponents and build your own strategies to survive. The monster is displayed on the battle stage with their name, health, and mana if applicable. Take down those bars and you'll be well on your way to victory."},
        {"hotkeys","Hotkeys","Almost every element in the game can be accessed with a simple press of a key. There's no need to navigate the interface exclusively with the mouse. Without listing all hotkeys, they should be intuitive enough that you'll discover them quickly. To get you started, each item on the top navigation is connected to a key.  World Map is W, Guide is G, and so forth. The belt slots are assigned to numbers from 1 to 8. As mentioned, the Enter key highlights the input box in the information window and sends the information once you've entered a message. Experiment with these keys and find an efficient alternative to navigating the interface solely with your mouse. You can easily loot after a fight by hitting the L key to loot everything available, navigate a dungeon with arrow keys, and instead of clicking \"back\" you can hit the escape key."}
	};
	
    public GuidePanel() {
        super("guide", "Guide");
        guideSpeechPanel = new GuideSpeechPanel();
        setRightPanel(guideSpeechPanel);
        
        for(String[] speechInstance : speech) {
            addLink(speechInstance[0], speechInstance[1], null);
        }
        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);
        
        guideSpeechPanel.setSpeechText("Welcome to the Realms, adventurer.  I'm here to familiarize you with the world. Choose an option and I'll provide you with the fundamentals, or click close to begin your adventure.");
	}
	
	@Override
	protected void onNavigation(String key) {
        select(key);
        AuditManager.audit("guide", key);
        
        String speechText = null;
        for(String[] speechInstance : speech) {
            if(speechInstance[0].equals(key)) {
                speechText = speechInstance[2];
                break;
            }
        }
        assert speechText != null;
        
        guideSpeechPanel.setSpeechText(speechText);
	}
}
