package restless.realms.server.item;

import org.springframework.stereotype.Service;

import restless.realms.server.database.AbstractDao;

@Service
public class ItemDao extends AbstractDao<Item> {
    public Item get(Integer id) {
        return super.get(id);
    }

    public Item getByName(String name) {
        return uniqueResult(template.findByNamedQuery("item.getByName", name));
    }

    @Override
    protected Class<?> getManagedClass() {
        return Item.class;
    }
}