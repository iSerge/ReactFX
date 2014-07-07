package org.reactfx;

import static org.reactfx.util.Tuples.*;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Task;

import org.reactfx.util.Either;
import org.reactfx.util.TriConsumer;
import org.reactfx.util.TriFunction;
import org.reactfx.util.TriPredicate;
import org.reactfx.util.Tuple2;
import org.reactfx.util.Tuple3;

public interface TriEventStream<A, B, C> extends EventStream<Tuple3<A, B, C>> {

    Subscription subscribe(TriConsumer<? super A, ? super B, ? super C> subscriber);

    @Override
    default Subscription subscribe(Consumer<? super Tuple3<A, B, C>> subscriber) {
        return subscribe((a, b, c) -> subscriber.accept(t(a, b, c)));
    }

    default Subscription watch(
            TriConsumer<? super A, ? super B, ? super C> subscriber,
            Consumer<? super Throwable> monitor) {
        return monitor(monitor).and(subscribe(subscriber));
    }

    default Subscription feedTo3(TriEventSink<? super A, ? super B, ? super C> sink) {
        return subscribe((a, b, c) -> sink.push(a, b, c));
    }

    default TriEventStream<A, B, C> hook(
            TriConsumer<? super A, ? super B, ? super C> sideEffect) {
        return new SideEffectTriStream<>(this, sideEffect);
    }

    default TriEventStream<A, B, C> filter(
            TriPredicate<? super A, ? super B, ? super C> predicate) {
        return new FilterTriStream<>(this, predicate);
    }

    @Override
    default TriEventStream<A, B, C> distinct() {
        return new DistinctTriStream<>(this);
    }

    default <U> EventStream<U> map(
            TriFunction<? super A, ? super B, ? super C, ? extends U> f) {
        return new MappedTriStream<>(this, f);
    }

    default <D, E> BiEventStream<D, E> mapToBi(TriFunction<? super A, ? super B, ? super C, Tuple2<D, E>> f) {
        return new MappedTriToBiStream<>(this, f);
    }

    default <D, E, F> TriEventStream<D, E, F> mapToTri(TriFunction<? super A, ? super B, ? super C, Tuple3<D, E, F>> f) {
        return new MappedTriToTriStream<>(this, f);
    }

    /**
     * @deprecated See deprecation comment at {@link EitherEventStream}.
     */
    @Deprecated
    default <L, R> EitherEventStream<L, R> split(
            TriFunction<? super A, ? super B, ? super C, Either<L, R>> f) {
        return new MappedToEitherTriStream<>(this, f);
    }

    default <U> CompletionStageStream<U> mapToCompletionStage(
            TriFunction<? super A, ? super B, ? super C, CompletionStage<U>> f) {
        return new MappedToCompletionStageTriStream<>(this, f);
    }

    default <U> TaskStream<U> mapToTask(
            TriFunction<? super A, ? super B, ? super C, Task<U>> f) {
        return new MappedToTaskTriStream<>(this, f);
    }

    default <U> EventStream<U> filterMap(
            TriPredicate<? super A, ? super B, ? super C> predicate,
            TriFunction<? super A, ? super B, ? super C, ? extends U> f) {
        return new FilterMapTriStream<>(this, predicate, f);
    }

    default <U> EventStream<U> filterMap(TriFunction<? super A, ? super B, ? super C, Optional<U>> f) {
        return filterMap(t -> f.apply(t._1, t._2, t._3));
    }

    default <U> EventStream<U> flatMap(TriFunction<? super A, ? super B, ? super C, ? extends EventStream<U>> f) {
        return flatMap(t -> f.apply(t._1, t._2, t._3));
    }

    /**
     * @deprecated Since 1.2.1. Renamed to {@link #filterMap(TriFunction)}.
     */
    @Deprecated
    default <U> EventStream<U> flatMapOpt(TriFunction<? super A, ? super B, ? super C, Optional<U>> f) {
        return flatMapOpt(t -> f.apply(t._1, t._2, t._3));
    }

    @Override
    default TriEventStream<A, B, C> emitOn(EventStream<?> impulse) {
        return new EmitOnTriStream<>(this, impulse);
    }

    @Override
    default TriEventStream<A, B, C> emitOnEach(EventStream<?> impulse) {
        return new EmitOnEachTriStream<>(this, impulse);
    }

    @Override
    default TriEventStream<A, B, C> repeatOn(EventStream<?> impulse) {
        return new RepeatOnTriStream<>(this, impulse);
    }

    @Override
    default InterceptableTriEventStream<A, B, C> interceptable() {
        if(this instanceof InterceptableTriEventStream) {
            return (InterceptableTriEventStream<A, B, C>) this;
        } else {
            return new InterceptableTriEventStreamImpl<A, B, C>(this);
        }
    }

    @Override
    default TriEventStream<A, B, C> threadBridge(
            Executor sourceThreadExecutor,
            Executor targetThreadExecutor) {
        return new TriThreadBridge<A, B, C>(this, sourceThreadExecutor, targetThreadExecutor);
    }

    @Override
    default TriEventStream<A, B, C> threadBridgeFromFx(Executor targetThreadExecutor) {
        return threadBridge(Platform::runLater, targetThreadExecutor);
    }

    @Override
    default TriEventStream<A, B, C> threadBridgeToFx(Executor sourceThreadExecutor) {
        return threadBridge(sourceThreadExecutor, Platform::runLater);
    }

    @Override
    default TriEventStream<A, B, C> guardedBy(Guardian... guardians) {
        return new GuardedTriStream<>(this, guardians);
    }
}
