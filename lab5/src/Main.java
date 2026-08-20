public class Main {
    public static void main(String[] args) {
        CNFConverter conv = new CNFConverter();
        CNFConverter.Grammar variant = CNFConverter.buildVariant16();

        System.out.println("Original grammar (Variant 16):");
        CNFConverter.printGrammar(variant);

        CNFConverter.Grammar cnf = conv.toCNF(variant);
        System.out.println("Converted CNF grammar:");
        CNFConverter.printGrammar(cnf);

        // Quick checks: every production either A->BC or A->a (or epsilon only for start)
        boolean ok = true;
        for (CNFConverter.Production p : cnf.productions) {
            if (p.rhs.isEmpty()) {
                if (!p.lhs.equals(cnf.startSymbol)) {
                    ok = false;
                    System.out.println("Invalid epsilon at non-start: " + p);
                }
            } else if (p.rhs.size() == 1) {
                String s = p.rhs.get(0);
                if (!cnf.terminals.contains(s)) {
                    ok = false;
                    System.out.println("Invalid unit production remains: " + p);
                }
            } else if (p.rhs.size() == 2) {
                if (!(cnf.nonTerminals.contains(p.rhs.get(0)) && cnf.nonTerminals.contains(p.rhs.get(1)))) {
                    ok = false;
                    System.out.println("Invalid binary production (terminals present): " + p);
                }
            } else {
                ok = false;
                System.out.println("Production has length >2: " + p);
            }
        }
        System.out.println("CNF basic validity: " + ok);
    }
}