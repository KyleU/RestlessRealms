package restless.realms.server.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if(SpringUtils.context != null) {
            throw new RuntimeException("Double insertion... heheh");
        }
        SpringUtils.context = context;
    }
    
    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
