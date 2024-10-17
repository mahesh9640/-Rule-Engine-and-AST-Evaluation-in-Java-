import java.util.*;
import java.util.Stack;

// Class representing the nodes of the AST (Abstract Syntax Tree)
class Node {
    String type;  // "operator" or "operand"
    Node left, right;  // left and right children for operators
    String value;  // value for operand nodes

    // Constructor for operand nodes
    Node(String type, String value) {
        this.type = type;
        this.value = value;
    }

    // Constructor for operator nodes
    Node(String type, Node left, Node right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }
}

public class RuleEngine {

    // Create AST from a rule string
    public Node createRule(String ruleString) {
        Stack<Node> stack = new Stack<>();
        String[] tokens = ruleString.split(" ");

        for (String token : tokens) {
            if (token.equals("AND") || token.equals("OR")) {
                Node right = stack.pop();
                Node left = stack.pop();
                Node node = new Node(token, left, right);  // create operator node
                stack.push(node);
            } else {
                stack.push(new Node("operand", token));  // create operand node
            }
        }
        return stack.peek();  // return root node of the AST
    }

    // Combine multiple rules into a single AST with AND operators
    public Node combineRules(List<String> rules) {
        List<Node> nodes = new ArrayList<>();
        for (String rule : rules) {
            nodes.add(createRule(rule));
        }

        // Combine rules into one AST, combining with AND operators
        Node combinedNode = nodes.get(0);
        for (int i = 1; i < nodes.size(); i++) {
            combinedNode = new Node("AND", combinedNode, nodes.get(i));
        }
        return combinedNode;
    }

    // Evaluate the AST against a data set
    public boolean evaluateRule(Node root, Map<String, Object> data) {
        if (root.type.equals("operand")) {
            String[] parts = root.value.split("=");
            String field = parts[0].trim();
            String value = parts[1].trim().replace("'", "");

            if (!data.containsKey(field)) return false;
            return data.get(field).toString().equals(value);
        }

        boolean leftResult = evaluateRule(root.left, data);
        boolean rightResult = evaluateRule(root.right, data);

        if (root.type.equals("AND")) {
            return leftResult && rightResult;
        } else if (root.type.equals("OR")) {
            return leftResult || rightResult;
        }

        return false;  // default false for unknown operators
    }

    public static void main(String[] args) {
        RuleEngine engine = new RuleEngine();

        // Sample rules
        String rule1 = "age > 30 AND department = 'Sales'";
        String rule2 = "salary > 50000 OR experience > 5";

        // Sample data to evaluate
        Map<String, Object> data = new HashMap<>();
        data.put("age", 35);
        data.put("department", "Sales");
        data.put("salary", 60000);
        data.put("experience", 3);

        // Create individual rule ASTs
        Node ruleNode1 = engine.createRule(rule1);
        Node ruleNode2 = engine.createRule(rule2);

        // Combine rules
        List<String> rules = Arrays.asList(rule1, rule2);
        Node combinedRule = engine.combineRules(rules);

        // Evaluate the combined rule against the data
        boolean result = engine.evaluateRule(combinedRule, data);
        System.out.println("Evaluation result: " + result);

        // Test evaluation
        RuleEngineTest.test(engine, rules, data);
    }
}

// Test class inside the same file for convenience
class RuleEngineTest {

    // Method to test rule evaluation
    public static void test(RuleEngine engine, List<String> rules, Map<String, Object> data) {
        Node combinedRule = engine.combineRules(rules);
        boolean result = engine.evaluateRule(combinedRule, data);
        System.out.println("Test evaluation result: " + result);
    }
}
