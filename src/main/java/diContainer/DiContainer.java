package diContainer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import BeanPostProcessor.AutowiredAnnotationBeanPostProcessor;
import BeanPostProcessor.PostConstructInvoker;
import BeanPostProcessor.PreDestroyInvoker;
import exceptions.*;
import lambdaSupport.LambdaBinary;
import lambdaSupport.LambdaExpression;
import lambdaSupport.LambdaSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scope.TypeScope;


public class DiContainer implements IDiContainer {
    private static final Logger log = LoggerFactory.getLogger(DiContainer.class);

    private final Map<String, BeanInfo> beansByNames;
    private final Map<Class<?>, List<BeanInfo>> beansByClass;

    private boolean shouldTryToCreateBean = false;
    private final TypeScope defaultScope = TypeScope.SINGLETON;
    private final Stack<Parameter[]> parameterGraph = new Stack<>();

    private final Set<Node> visitedNodes = new HashSet<>();

    private final IDiContainer parentDiContainer;

    private final AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor;

    public DiContainer() {
        beansByNames = new HashMap<>();
        beansByClass = new HashMap<>();
        parentDiContainer = null;
        autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setDiContainer(this);
    }

    public DiContainer(boolean shouldTryToCreateBean) {
        beansByNames = new HashMap<>();
        beansByClass = new HashMap<>();
        parentDiContainer = null;
        this.shouldTryToCreateBean = shouldTryToCreateBean;
        autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setDiContainer(this);
    }

    public DiContainer(boolean shouldTryToCreateBean, IDiContainer parentDiContainer) {
        beansByNames = new HashMap<>();
        beansByClass = new HashMap<>();
        this.shouldTryToCreateBean = shouldTryToCreateBean;
        this.parentDiContainer = parentDiContainer;
        autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setDiContainer(this);
    }

    @Override
    public void register(Object object) throws MultipleBeanDefinitionException {
        this.register(object, object.getClass().getName(), defaultScope);
    }

    @Override
    public void register(Object object, String name) throws MultipleBeanDefinitionException {
        this.register(object, name, defaultScope);
    }

    @Override
    public void register(LambdaExpression expression) throws ContainerException {
        register(expression, expression.getClass().getName());
    }

    @Override
    public <T> void register(LambdaSupplier<T> expression) throws ContainerException {
        register(expression, expression.getClass().getName());

    }

    @Override
    public <T> void register(LambdaBinary<T> expression) throws ContainerException {
        register(expression, expression.getClass().getName());
    }

    @Override
    public void registerLazyBean(Class<?> beanClass, TypeScope scope) throws MultipleBeanDefinitionException {
        var beanInfo = new BeanInfo(scope, null);
        register(beanInfo, beanClass.getName(), beanClass);
    }

    @Override
    public void register(Object object, String name, TypeScope scope) throws MultipleBeanDefinitionException {
        var beanInfo = new BeanInfo(scope, object);
        register(beanInfo, name, object.getClass());
    }


    private void register(BeanInfo beanInfo, String name, Class<?> beanClass) throws MultipleBeanDefinitionException {
        if (beansByNames.containsKey(name)) {
            throw new MultipleBeanDefinitionException("Bean with name " + name + " already exist");
        }

        beansByNames.put(name, beanInfo);
        registerParentClasses(beanClass, beanInfo);
    }

    private void registerParentClasses(Class<?> beanClass, BeanInfo beanInfo) {
        while (beanClass != null) {
            log.debug("Register " + beanClass);

            if (!beansByClass.containsKey(beanClass)) {
                beansByClass.put(beanClass, new ArrayList<>());
            }

            var beans = beansByClass.get(beanClass);
            beans.add(beanInfo);
            log.debug("");

            beanClass = beanClass.getSuperclass();
        }
    }

    @Override
    public <T> T resolveByClassOrInterface(Class<?> bean) throws ContainerException, InvocationTargetException, IllegalAccessException {
        try {
            return resolveByClass(bean);
        } catch (ContainerException | InvocationTargetException | IllegalAccessException resolveByClassException) {
            if (bean.isInterface()) {
                return resolveByInterface(bean);
            }
            throw resolveByClassException;
        }
    }

    @Override
    public <T> T resolveByClass(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException {
        if (beansByClass.containsKey(beanClass)) {
            var beanInfoList = beansByClass.get(beanClass);
            if (beanInfoList.size() == 0 && !shouldTryToCreateBean) {
                throw new NoSuchBeanException("Bean " + beanClass + " not found");
            }

            if (beanInfoList.size() > 1) {
                var definitions = beanInfoList.stream()
                        .map(element -> element.object().getClass().getName())
                        .collect(Collectors.toList());

                throw new MultipleBeanDefinitionException("Find two or more bean definitions: " + String.join(", ", definitions));
            }

            var beanInfo = beanInfoList.get(0);
            if (beanInfo.object() != null && !beanInfo.typeScope().equals(TypeScope.TRANSIENT)) {
                try {
                    return (T) postProcess(beanInfoList.get(0).object());
                } catch (ClassCastException | InvocationTargetException | IllegalAccessException e) {
                    throw new NoSuchBeanException("Bean " + beanClass + " not found");

                }
            }

            return resolveSingle(beanClass);
        }

        if (parentDiContainer != null) {
            return parentDiContainer.resolveByClass(beanClass);
        }

        if (!shouldTryToCreateBean) {
            throw new NoSuchBeanException("Bean " + beanClass + " not found");
        }

        return resolveSingle(beanClass);
    }

    @Override
    public <T> T resolveSingle(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException {
        checkModifiers(beanClass);

        Object createdObject = null;
        var reason = "";
        for (var constructor : beanClass.getConstructors()) {
            try {
                var createdBean = createBeanByConstructor(beanClass, constructor);
                if (createdObject != null) {
                    throw new MultipleConstructorException("bean " + beanClass + " can be resolved by two or more different constructors.");
                }
                createdObject = createdBean;
            } catch (CyclicDependenciesException exception) {
                throw exception;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ContainerException e) {
                log.debug(e.getMessage());
                reason = e.getMessage();
            }
        }

        if (createdObject == null && parentDiContainer != null) {
            createdObject = parentDiContainer.resolveSingle(beanClass);
        }

        if (createdObject == null) {
            throw new ResolveBeanException("bean " + beanClass + " can't be resolved. Reason: " + reason);
        }

        return (T) postProcess(createdObject);
    }

    @Override
    public <T> T resolveFirst(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException {
        checkModifiers(beanClass);

        Object createdObject = null;
        var reason = "";
        for (var constructor : beanClass.getConstructors()) {
            try {
                createdObject = createBeanByConstructor(beanClass, constructor);
                break;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ContainerException e) {
                log.debug(e.getMessage());
                reason = e.getMessage();
            }
        }

        if (createdObject == null && parentDiContainer != null) {
            createdObject = parentDiContainer.resolveFirst(beanClass);
        }

        if (createdObject == null) {
            throw new ResolveBeanException("bean " + beanClass + " can't be resolved. Reason: " + reason);
        }
        return (T) postProcess(createdObject);
    }


    @Override
    public <T> List<? extends T> resolveAll(Class<?> beanClass) throws ContainerException {
        List<? extends T> createdObjects = new ArrayList<>();
        try {
            var res = getClasses();
            for (var cl: res) {
                if (beanClass.isAssignableFrom(cl)) {
                    try {
                        createdObjects.add(resolveSingle(cl));
                    } catch (Exception ignored) {

                    }
                }
            }
            return createdObjects;
        } catch (Exception e) {
            throw new NoSuchBeanException("classes for bean " + beanClass + " not found");
        }
    }


    private List<Class<?>> getClasses() throws ClassNotFoundException, IOException {
        var classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        var file = new File(DiContainer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        var directories = file.list((current, name) -> new File(current, name).isDirectory());
        assert directories != null;

        var classes = new ArrayList<Class<?>>();

        for (var path: directories) {
            var resources = classLoader.getResources(path);
            var dirs = new ArrayList<File>();
            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (var directory : dirs) {
                classes.addAll(findClasses(directory, path));
            }
        }
        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        var classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        var files = directory.listFiles();

        if (files == null) {
            return classes;
        }

        for (var file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName));
            } else if (file.getName().endsWith(".class")) {
                var pattern = Pattern.quote(System.getProperty("file.separator"));
                var className = Arrays.stream(file.getPath().split(pattern)).collect(Collectors.toList());
                var index = className.indexOf(packageName);
                var name = String.join(".", className.subList(index, className.size()));

                classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
        }
        return classes;
    }

    private void checkModifiers(Class<?> beanClass) throws ModifierBeanException{
        var modifiers = beanClass.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new ModifierBeanException("Method doesn't support interface as input parameter");
        }
        if (Modifier.isAbstract(modifiers)) {
            throw new ModifierBeanException("Method doesn't support abstract class as input parameter");
        }
    }

    private Object createBeanByConstructor(Class<?> beanClass, Constructor<?> beanConstructor)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ContainerException {

        if (beanConstructor.getParameterCount() == 0) {
            return beanConstructor.newInstance((Object[]) null);
        }

        var params = beanConstructor.getParameters();

        if (!isParameterGraphCorrect(beanClass, params)) {
            throw new CyclicDependenciesException("constructor " + beanConstructor + " has cyclic dependencies");
        }
        parameterGraph.push(params);

        var i = 0;
        var resolvedParams = new Object[beanConstructor.getParameterCount()];
        for (var param : beanConstructor.getParameters()) {
            resolvedParams[i] = resolveByClass(param.getType());
            i++;
        }
        parameterGraph.pop();

        return beanConstructor.newInstance(resolvedParams);
    }

    private boolean isParameterGraphCorrect(Class<?> head, Parameter[] parameters) {
        if (parameters.length == 0) {
            return true;
        }

        var visitedNodes = new ArrayList<Node>();
        List<Class<?>> paramTypes = Arrays.stream(parameters).map(Parameter::getType).collect(Collectors.toList());
        var headNode = new Node(head, paramTypes);

        if (this.visitedNodes.contains(headNode)) {
            return true;
        }

        visitedNodes.add(headNode);

        if (!isValidClassesDependencies(paramTypes, visitedNodes)) {
            return false;
        }

        this.visitedNodes.addAll(visitedNodes);
        return true;
    }

    private boolean isValidClassesDependencies(List<Class<?>> types, List<Node> visitedNodes) {
        for (var type: types) {
            for (var constructor: type.getConstructors()) {
                List<Class<?>> paramTypes = Arrays.stream(constructor.getParameterTypes()).toList();
                var node = new Node(type, paramTypes);
                if (visitedNodes.contains(node)) {
                    if (!isValidNodeDependencies(node, visitedNodes.get(visitedNodes.indexOf(node)))) {
                        return false;
                    }
                } else {
                    visitedNodes.add(node);
                }
                return isValidClassesDependencies(paramTypes, visitedNodes);
            }
        }
        return true;
    }

    private boolean isValidNodeDependencies(Node node, Node visitedNode) {
        if (visitedNode.getTransitions().size() == 0) {
            return true;
        }
        return visitedNode.getTransitions().stream().noneMatch(it -> node.getTransitions().contains(it));
    }

    @Override
    public <T> T resolveByInterface(Class<?> beanInterface) throws ContainerException {
        if (!beanInterface.isInterface()) {
            throw new IllegalArgumentException(beanInterface + " is not interface");
        }
        Object result = null;
        for (var entry : beansByClass.entrySet()) {
            if (containsInterface(entry.getKey(), beanInterface)) {
                var info = entry.getValue();
                if (info.size() > 1 || result != null) {
                    var definitions = info.stream()
                            .map(element -> element.object().getClass().getName())
                            .collect(Collectors.toList());

                    throw new MultipleBeanDefinitionException("find two or more bean definitions: " + String.join(", ", definitions));
                }

                result = info.get(0).object();
            }
        }

        if (result == null && parentDiContainer != null) {
            result = parentDiContainer.resolveByInterface(beanInterface);
        }

        if (result == null)
            throw new NoSuchBeanException("Beans " + beanInterface + " not found");

        return (T) result;
    }

    private boolean containsInterface(Class<?> bean, Class<?> interfaceToSearch) {
        if (Arrays.asList(bean.getInterfaces()).contains(interfaceToSearch)) {
            return true;
        }

        var result = new ArrayList<>();
        for (var implemented : bean.getInterfaces()) {
            result.add(containsInterface(implemented, interfaceToSearch));
        }

        return result.contains(true);
    }

    @Override
    public <T> T resolveByName(String name) throws ContainerException, InvocationTargetException, IllegalAccessException {
        BeanInfo beanInfo = beansByNames.get(name);
        if (beanInfo != null) {
            var beanScope = beanInfo.typeScope();
            if (!beanScope.equals(TypeScope.TRANSIENT)) {
                return (T) postProcess(beanInfo.object());
            }
            throw new ContainerException("Can't resolve " + beanScope + " bean by name");
        } else {
            if (parentDiContainer == null) {
                throw new NoSuchBeanException("Don't find bean by name " + name);
            }
            return postProcess(parentDiContainer.resolveByName(name));
        }
    }


    private <T> T postProcess(Object object) throws InvocationTargetException, IllegalAccessException {
        autowiredAnnotationBeanPostProcessor.processInjection(object);
        PostConstructInvoker postConstructInvoker = new PostConstructInvoker();
        postConstructInvoker.invokePostConstructMethods(object);
        return (T)object;
    }

    @Override
    public void unregister(String name) {
        var beanInfo = beansByNames.remove(name);
        preDestroy(beanInfo);
        if (beanInfo != null) {
            for (var item : beansByClass.entrySet()) {
                preDestroy(item);
                item.getValue().remove(beanInfo);
            }
        }
    }

    @Override
    public void unregister(Class<?> bean) {
        var info = beansByClass.remove(bean);
        if (info.size() == 0) {
            return;
        }
        for (var beanInfo : info) {
            preDestroy(beanInfo.object());
            beansByNames.values().remove(beanInfo);
            for (var byClass : beansByClass.values()) {
                preDestroy(byClass);
                byClass.remove(beanInfo);
            }
        }
    }

    private void preDestroy(Object bean){
        PreDestroyInvoker preDestroyInvoker = new PreDestroyInvoker();
        preDestroyInvoker.invokePostConstructMethods(bean);
    }

    @Override
    public void dispose() {
        beansByNames.keySet().forEach(this::unregister);
    }

}
