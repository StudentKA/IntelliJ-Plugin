package highlight;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.Configuration;
import plugin.ExpressionProcessor;
import plugin.MyConfigurable;
import stuff.ExpressionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighlightFilter implements Filter {

    private Project project;
    private MyConfigurable configuration;
    private List<ExpressionProcessor> expressionProcessors;
    private ConsoleViewContentType lastTextAttributes = null;

    public HighlightFilter(@NotNull Project project, MyConfigurable configuration) {
        this.project = project;
        this.configuration = configuration;
        expressionProcessors = new ArrayList<>();
        for (ExpressionItem item : configuration.getExpressionItems()) {
            expressionProcessors.add(new ExpressionProcessor(item));
            System.out.println(item.toString());
        }
    }

    public ExpressionItem getExpressionItem() {
        return expressionProcessors.get(0).getExpressionItem();
    }

    @Nullable
    @Override
    public Result applyFilter(String line, int entireLength) {
        System.out.println("We entered apply" + line);
        int offset = entireLength;
        if (line != null)
            offset = entireLength-line.length();
        FilterState state = filter(line, offset);
        Result result = null;
        if (state != null) {
            result = prepareResult(entireLength, state);
        }
        return result;
    }

    private final FilterState filter(@Nullable String text, int offset) {
        if (!StringUtils.isEmpty(text) && !expressionProcessors.isEmpty()) {
            String substring = configuration.limitInputLength_andCutNewLine(text);
            System.out.println("substring: " + substring);
            CharSequence charSequence = configuration.limitProcessingTime(substring);
            System.out.println(charSequence.toString());

            FilterState state = new FilterState(offset, text, configuration, charSequence);
            for (ExpressionProcessor processor : expressionProcessors) {
                    state = processor.process(state);
            }

            return state;
        }
        return null;
    }

    private Result prepareResult(int entireLength, FilterState state) {
        Result result = null;
        List<ResultItem> resultItemList = adjustWholeLineMatch(entireLength, state);
        if (resultItemList != null) {
            result = new Result(resultItemList);
            result.setNextAction(NextAction.CONTINUE_FILTERING);
        }
        return result;
    }

    protected List<ResultItem> adjustWholeLineMatch(int entireLength, FilterState state) {
        ConsoleViewContentType textAttributes = state.getConsoleViewContentType();
        List<ResultItem> resultItemList = state.getResultItemList();
        if (textAttributes != null) {
            lastTextAttributes = textAttributes;
            if (resultItemList == null) {
                resultItemList = Collections.singletonList(getResultItem(entireLength, state, textAttributes));
            } else {
                resultItemList.add(getResultItem(entireLength, state, textAttributes));
            }
        } else if (lastTextAttributes != null) {
            if (resultItemList == null) {
                resultItemList = Collections.singletonList(getResultItem(entireLength, state, lastTextAttributes));
            } else {
                resultItemList.add(getResultItem(entireLength, state, lastTextAttributes));
            }
        }
        return resultItemList;
    }
    private ResultItem getResultItem(int entireLength, FilterState state, ConsoleViewContentType textAttributes) {
        return new ResultItem(state.getOffset(), entireLength, null, textAttributes.getAttributes());
    }
}
