package com.mdc.mspring.context.factory;

import com.mdc.mspring.context.annotation.Autowired;
import com.mdc.mspring.context.annotation.Aware;
import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Value;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.exception.BeanCreateException;
import com.mdc.mspring.context.factory.support.*;
import com.mdc.mspring.context.factory.support.impl.DefaultBeanDefinitionRegistry;
import com.mdc.mspring.context.factory.support.impl.DefaultPropertyRegistry;
import com.mdc.mspring.context.resolver.ClassPathResourceResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.context.utils.ClassUtils;
import com.mdc.mspring.context.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:28
 * @Description:
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext {
    private final static Logger logger = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    private record Result(Method factoryMethod, Object[] args) {
    }

    private BeanDefinitionRegistry beanDefinitionRegistry = new DefaultBeanDefinitionRegistry();
    private PropertyRegistry propertyRegistry = new DefaultPropertyRegistry();
    private final ClassPathResourceResolver resourceResolver = new ClassPathResourceResolver(this, this);
    // save beanDefinition objects
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final Set<BeanDefinition> waiterDefinitions = new HashSet<>();
//    private final ResourceResolver resourceResolver;

    public AnnotationConfigApplicationContext(Class<?> configClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, URISyntaxException {
        // 1 scan on config class
        resourceResolver.scanOn(configClass);
        // 2 get all beanPostPocessors
        scanBeanPostProcessors();
        // 4 instantiate all beans, and inject dependencies
        instantiateBeans();
        // 5 set originInstance to itseslf
        setAllNullOriginInstance();
        // 6 inject weak dependencies, besides values
        injectWeakDependencies();
        // 7 invoke initial methods for all (origin) beans
        invokeInitialMethods();
        logger.info("Loaded all beans in IoC container");
    }

    private void invokeInitialMethods()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        for (var beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(beanName);
            if (definition.getInitMethod() != null) {

            } else if (!StringUtils.isEmpty(definition.getInitMethodName())) {
                Method initMethod = definition.getDeclaredClass().getMethod(definition.getInitMethodName());
                initMethod.invoke(definition.getOriginInstance());
            }
        }
    }

    @Override
    public Object createBeanAsEarlySingleton(BeanDefinition definition) {
        try {
            instantiateBean(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition.getInstance();
    }

    private static String getScanClass(Class<?> confgClass) {
        ComponentScan componentSacn = (ComponentScan) ClassUtils.getAnnotation(confgClass, ComponentScan.class);
        String scanPackage = null;
        if (componentSacn == null) {
            scanPackage = confgClass.getPackageName();
        } else {
            scanPackage = componentSacn.value();
        }
        if (StringUtils.isEmpty(scanPackage)) {
            scanPackage = confgClass.getPackageName();
        }
        return scanPackage;
    }

    private void setAllNullOriginInstance() {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(beanName);
            if (definition.getOriginInstance() == null) {
                definition.setOriginInstance(definition.getInstance());
            }
        }
    }

    private void scanBeanPostProcessors()
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(beanName);
            if (BeanPostProcessor.class.isAssignableFrom(definition.getDeclaredClass())) {
                definition.setInstance(definition.getDeclaredClass().getDeclaredConstructor().newInstance());
                beanPostProcessors.add((BeanPostProcessor) definition.getInstance());
            }
            if (definition.getInstance() instanceof Aware) {
                ((Aware) definition.getInstance()).setContext(this);
            }
        }
    }

    private void injectWeakDependencies()
            throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(beanName);
            Class<?> clazz = definition.getOriginInstance().getClass();
            // invoke setter methods
            for (Method method : clazz.getMethods()) {
                if (method.getAnnotation(Autowired.class) != null) {
                    Result result = getAllArgsOfMethod(method, new HashSet<>());
                    method.invoke(definition.getOriginInstance(), result.args);
                }
            }
            // inject fields
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Autowired.class) != null) {
                    Autowired autowired = (Autowired) field.getAnnotation(Autowired.class);
                    field.setAccessible(true);
                    if (StringUtils.isEmpty(autowired.value())) {
                        field.set(definition.getOriginInstance(), getOrConstruct(field.getType()));
                    } else {
                        Object bean = getBean(autowired.value());
                        field.set(definition.getOriginInstance(), bean);
                    }
                }
                if (field.getAnnotation(Value.class) != null) {
                    field.setAccessible(true);
                    String valueName = field.getAnnotation(Value.class).value();
                    if (valueName == null) {
                        valueName = field.getName();
                    }
                    field.set(definition.getOriginInstance(),
                            ClassPathResourceResolver.parseTo(field.getType(), propertyRegistry.getProperty(valueName)));
                }
            }
        }
    }

    private void instantiateBeans()
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(beanName);
            instantiateBean(definition);
        }
    }

    private void instantiateBean(BeanDefinition definition)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (definition.getInstance() != null) {
            return;
        }
        if (waiterDefinitions.contains(definition)) {
            throw new BeanCreateException("circular dependency");
        } else {
            waiterDefinitions.add(definition);
        }
        if (definition.getFactoryMethod() != null) {
            Method factoryMethod = definition.getFactoryMethod();
            // use factory method to instantiate
            Result result = getAllArgsOfMethod(factoryMethod, waiterDefinitions);
            // instantiate factory bean
            Object factory = getOrConstruct(Class.forName(definition.getFactoryName()));
            result.factoryMethod().setAccessible(true);
            definition.setInstance(result.factoryMethod().invoke(factory, result.args()));
        } else if (definition.getConstructor() != null) {
            // use constructor to instantiate
            Constructor<?> constructor = definition.getConstructor();
            constructor.setAccessible(true);
            Class<?>[] argTypes = constructor.getParameterTypes();
            Annotation[] annotations = ClassUtils.getTargetAnnotaionOnConstructorArgs(constructor,
                    Set.of(Autowired.class, Value.class));
            Object[] args = new Object[argTypes.length];
            for (int i = 0; i < args.length; i++) {
                if (annotations[i] == null)
                    throw new BeanCreateException("all constructor args must be annotated by @Autowired");
                if (StringUtils.isEmpty(((Autowired) annotations[i]).value()))
                    args[i] = getOrConstruct(argTypes[i]);
                else
                    args[i] = getOrConstruct(((Autowired) annotations[i]).value());
            }
            definition.setInstance(constructor.newInstance(args));
        }
        invokeBeanPostProcessors(definition);
    }

    private Result getAllArgsOfMethod(Method factoryMethod, Set<BeanDefinition> waiterDefinitions)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<?>[] argTypes = factoryMethod.getParameterTypes();
        Annotation[] annotations = ClassUtils.getTargetAnnotaionOnMethodArgs(factoryMethod,
                Set.of(Autowired.class, Value.class));
        // instantiate args of factory method
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            if (annotations[i] == null)
                throw new BeanCreateException("all factory method args must be annotated by @Autowired");
            else if (annotations[i] instanceof Autowired) {
                if (StringUtils.isEmpty(((Autowired) annotations[i]).value()))
                    args[i] = getOrConstruct(argTypes[i]);
                else
                    args[i] = getOrConstruct(((Autowired) annotations[i]).value());
            } else if (annotations[i] instanceof Value) {
                args[i] = ResourceResolver.parseTo(argTypes[i],
                        propertyRegistry.getProperty(((Value) annotations[i]).value()));
            }
        }
        Result result = new Result(factoryMethod, args);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrConstruct(Class<T> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = getBeanDefinition(clazz);
        instantiateBean(beanDefinition);
        if (beanDefinition.getOriginInstance() instanceof Aware) {
            ((Aware) beanDefinition.getOriginInstance()).setContext(this);
        }
        invokeBeanPostProcessors(beanDefinition);
        return (T) beanDefinition.getInstance();
    }

    private Object getOrConstruct(String beanName)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        instantiateBean(beanDefinition);
        if (beanDefinition.getOriginInstance() instanceof Aware) {
            ((Aware) beanDefinition.getOriginInstance()).setContext(this);
        }
        invokeBeanPostProcessors(beanDefinition);
        return beanDefinition.getInstance();
    }

    private void invokeBeanPostProcessors(BeanDefinition beanDefinition) {
        for (BeanPostProcessor processor : this.beanPostProcessors) {
            if (beanDefinition.getOriginInstance() == null
                    || beanDefinition.getOriginInstance() == beanDefinition.getInstance()) {
                beanDefinition.setOriginInstance(beanDefinition.getInstance());
                beanDefinition.setInstance(processor.postProcessBeforeInitialization(beanDefinition.getInstance(),
                        beanDefinition.getBeanName()));
            }
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition definition = getBeanDefinition(beanName);
        return definition.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> clazz) {
        BeanDefinition definition = getBeanDefinition(beanName);
        return (T) definition.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) getBeanDefinition(clazz).getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> clazz) {
        return (List<T>) getBeanDefinitions(clazz).stream().map(BeanDefinition::getInstance).toList();
    }

    @Override
    public void close() {
        for (String name : beanDefinitionRegistry.getBeanDefinitionNames()) {
            var definition = beanDefinitionRegistry.getBeanDefinition(name);
            if (definition.getOriginInstance() != null) {
                try {
                    invokeDestroyMethod(definition);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void invokeDestroyMethod(BeanDefinition definition)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (definition.getDestroyMethod() != null) {
            definition.getDestroyMethod().invoke(definition.getOriginInstance());
        } else if (!StringUtils.isEmpty(definition.getDestroyMethodName())) {
            Method destroyMethod = definition.getDeclaredClass().getMethod(definition.getDestroyMethodName());
            destroyMethod.invoke(definition.getOriginInstance());
        }
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions(Class<?> type) {
        return beanDefinitionRegistry.getBeanDefinitions(type);
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> type) {
        return beanDefinitionRegistry.getBeanDefinition(type);
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        beanDefinitionRegistry.registerBeanDefinition(name, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String name) {
        beanDefinitionRegistry.removeBeanDefinition(name);
    }

    @Override
    public BeanDefinition getBeanDefinition(String name) {
        return beanDefinitionRegistry.getBeanDefinition(name);
    }

    @Override
    public BeanDefinition getBeanDefinition(String name, Class<?> requiredType) {
        return getBeanDefinition(name, requiredType);
    }

    @Override
    public boolean containsBeanDefinition(String name) {
        return false;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return new String[0];
    }

    @Override
    public int getBeanDefinitinoCount() {
        return 0;
    }

    @Override
    public List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> anno) {
        return beanDefinitionRegistry.getBeanDefinitionsByAnnotation(anno);
    }

    @Override
    public void set(String key, String value) {
        propertyRegistry.set(key, value);
    }

    @Override
    public String getProperty(String key) {
        return propertyRegistry.getProperty(key);
    }

    @Override
    public PropertyRegistry getPropertyRegistry() {
        return propertyRegistry;
    }

    @Override
    public void setProperties(Properties properties) {
        propertyRegistry.setProperties(properties);
    }
}
