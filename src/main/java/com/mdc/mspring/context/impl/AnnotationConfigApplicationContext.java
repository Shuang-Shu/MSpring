package com.mdc.mspring.context.impl;

import com.mdc.mspring.anno.ioc.*;
import com.mdc.mspring.context.Context;
import com.mdc.mspring.entity.Resource;
import com.mdc.mspring.entity.ioc.BeanDefinition;
import com.mdc.mspring.exception.BeanCreateException;
import com.mdc.mspring.exception.DuplicatedBeanNameException;
import com.mdc.mspring.exception.NoUniqueBeanDefinitionException;
import com.mdc.mspring.resolver.ioc.PropertyResolver;
import com.mdc.mspring.resolver.ioc.ResourceResolver;
import com.mdc.mspring.utils.ClassUtils;
import com.mdc.mspring.utils.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:28
 * @Description:
 */
public class AnnotationConfigApplicationContext implements Context {
    private record Result(Method factoryMethod, Object[] args) {
    }

    // save beanDefinition objects
    private Map<String, BeanDefinition> beans = new HashMap<>();
    private final PropertyResolver propertyResolver = new PropertyResolver();

    public AnnotationConfigApplicationContext(Class<?> confgClass, ResourceResolver resourceResolver, PropertyResolver propertyResolver) throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 1 get class names in scanPackage
        String scanPackage = ((ComponentScan) Objects.requireNonNull(ClassUtils.getAnnotation(confgClass, ComponentScan.class, new HashSet<>()))).value();
        List<String> classNames = resourceResolver.scan(scanPackage, Resource::name, ResourceResolver.CLASS_SUFFIX, -1).stream().map(n -> {
            n = n.replace('/', '.');
            return n.substring(0, n.length() - 6);
        }).toList();
        // 2 create BeanDefinitions
        this.beans = createBeanDefinitions(classNames);
        // 3 instantiate all beans, and inject dependencies
        instantiateBeans();
        // 4 inject weak dependencies, besides values
        injectWeakDependencies();
    }

    private void injectValues() {

    }

    private void injectWeakDependencies() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (BeanDefinition definition : this.beans.values()) {
            Class<?> clazz = definition.getInstance().getClass();
            // invoke setter methods
            for (Method method : clazz.getMethods()) {
                if (method.getAnnotation(Autowired.class) != null) {
                    Result result = getAllArgsOfMethod(method, new HashSet<>());
                    method.invoke(definition.getInstance(), result.args);
                }
            }
            // inject fields
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Autowired.class) != null) {
                    Autowired autowired = (Autowired) field.getAnnotation(Autowired.class);
                    field.setAccessible(true);
                    if (StringUtils.isEmpty(autowired.value())) {
                        field.set(definition.getInstance(), getBean(field.getType()));
                    } else {
                        Object bean = getBean(autowired.value());
                        field.set(definition.getInstance(), bean);
                    }
                }
                if (field.getAnnotation(Value.class) != null) {
                    Value value = (Value) field.getAnnotation(Value.class);
                    field.setAccessible(true);
                    field.set(definition.getInstance(), propertyResolver.getProperty(value.value()));
                }
            }
        }
    }

    private void instantiateBeans() throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (BeanDefinition definition : this.beans.values()) {
            instantiateBean(definition, new HashSet<>());
        }
    }

    private void instantiateBean(BeanDefinition definition, Set<BeanDefinition> waiterDefinitions) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
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
            Object factory = getOrConstruct(Class.forName(definition.getFactoryName()), waiterDefinitions);
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
                    args[i] = getOrConstruct(argTypes[i], waiterDefinitions);
                else
                    args[i] = getOrConstruct(((Autowired) annotations[i]).value(), waiterDefinitions);
            }
            definition.setInstance(constructor.newInstance(args));
        }
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
                args[i] = getOrConstruct(argTypes[i], waiterDefinitions);
            else
                args[i] = getOrConstruct(((Autowired) annotations[i]).value(), waiterDefinitions);
        }
        Result result = new Result(factoryMethod, args);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrConstruct(Class<T> clazz, Set<BeanDefinition> waiterDefinitions) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = findDefinition(clazz);
        instantiateBean(beanDefinition, waiterDefinitions);
        return (T) beanDefinition.getInstance();
    }

    private Object getOrConstruct(String beanName, Set<BeanDefinition> waiterDefinitions) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BeanDefinition beanDefinition = findDefinition(beanName);
        instantiateBean(beanDefinition, waiterDefinitions);
        return beanDefinition.getInstance();
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
        ).sorted().collect(Collectors.toList());
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
}
