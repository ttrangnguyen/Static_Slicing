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

    public String getKey() {
        return String.format("%s.%s", className, productLineNumber);
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", featureName, FileUtil.convertRelativeJavaFilePathToClass(sourceFilePath), featureLineNumber);
    }

    public String getFeatureName() {
        return featureName;
    }
}
