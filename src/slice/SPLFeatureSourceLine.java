package slice;

public class SPLFeatureSourceLine {
    private String featureName;
    private String className;
    private String sourceFilePath;
    private String methodName;
    private int featureLineNumber;
    private int productLineNumber;

    public SPLFeatureSourceLine(String sourceFilePath, String featureName, String className, String methodName, int featureLineNumber, int productLineNumber) {
        this.featureName = featureName;
        this.className = MappingUtil.normalizeClassName(className);
        this.methodName = methodName;
        this.sourceFilePath = sourceFilePath;
        this.featureLineNumber = featureLineNumber;
        this.productLineNumber = productLineNumber;
    }

    public String getProductLineKey() {
        return String.format("%s.%s", className, productLineNumber);
    }

    public String getFeatureLineKey(){
        return String.format("%s.%s.%s", featureName, FileUtil.convertRelativeJavaFilePathToClass(sourceFilePath), featureLineNumber);
    }

    @Override
    public String toString() {
        return this.getFeatureLineKey();
    }

    public String getFeatureName() {
        return featureName;
    }
}
