package slice;

import com.ibm.wala.cast.java.client.ECJJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.JarFile;

public class MyECJJavaSourceAnalysisEngine extends ECJJavaSourceAnalysisEngine {
    private HashMap<String, SPLFeatureSourceLine> featureSourceLineMapping = new HashMap<>();
    public static MyECJJavaSourceAnalysisEngine instance = null;

    public MyECJJavaSourceAnalysisEngine() {
        super();
        instance = this;
    }

    public static SPLFeatureSourceLine getFeatureLineInfo(String key) {
        return instance.featureSourceLineMapping.get(key);
    }


    public AnalysisScope getScope() {
        return this.scope;
    }

    protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
        return allSourceEntrypoints(JavaSourceAnalysisScope.SOURCE, cha);
    }

    public static Iterable<Entrypoint> allSourceEntrypoints(ClassLoaderReference clr, IClassHierarchy cha) {
        if (cha == null) {
            throw new IllegalArgumentException("cha is null");
        } else {
            HashSet<Entrypoint> result = HashSetFactory.make();
            Iterator var4 = cha.iterator();

            while (var4.hasNext()) {
                IClass klass = (IClass) var4.next();
                if (klass.getClassLoader().getReference().equals(clr)) {
                    instance.featureSourceLineMapping.putAll(MappingUtil.getAllFeatureMappingLineFromFiles(klass.getSourceFileName(), klass.getName().toString()));
                    Iterator var5 = klass.getDeclaredMethods().iterator();

                    while (var5.hasNext()) {
                        IMethod method = (IMethod) var5.next();
                        if (!method.isAbstract()) {
                            result.add(new ArgumentTypeEntrypoint(method, cha));
                        }
                    }
                }
            }
            return result::iterator;
        }
    }

    public void addSystemDependencies() throws IOException {
        String[] stdlibs = WalaProperties.getJ2SEJarFiles();
        for (int i = 0; i < stdlibs.length; i++) {
            this.addSystemModule(new JarFileModule(new JarFile(stdlibs[i])));
        }
    }
}