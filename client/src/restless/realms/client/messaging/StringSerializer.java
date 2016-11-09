package restless.realms.client.messaging;

import com.greencat.gwt.comet.client.CometSerializer;
import com.greencat.gwt.comet.client.SerialTypes;

@SerialTypes(value={String.class})
public abstract class StringSerializer extends CometSerializer {
}