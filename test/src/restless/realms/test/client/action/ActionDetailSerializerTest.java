package restless.realms.test.client.action;

import junit.framework.Assert;

import org.junit.Test;

import restless.realms.client.action.ActionDetailSerializer;
import restless.realms.test.client.ClientTestCase;

public class ActionDetailSerializerTest extends ClientTestCase {
    @Test
    public void testBasicSerialization() {
        String effectString = ActionDetailSerializer.getEffectString("item", "ENEMY", 0, "PHYSICAL", 100, 1, 10, 0, 0);
        Assert.assertEquals("1 - 10 physical damage.", effectString);
    }
}
