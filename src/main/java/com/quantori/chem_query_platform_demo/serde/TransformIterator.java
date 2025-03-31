package com.quantori.chem_query_platform_demo.serde;

import java.util.Iterator;
import java.util.function.Function;

public class TransformIterator<A, R> implements Iterator<R> {
    private final Iterator<A> baseIterator;
    private final Function<A, R> converter;

    public TransformIterator(Iterator<A> baseIterator, Function<A, R> converter) {
        this.baseIterator = baseIterator;
        this.converter = converter;
    }

    @Override
    public boolean hasNext() {
        return baseIterator.hasNext();
    }

    @Override
    public R next() {
        return converter.apply(baseIterator.next());
    }
}
