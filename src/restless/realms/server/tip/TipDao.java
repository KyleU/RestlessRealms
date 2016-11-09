package restless.realms.server.tip;

import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.quest.Quest;

@Service
public class TipDao extends AbstractDao<Quest> {
    private List<Tip> tips;
    private Random r;
    
    public TipDao() {
    }
    
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        tips = template.find("select t from Tip t");
        r = new Random();
    }
    
    public Tip get() {
        Tip ret = null;
        int index = r.nextInt(tips.size());
        ret = tips.get(index);
        return ret;
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Tip.class;
    }
}