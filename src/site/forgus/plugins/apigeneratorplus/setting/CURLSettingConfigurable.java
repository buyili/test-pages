package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {
    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";

    Project project;
    JBTabbedPane jbTabbedPane;

    CURLSettingState oldState;
    JBTextField baseApiTextField;

    CURLModuleInfoUI curlModuleInfoUI;
    JBTextArea canonicalClassNameTextFields;
    JBTextArea includeFiledTextFields;
    JBTextArea excludeFieldTextFields;
    JBTextField arrayFormatTextFields;
    JBCheckBox excludeChildrenCheckBox;

    JBTextField credentialsTextField;
    JBTextField cacheTextField;
    JBTextField redirectTextField;
    JBTextField referrerTextField;
    JBTextField referrerPolicyTextField;
    JBTextField integrityTextField;

    CopyAsAxiosUI copyAsAxiosUI;

    public CURLSettingConfigurable(Project project) {
        this.project = project;
        oldState = ServiceManager.getService(project, CURLSettingState.class);

        //for (CURLModuleInfo curlModuleInfo : oldState.moduleInfoList) {
        //    if (curlModuleInfo.getHeaders() != null && curlModuleInfo.getHeaders().size() > 0) {
        //        for (String[] header : curlModuleInfo.getHeaders()) {
        //            if (header.length == 2 && StringUtils.isNotBlank(header[0]) && StringUtils.isNotBlank(header[1])) {
        //                Header requestHeader = new Header();
        //                requestHeader.setKey(header[0]);
        //                requestHeader.setValue(header[1]);
        //                curlModuleInfo.getRequestHeaders().add(requestHeader);
        //            }
        //        }
        //    }
        //    curlModuleInfo.setHeaders(null);
        //}
    }

    @Override
    public String getDisplayName() {
        return "Copy as CURL";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        baseApiTextField = new JBTextField(oldState.baseApi);
        canonicalClassNameTextFields = new JBTextArea(oldState.filterFieldInfo.canonicalClassName, 3, 0);
        includeFiledTextFields = new JBTextArea(oldState.filterFieldInfo.includeFiled, 3, 0);
        excludeFieldTextFields = new JBTextArea(oldState.filterFieldInfo.excludeField, 3, 0);
        arrayFormatTextFields = new JBTextField(oldState.arrayFormat);
        excludeChildrenCheckBox = new JBCheckBox("", oldState.filterFieldInfo.excludeChildren);

        curlModuleInfoUI = new CURLModuleInfoUI(oldState);
        curlModuleInfoUI.reset(oldState.moduleInfoList);

        credentialsTextField = new JBTextField(oldState.fetchConfig.credentials);
        cacheTextField = new JBTextField(oldState.fetchConfig.cache);
        redirectTextField = new JBTextField(oldState.fetchConfig.redirect);
        referrerTextField = new JBTextField(oldState.fetchConfig.referrer);
        referrerPolicyTextField = new JBTextField(oldState.fetchConfig.referrerPolicy);
        integrityTextField = new JBTextField(oldState.fetchConfig.integrity);

        jbTabbedPane = new JBTabbedPane();

        JPanel modulePortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modulePortLabelPanel.add(new JBLabel("Module Info"));
        modulePortLabelPanel.add(LinkLabel.create("Find Module Info", new Runnable() {
            @Override
            public void run() {
                List<CURLModuleInfo> foundList = CurlUtils.findModuleInfo(project);
                if (isRepeat(foundList)) {
                    int yesNoCancel = Messages.showYesNoCancelDialog("???????????????????????????", "??????", Messages.getQuestionIcon());
                    if (Messages.YES == yesNoCancel) {
                        for (CURLModuleInfo foundItem : foundList) {
                            List<CURLModuleInfo> entries = curlModuleInfoUI.editor.getModel().getItems();
                            boolean repeat = false;
                            for (CURLModuleInfo entry : entries) {
                                if (entry.getModuleName().equals(foundItem.getModuleName())) {
                                    CURLModuleInfo mutable = curlModuleInfoUI.editor.getMutable(entry);
                                    mutable.setPort(entry.getPort());
                                    mutable.setContextPath(entry.getContextPath());

                                    CURLModuleInfo selected = curlModuleInfoUI.editor.getSelected();
                                    if (selected != null && entry.getId().equals(selected.getId())) {
                                        curlModuleInfoUI.itemPanel.setItem(mutable);
                                    }
                                    repeat = true;
                                    break;
                                }
                            }
                            if (!repeat) {
                                curlModuleInfoUI.editor.getModel().add(foundItem);
                            }
                        }
                    } else if (Messages.NO == yesNoCancel) {
                        for (CURLModuleInfo entry : curlModuleInfoUI.editor.getModel().getItems()) {
                            foundList.removeIf(curlModuleInfo -> curlModuleInfo.getModuleName().equals(entry.getModuleName()));
                        }
                        curlModuleInfoUI.editor.getModel().add(foundList);
                    }
                } else {
                    curlModuleInfoUI.editor.getModel().add(foundList);
                }
            }

            public boolean isRepeat(List<CURLModuleInfo> foundList) {
                for (CURLModuleInfo entry : curlModuleInfoUI.editor.getModel().getItems()) {
                    for (CURLModuleInfo curlModuleInfo : foundList) {
                        if (entry.getModuleName().equals(curlModuleInfo.getModuleName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }));


        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Base Api:"), baseApiTextField, 1, false)
                .addLabeledComponent(new JBLabel("Canonical Class Name:"), canonicalClassNameTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Include Fields:"), includeFiledTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Exclude Fields:"), excludeFieldTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Array Format:"), arrayFormatTextFields, 1, false)
                .addTooltip("indices    // 'a[0]=b&a[1]=c'      brackets    // 'a[]=b&a[]=c'        repeat  // 'a=b&a=c'        comma   // 'a=b,c'")
                .addLabeledComponent(new JBLabel("Exclude Children Field"), excludeChildrenCheckBox)
                .addVerticalGap(16)
                .addSeparator()
                .addComponent(modulePortLabelPanel, 0)
                .addComponentFillVertically(curlModuleInfoUI.getComponent(), 0)
                .getPanel();
        jbTabbedPane.add("Copy as cURL", jPanel);

        JPanel fetchPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("credentials:"), credentialsTextField, 1, false)
                .addTooltip("????????? credentials?????? omit???same-origin ?????? include??????????????????????????????????????? cookie ??? ??????????????????????????? ")
                .addTooltip("??? Chrome 50 ????????? ??????????????????????????? FederatedCredential ?????????????????? PasswordCredential ?????????")
                .addLabeledComponent(new JBLabel("cache:"), cacheTextField, 1, false)
                .addTooltip("????????? cache ??????: default??? no-store??? reload ??? no-cache ??? force-cache ?????? only-if-cached ???")
                .addLabeledComponent(new JBLabel("redirect:"), redirectTextField, 1, false)
                .addTooltip("????????? redirect ??????: follow (???????????????), error (???????????????????????????????????????????????????????????????, ?????? manual (?????????????????????). ")
                .addTooltip("???Chrome???????????????follow???Chrome 47?????????????????????manual??????")
                .addLabeledComponent(new JBLabel("referrer:"), referrerTextField, 1, false)
                .addTooltip("?????? USVString ????????? no-referrer???client????????? URL???????????? client???")
                .addLabeledComponent(new JBLabel("referrerPolicy:"), referrerPolicyTextField, 1, false)
                .addTooltip("no-referrer??? no-referrer-when-downgrade??? origin???origin-when-cross-origin??? unsafe-url ???")
                .addLabeledComponent(new JBLabel("integrity:"), integrityTextField)
                .addTooltip("???????????????  subresource integrity ??? ??? ????????? sha256-BpfBw7ivV8q2jLiT13fxDYAe2tJllusRSZ273h2nFSE=??????")
                .addVerticalGap(4)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        jbTabbedPane.add("Copy as fetch", fetchPanel);

        copyAsAxiosUI = new CopyAsAxiosUI(project, oldState);
        jbTabbedPane.add("Copy as Axios", copyAsAxiosUI.createComponent());

        return jbTabbedPane;
    }

    @Override
    public boolean isModified() {
        if (!oldState.baseApi.equals(baseApiTextField.getText())
                || !oldState.filterFieldInfo.canonicalClassName.equals(canonicalClassNameTextFields.getText())
                || !oldState.filterFieldInfo.includeFiled.equals(includeFiledTextFields.getText())
                || !oldState.filterFieldInfo.excludeField.equals(excludeFieldTextFields.getText())
                || !oldState.arrayFormat.equals(arrayFormatTextFields.getText())
                || oldState.filterFieldInfo.excludeChildren != excludeChildrenCheckBox.isSelected()
        ) {
            return true;
        }
        if (!oldState.fetchConfig.credentials.equals(credentialsTextField.getText())
                || !oldState.fetchConfig.cache.equals(cacheTextField.getText())
                || !oldState.fetchConfig.redirect.equals(redirectTextField.getText())
                || !oldState.fetchConfig.referrer.equals(referrerTextField.getText())
                || !oldState.fetchConfig.referrerPolicy.equals(referrerPolicyTextField.getText())
                || !oldState.fetchConfig.integrity.equals(integrityTextField.getText())
        ) {
            return true;
        }

        if(copyAsAxiosUI.isModified()){
            return true;
        }

        return curlModuleInfoUI.isModified(oldState.moduleInfoList);
    }

    @Override
    public void apply() throws ConfigurationException {
        oldState.baseApi = baseApiTextField.getText();
        oldState.filterFieldInfo.canonicalClassName = canonicalClassNameTextFields.getText();
        oldState.filterFieldInfo.includeFiled = includeFiledTextFields.getText();
        oldState.filterFieldInfo.excludeField = excludeFieldTextFields.getText();
        oldState.arrayFormat = arrayFormatTextFields.getText();
        oldState.filterFieldInfo.excludeChildren = excludeChildrenCheckBox.isSelected();


        oldState.fetchConfig.credentials = credentialsTextField.getText();
        oldState.fetchConfig.cache = cacheTextField.getText();
        oldState.fetchConfig.redirect = redirectTextField.getText();
        oldState.fetchConfig.referrer = referrerTextField.getText();
        oldState.fetchConfig.referrerPolicy = referrerPolicyTextField.getText();
        oldState.fetchConfig.integrity = integrityTextField.getText();

        curlModuleInfoUI.apply(oldState.moduleInfoList);
        copyAsAxiosUI.apply();
    }

    @Override
    public void reset() {
        baseApiTextField.setText(oldState.baseApi);
        canonicalClassNameTextFields.setText(oldState.filterFieldInfo.canonicalClassName);
        includeFiledTextFields.setText(oldState.filterFieldInfo.includeFiled);
        excludeFieldTextFields.setText(oldState.filterFieldInfo.excludeField);
        arrayFormatTextFields.setText(oldState.arrayFormat);
        excludeChildrenCheckBox.setSelected(oldState.filterFieldInfo.excludeChildren);

        credentialsTextField.setText(oldState.fetchConfig.credentials);
        cacheTextField.setText(oldState.fetchConfig.cache);
        redirectTextField.setText(oldState.fetchConfig.redirect);
        referrerTextField.setText(oldState.fetchConfig.referrer);
        referrerPolicyTextField.setText(oldState.fetchConfig.referrerPolicy);
        integrityTextField.setText(oldState.fetchConfig.integrity);

        curlModuleInfoUI.reset(oldState.moduleInfoList);
        copyAsAxiosUI.reset();
    }

    @Override
    public void disposeUIResources() {
        jbTabbedPane = null;
        copyAsAxiosUI = null;
    }
}
