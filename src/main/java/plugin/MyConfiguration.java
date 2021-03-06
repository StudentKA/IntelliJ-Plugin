package plugin;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import filtering.ExpressionInputFilter;
import filtering.HighlightFilter;
import stuff.Rehighlighter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stuff.ExpressionItem;
import stuff.Operation;
import ui.MyForm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@State(
        name="Highlighting console",
        storages = {
                @Storage("/HighlightingConsole.xml")}
)
public class MyConfiguration implements ApplicationComponent, Configurable, PersistentStateComponent<MyConfiguration> {
    private static final String MAX_PROCESSING_TIME_DEFAULT = "1000";
    private static final int maxLengthToMatch = 120;
    private List<ExpressionItem> expressionItems = new ArrayList<>();

    @Transient
    private MyForm form;
    @Transient
    private ConsoleView console;

    public MyConfiguration() {

    }

    public static MyConfiguration getInstance() {
        return ApplicationManager.getApplication().getComponent(MyConfiguration.class);
    }

    public HighlightFilter createHighlightFilter() {
        return new HighlightFilter();
    }

    public InputFilter createInputFilter() {
        return new ExpressionInputFilter();
    }

    public List<ExpressionItem> getExpressionItems() {
        return expressionItems;
    }

    public MyForm getForm() {
        return form;
    }

    public void setExpressionItems(ExpressionItem item) {
        if (!expressionItems.contains(item))
            this.expressionItems.add(item);
    }


    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Highlighting console";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new MyForm();
        }
        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form.isChanged();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (console != null) {
            new Rehighlighter().rehighlight(console);
        }
    }

    @NotNull
    public String limitInputLength_andCutNewLine(@NotNull String text) {
        int endIndex = text.length();
        if (text.endsWith("\n")) {
            --endIndex;
        }
        endIndex = Math.min(endIndex, maxLengthToMatch);
        return text.substring(0, endIndex);
    }

    @NotNull
    public CharSequence limitProcessingTime(String substring) {
        return StringUtil.newBombedCharSequence(substring, Integer.valueOf(MAX_PROCESSING_TIME_DEFAULT));
    }

    public void setConsole(ConsoleView consoleView) {
        if (consoleView != null && consoleView != console) {
            this.console = consoleView;
        }
        if (consoleView == null) System.out.println("It's null (((");
    }

    public void createHighlightFilterIfMissing(@NotNull ConsoleView console) {
        if (console instanceof ConsoleViewImpl) {
            HighlightFilter highlightFilter = createHighlightFilter();
            console.addMessageFilter(highlightFilter);
        }
    }

    @Nullable
    @Override
    public MyConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MyConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void deleteItem(ExpressionItem delete) {
        for (Iterator<ExpressionItem> i = expressionItems.iterator(); i.hasNext();) {
            ExpressionItem item = i.next();
            if (item.equals(delete)) {
                i.remove();
            }
        }
    }

    public void addToPanel(String expression, Color color, Operation operation) {
        if (form == null) return;
        form.addUIItemFromEditor(expression, color, operation);
    }

}
