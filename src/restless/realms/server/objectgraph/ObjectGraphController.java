package restless.realms.server.objectgraph;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.session.Session;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/objectgraph")
public class ObjectGraphController {
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private HibernateTemplate template;
    
    private HibernateTemplate rowLimitedTemplate;

    private String[] simpleTypes = {"char", "String", "int", "Integer", "Date"};
    
    @PostConstruct
    public void init() {
        rowLimitedTemplate = new HibernateTemplate(template.getSessionFactory());
        rowLimitedTemplate.setMaxResults(501);
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping(method=RequestMethod.GET)
    public String types(HttpServletRequest request, Model model) {
        validateRequest(request, model);
        
        Map<String, AbstractEntityPersister> classMetadata = template.getSessionFactory().getAllClassMetadata();
        Map<String, String> sortedMap = new TreeMap<String, String>();
        for(Entry<String, AbstractEntityPersister> entry : classMetadata.entrySet()) {
            sortedMap.put(entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1), entry.getKey());
        }
        model.addAttribute("classMetadata", sortedMap);
        return "objectgraph/types";
    }

    @RequestMapping(value="/{type}", method=RequestMethod.GET)
    public String list(
        @PathVariable("type") String type, 
        @RequestParam(value="order", required=false) String order, 
        @RequestParam(value="asc", required=false) Boolean asc,
        HttpServletRequest request,
        Model model
    ) {
        validateRequest(request, model);
        String hql = "select x from " + type + " x";
        if(order != null) {
            hql += " order by x." + order;
            if(asc != null && !asc) {
                hql += " desc";
            }
        }
        List<?> resultObjects = rowLimitedTemplate.find(hql);
        List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
        for(Object result : resultObjects) {
            LinkedHashMap<String, Object> resultMap = dumpObjectProperties(result, false);
            results.add(resultMap);
        }
        
        ClassMetadata metadata = template.getSessionFactory().getClassMetadata(type);
        
        model.addAttribute("idProperty", metadata.getIdentifierPropertyName());
        model.addAttribute("type", type);
        model.addAttribute("typeName", type.substring(type.lastIndexOf(".") + 1));
        model.addAttribute("results", results);
        model.addAttribute("order", order);
        model.addAttribute("asc", asc);
        return "objectgraph/list";
    }

    private LinkedHashMap<String, Object> dumpObjectProperties(Object result, boolean includeComplexProperties) {
        LinkedHashMap<String, Object> ret = new LinkedHashMap<String, Object>();
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(result);
        for(PropertyDescriptor descriptor : propertyDescriptors) {
            boolean use = includeComplexProperties;
            if(!use) {
                if(Enum.class.isAssignableFrom(descriptor.getPropertyType())) {
                    use = true;
                } else {
                    String propertyName = descriptor.getPropertyType().getSimpleName();
                    for(String simpleType : simpleTypes ) {
                        if(simpleType.equals(propertyName)) {
                            use = true;
                            break;
                        }
                    }
                }
            }
            
            Object value = null;
            if(use) {
                Method readMethod = descriptor.getReadMethod();
                if(readMethod == null) {
                    use = false;
                } else {
                    try {
                        value = readMethod.invoke(result);
                    } catch(Exception e) {
                        value = e;
                    }
                }
            } else {
                if(!"class".equals(descriptor.getName())) {
                    use = true;
                    value = "{" + descriptor.getPropertyType().getSimpleName() + "}";
                }
            }

            if(use) {
                ret.put(descriptor.getName(), value);
            }
        }
        return ret;
    }

    @RequestMapping(value="/{type}/{id}", method=RequestMethod.GET)
    public String get(@PathVariable("type") String type, @PathVariable("id") String id, HttpServletRequest request, Model model) {
        validateRequest(request, model);
        return "objectgraph/view";
    }

    @RequestMapping(value="/{type}/new")
    public String newForm(@PathVariable("type") String type, HttpServletRequest request, Model model) {
        validateRequest(request, model);
        return "objectgraph/edit";
    }

    @RequestMapping(value="/{type}/{id}", method=RequestMethod.POST)
    public String save(
        @PathVariable("type") String type,
        @PathVariable("id") String id,
        HttpServletRequest request,
        Model model
    ) {
        validateRequest(request, model);
        return "redirect:/objectgraph/" + type + "/" + id + ".html";
    }

    private void validateRequest(HttpServletRequest request, Model model) {
        Session s = RequestUtils.getSession(request);
        Account account = accountDao.get(s.getAccountId());
        if(!account.isAdmin()) {
            throw new IllegalStateException("You are not an administrator.");
        }

        int extensionIndex = request.getRequestURI().lastIndexOf(".");
        String extension = extensionIndex == -1 ? "" : request.getRequestURI().substring(extensionIndex + 1);
        model.addAttribute("extension", extension );
    }
}