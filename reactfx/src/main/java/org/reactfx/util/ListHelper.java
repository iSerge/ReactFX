package org.reactfx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class ListHelper<T> {

    public static <T> ListHelper<T> add(ListHelper<T> listHelper, T elem) {
        if(listHelper == null) {
            return new SingleElemHelper<>(elem);
        } else {
            return listHelper.add(elem);
        }
    }

    public static <T> ListHelper<T> remove(ListHelper<T> listHelper, T elem) {
        if(listHelper == null) {
            return listHelper;
        } else {
            return listHelper.remove(elem);
        }
    }

    public static <T> void forEach(ListHelper<T> listHelper, Consumer<T> f) {
        if(listHelper != null) {
            listHelper.forEach(f);
        }
    }

    public static <T> Optional<T> reduce(ListHelper<T> listHelper, BinaryOperator<T> f) {
        if(listHelper == null) {
            return Optional.empty();
        } else {
            return listHelper.reduce(f);
        }
    }

    public static <T, U> U reduce(ListHelper<T> listHelper, U unit, BiFunction<U, T, U> f) {
        if(listHelper == null) {
            return unit;
        } else {
            return listHelper.reduce(unit, f);
        }
    }

    public static <T> T[] toArray(ListHelper<T> listHelper, IntFunction<T[]> allocator) {
        if(listHelper == null) {
            return allocator.apply(0);
        } else {
            return listHelper.toArray(allocator);
        }
    }

    public static <T> boolean isEmpty(ListHelper<T> listHelper) {
        return listHelper == null;
    }

    public static <T> int size(ListHelper<T> listHelper) {
        if(listHelper == null) {
            return 0;
        } else {
            return listHelper.size();
        }
    }

    private ListHelper() {
        // private constructor to prevent subclassing
    };

    protected abstract ListHelper<T> add(T elem);

    protected abstract ListHelper<T> remove(T elem);

    protected abstract void forEach(Consumer<T> f);

    protected abstract Optional<T> reduce(BinaryOperator<T> f);

    protected abstract <U> U reduce(U unit, BiFunction<U, T, U> f);

    protected abstract T[] toArray(IntFunction<T[]> allocator);

    protected abstract int size();


    private static class SingleElemHelper<T> extends ListHelper<T> {
        private final T elem;

        public SingleElemHelper(T elem) {
            this.elem = elem;
        }

        @Override
        protected ListHelper<T> add(T elem) {
            return new MultiElemHelper<>(this.elem, elem);
        }

        @Override
        protected ListHelper<T> remove(T elem) {
            if(Objects.equals(this.elem, elem)) {
                return null;
            } else {
                return this;
            }
        }

        @Override
        protected void forEach(Consumer<T> f) {
            f.accept(elem);
        }

        @Override
        protected Optional<T> reduce(BinaryOperator<T> f) {
            return Optional.of(elem);
        }

        @Override
        protected <U> U reduce(U unit, BiFunction<U, T, U> f) {
            return f.apply(unit, elem);
        }

        @Override
        protected T[] toArray(IntFunction<T[]> allocator) {
            T[] res = allocator.apply(1);
            res[0] = elem;
            return res;
        }

        @Override
        protected int size() {
            return 1;
        }
    }

    private static class MultiElemHelper<T> extends ListHelper<T> {
        private final List<T> elems = new ArrayList<>();

        @SafeVarargs
        public MultiElemHelper(T... elems) {
            this.elems.addAll(Arrays.asList(elems));
        }

        @Override
        protected ListHelper<T> add(T elem) {
            elems.add(elem);
            return this;
        }

        @Override
        protected ListHelper<T> remove(T elem) {
            elems.remove(elem);
            switch(elems.size()) {
                case 0: return null;
                case 1: return new SingleElemHelper<>(elems.get(0));
                default: return this;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void forEach(Consumer<T> f) {
            for(Object elem: elems.toArray()) {
                f.accept((T) elem);
            }
        }

        @Override
        protected Optional<T> reduce(BinaryOperator<T> f) {
            return elems.stream().reduce(f);
        }

        @Override
        protected <U> U reduce(U unit, BiFunction<U, T, U> f) {
            U u = unit;
            for(T elem: elems) {
                u = f.apply(u, elem);
            }
            return u;
        }

        @Override
        protected T[] toArray(IntFunction<T[]> allocator) {
            return elems.toArray(allocator.apply(size()));
        }

        @Override
        protected int size() {
            return elems.size();
        }
    }
}
