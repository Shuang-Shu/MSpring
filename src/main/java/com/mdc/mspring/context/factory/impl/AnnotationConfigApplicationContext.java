package com.mdc.mspring.context.factory.impl;

import com.mdc.mspring.context.anno.*;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.entity.Resource;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.exception.BeanCreateException;
import com.mdc.mspring.context.exception.DuplicatedBeanNameException;
import com.mdc.mspring.context.exception.NoUniqueBeanDefinitionException;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.utils.ClassUtils;
import com.mdc.mspring.utils.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:28
 * @Description:
 */
public class AnnotationConfigApplicationContext implements ConfigurableApplicationContext {
    // save beanDefinition objects
    private Map<String, BeanDefinition> beans = new HashMap<>();
    private final PropertyResolver propertyResolver = new PropertyResolver();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final Set<BeanDefinition> waiterDefinitions = new HashSet<>();

    @Override
    public Object createBeanAsEarlySingleton(BeanDefinition definition) {
        try {
            instantiateBean(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition.getInstance();
    }

    private record Result(Method factoryMethod, Object[] args) {
    }

    public AnnotationConfigApplicationContext(Class<?> confgClass, ResourceResolver resourceResolver, PropertyResolver propertyResolver) throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 1 get class names in scanPackage
        String scanPackage = ((ComponentScan) Objects.requireNonNull(ClassUtils.getAnnotation(confgClass, ComponentScan.class, new HashSet<>()))).value();
        List<String> classNames = resourceResolver.scan(scanPackage, Resource::name, ResourceResolver.CLASS_SUFFIX, -1).stream().map(n -> {
            n = n.replace('/', '.');
            return n.substring(0, n.length() - 6);
        }).filter(n -> {
            return !ClassUtils.isAnnotation(n);
        }).toList();
        // 2 create BeanDefinitions
        this.beans = createBeanDefinitions(classNames);
        // 3 get all beanPostPocessors
        scanBeanPostProcessors();
        // 4 instantiate all beans, and inject dependencies
        instantiateBeans();
        // 5 set originInstance to itseslf
        setAllNullOriginInstance();
        // 6 inject weak dependencies, besides values
        injectWeakDependencies();
    }

    private void setAllNullOriginInstance() {
        for (BeanDefinition definition : this.beans.values()) {
            if (definition.getOriginInstance() == null) {
                definition.setOriginInstance(definition.getInstance());
            }
        }
    }

    private void scanBeanPostProcessors() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (BeanDefinition definition : this.beans.values()) {
            if (BeanPostProcessor.class.isAssignableFrom(definition.getDeclaredClass())) {
                definition.setInstance(definition.getDeclaredClass().getDeclaredConstructor().newInstance());
                beanPostProcessors.add((BeanPostProcessor) definition.getInstance());
            }
            if (definition.getInstance() instanceof Aware) {
                ((Aware) definition.getInstance()).setContext(this);
            }
        }
    }

    private void injectWeakDependencies() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (BeanDefinition definition : this.beans.values()) {
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
                    Value value = (Value) field.getAnnotation(Value.class);
                    field.setAccessible(true);
                    field.set(definition.getOriginInstance(), propertyResolver.getProperty(value.value()));
                }
            }
        }
    }

    private void instantiateBeans() throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (BeanDefinition definition : this.beans.values()) {
            instantiateBean(definition);
        }
    }

    private void instantiateBean(BeanDefinition definition) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
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
            definition.setInstance(result.factoryMethod().invoke(factory, result.args()));
        } else if (definition.getConstructor() != null) {
            // use constructor to instantiate
            Constructor<?> constructor = definition.getConstructor();
            Class<?>[] argTypes = constructor.getParameterTypes();
            Annotation[] annotations = ClassUtils.getTargetAnnotaionOnConstructorArgs(constructor, Autowired.class);
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

    private Result getAllArgsOfMethod(Method factoryMethod, Set<BeanDefinition> waiterDefinitions) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<?>[] argTypes = factoryMethod.getParameterTypes();
        Annotation[] annotations = ClassUtils.getTargetAnnotaionOnMethodArgs(factoryMethod, Autowired.class);
        // instantiate args of factory method
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            if (annotations[i] == null)
                throw new BeanCreateException("all factory method args must be annotated by @Autowired");
            if (StringUtils.isEmpty(((Autowired) annotations[i]).value()))
                args[i] = getOrConstruct(argTypes[i]);
            else
                args[i] = getOrConstruct(((Autowired) annotations[i]).value());
        }
        Result result = new Result(factoryMethod, args);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrConstruct(Class<T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = findDefinition(clazz);
        instantiateBean(beanDefinition);
        if (beanDefinition.getOriginInstance() instanceof Aware) {
            ((Aware) beanDefinition.getOriginInstance()).setContext(this);
        }
        invokeBeanPostProcessors(beanDefinition);
        return (T) beanDefinition.getInstance();
    }

    private Object getOrConstruct(String beanName) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = findDefinition(beanName);
        instantiateBean(beanDefinition);
        if (beanDefinition.getOriginInstance() instanceof Aware) {
            ((Aware) beanDefinition.getOriginInstance()).setContext(this);
        }
        invokeBeanPostProcessors(beanDefinition);
        return beanDefinition.getInstance();
    }

    private void invokeBeanPostProcessors(BeanDefinition beanDefinition) {
        for (BeanPostProcessor processor : this.beanPostProcessors) {
            if (beanDefinition.getOriginInstance() == null || beanDefinition.getOriginInstance() == beanDefinition.getInstance()) {
                beanDefinition.setOriginInstance(beanDefinition.getInstance());
                beanDefinition.setInstance(processor.postProcessBeforeInitialization(beanDefinition.getInstance(), beanDefinition.getBeanName()));
            }
        }
    }

    private Map<String, BeanDefinition> createBeanDefinitions(List<String> classNames) throws NoSuchMethodException {
        Map<String, BeanDefinition> result = new HashMap<>();
        for (String className : classNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeanCreateException("class: " + classNames + " not found");
            }
            Component component = (Component) ClassUtils.getAnnotation(clazz, Component.class, new HashSet<>());
            if (component != null) {
                String beanName = null;
                if (!StringUtils.isEmpty(component.value())) {
                    beanName = component.value();
                } else {
                    beanName = StringUtils.simpleName(className);
                }
                BeanDefinition beanDefinition = BeanDefinition.builder().beanName(beanName).declaredClass(clazz).initMethod(null).destroyMethod(null).initMethod(ClassUtils.getAnnotationMethod(clazz, PostConstruct.class)).destroyMethod(ClassUtils.getAnnotationMethod(clazz, BeforeDestroy.class)).constructor(ClassUtils.getSuitableConstructor(clazz)).order(ClassUtils.getOrder(clazz)).primary(ClassUtils.getPrimary(clazz)).build();

                result.put(beanName, beanDefinition);
                if (ClassUtils.getAnnotation(clazz, Configuration.class, new HashSet<>()) != null) {
                    addBeanFactoryDefinition(clazz, result);
                }
            }
        }
        return result;
    }

    private void addBeanFactoryDefinition(Class<?> clazz, Map<String, BeanDefinition> result) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Bean.class) != null) {
                Bean bean = method.getAnnotation(Bean.class);
                Class<?> returnType = method.getReturnType();
                BeanDefinition beanDefinition = BeanDefinition.builder().beanName(StringUtils.isEmpty(bean.value()) ? StringUtils.getLowerCase(returnType.getName()) : bean.value()).declaredClass(returnType).factoryMethod(method).factoryName(clazz.getName()).order(ClassUtils.getOrderMethod(method)).primary(ClassUtils.getPrimaryMethod(method)).initMethodName(bean.initMethod()).destroyMethodName(bean.destroyMethod()).build();
                result.put(beanDefinition.getBeanName(), beanDefinition);
            }
        }
    }

    /**
     * @Description: Find method in clazz type which is annotationed by annotationClass
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date: 2023/8/11 22:34
     */
    @Override
    public void register(BeanDefinition definition) {
        if (beans.containsKey(definition.getBeanName())) {
            throw new DuplicatedBeanNameException("duplicated bean names found: " + definition.getBeanName());
        }
    }

    public BeanDefinition findDefinition(String name) {
        return beans.get(name);
    }

    public List<BeanDefinition> findDefinitions(Class<?> clazz) {
        return beans.values().stream().filter(
                bd -> {
                    return clazz.isAssignableFrom(bd.getDeclaredClass());
                }
        ).sorted(new Comparator<BeanDefinition>() {
            @Override
            public int compare(BeanDefinition d1, BeanDefinition d2) {
                return d1.getOrder() - d2.getOrder();
            }
        }).collect(Collectors.toList());
    }

    public BeanDefinition findDefinition(Class<?> clazz) {
        List<BeanDefinition> definitions = findDefinitions(clazz);
        if (definitions.size() == 1) {
            return definitions.get(0);
        }
        List<BeanDefinition> primaryDefs = definitions.stream().filter(
                BeanDefinition::isPrimary
        ).toList();
        if (primaryDefs.size() != 1) {
            throw new NoUniqueBeanDefinitionException("no/more than 1 @Primary specifed beans are found of type: " + clazz.getName());
        } else {
            return primaryDefs.get(0);
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition definition = findDefinition(beanName);
        return definition.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> clazz) {
        BeanDefinition definition = findDefinition(beanName);
        return (T) definition.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) findDefinition(clazz).getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> clazz) {
        return (List<T>) findDefinitions(clazz).stream().map(BeanDefinition::getInstance).toList();
    }

    @Override
    public void close() {
        for (BeanDefinition definition : beans.values()) {
            if (definition.getOriginInstance() != null) {
                try {
                    invokeDestroyMethod(definition);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void invokeDestroyMethod(BeanDefinition definition) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (definition.getDestroyMethod() != null) {
            definition.getDestroyMethod().invoke(definition.getOriginInstance());
        } else if (!StringUtils.isEmpty(definition.getDestroyMethodName())) {
            Method destroyMethod = definition.getDeclaredClass().getMethod(definition.getDestroyMethodName());
            destroyMethod.invoke(definition.getOriginInstance());
        }
    }

    @Override
    public List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return findDefinitions(type);
    }

    @Override
    public BeanDefinition findBeanDefinition(Class<?> type) {
        return findDefinition(type);
    }

    @Override
    public BeanDefinition findBeanDefinition(String name) {
        return findDefinition(name);
    }

    @Override
    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        return findDefinition(name);
    }
}
