package restless.realms.client.util;

import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;

public interface CommonServiceCallbacks {
    public static final ServiceCallback NO_OP = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject ret) {
        }
    };

    public static final ServiceCallback DEBUG_RESULT = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ConsoleUtils.log(ConsoleChannel.Debug, result.toDebugString());
        }
    };
}
