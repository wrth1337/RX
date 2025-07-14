package eval;

public record TraceEntry(int step,
                         String expression,
                         String context,
                         String rule,
                         String result) {
}
