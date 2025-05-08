package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContextListener;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.UUID;
@Named
@ApplicationScoped
public class RegMBeans implements ServletContextListener {
    private final HashMap<Object, ObjectName> bean_names = new HashMap<>();
    public void registerBean(Object bean) {
        try {
            String cur_name = UUID.randomUUID().toString();
            var domain = bean.getClass().getPackageName();
            var type = bean.getClass().getSimpleName();
            var objectName = new ObjectName(String.format("%s:type=%s,name=%s", domain, type, cur_name));
            System.out.println(objectName);
            bean_names.put(bean, objectName);
            ManagementFactory.getPlatformMBeanServer().registerMBean(bean, objectName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    public void unregisterBean(Object bean) {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(bean_names.get(bean));
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            e.printStackTrace();
        }
    }
}