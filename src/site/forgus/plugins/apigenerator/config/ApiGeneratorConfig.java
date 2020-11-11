package site.forgus.plugins.apigenerator.config;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@State(name = "ApiGeneratorConfig2")
public class ApiGeneratorConfig implements PersistentStateComponent<ApiGeneratorConfig> {

    public Set<String> excludeFieldNames = new HashSet<>();
    public String excludeFields = "serialVersionUID";
    public String dirPath = "";
    public String prefix = "└";
    public Boolean cnFileName = false;
    public Boolean overwrite = true;

    public String yApiServerUrl = "";
    public String projectToken = "";
    public String projectId = "";
    public Boolean autoCat = false;
    public String defaultCat = "api_generator";


    public List<YApiProjectConfigInfo> yApiProjectConfigInfoList = new ArrayList<>();



    @Nullable
    @Override
    public ApiGeneratorConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiGeneratorConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
