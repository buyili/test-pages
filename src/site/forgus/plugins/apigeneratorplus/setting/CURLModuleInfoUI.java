package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.*;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.ListItemEditor;
import com.intellij.util.ui.ListModelEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

/**
 * @author lmx 2020/12/1 11:37
 */

public class CURLModuleInfoUI implements ConfigurableUi<List<CURLModuleInfo>> {

    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";

    private final JPanel itemPanelWrapper;
    final CardLayout cardLayout;
    CURLModuleInfoPanel itemPanel;

    private CURLSettingState oldState;

    private final ListItemEditor<CURLModuleInfo> itemEditor = new ListItemEditor<CURLModuleInfo>() {

        @NotNull
        @Override
        public Class<? extends CURLModuleInfo> getItemClass() {
            return CURLModuleInfo.class;
        }

        @Override
        public CURLModuleInfo clone(@NotNull CURLModuleInfo item, boolean forInPlaceEditing) {
            return item.clone();
        }

        @Override
        public boolean isEmpty(@NotNull CURLModuleInfo item) {
            return item.getModuleName().isEmpty();
        }

        @Override
        public boolean isEditable(@NotNull CURLModuleInfo item) {
            return !oldState.moduleInfoList.contains(item);
        }

        @NotNull
        @Override
        public String getName(@NotNull CURLModuleInfo item) {
            return item.getModuleName();
        }
    };

    private final ListModelEditor<CURLModuleInfo> editor = new ListModelEditor<>(itemEditor);

    private final JComponent component;

    protected CURLModuleInfoUI(CURLSettingState state) {
        this.oldState = state;
        cardLayout = new CardLayout();

        editor.disableUpDownActions();
        editor.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                CURLModuleInfo item = editor.getSelected();
                if (item == null) {
                    cardLayout.show(itemPanelWrapper, EMPTY);
                    itemPanel.setItem(null);
                } else {
                    cardLayout.show(itemPanelWrapper, PANEL);
                    itemPanel.setItem(editor.getMutable(item));
                }
            }
        });

        itemPanel = new CURLModuleInfoPanel(editor.getModel());
        itemPanel.moduleNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                CURLModuleInfo item = itemPanel.item;
                if (item != null) {
                    String name = itemPanel.moduleNameTextField.getText();
                    boolean changed = !item.getModuleName().equals(name);
                    item.setModuleName(name);
                    if (changed) {
                        editor.getList().repaint();
                    }
                }
            }
        });


        itemPanelWrapper = new JPanel(cardLayout);

        JLabel descLabel =
                new JLabel("<html>select module on left</html>");
        descLabel.setBorder(new EmptyBorder(0, 25, 0, 25));

        itemPanelWrapper.add(descLabel, EMPTY);
        itemPanelWrapper.add(itemPanel.getPanel(), PANEL);

        Splitter splitter = new Splitter(false, 0.25f);
        splitter.setFirstComponent(editor.createComponent());
        splitter.setSecondComponent(itemPanelWrapper);
        component = splitter;
    }


    protected CURLModuleInfo createElement() {
        CURLModuleInfo curlModuleInfo = new CURLModuleInfo();
        curlModuleInfo.setId(String.valueOf(System.currentTimeMillis()));
        curlModuleInfo.setModuleName(StringUtil.getName());
        return curlModuleInfo;
    }


    @Override
    public void reset(@NotNull List<CURLModuleInfo> settings) {
        editor.reset(settings);
    }

    @Override
    public boolean isModified(@NotNull List<CURLModuleInfo> settings) {
        itemPanel.apply();
        return editor.isModified();
    }

    @Override
    public void apply(@NotNull List<CURLModuleInfo> settings) throws ConfigurationException {
        itemPanel.apply();

        editor.ensureNonEmptyNames("Quick list should have non empty name");
        editor.processModifiedItems((newItem, oldItem) -> {
            if (!oldItem.getModuleName().equals(newItem.getModuleName())) {
//                keymapListener.quickListRenamed(oldItem, newItem);
            }
            return true;
        });

        if (isModified(settings)) {
            List<CURLModuleInfo> result = editor.apply();
            if(result.size() == 0){
                editor.reset(result);
            }

            CURLModuleInfo item = editor.getSelected();
            if (item != null) {
                itemPanel.setItem(editor.getMutable(item));
            }
            oldState.moduleInfoList = result;
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return component;
    }
}
