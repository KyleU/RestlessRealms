package restless.realms.client.skill;

import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;

public interface SkillsHandler {
    void onSkills(JsArray<ScriptObject> skills);
}
