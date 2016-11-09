package restless.realms.server.shop;

import org.springframework.stereotype.Service;

import restless.realms.server.database.AbstractDao;

@Service
public class ShopDao extends AbstractDao<Shop> {
    public Shop get(String merchant) {
        Shop ret = super.get(merchant);
        if(ret == null) {
            throw new IllegalArgumentException("Invalid merchant \"" + merchant + "\".");
        }
        return ret;
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Shop.class;
    }
}