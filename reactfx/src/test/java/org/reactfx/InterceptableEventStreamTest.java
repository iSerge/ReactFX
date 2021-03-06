package org.reactfx;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class InterceptableEventStreamTest {

    @Test
    public void testInterceptionStacking() {
        EventSource<Integer> src = new EventSource<>();
        InterceptableEventStream<Integer> stream = src.interceptable();
        List<Integer> accum = new ArrayList<>();
        stream.subscribe(i -> accum.add(i));

        src.push(1);
        src.push(2);
        assertEquals(Arrays.asList(1, 2), accum);
        stream.pauseWhile(() -> {
            src.push(3);
            src.push(4);
            stream.tryReduceWhile((Integer a, Integer b) -> {
                if(a + b == 0) {
                    return ReductionResult.annihilated();
                } else if(a + b <= 20) {
                    return ReductionResult.reduced(a + b);
                } else {
                    return ReductionResult.failed();
                }
            }, () -> {
                src.push(5);
                src.push(6);
                src.push(7);
                src.push(8);
                src.push(9);
                src.push(-7);
                stream.retainLatestWhile(() -> {
                    src.push(100);
                    src.push(200);
                    src.push(-10);
                });
                src.push(4);
                stream.reduceWhile((Integer a, Integer b) -> a + b, () -> {
                    src.push(1);
                    src.push(2);
                    src.push(3);
                    stream.muteWhile(() -> {
                        src.push(4);
                        src.push(5);
                        src.push(6);
                    });
                });
                src.push(5);
                src.push(10);
                src.push(10);
            });
            src.push(5);
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 18, 15, 20, 5), accum);
        src.push(4);
        assertEquals(Arrays.asList(1, 2, 3, 4, 18, 15, 20, 5, 4), accum);
    }

    @Test(expected = IllegalStateException.class)
    public void testInteceptionReleaseOrder() {
        EventSource<Integer> src = new EventSource<>();
        InterceptableEventStream<Integer> stream = src.interceptable();
        Guard g1 = stream.pause();
        Guard g2 = stream.retainLatest();
        g1.close();
        g2.close();
    }
}
