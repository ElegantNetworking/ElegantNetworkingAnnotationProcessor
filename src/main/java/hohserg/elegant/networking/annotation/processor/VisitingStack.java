package hohserg.elegant.networking.annotation.processor;

import javax.lang.model.element.Element;
import java.util.Stack;
import java.util.stream.Collectors;

public class VisitingStack {
    private Stack<Element> stack = new Stack<>();

    public void visit(Element e) {
        stack.push(e);
    }

    public void back() {
        stack.pop();
    }

    @Override
    public String toString() {
        return "VisitingStack(" + stack.stream().map(Element::getSimpleName).collect(Collectors.joining(" -> ")) + ')';
    }
}
