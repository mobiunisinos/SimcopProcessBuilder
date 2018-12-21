package br.unisinos.simcop.builder.repository;

import br.unisinos.simcop.Utils;
import br.unisinos.simcop.core.ISimcopClass;
import br.unisinos.simcop.core.config.ISimcopConfig;
import br.unisinos.simcop.data.source.ISequenceSource;
import br.unisinos.simcop.interfaces.filters.IFilterInput;
import br.unisinos.simcop.interfaces.filters.IFilterOutput;
import br.unisinos.simcop.interfaces.similarity.IAttributeSimilarity;
import br.unisinos.simcop.interfaces.similarity.IContextSimilarity;
import br.unisinos.simcop.interfaces.similarity.ISequenceSimilarity;
import br.unisinos.simcop.interfaces.transformations.ITransformInput;
import br.unisinos.simcop.interfaces.transformations.ITransformOutput;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tiago
 */
public class KnowClasses {

    private List<ISequenceSimilarity> forSequences;
    private List<IContextSimilarity> forContexts;
    private List<IAttributeSimilarity> forAttributes;
    private List<ISequenceSource> forSources;
    private List<IFilterInput> forInputFilters;
    private List<IFilterOutput> forOutputFilters;
    private List<ITransformInput> forInputTransformations;
    private List<ITransformOutput> forOutputTransformations;
    private List<ISimcopConfig> forConfigStore;

    public void refresh() {
        Utils.log("BEGIN: LOADING CLASSES");
        Utils.log("  - initialize");
        forSequences = new ArrayList<ISequenceSimilarity>();
        forContexts = new ArrayList<IContextSimilarity>();
        forAttributes = new ArrayList<IAttributeSimilarity>();
        forSources = new ArrayList<ISequenceSource>();
        forConfigStore = new ArrayList<ISimcopConfig>();
        forInputFilters = new ArrayList<IFilterInput>();
        forOutputFilters = new ArrayList<IFilterOutput>();
        forInputTransformations = new ArrayList<ITransformInput>();
        forOutputTransformations = new ArrayList<ITransformOutput>();

        String cp = System.getProperty("java.class.path");
        if (cp != null) {
            String[] paths = cp.split(":");
               
            Set<String> classFiles = new HashSet<String>();
            for (String path : paths) {                
                loadFiles(path, classFiles, path);
            }
            int cnt = 0;
            for (String className : classFiles) {
                
                if (createInstance(className)) {
                    cnt++;
                    Utils.log("  " + cnt + ". " + className);
                }
            }
        }
        Utils.log("END: LOADING CLASSES");
    }

    private boolean isConcrete(Class c) {
        return c != null
                && !Modifier.isAbstract(c.getModifiers())
                && !Modifier.isInterface(c.getModifiers())
                && Modifier.isPublic(c.getModifiers());
    }

    private boolean isInterface(Class c, Class itf) {
        if (c.getSuperclass() != null) {
            if (isInterface(c.getSuperclass(), itf)) {
                return true;
            }
        }
        for (Class implemented : c.getInterfaces()) {
            if (implemented.getName().equalsIgnoreCase(itf.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean createInstance(String className) {
        try {
            Class c = Class.forName(className);
            if (isConcrete(c)) {

                if (isInterface(c, ISequenceSimilarity.class)) {
                    forSequences.add((ISequenceSimilarity) c.newInstance());
                    return true;

                } else if (isInterface(c, IContextSimilarity.class)) {
                    forContexts.add((IContextSimilarity) c.newInstance());
                    return true;

                } else if (isInterface(c, IAttributeSimilarity.class)) {
                    forAttributes.add((IAttributeSimilarity) c.newInstance());
                    return true;
                    
                } else if (isInterface(c, ISequenceSource.class)) {
                    forSources.add((ISequenceSource) c.newInstance());
                    return true;

                } else if (isInterface(c, IFilterInput.class)) {
                    forInputFilters.add((IFilterInput) c.newInstance());
                    return true;

                } else if (isInterface(c, IFilterOutput.class)) {
                    forOutputFilters.add((IFilterOutput) c.newInstance());
                    return true;

                } else if (isInterface(c, ITransformInput.class)) {
                    forInputTransformations.add((ITransformInput) c.newInstance());
                    return true;

                } else if (isInterface(c, ITransformOutput.class)) {
                    forOutputTransformations.add((ITransformOutput) c.newInstance());
                    return true;
                } else if (isInterface(c, ISimcopConfig.class)) {
                    forConfigStore.add((ISimcopConfig) c.newInstance());
                    return true;
                }
            }
            return false;

        } catch (Throwable e) {
            Utils.log("  - skipped due to error: className=" + className + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            System.exit(-1);
            return false;
        }

    }

    private void loadFiles(final String path, final Set<String> list, final String sourcePath) {
        File dir = new File(path);
        dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    loadFiles(pathname.getAbsolutePath(), list, sourcePath);
                    return false;
                } else {
                    boolean exists = pathname.exists() && pathname.canRead();
                    //TODO: process jar file
                    boolean valid = (!pathname.getName().contains("$")) && (!pathname.getName().contains("<"))
                            && pathname.getName().endsWith(".class");

                    if (exists && valid) {
                        String className = pathname.getAbsolutePath().substring(sourcePath.length()); //sourcePath always at begin of the string
                        className = className.replace('/', '.').replace('\\', '.');
                        if (className.startsWith(".")) {
                            className = className.substring(1);
                        }
                        className = className.substring(0, className.lastIndexOf("."));
                        list.add(className);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });
    }

    public List<ISequenceSimilarity> getForSequences() {
        return forSequences;
    }

    public void setForSequences(List<ISequenceSimilarity> forSequences) {
        this.forSequences = forSequences;
    }

    public List<IContextSimilarity> getForContexts() {
        return forContexts;
    }

    public void setForContexts(List<IContextSimilarity> forContexts) {
        this.forContexts = forContexts;
    }

    public List<ISequenceSource> getForSources() {
        return forSources;
    }

    public void setForSources(List<ISequenceSource> forSources) {
        this.forSources = forSources;
    }

    public List<ISimcopConfig> getForConfigStore() {
        return forConfigStore;
    }

    public void setForConfigStore(List<ISimcopConfig> forConfigStore) {
        this.forConfigStore = forConfigStore;
    }

    public List<IFilterInput> getForInputFilters() {
        return forInputFilters;
    }

    public List<IFilterOutput> getForOutputFilters() {
        return forOutputFilters;
    }

    public List<ITransformInput> getForInputTransformations() {
        return forInputTransformations;
    }

    public List<ITransformOutput> getForOutputTransformations() {
        return forOutputTransformations;
    }

    public ISimcopClass getInstanceForConfig(String className) {
        return getInstance(forConfigStore, className);
    }
    public ISimcopClass getInstanceForContext(String className) {
        return getInstance(forContexts, className);
    }
    public ISimcopClass getInstanceForInFilter(String className) {
        return getInstance(forInputFilters, className);
    }
    public ISimcopClass getInstanceForInTransf(String className) {
        return getInstance(forInputTransformations, className);
    }
    public ISimcopClass getInstanceForOutFilter(String className) {
        return getInstance(forOutputFilters, className);
    }
    public ISimcopClass getInstanceForOutTransf(String className) {
        return getInstance(forOutputTransformations, className);
    }
    public ISimcopClass getInstanceForSequence(String className) {
        return getInstance(forSequences, className);
    }
    public ISimcopClass getInstanceForSource(String className) {
        return getInstance(forSources, className);
    }



    private ISimcopClass getInstance(List list, String className) {
        ISimcopClass result = null;
        if (list != null) {
            Iterator it = list.iterator();
            while (result == null && it.hasNext()) {
                Object instance = it.next();
                if (instance instanceof ISimcopClass && instance.getClass().getName().equalsIgnoreCase(className)) {
                    result = (ISimcopClass)instance;
                }
            }
        }
        return result;
    }

    public List<IAttributeSimilarity> getForAttributes() {
        return forAttributes;
    }

    public void setForAttributes(List<IAttributeSimilarity> forAttributes) {
        this.forAttributes = forAttributes;
    }
}
