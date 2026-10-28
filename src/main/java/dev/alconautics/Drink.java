package dev.alconautics;

import java.util.List;

public record Drink(Kind kind, Variant variant) {
    public enum Kind { SUGAR, FRUIT, VEGETABLE;
        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public enum Variant { NORMAL, LONG, STRONG;
        public String suffix() { return this == NORMAL ? "" : "_" + name().toLowerCase(java.util.Locale.ROOT); }
        public int ticks() { return this == LONG ? 7200 : this == STRONG ? 1800 : 3600; }
        public int amplifier() { return this == STRONG ? 1 : 0; }
    }
    public String id() { return kind.id() + "_moonshine" + variant.suffix(); }
    public static final List<Drink> ALL = List.of(
        new Drink(Kind.SUGAR, Variant.NORMAL), new Drink(Kind.SUGAR, Variant.LONG), new Drink(Kind.SUGAR, Variant.STRONG),
        new Drink(Kind.FRUIT, Variant.NORMAL), new Drink(Kind.FRUIT, Variant.LONG), new Drink(Kind.FRUIT, Variant.STRONG),
        new Drink(Kind.VEGETABLE, Variant.NORMAL));
}
