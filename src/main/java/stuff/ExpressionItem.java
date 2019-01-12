package stuff;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.util.xmlb.annotations.Transient;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ExpressionItem {
    public static AtomicInteger atomicInteger = new AtomicInteger();

    @Transient
    private String id;
    private String expression;
    private transient Pattern pattern;
    //there it works only after re-running
    private Boolean shallHide;
    private Boolean isWholeLine;
    private Boolean shallHighlight;
    private ItemStyle style = new ItemStyle();

    public ExpressionItem(String id) {
        this.id = String.valueOf(atomicInteger.incrementAndGet());
    }

    public void setStyle(Color foreground, Color background) {
        this.style.setForeground(foreground);
        this.style.setBackground(background);
    }

    public void setWholeLine(Boolean wholeLine) {
        isWholeLine = wholeLine;
    }

    public Boolean isWholeLine() {
        return isWholeLine;
    }

    public Boolean shallHide() {
        return shallHide;
    }

    @Transient
    public String getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        if (this.expression == null || expression == null || !this.expression.equals(expression)) {
            this.expression = expression;
            this.pattern = Pattern.compile(expression);
        }
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean isEnabled() {
        return true;
    }

    //TODO (NOT MY) not very good implementation
    public ConsoleViewContentType getConsoleViewContentType(ConsoleViewContentType consoleViewContentType) {
        ConsoleViewContentType result;
        if (consoleViewContentType != null) {
            // TODO maybe tree would have better performance?
            final String newId = consoleViewContentType.toString() + "-" + getId();
            result = Cache.getInstance().get(newId);
            if (result == null) {
                result = createConsoleViewContentType(newId, consoleViewContentType.getAttributes().clone());
            }
        } else {
            String cacheIdentifier = getId();
            result = Cache.getInstance().get(cacheIdentifier);
            if (result == null) {
                result = createConsoleViewContentType(cacheIdentifier, new TextAttributes());
            }
        }
        return result;
    }

    private ConsoleViewContentType createConsoleViewContentType(String newId, TextAttributes newTextAttributes) {
        ConsoleViewContentType result;
        style.applyTo(newTextAttributes);
        result = new ConsoleViewContentType(newId, newTextAttributes);
        Cache.getInstance().put(newId, result);
        return result;
    }

    @Override
    public String toString() {
        return "ExpressionItem{" +
                "expression='" + expression + '\'' +
                ", pattern=" + pattern +
                '}';
    }

    private class ItemStyle {
        private Color foreground = Color.BLACK;
        private Color background = Color.BLACK;

        public void applyTo(TextAttributes newTextAttributes) {
            if (foreground != null) {
                newTextAttributes.setForegroundColor(foreground);
            }
            if (background != null) {
                newTextAttributes.setBackgroundColor(background);
            }
        }

        public void setForeground(Color foreground) {
            this.foreground = foreground;
        }

        public void setBackground(Color background) {
            this.background = background;
        }
    }
}
