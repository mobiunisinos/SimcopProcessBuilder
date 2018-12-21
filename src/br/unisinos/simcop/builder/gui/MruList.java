package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.Utils;
import br.unisinos.simcop.builder.repository.KnowClasses;
import br.unisinos.simcop.core.Parameter;
import br.unisinos.simcop.core.Parameters;
import br.unisinos.simcop.core.config.ISimcopConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tiago
 */
public class MruList {

    public static final int MAX_MRU = 10;
    private File storeFile;
    private KnowClasses knowClasses;
    private Map<String, List<MruItem>> internalStore;

    public MruList(KnowClasses knowClasses) {
        this.knowClasses = knowClasses;
        String home = System.getProperty("user.home") + "/.simcop/";
        File homeDir = new File(home);
        if (!homeDir.exists()) {
            homeDir.mkdir();
        }
        storeFile = new File(home + "builder.mru");
        internalStore = new HashMap<String, List<MruItem>>();
    }

    public List<String> getList(String className) {
        List<String> result = new ArrayList<String>();
        if (internalStore != null) {
            List<MruItem> itens = internalStore.get(className);
            if (itens != null) {
                for (MruItem item : itens) {
                    result.add(item.name);
                }
            }
        }
        return result;
    }

    public void load() {
        internalStore = new HashMap<String, List<MruItem>>();
        if (storeFile != null && storeFile.exists() && storeFile.canRead()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(storeFile));
                String linha;
                String className, name;
                MruItem mruItem = null;
                while ((linha = in.readLine()) != null) {
                    linha = linha.trim();
                    if (!linha.startsWith("#") && linha.length() > 0) {
                        if (linha.startsWith("PAR:")) {
                            if (mruItem != null) {
                                String[] fields = linha.split(":");
                                String key = null, value = null, pattern = null;
                                switch (fields.length) {
                                    case 3:
                                        key = fields[1];
                                        value = fields[2];
                                        break;
                                    case 4:
                                        key = fields[1];
                                        value = fields[2];
                                        pattern = fields[3];
                                        break;
                                }
                                if (key != null) {
                                    mruItem.parameters.addParameter(key, value, pattern);
                                }
                            }
                        } else {
                            String[] fields = linha.split(":");
                            if (fields.length == 2) {
                                className = fields[0];
                                name = fields[1];
                                mruItem = new MruItem(className, name, new Parameters());
                                List<MruItem> lst = internalStore.get(className);
                                if (lst == null) {
                                    lst = new LinkedList<MruItem>();
                                    internalStore.put(className, lst);
                                }
                                lst.add(mruItem);
                            }
                        }
                    }
                }
                in.close();
            } catch (Exception e) {
                Utils.log(e);
            }
        }
    }

    public void save() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(storeFile));
            out.write("# MRU File updated at " + new Date());
            out.newLine();
            for (String className : internalStore.keySet()) {
                List<MruItem> mruList = internalStore.get(className);
                for (MruItem mruItem : mruList) {
                    out.newLine();
                    out.write(mruItem.className + ":" + mruItem.name);
                    if (mruItem.parameters != null && !mruItem.parameters.isEmpty()) {
                        for (Parameter par : mruItem.parameters.asList()) {
                            out.newLine();
                            if (Utils.isEmpty(par.getPattern())) {
                                out.write("PAR:" + par.getKey() + ":" + par.getValue());
                            } else {
                                out.write("PAR:" + par.getKey() + ":" + par.getValue() + ":" + par.getPattern());
                            }
                        }

                    }
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            Utils.log(e);
        }
    }

    public void store(String className, String name, Parameters parameters) {
        List<MruItem> lst = internalStore.get(className);
        if (lst == null) {
            lst = new LinkedList<MruItem>();
            internalStore.put(className, lst);
        }
        MruItem mruItem = findItem(lst, name);
        if (mruItem == null) {
            lst.add(0, new MruItem(className, name, parameters));
            if (lst.size() > MAX_MRU) {
                lst.remove(lst.size() - 1);
            }
        } else {
            mruItem.parameters = parameters;
        }
        save();
    }

    private MruItem findItem(List<MruItem> lst, String name) {
        MruItem result = null;
        if (lst != null) {
            Iterator<MruItem> it = lst.iterator();
            while (result == null && it.hasNext()) {
                MruItem itm = it.next();
                if (itm.name.equalsIgnoreCase(name)) {
                    result = itm;
                }
            }
        }
        return result;
    }

    public ISimcopConfig open(String className, String name) throws Exception {
        ISimcopConfig cfg = (ISimcopConfig) knowClasses.getInstanceForConfig(className);
        if (cfg != null) {
            MruItem configItem = findItem(internalStore.get(className), name);
            if (configItem != null) {
                cfg.setParameters(configItem.parameters);
                cfg.load();
            } else {
                Utils.log("Not Found: " + name + " in " + className);
            }
        }
        return cfg;
    }

    class MruItem {

        String className;
        String name;
        Parameters parameters;

        public MruItem(String className, String name, Parameters parameters) {
            this.className = className;
            this.name = name;
            this.parameters = parameters;
        }
    }
}
